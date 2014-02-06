/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

public class OneWayUnMarshallerHTTP implements UnMarshaller
{
   private static Logger log = Logger.getLogger(OneWayUnMarshallerHTTP.class);

   public Object read(InputStream inputStream, Map metadata) throws IOException, ClassNotFoundException
   {
      Integer resCode = (Integer)metadata.get(HTTPMetadataConstants.RESPONSE_CODE);
      if(!resCode.equals(200))
      {
         throw new WSException("One-way operation returned invalid HTTP response: " + resCode);
      }
      return null;
   }

   public UnMarshaller cloneUnMarshaller() throws CloneNotSupportedException
   {
      return new OneWayUnMarshallerHTTP();
   }

   public void setClassLoader(ClassLoader cl)
   {
      //noop
   }

   private MimeHeaders getMimeHeaders(Map metadata)
   {
      log.debug("getMimeHeaders from: " + metadata);

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
