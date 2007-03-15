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
package org.jboss.ws.core;

// $Id:CommonClient.java 660 2006-08-01 16:29:43Z thomas.diesler@jboss.com $

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.ParameterMode;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.JAXWSAConstants;

import org.jboss.logging.Logger;
import org.jboss.ws.Constants;
import org.jboss.ws.WSException;
import org.jboss.ws.core.DirectionHolder.Direction;
import org.jboss.ws.core.jaxrpc.ParameterWrapping;
import org.jboss.ws.core.jaxrpc.Style;
import org.jboss.ws.core.jaxrpc.handler.HandlerChainBaseImpl;
import org.jboss.ws.core.soap.EndpointInfo;
import org.jboss.ws.core.soap.MessageContextAssociation;
import org.jboss.ws.core.soap.SOAPBodyImpl;
import org.jboss.ws.core.soap.SOAPConnectionImpl;
import org.jboss.ws.core.soap.UnboundHeader;
import org.jboss.ws.core.utils.HolderUtils;
import org.jboss.ws.extensions.addressing.AddressingConstantsImpl;
import org.jboss.ws.integration.ResourceLoaderAdapter;
import org.jboss.ws.metadata.umdm.ClientEndpointMetaData;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.OperationMetaData;
import org.jboss.ws.metadata.umdm.ParameterMetaData;
import org.jboss.ws.metadata.umdm.ServiceMetaData;
import org.jboss.ws.metadata.umdm.UnifiedMetaData;
import org.jboss.ws.metadata.umdm.EndpointMetaData.Type;
import org.jboss.ws.metadata.umdm.HandlerMetaData.HandlerType;

/**
 * Provides support for the dynamic invocation of a service endpoint.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 10-Oct-2004
 */
public abstract class CommonClient implements StubExt
{
   // provide logging
   private static Logger log = Logger.getLogger(CommonClient.class);

   // The endpoint together with the operationName uniquely identify the call operation
   protected EndpointMetaData epMetaData;
   // The current operation name
   protected QName operationName;
   // Output parameters
   protected EndpointInvocation epInv;
   // The binding provider
   protected CommonBindingProvider bindingProvider;
   // A Map<QName,UnboundHeader> of header entries
   private Map<QName, UnboundHeader> unboundHeaders = new LinkedHashMap<QName, UnboundHeader>();
   // A List<AttachmentPart> of attachment parts set through the proxy
   private List<AttachmentPart> attachmentParts = new ArrayList<AttachmentPart>();

   /** Create a call that needs to be configured manually
    */
   protected CommonClient(ServiceMetaData serviceMetaData)
   {
      // If the WSDLService has only one endpoint, use it
      if (serviceMetaData != null && serviceMetaData.getEndpoints().size() == 1)
      {
         this.epMetaData = serviceMetaData.getEndpoints().get(0);
      }
   }

   /** Create a call for a known WSDL endpoint.
    */
   protected CommonClient(EndpointMetaData epMetaData)
   {
      this.epMetaData = epMetaData;
   }

   /** Create a call for a known WSDL endpoint.
    */
   protected CommonClient(ServiceMetaData serviceMetaData, QName portName, QName opName)
   {
      if (serviceMetaData != null)
      {
         EndpointMetaData epMetaData = null;
         if (serviceMetaData.getEndpoints().size() > 0)
         {
            epMetaData = serviceMetaData.getEndpoint(portName);
            if (epMetaData == null)
               throw new WSException("Cannot find endpoint for name: " + portName);
         }

         if (epMetaData != null)
         {
            this.epMetaData = epMetaData;
         }
      }

      if (opName != null)
      {
         setOperationName(opName);
      }
   }

   /** Gets the address of a target service endpoint.
    */
   public abstract String getTargetEndpointAddress();

   /** Sets the address of the target service endpoint.
    */
   public abstract void setTargetEndpointAddress(String address);

   /** Gets the name of the operation to be invoked using this Call instance.
    */
   public QName getOperationName()
   {
      return this.operationName;
   }

   /** Sets the name of the operation to be invoked using this Call instance.
    */
   public void setOperationName(QName operationName)
   {
      this.operationName = operationName;
   }

   /** Get the OperationMetaData for the given operation name
    * If it does not exist, it will be created
    */
   public OperationMetaData getOperationMetaData()
   {
      if (operationName == null)
         throw new WSException("Operation name not set");

      return getOperationMetaData(operationName);
   }

   // Get the OperationMetaData for the given operation name
   // If it does not exist, it will be created
   public OperationMetaData getOperationMetaData(QName opName)
   {
      if (opName == null)
         throw new IllegalArgumentException("Cannot get OperationMetaData for null");

      EndpointMetaData epMetaData = getEndpointMetaData();
      OperationMetaData opMetaData = epMetaData.getOperation(opName);
      if (opMetaData == null && epMetaData.getServiceMetaData().getWsdlDefinitions() == null)
      {
         opMetaData = new OperationMetaData(epMetaData, opName, opName.getLocalPart());
         epMetaData.addOperation(opMetaData);
      }

      if (opMetaData == null)
         throw new WSException("Cannot obtain operation meta data for: " + opName);

      return opMetaData;
   }

   // Get the EndpointMetaData for all OperationMetaData
   public EndpointMetaData getEndpointMetaData()
   {
      if (epMetaData == null)
      {
         ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
         ResourceLoaderAdapter vfsRoot = new ResourceLoaderAdapter();
         UnifiedMetaData wsMetaData = new UnifiedMetaData(vfsRoot);
         wsMetaData.setClassLoader(ctxLoader);

         ServiceMetaData serviceMetaData = new ServiceMetaData(wsMetaData, new QName(Constants.NS_JBOSSWS_URI, "AnonymousService"));
         wsMetaData.addService(serviceMetaData);

         epMetaData = new ClientEndpointMetaData(serviceMetaData, new QName(Constants.NS_JBOSSWS_URI, "AnonymousPort"),
               new QName(Constants.NS_JBOSSWS_URI, "Anonymous"), Type.JAXRPC);
         epMetaData.setStyle(Style.RPC);

         serviceMetaData.addEndpoint(epMetaData);
      }
      return epMetaData;
   }

   protected abstract boolean callRequestHandlerChain(QName portName, HandlerType type);

   protected abstract boolean callResponseHandlerChain(QName portName, HandlerType type);

   protected abstract boolean callFaultHandlerChain(QName portName, HandlerType type, Exception ex);

   protected abstract void closeHandlerChain(QName portName, HandlerType type);

   protected abstract void setInboundContextProperties();

   protected abstract void setOutboundContextProperties();

   /** Call invokation goes as follows:
    *
    * 1) synchronize the operation name with the operation meta data
    * 2) synchronize the input parameters with the operation meta data
    * 3) generate the payload using a BindingProvider
    * 4) get the Invoker from Remoting, based on the target endpoint address
    * 5) do the invocation through the Remoting framework
    * 6) unwrap the result using the BindingProvider
    * 7) return the result
    */
   protected Object invoke(QName opName, Object[] inputParams, Map<String, Object> resContext, boolean forceOneway) throws Exception
   {
      if (opName.equals(operationName) == false)
         setOperationName(opName);

      OperationMetaData opMetaData = getOperationMetaData();
      boolean oneway = forceOneway || opMetaData.isOneWay();

      // Associate a message context with the current thread
      CommonMessageContext reqContext = MessageContextAssociation.peekMessageContext();
      reqContext.setOperationMetaData(opMetaData);

      // copy properties to the message context
      for (String key : getRequestContext().keySet())
      {
         Object value = getRequestContext().get(key);
         reqContext.setProperty(key, value);
      }

      // The direction of the message
      DirectionHolder direction = new DirectionHolder(Direction.OutBound);
      
      QName portName = epMetaData.getPortName();
      try
      {
         // Get the binding from the provider
         CommonBinding binding = (CommonBinding)getCommonBindingProvider().getCommonBinding();

         // Create the invocation and sync the input parameters
         epInv = new EndpointInvocation(opMetaData);
         epInv.initInputParams(inputParams);

         // Bind the request message
         SOAPMessage reqMessage = (SOAPMessage)binding.bindRequestMessage(opMetaData, epInv, unboundHeaders);

         // Add possible attachment parts
         addAttachmentParts(reqMessage);

         setOutboundContextProperties();

         // Call the request handlers
         boolean handlerPass = callRequestHandlerChain(portName, HandlerType.PRE);
         handlerPass = handlerPass && callRequestHandlerChain(portName, HandlerType.ENDPOINT);
         handlerPass = handlerPass && callRequestHandlerChain(portName, HandlerType.POST);

         if (handlerPass)
         {
            String targetAddress = getTargetEndpointAddress();

            // Fall back to wsa:To
            AddressingProperties addrProps = (AddressingProperties)reqContext.getProperty(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND);
            if (targetAddress == null && addrProps != null && addrProps.getTo() != null)
            {
               AddressingConstantsImpl ADDR = new AddressingConstantsImpl();
               String wsaTo = addrProps.getTo().getURI().toString();
               if (wsaTo.equals(ADDR.getAnonymousURI()) == false)
               {
                  try
                  {
                     URL wsaToURL = new URL(wsaTo);
                     if(log.isDebugEnabled()) log.debug("Sending request to addressing destination: " + wsaToURL);
                     targetAddress = wsaToURL.toExternalForm();
                  }
                  catch (MalformedURLException ex)
                  {
                     if(log.isDebugEnabled()) log.debug("Not a valid URL: " + wsaTo);
                  }
               }
            }

            // The endpoint address must be known beyond this point
            if (targetAddress == null)
               throw new WSException("Target endpoint address not set");

            EndpointInfo epInfo = new EndpointInfo(epMetaData, targetAddress, getRequestContext());

            SOAPMessage resMessage;
            if (oneway)
            {
               resMessage = new SOAPConnectionImpl().callOneWay(reqMessage, epInfo);
            }
            else
            {
               resMessage = new SOAPConnectionImpl().call(reqMessage, epInfo);
            }

            // at pivot the message context might be replaced
            reqContext = processPivotInternal(reqContext, direction);

            // Associate response message with message context
            reqContext.setSOAPMessage(resMessage);
         }

         setInboundContextProperties();

         // Get the return object
         Object retObj = null;
         if (oneway == false && handlerPass)
         {
            // Call the response handlers
            handlerPass = callResponseHandlerChain(portName, HandlerType.POST);

            // unbind the return values
            if (handlerPass)
            {
               // unbind the return values
               SOAPMessage resMessage = reqContext.getSOAPMessage();
               binding.unbindResponseMessage(opMetaData, resMessage, epInv, unboundHeaders);
            }

            handlerPass = handlerPass && callResponseHandlerChain(portName, HandlerType.ENDPOINT);
            handlerPass = handlerPass && callResponseHandlerChain(portName, HandlerType.PRE);

            // BP-1.0 R1027
            if (handlerPass)
               HandlerChainBaseImpl.checkMustUnderstand(reqContext, new String[] {});

            // Check if protocol handlers modified the payload
            if (((SOAPBodyImpl)reqMessage.getSOAPBody()).isModifiedFromSource())
            {
               log.debug("Handler modified body payload, unbind message again");
               SOAPMessage resMessage = reqContext.getSOAPMessage();
               binding.unbindResponseMessage(opMetaData, resMessage, epInv, unboundHeaders);
            }

            retObj = syncOutputParams(inputParams, epInv);
         }

         return retObj;
      }
      catch (Exception ex)
      {
         // Reverse the message direction
         processPivotInternal(reqContext, direction);
         
         callFaultHandlerChain(portName, HandlerType.POST, ex);
         callFaultHandlerChain(portName, HandlerType.ENDPOINT, ex);
         callFaultHandlerChain(portName, HandlerType.PRE, ex);
         throw ex;
      }
      finally
      {
         resContext.putAll(reqContext.getProperties());
         
         closeHandlerChain(portName, HandlerType.POST);
         closeHandlerChain(portName, HandlerType.ENDPOINT);
         closeHandlerChain(portName, HandlerType.PRE);
      }
   }

   private CommonMessageContext processPivotInternal(CommonMessageContext msgContext, DirectionHolder direction)
   {
      if (direction.getDirection() == Direction.OutBound)
      {
         msgContext = processPivot(msgContext);
         direction.setDirection(Direction.InBound);
      }
      return msgContext;
   }

   protected void addAttachmentParts(SOAPMessage reqMessage)
   {
      for (AttachmentPart part : attachmentParts)
      {
         if(log.isDebugEnabled()) log.debug("Adding attachment part: " + part.getContentId());
         reqMessage.addAttachmentPart(part);
      }
   }

   protected abstract CommonMessageContext processPivot(CommonMessageContext requestContext);

   protected CommonBindingProvider getCommonBindingProvider()
   {
      if (bindingProvider == null)
      {
         bindingProvider = new CommonBindingProvider(getEndpointMetaData());
      }
      return bindingProvider;
   }

   protected abstract Map<String, Object> getRequestContext();

   /** Synchronize the operation paramters with the call output parameters.
    */
   private Object syncOutputParams(Object[] inParams, EndpointInvocation epInv) throws SOAPException
   {
      Object retValue = null;

      // Assign the return value, if we have a return param
      OperationMetaData opMetaData = getOperationMetaData();
      ParameterMetaData retMetaData = opMetaData.getReturnParameter();
      if (retMetaData != null)
      {
         retValue = epInv.getReturnValue();
         if (opMetaData.isDocumentWrapped() && retMetaData.isMessageType() == false)
            retValue = ParameterWrapping.unwrapResponseParameters(retMetaData, retValue, inParams);
      }

      // Set the holder values for INOUT parameters
      for (ParameterMetaData paramMetaData : opMetaData.getParameters())
      {
         ParameterMode paramMode = paramMetaData.getMode();

         int index = paramMetaData.getIndex();
         if (index == -1 || paramMode == ParameterMode.INOUT || paramMode == ParameterMode.OUT)
         {
            QName xmlName = paramMetaData.getXmlName();
            Object value = epInv.getResponseParamValue(xmlName);
            // document/literal wrapped return value header
            if (index == -1)
            {
               retValue = value;
            }
            else
            {
               if (log.isTraceEnabled())
                  log.trace("holder [" + index + "] " + xmlName);
               HolderUtils.setHolderValue(inParams[index], value);
            }
         }
      }

      return retValue;
   }

   /**
    * Add a header that is not bound to an input parameter.
    * A propriatory extension, that is not part of JAXRPC.
    *
    * @param xmlName The XML name of the header element
    * @param xmlType The XML type of the header element
    */
   public void addUnboundHeader(QName xmlName, QName xmlType, Class javaType, ParameterMode mode)
   {
      UnboundHeader unboundHeader = new UnboundHeader(xmlName, xmlType, javaType, mode);
      unboundHeaders.put(xmlName, unboundHeader);
   }

   /**
    * Get the header value for the given XML name.
    * A propriatory extension, that is not part of JAXRPC.
    *
    * @param xmlName The XML name of the header element
    * @return The header value, or null
    */
   public Object getUnboundHeaderValue(QName xmlName)
   {
      UnboundHeader unboundHeader = unboundHeaders.get(xmlName);
      return (unboundHeader != null ? unboundHeader.getHeaderValue() : null);
   }

   /**
    * Set the header value for the given XML name.
    * A propriatory extension, that is not part of JAXRPC.
    *
    * @param xmlName The XML name of the header element
    */
   public void setUnboundHeaderValue(QName xmlName, Object value)
   {
      UnboundHeader unboundHeader = unboundHeaders.get(xmlName);
      if (unboundHeader == null)
         throw new IllegalArgumentException("Cannot find unbound header: " + xmlName);

      unboundHeader.setHeaderValue(value);
   }

   /**
    * Clear all registered headers.
    * A propriatory extension, that is not part of JAXRPC.
    */
   public void clearUnboundHeaders()
   {
      unboundHeaders.clear();
   }

   /**
    * Remove the header for the given XML name.
    * A propriatory extension, that is not part of JAXRPC.
    */
   public void removeUnboundHeader(QName xmlName)
   {
      unboundHeaders.remove(xmlName);
   }

   /**
    * Get an Iterator over the registered header XML names.
    * A propriatory extension, that is not part of JAXRPC.
    */
   public Iterator getUnboundHeaders()
   {
      return unboundHeaders.keySet().iterator();
   }

   /**
    * Adds the given AttachmentPart object to the outgoing SOAPMessage.
    * An AttachmentPart object must be created before it can be added to a message.
    */
   public void addAttachmentPart(AttachmentPart part)
   {
      attachmentParts.add(part);
   }

   /**
    * Clears the list of attachment parts.
    */
   public void clearAttachmentParts()
   {
      attachmentParts.clear();
   }

   /**
    * Creates a new empty AttachmentPart object.
    */
   public AttachmentPart createAttachmentPart()
   {
      try
      {
         MessageFactory factory = MessageFactory.newInstance();
         return factory.createMessage().createAttachmentPart();
      }
      catch (SOAPException ex)
      {
         throw new JAXRPCException("Cannot create attachment part");
      }
   }

   public String getConfigName()
   {
      EndpointMetaData epMetaData = getEndpointMetaData();
      return epMetaData.getConfigName();
   }

   public void setConfigName(String configName)
   {
      setConfigName(configName, null);
   }

   public abstract void setConfigName(String configName, String configFile);

   public String getConfigFile()
   {
      EndpointMetaData epMetaData = getEndpointMetaData();
      return epMetaData.getConfigFile();
   }
}
