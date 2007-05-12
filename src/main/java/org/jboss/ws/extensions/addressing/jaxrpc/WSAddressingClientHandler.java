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
package org.jboss.ws.extensions.addressing.jaxrpc;

import javax.xml.namespace.QName;
import javax.xml.rpc.handler.GenericHandler;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.addressing.AddressingBuilder;
import javax.xml.ws.addressing.AddressingException;
import javax.xml.ws.addressing.JAXWSAConstants;
import javax.xml.ws.addressing.soap.SOAPAddressingBuilder;
import javax.xml.ws.addressing.soap.SOAPAddressingProperties;

import org.jboss.logging.Logger;
import org.jboss.ws.extensions.addressing.soap.SOAPAddressingPropertiesImpl;

/**
 * A client side handler that reads/writes the addressing properties
 * and puts then into the message context.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 24-Nov-2005
 */
public class WSAddressingClientHandler extends GenericHandler
{
   // Provide logging
   private static Logger log = Logger.getLogger(WSAddressingClientHandler.class);

   // addressing builder & constants
   private AddressingBuilder addrBuilder;

   // should the request be normalized according to the specifications
   private boolean normalize;

   public WSAddressingClientHandler()
   {
      addrBuilder = AddressingBuilder.getAddressingBuilder();
   }

   public QName[] getHeaders()
   {
      return new QName[] {};
   }

   public void init(HandlerInfo handlerInfo)
   {
      super.init(handlerInfo);
      normalize = "true".equals(handlerInfo.getHandlerConfig().get("normalize"));

   }

   public boolean handleRequest(MessageContext msgContext)
   {
      if(log.isDebugEnabled()) log.debug("handleRequest");

      SOAPAddressingProperties addrProps = (SOAPAddressingProperties)msgContext.getProperty(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND);

      if (addrProps != null)
      {
         if (normalize)
            normalizeRequest(msgContext, addrProps);

         SOAPMessage soapMessage = ((SOAPMessageContext)msgContext).getMessage();
         addrProps.writeHeaders(soapMessage);
      }
      else
      {
         // supply default addressing properties
         addrProps = (SOAPAddressingPropertiesImpl)addrBuilder.newAddressingProperties();
         msgContext.setProperty(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND, addrProps);

         if (normalize)
            normalizeRequest(msgContext, addrProps);
      }

      return true;
   }

   /* supply the default addressing properties in case elements are missing */
   private void normalizeRequest(MessageContext msgContext, SOAPAddressingProperties addrProps)
   {
      // TODO: supply default header
   }

   public boolean handleResponse(MessageContext msgContext)
   {
      if(log.isDebugEnabled()) log.debug("handleResponse");

      try
      {
         SOAPMessage soapMessage = ((SOAPMessageContext)msgContext).getMessage();
         if (soapMessage.getSOAPPart().getEnvelope() != null)
         {
            SOAPAddressingBuilder builder = (SOAPAddressingBuilder)SOAPAddressingBuilder.getAddressingBuilder();
            SOAPAddressingProperties addrProps = (SOAPAddressingProperties)builder.newAddressingProperties();
            addrProps.readHeaders(soapMessage);
            msgContext.setProperty(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_INBOUND, addrProps);
         }
      }
      catch (SOAPException ex)
      {
         throw new AddressingException("Cannot handle response", ex);
      }

      return true;
   }
}
