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
package org.jboss.ws.core.server;

// $Id$

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;

import javax.management.MBeanException;
import javax.xml.namespace.QName;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;

import org.jboss.logging.Logger;
import org.jboss.ws.Constants;
import org.jboss.ws.core.CommonBinding;
import org.jboss.ws.core.CommonBindingProvider;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.CommonSOAPBinding;
import org.jboss.ws.core.DirectionHolder;
import org.jboss.ws.core.EndpointInvocation;
import org.jboss.ws.core.DirectionHolder.Direction;
import org.jboss.ws.core.jaxrpc.handler.HandlerDelegateJAXRPC;
import org.jboss.ws.core.jaxrpc.handler.MessageContextJAXRPC;
import org.jboss.ws.core.jaxws.binding.BindingProviderImpl;
import org.jboss.ws.core.jaxws.handler.HandlerDelegateJAXWS;
import org.jboss.ws.core.jaxws.handler.MessageContextJAXWS;
import org.jboss.ws.core.soap.MessageContextAssociation;
import org.jboss.ws.core.soap.SOAPBodyImpl;
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
   public SOAPMessage invoke(Object context) throws Exception
   {
      CommonMessageContext msgContext = MessageContextAssociation.peekMessageContext();
      ServerEndpointMetaData sepMetaData = (ServerEndpointMetaData)msgContext.getEndpointMetaData();
      SOAPMessageImpl reqMessage = (SOAPMessageImpl)msgContext.getSOAPMessage();

      // Load the endpoint implementation bean
      Class seImpl = loadServiceEndpoint();

      // Create an instance of the endpoint implementation bean
      Object seInstance = createServiceEndpointInstance(context, seImpl);

      // The direction of the message
      DirectionHolder direction = new DirectionHolder(Direction.InBound);
      
      try
      {
         boolean oneway = false;
         EndpointInvocation epInv = null;
         OperationMetaData opMetaData = null;
         CommonBinding binding = bindingProvider.getCommonBinding();
         binding.setHeaderSource(delegate);

         // call the handler chain
         boolean handlersPass = callRequestHandlerChain(sepMetaData, HandlerType.PRE);

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

         handlersPass = handlersPass && callRequestHandlerChain(sepMetaData, HandlerType.ENDPOINT);
         handlersPass = handlersPass && callRequestHandlerChain(sepMetaData, HandlerType.POST);

         if (handlersPass)
         {
            // Check if protocol handlers modified the payload
            if (((SOAPBodyImpl)reqMessage.getSOAPBody()).isModifiedFromSource())
            {
               log.debug("Handler modified body payload, unbind message again");
               epInv = binding.unbindRequestMessage(opMetaData, reqMessage);
            }

            // Invoke the service endpoint
            msgContext.setProperty(CommonMessageContext.ALLOW_EXPAND_TO_DOM, Boolean.TRUE);
            try
            {
               invokeServiceEndpointInstance(seInstance, epInv);
            }
            finally
            {
               msgContext.removeProperty(CommonMessageContext.ALLOW_EXPAND_TO_DOM);
            }

            // Reverse the message direction
            msgContext = processPivotInternal(msgContext, direction);

            if (binding instanceof CommonSOAPBinding)
               XOPContext.setMTOMEnabled(((CommonSOAPBinding)binding).isMTOMEnabled());

            // Bind the response message
            SOAPMessage resMessage = (SOAPMessage)binding.bindResponseMessage(opMetaData, epInv);
            msgContext.setSOAPMessage(resMessage);
         }
         else
         {
            // Reverse the message direction without calling the endpoint
            SOAPMessage resMessage = msgContext.getSOAPMessage();
            msgContext = processPivotInternal(msgContext, direction);
            msgContext.setSOAPMessage(resMessage);
         }

         // call the handler chain
         if (oneway == false)
         {
            handlersPass = callResponseHandlerChain(sepMetaData, HandlerType.POST);
            handlersPass = handlersPass && callResponseHandlerChain(sepMetaData, HandlerType.ENDPOINT);
            handlersPass = handlersPass && callResponseHandlerChain(sepMetaData, HandlerType.PRE);
         }

         SOAPMessage resMessage = msgContext.getSOAPMessage();
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

            // call the handler chain
            boolean handlersPass = callFaultHandlerChain(sepMetaData, HandlerType.POST, ex);
            handlersPass = handlersPass && callFaultHandlerChain(sepMetaData, HandlerType.ENDPOINT, ex);
            handlersPass = handlersPass && callFaultHandlerChain(sepMetaData, HandlerType.PRE, ex);
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
         closeHandlerChain(sepMetaData, HandlerType.POST);
         closeHandlerChain(sepMetaData, HandlerType.ENDPOINT);
         closeHandlerChain(sepMetaData, HandlerType.PRE);

         destroyServiceEndpointInstance(seInstance);
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

   private OperationMetaData getDispatchDestination(EndpointMetaData epMetaData, SOAPMessageImpl reqMessage) throws SOAPException
   {
      OperationMetaData opMetaData = reqMessage.getOperationMetaData(epMetaData);
      SOAPHeader soapHeader = reqMessage.getSOAPHeader();

      // Report a MustUnderstand fault
      if (opMetaData == null)
      {
         String faultString;
         SOAPBody soapBody = reqMessage.getSOAPBody();
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
