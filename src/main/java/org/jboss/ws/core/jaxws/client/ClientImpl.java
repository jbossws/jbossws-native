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
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.http.HTTPException;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.SOAPFaultException;

import org.jboss.logging.Logger;
import org.jboss.util.NotImplementedException;
import org.jboss.ws.core.CommonBindingProvider;
import org.jboss.ws.core.CommonClient;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.StubExt;
import org.jboss.ws.core.jaxws.binding.BindingExt;
import org.jboss.ws.core.jaxws.binding.BindingProviderImpl;
import org.jboss.ws.core.jaxws.handler.HandlerChainExecutor;
import org.jboss.ws.core.jaxws.handler.MessageContextJAXWS;
import org.jboss.ws.core.jaxws.handler.PortInfoImpl;
import org.jboss.ws.core.jaxws.handler.SOAPMessageContextJAXWS;
import org.jboss.ws.core.soap.MessageContextAssociation;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.OperationMetaData;
import org.jboss.ws.metadata.umdm.HandlerMetaData.HandlerType;

/** 
 * Provides support for the dynamic invocation of a service endpoint.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 04-Jul-2006
 */
public class ClientImpl extends CommonClient implements BindingProvider
{
   // provide logging
   private static Logger log = Logger.getLogger(ClientImpl.class);
   
   public ClientImpl(EndpointMetaData epMetaData, HandlerResolver handlerResolver)
   {
      super(epMetaData);
      setTargetEndpointAddress(epMetaData.getEndpointAddress());
      initBindingHandlerChain(epMetaData, handlerResolver);
   }

   // Init the cilent handler chain in the binding
   private void initBindingHandlerChain(EndpointMetaData epMetaData, HandlerResolver handlerResolver)
   {
      Binding binding = getBindingProvider().getBinding();
      if (handlerResolver != null)
      {
         PortInfoImpl portInfo = new PortInfoImpl(epMetaData);
         List<Handler> handlerChain = binding.getHandlerChain();
         for (Handler handler : handlerResolver.getHandlerChain(portInfo))
         {
            handlerChain.add(handler);
         }
      }
   }

   @Override
   protected boolean callRequestHandlerChain(QName portName, HandlerType type)
   {
      BindingExt binding = (BindingExt)getBindingProvider().getBinding();
      HandlerChainExecutor executor = new HandlerChainExecutor(epMetaData, binding.getHandlerChain(type));
      MessageContext msgContext = (MessageContext)MessageContextAssociation.peekMessageContext();
      return executor.handleRequest(msgContext);
   }

   @Override
   protected boolean callResponseHandlerChain(QName portName, HandlerType type)
   {
      BindingExt binding = (BindingExt)getBindingProvider().getBinding();
      HandlerChainExecutor executor = new HandlerChainExecutor(epMetaData, binding.getHandlerChain(type));
      MessageContext msgContext = (MessageContext)MessageContextAssociation.peekMessageContext();
      return executor.handleResponse(msgContext);
   }

   @Override
   protected void setInboundContextProperties()
   {
      // Mark the message context as outbound
      CommonMessageContext msgContext = MessageContextAssociation.peekMessageContext();
      msgContext.setProperty(MessageContextJAXWS.MESSAGE_OUTBOUND_PROPERTY, new Boolean(false));
   }

   @Override
   protected void setOutboundContextProperties()
   {
      // Mark the message context as outbound
      CommonMessageContext msgContext = MessageContextAssociation.peekMessageContext();
      msgContext.setProperty(MessageContextJAXWS.MESSAGE_OUTBOUND_PROPERTY, new Boolean(true));
   }
   
   // Invoked by the proxy invokation handler
   public Object invoke(QName opName, Object[] args, Map<String, Object> resContext) throws RemoteException
   {
      // Associate a message context with the current thread
      SOAPMessageContextJAXWS msgContext = new SOAPMessageContextJAXWS();
      MessageContextAssociation.pushMessageContext(msgContext);
      
      // The contents of the request context are used to initialize the message context (see section 9.4.1)
      // prior to invoking any handlers (see chapter 9) for the outbound message. Each property within the
      // request context is copied to the message context with a scope of HANDLER.
      Map<String, Object> reqContext = getBindingProvider().getRequestContext();
      msgContext.putAll(reqContext);

      try
      {
         Object retObj = invoke(opName, args, resContext, false);
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
         // Reset the message context association
         MessageContextAssociation.popMessageContext();
      }
   }

   protected CommonMessageContext processPivot(CommonMessageContext requestContext) {
      log.debug("Begin response processing");

      // remove existing context
      MessageContextAssociation.popMessageContext();

      SOAPMessageContextJAXWS responseContext = new SOAPMessageContextJAXWS(requestContext);
      responseContext.setSOAPMessage(null);
      responseContext.clear(); // clear message context properties

      // associate new context
      MessageContextAssociation.pushMessageContext(responseContext);
      
      return responseContext;
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
         else
         {
            throw new SOAPFaultException(ex);
         }
      }
      else if (HTTPBinding.HTTP_BINDING.equals(bindingId))
      {
         // FIXME: provide actual status code
         throw new HTTPException(-1);
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

   public void setConfigName(String configName)
   {
      EndpointMetaData epMetaData = getEndpointMetaData();
      epMetaData.setConfigName(configName);
      // TODO: Handlers not re-initialized
      log.warn("Handlers not re-initialized");
   }
}
