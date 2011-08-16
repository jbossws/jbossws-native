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
package org.jboss.test.ws.jaxws.jbws2982;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

/**
 * Endpoint implementation.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
@WebService
(
   name = "Endpoint",
   serviceName = "EndpointService",
   targetNamespace="http://jboss.org/jbws2982",
   endpointInterface="org.jboss.test.ws.jaxws.jbws2982.Endpoint"
)
public class EndpointImpl implements Endpoint
{
   @Resource
   WebServiceContext ctx;

   private int getRequestParameterInternal(String key)
   {
      int ctxValue = this.getValue(this.ctx, key);

      return ++ctxValue; 
   }

   private int getValue(WebServiceContext wsCtx, String paramKey)
   {
      Map<String, List<String>> requestHeaders = (Map<String, List<String>>)wsCtx.getMessageContext().get(MessageContext.HTTP_REQUEST_HEADERS);
      
      return Integer.valueOf(requestHeaders.get("extension-header").get(0));
   }
   
   @WebMethod
   public int getRequestParameter(String key)
   {
      return this.getRequestParameterInternal(key);
   }
}
