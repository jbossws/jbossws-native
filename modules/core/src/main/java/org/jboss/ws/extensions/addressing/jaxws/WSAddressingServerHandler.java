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

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.addressing.AddressingBuilder;
import javax.xml.ws.addressing.JAXWSAConstants;
import javax.xml.ws.addressing.soap.SOAPAddressingBuilder;
import javax.xml.ws.addressing.soap.SOAPAddressingProperties;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.soap.SOAPFaultException;

import org.jboss.logging.Logger;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.soap.SOAPFaultImpl;
import org.jboss.ws.extensions.addressing.AddressingConstantsImpl;
import org.jboss.ws.extensions.addressing.metadata.AddressingOpMetaExt;
import org.jboss.ws.metadata.umdm.FaultMetaData;
import org.jboss.ws.metadata.umdm.OperationMetaData;
import org.jboss.ws.metadata.umdm.ServerEndpointMetaData;
import org.jboss.wsf.common.handler.GenericSOAPHandler;
import org.w3c.dom.Element;

/**
 * A server side handler that reads/writes the addressing properties
 * and puts then into the message context.
 *
 * @author Thomas.Diesler@jboss.org
 * @author Heiko.Braun@jboss.com
 * @since 24-Nov-2005
 */
@SuppressWarnings("unchecked")
public class WSAddressingServerHandler extends GenericSOAPHandler
{
   // Provide logging
   private static Logger log = Logger.getLogger(WSAddressingServerHandler.class);

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

   protected boolean handleInbound(MessageContext msgContext)
   {
      if (log.isDebugEnabled())
         log.debug("handleInbound");

      SOAPAddressingProperties addrProps = (SOAPAddressingProperties) ADDR_BUILDER.newAddressingProperties();
      SOAPMessage soapMessage = ((SOAPMessageContext) msgContext).getMessage();
      CommonMessageContext commonMsgContext = (CommonMessageContext) msgContext;
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
      if (addrProps.getAction() != null)
      {
         msgContext.put(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND, addrProps);
         msgContext.setScope(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND, Scope.APPLICATION);
         msgContext.put(MessageContext.REFERENCE_PARAMETERS, convertToElementList(addrProps.getReferenceParameters()
               .getElements()));
         msgContext.setScope(MessageContext.REFERENCE_PARAMETERS, Scope.APPLICATION);

         //check if soap action matches wsa action
         String[] soapActions = commonMsgContext.getMessageAbstraction().getMimeHeaders().getHeader("SOAPAction");
         if (soapActions != null && soapActions.length > 0)
         {
            String soapAction = soapActions[0];
            if (!soapAction.equals("\"\"") && addrProps.getAction() != null)
            {
               String wsaAction = addrProps.getAction().getURI().toString();
               // R1109 The value of the SOAPAction HTTP header field in a HTTP request MESSAGE MUST be a quoted string.
               if (!soapAction.equals(wsaAction) && !soapAction.equals("\"" + wsaAction + "\""))
               {
                  try
                  {
                     SOAPFault fault = new SOAPFaultImpl();
                     fault.setFaultCode(new QName(ADDR_CONSTANTS.getNamespaceURI(), "ActionMismatch"));
                     fault.setFaultString("Mismatch between soap action:" + soapAction + " and wsa action:\""
                           + addrProps.getAction().getURI() + "\"");
                     Detail detail = fault.addDetail();
                     detail.addDetailEntry(new QName(ADDR_CONSTANTS.getNamespaceURI(), "ProblemAction"));
                     throw new SOAPFaultException(fault);
                  }
                  catch (SOAPException e)
                  {
                     throw new WebServiceException(e);
                  }
               }
            }
         }
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

   protected boolean handleOutbound(MessageContext msgContext)
   {
      if (log.isDebugEnabled())
         log.debug("handleOutbound");

      if (this.isAddressingRequest(msgContext))
      {
         handleResponseOrFault(msgContext, false);
      }
      return true;
   }

   /**
    * Get a SOAPAddressingProperties object from the message context
    * and write the adressing headers
    */
   public boolean handleFault(MessageContext msgContext)
   {
      if (log.isDebugEnabled())
         log.debug("handleFault");
      if (this.isAddressingRequest(msgContext))
      {
         handleResponseOrFault(msgContext, true);
      }

      return true;
   }

   private void handleResponseOrFault(MessageContext msgContext, boolean isFault)
   {
      SOAPAddressingBuilder builder = (SOAPAddressingBuilder) SOAPAddressingBuilder.getAddressingBuilder();
      SOAPMessage soapMessage = ((SOAPMessageContext) msgContext).getMessage();

      SOAPAddressingProperties inProps = (SOAPAddressingProperties) msgContext
            .get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
      SOAPAddressingProperties outProps = (SOAPAddressingProperties) msgContext
            .get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_OUTBOUND);

      if (outProps == null)
      {
         // create new response properties
         outProps = (SOAPAddressingProperties) builder.newAddressingProperties();
         msgContext.put(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_OUTBOUND, outProps);
         msgContext.setScope(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_OUTBOUND, Scope.APPLICATION);
      }

      if (inProps != null)
         outProps.initializeAsReply(inProps, isFault);

      try
      {
         // supply the response action
         CommonMessageContext commonCtx = (CommonMessageContext) msgContext;
         OperationMetaData operationMD = commonCtx.getOperationMetaData();

         AddressingOpMetaExt addressingMD = (AddressingOpMetaExt) operationMD.getExtension(ADDR_CONSTANTS
               .getNamespaceURI());

         if (addressingMD == null)
            throw new IllegalStateException("Addressing meta data not available");

         if (!isFault && !operationMD.isOneWay())
         {
            outProps.setAction(ADDR_BUILDER.newURI(addressingMD.getOutboundAction()));
         }
         else if (isFault)
         {
            Throwable exception = commonCtx.getCurrentException();
            String faultAction = getFaultAction(operationMD, addressingMD, exception);

            outProps.setAction(ADDR_BUILDER.newURI(faultAction));
         }

      }
      catch (URISyntaxException e)
      {
         log.error("Error setting response action", e);
      }

      outProps.writeHeaders(soapMessage);
   }

   private String getFaultAction(final OperationMetaData operationMD, final AddressingOpMetaExt addressingMD,
         final Throwable exception)
   {
      final FaultMetaData faultMD = operationMD.getFaultMetaData(exception.getClass());

      if (faultMD != null)
      {
         final String beanName = faultMD.getFaultBeanName();
         return addressingMD.getFaultAction(beanName);
      }

      return ADDR_CONSTANTS.getDefaultFaultAction();
   }

   private boolean isAddressingRequired(final MessageContext msgContext)
   {
      final AddressingFeature addrFeature = this.getAddressingFeature(msgContext);

      return addrFeature != null && addrFeature.isEnabled() && addrFeature.isRequired();
   }

   private AddressingFeature getAddressingFeature(final MessageContext msgContext)
   {
      final CommonMessageContext commonMsgContext = (CommonMessageContext) msgContext;
      final ServerEndpointMetaData serverMetaData = (ServerEndpointMetaData) commonMsgContext.getEndpointMetaData();

      return serverMetaData.getFeature(AddressingFeature.class);
   }

   private boolean isAddressingRequest(final MessageContext msgContext)
   {
      return msgContext.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND) != null;
   }
}
