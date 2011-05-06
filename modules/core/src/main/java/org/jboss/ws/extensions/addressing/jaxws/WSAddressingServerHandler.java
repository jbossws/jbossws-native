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
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.addressing.AddressingBuilder;
import javax.xml.ws.addressing.AttributedURI;
import javax.xml.ws.addressing.EndpointReference;
import javax.xml.ws.addressing.JAXWSAConstants;
import javax.xml.ws.addressing.soap.SOAPAddressingBuilder;
import javax.xml.ws.addressing.soap.SOAPAddressingProperties;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.AddressingFeature;

import org.jboss.logging.Logger;
import org.jboss.util.xml.DOMUtils;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.extensions.addressing.AddressingConstantsImpl;
import org.jboss.ws.extensions.addressing.DetailedAddressingException;
import org.jboss.ws.extensions.addressing.metadata.AddressingOpMetaExt;
import org.jboss.ws.metadata.umdm.FaultMetaData;
import org.jboss.ws.metadata.umdm.OperationMetaData;
import org.jboss.ws.metadata.umdm.ServerEndpointMetaData;
import org.jboss.ws.api.addressing.AddressingConstants;
import org.jboss.ws.api.handler.GenericSOAPHandler;
import org.jboss.ws.common.utils.UUIDGenerator;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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
                  final QName code = new QName(ADDR_CONSTANTS.getNamespaceURI(), "ActionMismatch");
                  final String reason = "Mismatch between soap action:" + soapAction + " and wsa action:\""
                        + addrProps.getAction().getURI() + "\"";
                  final Node detail = DOMUtils.createElement(new QName(ADDR_CONSTANTS.getNamespaceURI(),
                        "ProblemAction"));

                  throw new DetailedAddressingException(code, reason, detail);
               }
            }
         }
      }

      this.ensureAnonymousPolicy(addrProps, msgContext);

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

      OperationMetaData operationMD = this.getOperationMetaData(msgContext);
      AddressingOpMetaExt addressingMD = this.getAddressingMetaData(msgContext);

      if (!isFault && !operationMD.isOneWay())
      {
         outProps.setAction(this.newURI(addressingMD.getOutboundAction()));
      }
      else if (isFault)
      {
         String faultAction = this.getFaultAction(msgContext);

         outProps.setAction(this.newURI(faultAction));
      }
      
      if (outProps.getMessageID() == null) 
      {
         try
         {
            outProps.setMessageID(ADDR_BUILDER.newURI("urn:uuid:" + UUIDGenerator.generateRandomUUIDString()));
         }
         catch (URISyntaxException e)
         {
            log.error("Error setting response messageId", e);
         }   
      }

      outProps.writeHeaders(soapMessage);
   }

   private AttributedURI newURI(final String uri) // TODO: client addressing handler have the same method - refactor it to some helper class
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

   private OperationMetaData getOperationMetaData(final MessageContext msgContext)
   {
      CommonMessageContext commonCtx = (CommonMessageContext) msgContext;

      return commonCtx.getOperationMetaData();
   }

   private AddressingOpMetaExt getAddressingMetaData(final MessageContext msgContext)
   {
      OperationMetaData operationMD = this.getOperationMetaData(msgContext);
      AddressingOpMetaExt addressingMD = null;

      if (operationMD != null)
      {
         addressingMD = (AddressingOpMetaExt) operationMD.getExtension(ADDR_CONSTANTS.getNamespaceURI());

         if (addressingMD == null)
            throw new IllegalStateException("Addressing meta data not available");
      }

      return addressingMD;
   }

   private void ensureAnonymousPolicy(final SOAPAddressingProperties inProps, final MessageContext msgContext)
   {
      final CommonMessageContext commonCtx = (CommonMessageContext) msgContext;
      final AddressingFeature addressing = commonCtx.getEndpointMetaData().getFeature(AddressingFeature.class);
      final boolean isOnlyAnonymousEnabled = addressing != null
            && addressing.getResponses() == AddressingFeature.Responses.ANONYMOUS;
      final boolean isOnlyNonAnonymousEnabled = addressing != null
            && addressing.getResponses() == AddressingFeature.Responses.NON_ANONYMOUS;
      final boolean isReplyToAnonymous = this.isAnonymous(inProps.getReplyTo());
      final boolean isFaultToAnonymous = this.isAnonymous(inProps.getFaultTo());

      if ((isOnlyAnonymousEnabled) && (!isReplyToAnonymous || !isFaultToAnonymous))
      {
         try
         {
            final QName faultCode = new QName(ADDR_CONSTANTS.getNamespaceURI(), "OnlyAnonymousAddressSupported");
            final String reason = "A header representing a Message Addressing Property is not valid and the message cannot be processed";
            final Object detail = this.getProblemHeaderDetail(!isReplyToAnonymous
                  ? AddressingConstants.Core.Elements.REPLYTO_QNAME
                  : AddressingConstants.Core.Elements.FAULTTO_QNAME);

            throw new DetailedAddressingException(faultCode, reason, detail);
         }
         catch (SOAPException e)
         {
            throw new WebServiceException(e);
         }
      }
      else if ((isOnlyNonAnonymousEnabled) && (isReplyToAnonymous || isFaultToAnonymous))
      {
         try
         {
            final QName faultCode = new QName(ADDR_CONSTANTS.getNamespaceURI(), "OnlyNonAnonymousAddressSupported");
            final String reason = "A header representing a Message Addressing Property is not valid and the message cannot be processed";
            final Object detail = this.getProblemHeaderDetail(isReplyToAnonymous
                  ? AddressingConstants.Core.Elements.REPLYTO_QNAME
                  : AddressingConstants.Core.Elements.FAULTTO_QNAME);

            throw new DetailedAddressingException(faultCode, reason, detail);
         }
         catch (SOAPException e)
         {
            throw new WebServiceException(e);
         }
      }
   }

   private Node getProblemHeaderDetail(final QName problemHeaderQName) throws SOAPException
   {
      final Element problemHeaderQNameElement = DOMUtils
            .createElement(AddressingConstants.Core.Elements.PROBLEMHEADERQNAME_QNAME);
      problemHeaderQNameElement.setTextContent(problemHeaderQName.toString());

      return problemHeaderQNameElement;
   }

   private boolean isAnonymous(final EndpointReference epr)
   {
      if ((epr != null) && (epr.getAddress() != null))
         return ADDR_CONSTANTS.getAnonymousURI().equals(epr.getAddress().getURI().toString());

      return true;
   }

   private String getFaultAction(final MessageContext msgContext)
   {
      final AddressingOpMetaExt addressingMD = this.getAddressingMetaData(msgContext);
      final Throwable exception = ((CommonMessageContext) msgContext).getCurrentException();
      final OperationMetaData operationMD = this.getOperationMetaData(msgContext);
      final FaultMetaData faultMD = operationMD != null ? operationMD.getFaultMetaData(exception.getClass()) : null;

      if (faultMD != null)
      {
         final QName faultQName = faultMD.getXmlName();
         return addressingMD.getFaultAction(faultQName);
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
