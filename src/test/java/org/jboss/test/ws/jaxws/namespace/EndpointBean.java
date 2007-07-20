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
package org.jboss.test.ws.jaxws.namespace;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.logging.Logger;
import org.jboss.wsf.common.DOMWriter;

/**
 * Test namespace differences at service and portType levels
 *
 * @author Thomas.Diesler@jboss.org
 * @since 29-Apr-2005
 */
@WebService(endpointInterface = "org.jboss.test.ws.jaxws.namespace.EndpointInterface", targetNamespace = "http://example.org/impl")
public class EndpointBean implements EndpointInterface
{
   private static final Logger log = Logger.getLogger(EndpointBean.class);

   @Resource
   WebServiceContext context;

   public String echo(String message)
   {
      log.info("echo:" + message);

      try
      {
         SOAPMessageContext msgContext = (SOAPMessageContext)context.getMessageContext();
         SOAPBody body = msgContext.getMessage().getSOAPBody();
         String bodyStr = DOMWriter.printNode(body, false);
         if (bodyStr.indexOf("http://example.org/sei") < 0)
            throw new WebServiceException("Invalid body: " + bodyStr);
      }
      catch (SOAPException ex)
      {
         throw new WebServiceException(ex);
      }

      return message;
   }
}
