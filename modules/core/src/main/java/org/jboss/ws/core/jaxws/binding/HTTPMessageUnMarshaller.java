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
package org.jboss.ws.core.jaxws.binding;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.MimeHeaders;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.core.HTTPMessageImpl;
import org.jboss.ws.core.client.UnMarshaller;
import org.jboss.ws.core.client.transport.NettyClient;

/**
 * @author Thomas.Diesler@jboss.org
 * @since 25-Nov-2004
 */
public class HTTPMessageUnMarshaller implements UnMarshaller
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(HTTPMessageUnMarshaller.class);
   // Provide logging
   private static Logger log = Logger.getLogger(HTTPMessageUnMarshaller.class);

   private static List<Integer> validResponseCodes = new ArrayList<Integer>();
   static
   {
      validResponseCodes.add(HttpServletResponse.SC_OK);
      validResponseCodes.add(HttpServletResponse.SC_ACCEPTED);
      validResponseCodes.add(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
   }

   public Object read(InputStream inputStream, Map<String, Object> metadata, Map<String, Object> headers) throws IOException
   {
      if (log.isTraceEnabled())
      {
         log.trace("Read input stream with metadata=" + metadata);
      }

      Integer resCode = (Integer)metadata.get(NettyClient.RESPONSE_CODE);
      String resMessage = (String)metadata.get(NettyClient.RESPONSE_CODE_MESSAGE);
      if (resCode != null && validResponseCodes.contains(resCode) == false)
         throw new WSException(BundleUtils.getMessage(bundle, "INVALID_HTTP_SERVER_RESPONSE", new Object[]{ resCode ,  resMessage}));

      MimeHeaders mimeHeaders = getMimeHeaders(headers);
      HTTPMessageImpl soapMsg = new HTTPMessageImpl(mimeHeaders, inputStream);
      return soapMsg;
   }

   private MimeHeaders getMimeHeaders(Map<String, Object> metadata)
   {
      MimeHeaders headers = new MimeHeaders();
      for (String key : metadata.keySet())
      {
         Object value = metadata.get(key);
         if (key != null && value instanceof List)
         {
            for (Object listValue : (List<?>)value)
            {
               headers.addHeader(key, listValue.toString());
            }
         }
      }
      return headers;
   }
}
