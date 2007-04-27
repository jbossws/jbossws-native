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
package org.jboss.ws.core.server.legacy;

// $Id$

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;

import javax.activation.DataHandler;
import javax.management.MBeanException;
import javax.xml.namespace.QName;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.ws.http.HTTPBinding;

import org.jboss.logging.Logger;
import org.jboss.ws.Constants;
import org.jboss.ws.core.CommonBinding;
import org.jboss.ws.core.CommonBindingProvider;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.CommonSOAPBinding;
import org.jboss.ws.core.DirectionHolder;
import org.jboss.ws.core.EndpointInvocation;
import org.jboss.ws.core.MessageAbstraction;
import org.jboss.ws.core.DirectionHolder.Direction;
import org.jboss.ws.core.jaxrpc.handler.HandlerDelegateJAXRPC;
import org.jboss.ws.core.jaxrpc.handler.MessageContextJAXRPC;
import org.jboss.ws.core.jaxws.binding.BindingProviderImpl;
import org.jboss.ws.core.jaxws.handler.HandlerDelegateJAXWS;
import org.jboss.ws.core.jaxws.handler.MessageContextJAXWS;
import org.jboss.ws.core.server.ServerHandlerDelegate;
import org.jboss.ws.core.soap.MessageContextAssociation;
import org.jboss.ws.core.soap.SOAPMessageImpl;
import org.jboss.ws.core.utils.JavaUtils;
import org.jboss.ws.extensions.xop.XOPContext;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.OperationMetaData;
import org.jboss.ws.metadata.umdm.ServerEndpointMetaData;
import org.jboss.ws.metadata.umdm.HandlerMetaData.HandlerType;

/** An implementation handles invocations on the endpoint
 *
 * @author Thomas.Diesler@jboss.org
 * @since 19-Jan-2005
 */
public abstract class AbstractServiceEndpointInvoker implements ServiceEndpointInvoker
{
   // provide logging
   private static Logger log = Logger.getLogger(AbstractServiceEndpointInvoker.class);

   protected ServiceEndpointInfo seInfo;
   protected CommonBindingProvider bindingProvider;
   protected ServerHandlerDelegate delegate;

   /** Initialize the service endpoint */
   public void init(ServiceEndpointInfo seInfo)
   {
      this.seInfo = seInfo;
      ServerEndpointMetaData sepMetaData = seInfo.getServerEndpointMetaData();

      if (sepMetaData.getType() == EndpointMetaData.Type.JAXRPC)
      {
         bindingProvider = new CommonBindingProvider(sepMetaData);
         delegate = new HandlerDelegateJAXRPC(sepMetaData);
      }
      else
      {
         bindingProvider = new BindingProviderImpl(sepMetaData);
         delegate = new HandlerDelegateJAXWS(sepMetaData);
      }
   }

   /** Load the SEI implementation bean if necessary */
   protected abstract Class loadServiceEndpoint() throws ClassNotFoundException;

   /** Create the instance of the SEI implementation bean if necessary */
   protected abstract Object createServiceEndpointInstance(Object context, Class seiImplClass) throws Exception;

   /** Invoke the instance of the SEI implementation bean */
   protected abstract void invokeServiceEndpointInstance(Object seiImpl, EndpointInvocation epInv) throws Exception;

   /** Destroy the instance of the SEI implementation bean if necessary */
   protected abstract void destroyServiceEndpointInstance(Object seiImpl);

   public boolean callRequestHandlerChain(ServerEndpointMetaData sepMetaData, HandlerType type)
   {
      return delegate.callRequestHandlerChain(sepMetaData, type);
   }

   public boolean callResponseHandlerChain(ServerEndpointMetaData sepMetaData, HandlerType type)
   {
      return delegate.callResponseHandlerChain(sepMetaData, type);
   }

   public void closeHandlerChain(ServerEndpointMetaData sepMetaData, HandlerType type)
   {
      delegate.closeHandlerChain(sepMetaData, type);
   }

   public boolean callFaultHandlerChain(ServerEndpointMetaData sepMetaData, HandlerType type, Exception ex)
   {
      return delegate.callFaultHandlerChain(sepMetaData, type, ex);
   }

   /** Invoke the the service endpoint */
   public MessageAbstraction invoke(Object context) throws Exception
   {
      CommonMessageContext msgContext = MessageContextAssociation.peekMessageContext();
      ServerEndpointMetaData sepMetaData = (ServerEndpointMetaData)msgContext.getEndpointMetaData();
      MessageAbstraction reqMessage = msgContext.getMessageAbstraction();

      // Load the endpoint implementation bean
      Class seImpl = loadServiceEndpoint();

      // Create an instance of the endpoint implementation bean
      Object seInstance = createServiceEndpointInstance(context, seImpl);

      // The direction of the message
      DirectionHolder direction = new DirectionHolder(Direction.InBound);

      // Get the order of pre/post handlerchains 
      HandlerType[] handlerType = delegate.getHandlerTypeOrder();
      HandlerType[] faultType = delegate.getHandlerTypeOrder();

      // Set the required inbound context properties
      setInboundContextProperties();

      try
      {
         boolean oneway = false;
         EndpointInvocation epInv = null;
         OperationMetaData opMetaData = null;
         CommonBinding binding = bindingProvider.getCommonBinding();
         binding.setHeaderSource(delegate);

         // call the request handler chain
         boolean handlersPass = callRequestHandlerChain(sepMetaData, handlerType[0]);

         // Unbind the request message
         if (handlersPass)
         {
            // Get the operation meta data from the SOAP message
            opMetaData = getDispatchDestination(sepMetaData, reqMessage);
            msgContext.setOperationMetaData(opMetaData);
            oneway = opMetaData.isOneWay();

            /* 
             * From JAX-WS 10.2.1 - "7. If the node does not understand how to process
             * the message, then neither handlers nor the endpoint
             * are invoked and instead the binding generates a SOAP must
             * understand exception"
             *
             * Therefore, this must precede the ENDPOINT chain; however, The PRE
             * chain still must happen first since the message may be encrypted, in which
             * case the operation is still not known. Without knowing the operation, it 
             * is not possible to determine what headers are understood by the endpoint.
             */
            if (binding instanceof CommonSOAPBinding)
               ((CommonSOAPBinding)binding).checkMustUnderstand(opMetaData);

            // Unbind the request message
            epInv = binding.unbindRequestMessage(opMetaData, reqMessage);
         }

         handlersPass = handlersPass && callRequestHandlerChain(sepMetaData, handlerType[1]);
         handlersPass = handlersPass && callRequestHandlerChain(sepMetaData, handlerType[2]);

         if (handlersPass)
         {
            msgContext.put(CommonMessageContext.ALLOW_EXPAND_TO_DOM, Boolean.TRUE);
            try
            {
               // Check if protocol handlers modified the payload
               if (msgContext.isModified())
               {
                  log.debug("Handler modified payload, unbind message again");
                  reqMessage = msgContext.getMessageAbstraction();
                  epInv = binding.unbindRequestMessage(opMetaData, reqMessage);
               }

               // Invoke the service endpoint
               invokeServiceEndpointInstance(seInstance, epInv);
            }
            finally
            {
               msgContext.remove(CommonMessageContext.ALLOW_EXPAND_TO_DOM);
            }

            // Reverse the message direction
            msgContext = processPivotInternal(msgContext, direction);

            // Set the required outbound context properties
            setOutboundContextProperties();

            if (binding instanceof CommonSOAPBinding)
               XOPContext.setMTOMEnabled(((CommonSOAPBinding)binding).isMTOMEnabled());

            // Bind the response message
            MessageAbstraction resMessage = binding.bindResponseMessage(opMetaData, epInv);
            msgContext.setMessageAbstraction(resMessage);
         }
         else
         {
            // Reverse the message direction without calling the endpoint
            MessageAbstraction resMessage = msgContext.getMessageAbstraction();
            msgContext = processPivotInternal(msgContext, direction);
            msgContext.setMessageAbstraction(resMessage);
         }

         if (oneway == false)
         {
            // call the  response handler chain, removing the fault type entry will not call handleFault for that chain 
            handlersPass = callResponseHandlerChain(sepMetaData, handlerType[2]);
            faultType[2] = null;
            handlersPass = handlersPass && callResponseHandlerChain(sepMetaData, handlerType[1]);
            faultType[1] = null;
            handlersPass = handlersPass && callResponseHandlerChain(sepMetaData, handlerType[0]);
            faultType[0] = null;
         }

         MessageAbstraction resMessage = msgContext.getMessageAbstraction();
         return resMessage;
      }
      catch (RuntimeException ex)
      {
         // Reverse the message direction
         processPivotInternal(msgContext, direction);

         try
         {
            CommonBinding binding = bindingProvider.getCommonBinding();
            binding.bindFaultMessage(ex);

            // call the fault handler chain
            boolean handlersPass = true;
            if (faultType[2] != null)
               handlersPass = handlersPass && callFaultHandlerChain(sepMetaData, faultType[2], ex);
            if (faultType[1] != null)
               handlersPass = handlersPass && callFaultHandlerChain(sepMetaData, faultType[1], ex);
            if (faultType[0] != null)
               handlersPass = handlersPass && callFaultHandlerChain(sepMetaData, faultType[0], ex);
         }
         catch (RuntimeException subEx)
         {
            log.warn("Exception while processing handleFault: ", ex);
            ex = subEx;
         }
         throw ex;
      }
      finally
      {
         closeHandlerChain(sepMetaData, handlerType[2]);
         closeHandlerChain(sepMetaData, handlerType[1]);
         closeHandlerChain(sepMetaData, handlerType[0]);

         destroyServiceEndpointInstance(seInstance);
      }
   }

   protected void setInboundContextProperties()
   {
      CommonMessageContext msgContext = MessageContextAssociation.peekMessageContext();
      if (msgContext instanceof MessageContextJAXWS)
      {
         // Map of attachments to a message for the outbound message, key is the MIME Content-ID, value is a DataHandler
         msgContext.put(MessageContextJAXWS.INBOUND_MESSAGE_ATTACHMENTS, new HashMap<String, DataHandler>());
      }
   }

   protected void setOutboundContextProperties()
   {
      CommonMessageContext msgContext = MessageContextAssociation.peekMessageContext();
      if (msgContext instanceof MessageContextJAXWS)
      {
         // Map of attachments to a message for the outbound message, key is the MIME Content-ID, value is a DataHandler
         msgContext.put(MessageContextJAXWS.OUTBOUND_MESSAGE_ATTACHMENTS, new HashMap<String, DataHandler>());
      }
   }

   private CommonMessageContext processPivotInternal(CommonMessageContext msgContext, DirectionHolder direction)
   {
      if (direction.getDirection() == Direction.InBound)
      {
         EndpointMetaData epMetaData = msgContext.getEndpointMetaData();
         if (epMetaData.getType() == EndpointMetaData.Type.JAXRPC)
         {
            msgContext = MessageContextJAXRPC.processPivot(msgContext);
         }
         else
         {
            msgContext = MessageContextJAXWS.processPivot(msgContext);
         }
         direction.setDirection(Direction.OutBound);
      }
      return msgContext;
   }

   private OperationMetaData getDispatchDestination(EndpointMetaData epMetaData, MessageAbstraction reqMessage) throws SOAPException
   {
      OperationMetaData opMetaData;

      String bindingID = epMetaData.getBindingId();
      if (HTTPBinding.HTTP_BINDING.equals(bindingID))
      {
         if (epMetaData.getOperations().size() != 1)
            throw new IllegalStateException("Multiple operations not supported for HTTP binding");

         opMetaData = epMetaData.getOperations().get(0);
      }
      else
      {
         SOAPMessageImpl soapMessage = (SOAPMessageImpl)reqMessage;

         opMetaData = soapMessage.getOperationMetaData(epMetaData);
         SOAPHeader soapHeader = soapMessage.getSOAPHeader();

         // Report a MustUnderstand fault
         if (opMetaData == null)
         {
            String faultString;
            SOAPBody soapBody = soapMessage.getSOAPBody();
            if (soapBody.getChildElements().hasNext())
            {
               SOAPBodyElement soapBodyElement = (SOAPBodyElement)soapBody.getChildElements().next();
               Name soapName = soapBodyElement.getElementName();
               faultString = "Endpoint " + epMetaData.getPortName() + " does not contain operation meta data for: " + soapName;
            }
            else
            {
               faultString = "Endpoint " + epMetaData.getPortName() + " does not contain operation meta data for empty soap body";
            }

            // R2724 If an INSTANCE receives a message that is inconsistent with its WSDL description, it SHOULD generate a soap:Fault
            // with a faultcode of "Client", unless a "MustUnderstand" or "VersionMismatch" fault is generated.
            if (soapHeader != null && soapHeader.examineMustUnderstandHeaderElements(Constants.URI_SOAP11_NEXT_ACTOR).hasNext())
            {
               QName faultCode = Constants.SOAP11_FAULT_CODE_MUST_UNDERSTAND;
               throw new SOAPFaultException(faultCode, faultString, null, null);
            }
            else
            {
               QName faultCode = Constants.SOAP11_FAULT_CODE_CLIENT;
               throw new SOAPFaultException(faultCode, faultString, null, null);
            }
         }
      }
      return opMetaData;
   }

   protected Method getImplMethod(Class implClass, Method seiMethod) throws ClassNotFoundException, NoSuchMethodException
   {
      String methodName = seiMethod.getName();
      Class[] paramTypes = seiMethod.getParameterTypes();
      for (int i = 0; i < paramTypes.length; i++)
      {
         Class paramType = paramTypes[i];
         if (JavaUtils.isPrimitive(paramType) == false)
         {
            String paramTypeName = paramType.getName();
            paramType = JavaUtils.loadJavaType(paramTypeName);
            paramTypes[i] = paramType;
         }
      }

      Method implMethod = implClass.getMethod(methodName, paramTypes);
      return implMethod;
   }

   /** handle invocation exceptions */
   public void handleInvocationException(Throwable th) throws Exception
   {
      if (th instanceof InvocationTargetException)
      {
         // unwrap the throwable raised by the service endpoint implementation
         Throwable targetEx = ((InvocationTargetException)th).getTargetException();
         handleInvocationThrowable(targetEx);
      }

      if (th instanceof MBeanException)
      {
         throw ((MBeanException)th).getTargetException();
      }

      handleInvocationThrowable(th);
   }

   private void handleInvocationThrowable(Throwable th) throws Exception
   {
      if (th instanceof Exception)
      {
         throw (Exception)th;
      }
      else if (th instanceof Error)
      {
         throw (Error)th;
      }
      else
      {
         throw new UndeclaredThrowableException(th);
      }
   }
}
