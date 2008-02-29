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
package org.jboss.ws.core.jaxws.client;

// $Id$

import java.net.URI;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.addressing.AddressingBuilder;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.JAXWSAConstants;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.PortInfo;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.http.HTTPException;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.ws.wsaddressing.BindingProvider21;

import org.jboss.logging.Logger;
import org.jboss.remoting.transport.http.HTTPMetadataConstants;
import org.jboss.util.NotImplementedException;
import org.jboss.ws.core.CommonBindingProvider;
import org.jboss.ws.core.CommonClient;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.jaxws.binding.BindingExt;
import org.jboss.ws.core.jaxws.binding.BindingProviderImpl;
import org.jboss.ws.core.jaxws.handler.HandlerChainExecutor;
import org.jboss.ws.core.jaxws.handler.HandlerResolverImpl;
import org.jboss.ws.core.jaxws.handler.MessageContextJAXWS;
import org.jboss.ws.core.jaxws.handler.SOAPMessageContextJAXWS;
import org.jboss.ws.core.soap.MessageContextAssociation;
import org.jboss.ws.extensions.addressing.AddressingClientUtil;
import org.jboss.ws.extensions.wsrm.RMAddressingConstants;
import org.jboss.ws.extensions.wsrm.RMConstant;
import org.jboss.ws.extensions.wsrm.RMClientSequence;
import org.jboss.ws.extensions.wsrm.api.RMException;
import org.jboss.ws.extensions.wsrm.common.RMHelper;
import org.jboss.ws.extensions.wsrm.spi.RMConstants;
import org.jboss.ws.extensions.wsrm.spi.RMProvider;
import org.jboss.ws.extensions.wsrm.spi.protocol.RMAckRequested;
import org.jboss.ws.extensions.wsrm.spi.protocol.RMCreateSequenceResponse;
import org.jboss.ws.extensions.wsrm.spi.protocol.RMSequence;
import org.jboss.ws.extensions.wsrm.spi.protocol.RMSequenceAcknowledgement;
import org.jboss.ws.extensions.wsrm.spi.protocol.RMSerializable;
import org.jboss.ws.metadata.config.Configurable;
import org.jboss.ws.metadata.config.ConfigurationProvider;
import org.jboss.ws.metadata.umdm.ClientEndpointMetaData;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.OperationMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerMetaData.HandlerType;

/**
 * Provides support for the dynamic invocation of a service endpoint.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 04-Jul-2006
 */
public class ClientImpl extends CommonClient implements org.jboss.ws.extensions.wsrm.api.RMProvider, BindingProvider21, Configurable
{
   // provide logging
   private static Logger log = Logger.getLogger(ClientImpl.class);

   // the associated endpoint meta data
   private final ClientEndpointMetaData epMetaData;

   // Keep a handle on the resolver so that updateConfig calls may revisit the associated chains
   private final HandlerResolver handlerResolver;

   private Map<HandlerType, HandlerChainExecutor> executorMap = new HashMap<HandlerType, HandlerChainExecutor>();
   private static HandlerType[] HANDLER_TYPES = new HandlerType[] { HandlerType.PRE, HandlerType.ENDPOINT, HandlerType.POST };

   // WS-RM sequence associated with the proxy
   private RMClientSequence wsrmSequence;

   public final void setWSRMSequence(RMClientSequence wsrmSequence)
   {
      this.wsrmSequence = wsrmSequence;
   }

   public final RMClientSequence getWSRMSequence()
   {
      return this.wsrmSequence;
   }

   public ClientImpl(EndpointMetaData epMetaData, HandlerResolver handlerResolver)
   {
      super(epMetaData);
      setTargetEndpointAddress(epMetaData.getEndpointAddress());

      this.epMetaData = (ClientEndpointMetaData)epMetaData;
      this.handlerResolver = handlerResolver;

      initBindingHandlerChain(false);

      // The config may change at some later point in time
      // when applications utilize the ServiceDecorator API
      // When clients change the config-name, we need reset the handlerchain
      ((ConfigurationProvider)epMetaData).registerConfigObserver(this);
   }

   /**
    * Reset or create the client handler chain in the binding.<br>
    */
   private void initBindingHandlerChain(boolean clearExistingHandlers)
   {
      BindingExt binding = (BindingExt)getBindingProvider().getBinding();

      PortInfo portInfo = epMetaData.getPortInfo();

      if (handlerResolver != null)
      {

         boolean jbossHandlerResolver = handlerResolver instanceof HandlerResolverImpl;

         if (jbossHandlerResolver) // knows about PRE and POST handlers
         {
            HandlerResolverImpl impl = (HandlerResolverImpl)handlerResolver;
            impl.initHandlerChain(epMetaData, HandlerType.PRE, clearExistingHandlers);
            impl.initHandlerChain(epMetaData, HandlerType.ENDPOINT, clearExistingHandlers);
            impl.initHandlerChain(epMetaData, HandlerType.POST, clearExistingHandlers);

            List<Handler> preChain = impl.getHandlerChain(portInfo, HandlerType.PRE);
            List<Handler> postChain = impl.getHandlerChain(portInfo, HandlerType.POST);

            binding.setHandlerChain(postChain, HandlerType.POST);
            binding.setHandlerChain(preChain, HandlerType.PRE);
         }

         // The regular handler chain
         List<Handler> endpointChain = handlerResolver.getHandlerChain(portInfo);
         binding.setHandlerChain(endpointChain);
      }
   }

   /**
    * Callback when the config-name or config-file changes.
    */
   public void update(Observable observable, Object object)
   {
      log.debug("Configuration change event received. Reconfigure handler chain: " + object);

      // re-populate the binding handler chain
      initBindingHandlerChain(true);
   }

   @Override
   protected boolean callRequestHandlerChain(QName portName, HandlerType type)
   {
      BindingExt binding = (BindingExt)getBindingProvider().getBinding();
      HandlerChainExecutor executor = new HandlerChainExecutor(epMetaData, binding.getHandlerChain(type));
      executorMap.put(type, executor);

      MessageContext msgContext = (MessageContext)MessageContextAssociation.peekMessageContext();
      return executor.handleMessage(msgContext);
   }

   @Override
   protected boolean callResponseHandlerChain(QName portName, HandlerType type)
   {
      MessageContext msgContext = (MessageContext)MessageContextAssociation.peekMessageContext();
      HandlerChainExecutor executor = executorMap.get(type);
      return (executor != null ? executor.handleMessage(msgContext) : true);
   }

   @Override
   protected boolean callFaultHandlerChain(QName portName, HandlerType type, Exception ex)
   {
      MessageContext msgContext = (MessageContext)MessageContextAssociation.peekMessageContext();
      HandlerChainExecutor executor = executorMap.get(type);
      return (executor != null ? executor.handleFault(msgContext, ex) : true);
   }

   @Override
   protected void closeHandlerChain(QName portName, HandlerType type)
   {
      MessageContext msgContext = (MessageContext)MessageContextAssociation.peekMessageContext();
      HandlerChainExecutor executor = executorMap.get(type);
      if (executor != null)
         executor.close(msgContext);
   }

   @Override
   protected void setInboundContextProperties()
   {
      MessageContext msgContext = (MessageContext)MessageContextAssociation.peekMessageContext();

      // Map of attachments to a message for the inbound message, key is  the MIME Content-ID, value is a DataHandler
      msgContext.put(MessageContext.INBOUND_MESSAGE_ATTACHMENTS, new HashMap<String, DataHandler>());

      // Remoting meta data are available on successfull call completion
      if (msgContext.containsKey(CommonMessageContext.REMOTING_METADATA))
      {
         Map<?, ?> remotingMetadata = (Map)msgContext.get(CommonMessageContext.REMOTING_METADATA);

         // Get the HTTP_RESPONSE_CODE
         Integer resposeCode = (Integer)remotingMetadata.get(HTTPMetadataConstants.RESPONSE_CODE);
         if (resposeCode != null)
            msgContext.put(MessageContextJAXWS.HTTP_RESPONSE_CODE, resposeCode);

         // [JBREM-728] Improve access to HTTP response headers
         Map<String, List> headers = new HashMap<String, List>();
         for (Map.Entry en : remotingMetadata.entrySet())
         {
            if (en.getKey() instanceof String && en.getValue() instanceof List)
               headers.put((String)en.getKey(), (List)en.getValue());
         }
         msgContext.put(MessageContext.HTTP_RESPONSE_HEADERS, headers);
      }
   }

   @Override
   protected void setOutboundContextProperties()
   {
      // Mark the message context as outbound
      CommonMessageContext msgContext = MessageContextAssociation.peekMessageContext();
      msgContext.put(MessageContextJAXWS.MESSAGE_OUTBOUND_PROPERTY, Boolean.TRUE);

      // Map of attachments to a message for the outbound message, key is the MIME Content-ID, value is a DataHandler
      msgContext.put(MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS, new HashMap<String, DataHandler>());
   }

   // Invoked by the proxy invokation handler
   public Object invoke(QName opName, Object[] args, Map<String, Object> resContext) throws RemoteException
   {
      // Associate a message context with the current thread
      CommonMessageContext msgContext = new SOAPMessageContextJAXWS();
      MessageContextAssociation.pushMessageContext(msgContext);

      // The contents of the request context are used to initialize the message context (see section 9.4.1)
      // prior to invoking any handlers (see chapter 9) for the outbound message. Each property within the
      // request context is copied to the message context with a scope of HANDLER.
      Map<String, Object> reqContext = getBindingProvider().getRequestContext();

      if (this.wsrmSequence != null)
      {
         if (RMConstant.PROTOCOL_OPERATION_QNAMES.contains(opName) == false)
         {
            if (this.wsrmSequence.getBackPort() != null)
            {
               // rewrite ReplyTo to use client addressable back port
               Map<String, Object> requestContext = getBindingProvider().getRequestContext();
               AddressingProperties addressingProps = (AddressingProperties)requestContext.get(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND);
               addressingProps.setReplyTo(AddressingBuilder.getAddressingBuilder().newEndpointReference(this.wsrmSequence.getBackPort()));
            }
            Map<String, Object> rmRequestContext = new HashMap<String, Object>();
            List<QName> outMsgs = new LinkedList<QName>();
            wsrmSequence.newMessageNumber();
            outMsgs.add(RMProvider.get().getConstants().getSequenceQName());
            outMsgs.add(RMProvider.get().getConstants().getAckRequestedQName());
            if (wsrmSequence.isAckRequested())
            {
               // piggy backing
               outMsgs.add(RMProvider.get().getConstants().getSequenceAcknowledgementQName());
            }
            rmRequestContext.put(RMConstant.PROTOCOL_MESSAGES, outMsgs);
            rmRequestContext.put(RMConstant.SEQUENCE_REFERENCE, wsrmSequence);
            reqContext.put(RMConstant.REQUEST_CONTEXT, rmRequestContext);
         }
      }

      msgContext.putAll(reqContext);

      try
      {
         Object retObj = invoke(opName, args, false);
         return retObj;
      }
      catch (Exception ex)
      {
         OperationMetaData opMetaData = getOperationMetaData();
         if (opMetaData.isOneWay())
         {
            handleOneWayException(opMetaData, ex);
         }
         else
         {
            handleRemoteException(opMetaData, ex);
         }
         return null;
      }
      finally
      {
         msgContext = MessageContextAssociation.peekMessageContext();
         if (this.wsrmSequence != null)
         {
            if (RMConstant.PROTOCOL_OPERATION_QNAMES.contains(opName) == false)
            {
               Map<String, Object> wsrmResCtx = (Map<String, Object>) msgContext.get(RMConstant.RESPONSE_CONTEXT);
               if (wsrmResCtx != null)
               {
                  RMConstants wsrmConstants = RMProvider.get().getConstants();
                  Map<QName, RMSerializable> mapping = (Map<QName, RMSerializable>)wsrmResCtx.get(RMConstant.PROTOCOL_MESSAGES_MAPPING);
                  QName seq = wsrmConstants.getSequenceQName();
                  if (mapping.keySet().contains(seq))
                  {
                     RMHelper.handleSequenceHeader((RMSequence)mapping.get(seq), this.wsrmSequence);
                  }
                  QName seqAck = wsrmConstants.getSequenceAcknowledgementQName();
                  if (mapping.keySet().contains(seqAck))
                  {
                     RMHelper.handleSequenceAcknowledgementHeader((RMSequenceAcknowledgement)mapping.get(seqAck), this.wsrmSequence);
                  }
                  QName ackReq = wsrmConstants.getAckRequestedQName();
                  if (mapping.keySet().contains(ackReq))
                  {
                     RMHelper.handleAckRequestedHeader((RMAckRequested)mapping.get(ackReq), this.wsrmSequence);
                  }
               }
            }
         }

         // Copy the inbound msg properties to the binding's response context
         for (String key : msgContext.keySet())
         {
            Object value = msgContext.get(key);
            resContext.put(key, value);
         }

         // Reset the message context association
         MessageContextAssociation.popMessageContext();
      }
   }

   protected CommonMessageContext processPivot(CommonMessageContext reqMessageContext)
   {
      MessageContextJAXWS resMessageContext = MessageContextJAXWS.processPivot(reqMessageContext);
      return resMessageContext;
   }

   /**
    * 6.7 Conformance (One-way operations): When sending a one-way message, implementations
    * a WebServiceException if any error is detected when sending the message.
    */
   private void handleOneWayException(OperationMetaData opMetaData, Exception ex)
   {
      if (ex instanceof WebServiceException)
      {
         throw (WebServiceException)ex;
      }
      else
      {
         throw new WebServiceException(ex);
      }
   }

   /**
    * 4.2.4  Conformance (Remote Exceptions): If an error occurs during a remote operation invocation, an implemention
    * MUST throw a service specific exception if possible. If the error cannot be mapped to a service
    * specific exception, an implementation MUST throw a ProtocolException or one of its subclasses, as
    * appropriate for the binding in use. See section 6.4.1 for more details.
    */
   private void handleRemoteException(OperationMetaData opMetaData, Exception ex)
   {
      String bindingId = opMetaData.getEndpointMetaData().getBindingId();
      if (bindingId.startsWith(SOAPBinding.SOAP11HTTP_BINDING) || bindingId.startsWith(SOAPBinding.SOAP12HTTP_BINDING))
      {
         if (ex instanceof SOAPFaultException)
         {
            throw (SOAPFaultException)ex;
         }
         else if (ex instanceof WebServiceException)
         {
            throw (WebServiceException)ex;
         }
         else
         {
            throw new WebServiceException(ex);
         }
      }
      else if (HTTPBinding.HTTP_BINDING.equals(bindingId))
      {
         // FIXME: provide actual status code
         WebServiceException wsEx = new HTTPException(-1);
         wsEx.initCause(ex);
         throw wsEx;
      }
      else
      {
         throw new WebServiceException("Unsuported binding: " + bindingId, ex);
      }
   }

   @Override
   public void setTargetEndpointAddress(String endpointAddress)
   {
      getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
   }

   @Override
   public String getTargetEndpointAddress()
   {
      return (String)getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
   }

   @Override
   protected CommonBindingProvider getCommonBindingProvider()
   {
      if (bindingProvider == null)
      {
         bindingProvider = new BindingProviderImpl(getEndpointMetaData());
      }
      return bindingProvider;
   }

   public Map<String, Object> getRequestContext()
   {
      return getBindingProvider().getRequestContext();
   }

   public Map<String, Object> getResponseContext()
   {
      return getBindingProvider().getResponseContext();
   }

   public Binding getBinding()
   {
      return getBindingProvider().getBinding();
   }

   public BindingProvider getBindingProvider()
   {
      return (BindingProvider)getCommonBindingProvider();
   }

   public EndpointReference getEndpointReference()
   {
      throw new NotImplementedException();
   }

   public <T extends EndpointReference> T getEndpointReference(Class<T> clazz)
   {
      throw new NotImplementedException();
   }

   public void setConfigName(String configName, String configFile)
   {
      ConfigurationProvider configProvider = (ConfigurationProvider)getEndpointMetaData();
      configProvider.setConfigName(configName, configFile);
   }

   /**
    * Retrieve header names that can be processed by this binding
    * @return
    */
   public Set<QName> getHeaders()
   {
      Set<QName> headers = new HashSet<QName>();

      BindingExt binding = (BindingExt)getBinding();

      for (HandlerType type : HANDLER_TYPES)
      {
         for (Handler bindingHandler : binding.getHandlerChain(type))
         {
            if (bindingHandler instanceof SOAPHandler)
               headers.addAll(((SOAPHandler)bindingHandler).getHeaders());
         }
      }

      return headers;
   }

   @Override
   protected boolean shouldMaintainSession()
   {
      Object bool = getRequestContext().get(BindingProvider.SESSION_MAINTAIN_PROPERTY);
      return Boolean.TRUE.equals(bool);
   }

   ///////////////////
   // WS-RM support //
   ///////////////////
   @SuppressWarnings("unchecked")
   public void createSequence() throws RMException
   {
      if (this.wsrmSequence != null)
         throw new IllegalStateException("Sequence already registered with proxy instance");

      try
      {
         // set up addressing data
         RMClientSequence candidateSequence = new RMClientSequence(getEndpointMetaData().getConfig().getRMMetaData());
         String address = getEndpointMetaData().getEndpointAddress();
         String action = RMAddressingConstants.CREATE_SEQUENCE_WSA_ACTION;
         AddressingProperties addressingProps = null;
         URI backPort = candidateSequence.getBackPort();
         if (backPort != null)
         {
            addressingProps = AddressingClientUtil.createDefaultProps(action, address);
            addressingProps.setReplyTo(AddressingBuilder.getAddressingBuilder().newEndpointReference(backPort));
         }
         else
         {
            addressingProps = AddressingClientUtil.createAnonymousProps(action, address);
         }
         Map requestContext = getBindingProvider().getRequestContext();
         requestContext.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND, addressingProps);
         // set up wsrm request context
         QName createSequenceQN = RMProvider.get().getConstants().getCreateSequenceQName();
         Map rmRequestContext = new HashMap();
         List outMsgs = new LinkedList();
         outMsgs.add(createSequenceQN);
         rmRequestContext.put(RMConstant.PROTOCOL_MESSAGES, outMsgs);
         rmRequestContext.put(RMConstant.SEQUENCE_REFERENCE, candidateSequence);
         requestContext.put(RMConstant.REQUEST_CONTEXT, rmRequestContext);
         // invoke stub method
         invoke(createSequenceQN, new Object[] {}, getBindingProvider().getResponseContext());
         // read WSRM sequence id from response context
         Map rmResponseContext = (Map)getBindingProvider().getResponseContext().get(RMConstant.RESPONSE_CONTEXT);
         RMCreateSequenceResponse createSequenceResponse = ((RMCreateSequenceResponse)((Map)rmResponseContext.get(RMConstant.PROTOCOL_MESSAGES_MAPPING)).get(RMProvider.get().getConstants().getCreateSequenceResponseQName())); 
         String outboundId = createSequenceResponse.getIdentifier();
         candidateSequence.setClient(this);
         candidateSequence.setOutboundId(outboundId);
         candidateSequence.setBehavior(createSequenceResponse.getIncompleteSequenceBehavior());
         candidateSequence.setDuration(RMHelper.durationToLong(createSequenceResponse.getExpires()));
         this.wsrmSequence = candidateSequence;
      }
      catch (Exception e)
      {
         throw new RMException("Unable to create WSRM sequence", e);
      }
   }
   
   public void closeSequence()
   {
      try
      {
         this.wsrmSequence.close();
      }
      finally
      {
         this.wsrmSequence = null;
      }
   }
}
