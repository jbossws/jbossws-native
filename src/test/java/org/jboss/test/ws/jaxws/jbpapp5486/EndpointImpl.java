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
package org.jboss.test.ws.jaxws.jbpapp5486;

import java.util.Map;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

/**
 * Test Endpoint implementation.
 * 
 * @author darran.lofthouse@jboss.com
 * @since 13th December 2010
 */
@WebService(name = "Endpoint", targetNamespace = "http://ws.jboss.org/jbpapp5486", endpointInterface = "org.jboss.test.ws.jaxws.jbpapp5486.Endpoint")
public class EndpointImpl implements Endpoint
{

   @Resource
   private WebServiceContext context;

   public String verifyNoTimeoutParameter(final String message)
   {
      if (getTimeout() != null)
      {
         throw new IllegalStateException("timeout parameter recieved.");
      }
      
      return message;
   }
   
   public String doSleep(final String message, final long timeout)
   {
      try
      {
         Thread.sleep(timeout);
      }
      catch (InterruptedException e)
      {
         throw new RuntimeException("We were interrupted!!", e);
      }

      return message;
   }

   private String getTimeout()
   {
      HttpServletRequest request = (HttpServletRequest)context.getMessageContext().get(MessageContext.SERVLET_REQUEST);
      String timeout = request.getParameter("timeout");      

      return timeout;
   }





}
