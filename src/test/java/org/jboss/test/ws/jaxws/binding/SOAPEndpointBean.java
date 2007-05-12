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
package org.jboss.test.ws.jaxws.binding;

// $Id: $

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.logging.Logger;

@WebService(name="SOAPEndpoint", targetNamespace="http://org.jboss.ws/jaxws/binding", 
      endpointInterface = "org.jboss.test.ws.jaxws.binding.SOAPEndpoint")
public class SOAPEndpointBean implements SOAPEndpoint
{
   private static Logger log = Logger.getLogger(SOAPEndpointBean.class);

   @Resource
   public WebServiceContext context;


   public String namespace()
   {
      try
      {
         SOAPMessageContext msgContext = (SOAPMessageContext)context.getMessageContext();
         SOAPMessage soapMessage = msgContext.getMessage();
         SOAPEnvelope soapEnvelope = (SOAPEnvelope)soapMessage.getSOAPPart().getEnvelope();
         String nsURI = soapEnvelope.getNamespaceURI();

         log.info(nsURI);

         return nsURI;
      }
      catch (SOAPException ex)
      {
         throw new WebServiceException(ex);
      }
   }
}
