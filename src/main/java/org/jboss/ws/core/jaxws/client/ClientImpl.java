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

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.PortInfo;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.http.HTTPException;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.SOAPFaultException;

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
import org.jboss.ws.metadata.umdm.ClientEndpointMetaData;
import org.jboss.ws.metadata.umdm.EndpointConfigMetaData;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.OperationMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerMetaData.HandlerType;

/**
 * Provides support for the dynamic invocation of a service endpoint.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 04-Jul-2006
 */

public class ClientImpl extends CommonClient implements BindingProvider
{

   // the associated endpoint meta data
   private final ClientEndpointMetaData epMetaData;
   private EndpointConfigMetaData epConfigMetaData;

   // Keep a handle on the resolver so that updateConfig calls may revisit the associated chains
   private final HandlerResolver handlerResolver;

   private Map<HandlerType, HandlerChainExecutor> executorMap = new HashMap<HandlerType, HandlerChainExecutor>();

   private static HandlerType[] HANDLER_TYPES = new HandlerType[] { HandlerType.PRE, HandlerType.ENDPOINT, HandlerType.POST };

   public ClientImpl(EndpointMetaData epMetaData, HandlerResolver handlerResolver)
   {
      super(epMetaData);
      setTargetEndpointAddress(epMetaData.getEndpointAddress());

      this.epMetaData = (ClientEndpointMetaData)epMetaData;
      this.epConfigMetaData = epMetaData.getEndpointConfigMetaData();

      if (handlerResolver instanceof HandlerResolverImpl)
      {
         this.handlerResolver = new HandlerResolverImpl((HandlerResolverImpl)handlerResolver);
      }
      else
      {
         this.handlerResolver = handlerResolver;
      }

      initBindingHandlerChain(false);
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
            impl.initHandlerChain(epConfigMetaData, HandlerType.PRE, clearExistingHandlers);
            impl.initHandlerChain(epConfigMetaData, HandlerType.ENDPOINT, clearExistingHandlers);
            impl.initHandlerChain(epConfigMetaData, HandlerType.POST, clearExistingHandlers);

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

   @Override
   protected boolean callRequestHandlerChain(QName portName, HandlerType type)
   {
      BindingExt binding = (BindingExt)getBindingProvider().getBinding();
      HandlerChainExecutor executor = new HandlerChainExecutor(epMetaData, binding.getHandlerChain(type), false);
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
         // Copy the inbound msg properties to the binding's response context
         msgContext = MessageContextAssociation.peekMessageContext();
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

   public EndpointConfigMetaData getEndpointConfigMetaData()
   {
      return epConfigMetaData;
   }

   public void setConfigName(String configName, String configFile)
   {
      if (configName == null)
         throw new IllegalArgumentException("Config name cannot be null");

      String orgConfigName = epConfigMetaData.getConfigName();
      String orgConfigFile = epConfigMetaData.getConfigFile();

      if (configFile == null)
      {
         configFile = orgConfigFile;
      }

      if (orgConfigName.equals(configName) == false || orgConfigFile.equals(configFile) == false)
      {
         epConfigMetaData = ((ClientEndpointMetaData)this.epMetaData).createEndpointConfigMetaData(configName, configFile);
         initBindingHandlerChain(true);
      }
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

}