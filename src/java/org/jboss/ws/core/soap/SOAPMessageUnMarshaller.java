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
package org.jboss.ws.core.soap;

// $Id$

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.jboss.logging.Logger;
import org.jboss.remoting.marshal.UnMarshaller;
import org.jboss.remoting.transport.http.HTTPMetadataConstants;
import org.jboss.ws.WSException;

/**
 * @author Thomas.Diesler@jboss.org
 * @since 25-Nov-2004
 */
public class SOAPMessageUnMarshaller implements UnMarshaller
{
   // Provide logging
   private static Logger log = Logger.getLogger(SOAPMessageUnMarshaller.class);

   private static List validResponseCodes = new ArrayList();
   static
   {
      validResponseCodes.add(HttpServletResponse.SC_OK);
      validResponseCodes.add(HttpServletResponse.SC_ACCEPTED);
      validResponseCodes.add(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
   }

   public Object read(InputStream inputStream, Map metadata) throws IOException, ClassNotFoundException
   {
      if (log.isTraceEnabled())
      {
         log.trace("Read input stream with metadata=" + metadata);
      }

      try
      {
         Integer resCode = (Integer)metadata.get(HTTPMetadataConstants.RESPONSE_CODE);
         String resMessage = (String)metadata.get(HTTPMetadataConstants.RESPONSE_CODE_MESSAGE);
         if (resCode != null && validResponseCodes.contains(resCode) == false)
            throw new WSException("Invalid HTTP server response [" + resCode + "] - " + resMessage);

         MimeHeaders mimeHeaders = getMimeHeaders(metadata);
         SOAPMessage soapMsg = new MessageFactoryImpl().createMessage(mimeHeaders, inputStream, true);
         return soapMsg;
      }
      catch (SOAPException e)
      {
         log.error("Cannot unmarshall SOAPMessage", e);
         IOException e2 = new IOException(e.toString());
         e2.initCause(e);
         throw e2;
      }
   }

   /**
    * Set the class loader to use for unmarhsalling.  This may
    * be needed when need to have access to class definitions that
    * are not part of this unmarshaller's parent classloader (especially
    * when doing remote classloading).
    *
    * @param classloader
    */
   public void setClassLoader(ClassLoader classloader)
   {
      //NO OP
   }

   public UnMarshaller cloneUnMarshaller() throws CloneNotSupportedException
   {
      return new SOAPMessageUnMarshaller();
   }

   private MimeHeaders getMimeHeaders(Map metadata)
   {
      if(log.isDebugEnabled()) log.debug("getMimeHeaders from: " + metadata);

      MimeHeaders headers = new MimeHeaders();
      Iterator i = metadata.keySet().iterator();
      while (i.hasNext())
      {
         String key = (String)i.next();
         Object value = metadata.get(key);
         if (key != null && value instanceof List)
         {
            for (Object listValue : (List)value)
            {
               headers.addHeader(key, listValue.toString());
            }
         }
      }
      return headers;
   }
}
