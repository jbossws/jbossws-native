/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jboss.ws.extensions.xop.jaxws;

import java.awt.Image;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlAttachmentRef;
import javax.xml.transform.Source;

import org.jboss.wsintegration.spi.utils.JavaUtils;

/**
 * Scans data types for MTOM and swaRef declarations.
 * It basically searches for
 * <ul>
 * <li><code>@XmlMimeType</code>
 * <li><code>@XmlAttachmentRef</code>
 * </ul>
 * and returns the appropriate mimetype. 
 * In order to re-use an instance of this class you need to invoke <code>reset()</code>
 * in between scans.
 *
 * @author Heiko Braun <heiko.braun@jboss.com>
 * @version $Id$
 * @since 04.12.2006
 *
 */
public class ReflectiveAttachmentRefScanner {

   private static List<Class> SUPPORTED_TYPES = new ArrayList<Class>(5);

   public static enum ResultType {XOP, SWA_REF};

   static {
      SUPPORTED_TYPES.add(String.class);
      SUPPORTED_TYPES.add(byte[].class);
      SUPPORTED_TYPES.add(Image.class);
      SUPPORTED_TYPES.add(Source.class);
      SUPPORTED_TYPES.add(DataHandler.class);
   }

   private List<Field> scannedFields = new ArrayList<Field>();

   /**
    * Scan java types for MTOM declarations
    *
    * @param xmlRoot
    * @return the first matching XmlMimeType#value() or <code>null</code> if none found
    */
   public AttachmentScanResult scan(Class xmlRoot)
   {
      if( isJDKType(xmlRoot) )
         return null;

      AttachmentScanResult result = null;

      for(Field field : xmlRoot.getDeclaredFields())
      {
         Class<?> type = field.getType();

         boolean exceptionToTheRule = isAttachmentDataType(type);

         // only non JDK types are inspected except for byte[] and java.lang.String
         if( !alreadyScanned(field) && (exceptionToTheRule || !isJDKType(type)) )
         {

            // Scan for swa:Ref type declarations first
            if(field.isAnnotationPresent(XmlAttachmentRef.class))
            {
               // arbitrary, it's not used
               result = new AttachmentScanResult("application/octet-stream", AttachmentScanResult.Type.SWA_REF);
            }

            // Scan for XOP field annotations
            else if(field.isAnnotationPresent(XmlMimeType.class))
            {
               XmlMimeType mimeTypeDecl = field.getAnnotation(XmlMimeType.class);
               result = new AttachmentScanResult(mimeTypeDecl.value(), AttachmentScanResult.Type.XOP);
            }

            if(null == result) // try getter methods
            {
               String mimeType = scanGetterAnnotation(xmlRoot, field);
               if(mimeType!=null)
                  result = new AttachmentScanResult(mimeType, AttachmentScanResult.Type.XOP);
            }

            // avoid recursive loops
            if(!isAttachmentDataType(type))
               scannedFields.add(field);

            // drill down if none found so far
            if(null == result)
               result = scan(type);

         }

      }

      return result;
   }

   private boolean alreadyScanned(Field field)
   {

      for(Field f : scannedFields)
      {
         if(f.equals(field))
            return true;
      }

      return false;
   }

   public void reset()
   {
      scannedFields.clear();
   }

   private static boolean isAttachmentDataType(Class clazz) {
      for(Class cl : SUPPORTED_TYPES)
      {
         if(JavaUtils.isAssignableFrom(cl, clazz))
            return true;
      }

      return false;
   }

   private static boolean isJDKType(Class clazz)
   {
      return clazz.getPackage()!= null ? clazz.getPackage().getName().startsWith("java") : true;
   }

   private static String scanGetterAnnotation(Class owner, Field field)
   {
      String getterMethodName = "get"+field.getName();
      for(Method method : owner.getDeclaredMethods())
      {
         if(method.getName().equalsIgnoreCase(getterMethodName)
            && method.isAnnotationPresent(XmlMimeType.class))
         {
            XmlMimeType mimeTypeDecl = method.getAnnotation(XmlMimeType.class);
            return mimeTypeDecl.value();
         }
      }

      return null;
   }
}
