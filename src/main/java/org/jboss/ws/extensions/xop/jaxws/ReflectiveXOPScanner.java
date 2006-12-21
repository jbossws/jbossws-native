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

import org.jboss.ws.core.utils.JavaUtils;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.transform.Source;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Scans data types for MTOM declarations.
 * In order to re-use an instance of this class you need to invoke <code>reset()</code>
 * in between scans.
 *
 * @author Heiko Braun <heiko.braun@jboss.com>
 * @version $Id$
 * @since 04.12.2006
 *
 */
public class ReflectiveXOPScanner {

   private static List<Class> SUPPORTED_TYPES = new ArrayList<Class>(5);

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
   public String scan(Class xmlRoot)
   {

      if( isJDKType(xmlRoot) )
         return null;

      String mimeType = null;

      for(Field field : xmlRoot.getDeclaredFields())
      {
         Class<?> type = field.getType();

         boolean exceptionToTheRule = isMTOMDataType(type);

         // only non JDK types are inspected except for byte[] and java.lang.String
         if( !alreadyScanned(field) && (exceptionToTheRule || !isJDKType(type)) )
         {
            if(field.isAnnotationPresent(XmlMimeType.class))
            {
               XmlMimeType mimeTypeDecl = field.getAnnotation(XmlMimeType.class);
               mimeType = mimeTypeDecl.value();
            }

            if(null == mimeType) // try getter methods
            {
               mimeType = scanGetterAnnotation(xmlRoot, field);
            }

            // avoid recursive loops
            if(!isMTOMDataType(type))
               scannedFields.add(field);

            // drill down if none found so far
            if(null == mimeType)
               mimeType = scan(type);

         }

      }

      return mimeType;
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

   private static boolean isMTOMDataType(Class clazz) {
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
