/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbpapp2143;

import java.util.Map;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

/**
 * Test Endpoint implementation.
 * 
 * @author darran.lofthouse@jboss.com
 * @since 3rd December 2010
 */
@WebService(name = "Endpoint", targetNamespace = "http://ws.jboss.org/jbpapp2143", endpointInterface = "org.jboss.test.ws.jaxws.jbpapp2143.Endpoint")
public class EndpointImpl implements Endpoint
{

   @Resource
   private WebServiceContext context;

   public String verifyNoContentLength(final String message)
   {
      if (getContentLength() != null)
      {
         throw new IllegalArgumentException("Unexpected content length recieved.");
      }

      return message;
   }

   public String verifyHasContentLength(final String message)
   {
      if (getContentLength() == null)
      {
         throw new IllegalArgumentException("Expected content length not recieved.");
      }

      return message;
   }

   private Object getContentLength()
   {
      Map headers = (Map)context.getMessageContext().get(MessageContext.HTTP_REQUEST_HEADERS);
      Object contentLength = headers.get("content-length");

      return contentLength;
   }

}
