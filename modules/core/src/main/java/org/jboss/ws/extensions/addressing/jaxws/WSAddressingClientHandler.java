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
package org.jboss.ws.extensions.addressing.jaxws;

import org.jboss.logging.Logger;
import org.jboss.ws.extensions.addressing.AddressingClientUtil;
import org.jboss.ws.extensions.addressing.AddressingConstantsImpl;
import org.jboss.ws.extensions.addressing.metadata.AddressingOpMetaExt;
import org.jboss.ws.extensions.addressing.soap.SOAPAddressingPropertiesImpl;
import org.jboss.ws.metadata.umdm.ClientEndpointMetaData;
import org.jboss.ws.metadata.umdm.OperationMetaData;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.api.handler.GenericSOAPHandler;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.addressing.AddressingBuilder;
import javax.xml.ws.addressing.AddressingException;
import javax.xml.ws.addressing.AttributedURI;
import javax.xml.ws.addressing.EndpointReference;
import javax.xml.ws.addressing.JAXWSAConstants;
import javax.xml.ws.addressing.soap.SOAPAddressingBuilder;
import javax.xml.ws.addressing.soap.SOAPAddressingProperties;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.AddressingFeature;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * A client side handler that reads/writes the addressing properties
 * and puts then into the message context.
 *
 * @author Thomas.Diesler@jboss.org
 * @author Heiko.Braun@jboss.com
 * @since 24-Nov-2005
 */
@SuppressWarnings("unchecked")
public class WSAddressingClientHandler extends GenericSOAPHandler
{
   // Provide logging
   private static Logger log = Logger.getLogger(WSAddressingClientHandler.class);

   private static AddressingBuilder ADDR_BUILDER;

   private static AddressingConstantsImpl ADDR_CONSTANTS;

   private static Set<QName> HEADERS = new HashSet<QName>();

   static
   {
      ADDR_CONSTANTS = new AddressingConstantsImpl();
      ADDR_BUILDER = AddressingBuilder.getAddressingBuilder();

      HEADERS.add(ADDR_CONSTANTS.getActionQName());
      HEADERS.add(ADDR_CONSTANTS.getToQName());
   }

   public Set getHeaders()
   {
      return Collections.unmodifiableSet(HEADERS);
   }

   protected boolean handleOutbound(MessageContext msgContext)
   {
      log.debug("handleOutbound");

      SOAPAddressingProperties addrProps = (SOAPAddressingProperties) msgContext
            .get(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES);
      if (addrProps != null)
      {
         msgContext.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND, addrProps);
         msgContext.setScope(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND, Scope.APPLICATION);
      }

      addrProps = (SOAPAddressingProperties) msgContext.get(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND);
      if (addrProps == null)
      {
         // supply default addressing properties
         addrProps = (SOAPAddressingPropertiesImpl) ADDR_BUILDER.newAddressingProperties();
         msgContext.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND, addrProps);
         msgContext.setScope(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND, Scope.APPLICATION);
      }

      // Add required wsa:Action
      if (addrProps.getAction() == null)
      {
         addrProps.setAction(this.getAction(msgContext));
      }

      // Add optional wsa:MessageID
      if (addrProps.getMessageID() == null)
      {
         addrProps.setMessageID(AddressingClientUtil.createMessageID());
      }

      // Add optional wsa:To
      if (addrProps.getTo() == null)
      {
         addrProps.setTo(this.getAddress(msgContext));
      }

      // Add optional wsa:ReplyTo
      if (addrProps.getReplyTo() == null)
      {
         addrProps.setReplyTo(this.newAnonymousURI());
      }

      SOAPMessage soapMessage = ((SOAPMessageContext) msgContext).getMessage();
      addrProps.writeHeaders(soapMessage);

      return true;
   }

   protected boolean handleInbound(MessageContext msgContext)
   {
      log.debug("handleInbound");
      
      try
      {
         SOAPMessage soapMessage = ((SOAPMessageContext) msgContext).getMessage();
         if (soapMessage.getSOAPPart().getEnvelope() != null)
         {
            SOAPAddressingBuilder builder = (SOAPAddressingBuilder) SOAPAddressingBuilder.getAddressingBuilder();
            SOAPAddressingProperties addrProps = (SOAPAddressingProperties) builder.newAddressingProperties();
            if (this.isAddressingRequired(msgContext))
            {
               try
               {
                  soapMessage.setProperty("isRequired", true);
               }
               catch (Exception e)
               {
                  //ignore 
               }

            }
            addrProps.readHeaders(soapMessage);
            msgContext.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES, addrProps);
            msgContext.setScope(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES, Scope.APPLICATION);
            msgContext.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_INBOUND, addrProps);
            msgContext.setScope(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_INBOUND, Scope.APPLICATION);
            msgContext.put(MessageContext.REFERENCE_PARAMETERS, convertToElementList(addrProps.getReferenceParameters()
                  .getElements()));
            msgContext.setScope(MessageContext.REFERENCE_PARAMETERS, Scope.APPLICATION);
         }
      }
      catch (SOAPException ex)
      {
         throw new AddressingException("Cannot handle response", ex);
      }

      return true;
   }

   private static List<Element> convertToElementList(List<Object> objects)
   {
      if (objects == null)
         return null;
      List<Element> elements = new LinkedList<Element>();
      for (Object o : objects)
      {
         if (o instanceof Element)
         {
            elements.add((Element) o);
         }
      }
      return elements;
   }

   private AttributedURI getAction(final MessageContext msgContext)
   {
      String action = "UNSPECIFIED";
      
      if (msgContext.get(BindingProvider.SOAPACTION_URI_PROPERTY) != null)
         action = msgContext.get(BindingProvider.SOAPACTION_URI_PROPERTY).toString();

      OperationMetaData operationMD = ((CommonMessageContext) msgContext).getOperationMetaData();
      if (operationMD != null)
      {
         AddressingOpMetaExt addressingMD = (AddressingOpMetaExt) 
            operationMD.getExtension(ADDR_CONSTANTS.getNamespaceURI());

         action = addressingMD.getInboundAction();
         if (action == null)
            action = operationMD.getJavaName();
      }

      return this.newURI(action);
   }
   
   private AttributedURI getAddress(final MessageContext msgContext)
   {
      final OperationMetaData operationMD = ((CommonMessageContext) msgContext).getOperationMetaData();
      if (operationMD != null)
      {
         final ClientEndpointMetaData endpointMD = (ClientEndpointMetaData) operationMD.getEndpointMetaData();

         return this.newURI(endpointMD.getEndpointAddress());
      }
      
      return null;
   }
   
   private AttributedURI newURI(final String uri)
   {
      try
      {
         return ADDR_BUILDER.newURI(uri);
      }
      catch (URISyntaxException e)
      {
         throw new WebServiceException(e.getMessage(), e);
      }
   }

   private EndpointReference newAnonymousURI()
   {
      return ADDR_BUILDER.newEndpointReference(this.newURI(ADDR_CONSTANTS.getAnonymousURI()).getURI());
   }

   private boolean isAddressingRequired(final MessageContext msgContext)
   {
      CommonMessageContext commonMsgContext = (CommonMessageContext)msgContext;
      ClientEndpointMetaData serverMetaData = (ClientEndpointMetaData)commonMsgContext.getEndpointMetaData();
      AddressingFeature addrFeature = serverMetaData.getFeature(AddressingFeature.class);
      
      return addrFeature != null && addrFeature.isRequired();
   }
}
