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
package org.jboss.test.ws.jaxws.context;

// $Id: $

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.soap.SOAPMessageContext;

@WebService(endpointInterface = "org.jboss.test.ws.jaxws.context.EndpointInterface", serviceName = "TestService", targetNamespace = "http://org.jboss.ws/jaxws/context")
public class EndpointBean
{
   @Resource
   public WebServiceContext context;

   public String echo(String input)
   {
      try
      {
         SOAPMessageContext msgContext = (SOAPMessageContext)context.getMessageContext();
         SOAPMessage soapMessage = msgContext.getMessage();
         SOAPElement soapElement = (SOAPElement)soapMessage.getSOAPBody().getChildElements().next();
         soapElement = (SOAPElement)soapElement.getChildElements().next();
         return soapElement.getValue();
      }
      catch (SOAPException ex)
      {
         throw new WebServiceException(ex);
      }
   }
}
