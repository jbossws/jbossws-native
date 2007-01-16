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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.Response;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.soap.SOAPFaultException;

import org.jboss.logging.Logger;
import org.jboss.util.NotImplementedException;
import org.jboss.ws.core.jaxws.binding.BindingProviderImpl;
import org.jboss.ws.core.soap.SOAPBodyImpl;
import org.jboss.ws.core.soap.SOAPConnectionImpl;
import org.jboss.ws.core.utils.DOMWriter;
import org.jboss.ws.metadata.umdm.EndpointMetaData;

/**
 * The Dispatch interface provides support for the dynamic invocation of a service endpoint operations. 
 * The javax.xml.ws.Service interface acts as a factory for the creation of Dispatch  instances.
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 04-Jul-2006
 */
public class DispatchImpl<T> implements Dispatch<T>
{
   // provide logging
   private final Logger log = Logger.getLogger(DispatchImpl.class);

   private BindingProvider bindingProvider;
   private EndpointMetaData epMetaData;
   private JAXBContext jaxbContext;
   private ExecutorService executor;
   private Class type;
   private Mode mode;

   public DispatchImpl(ExecutorService executor, EndpointMetaData epMetaData, Class<T> type, Mode mode)
   {
      this.bindingProvider = new BindingProviderImpl(epMetaData.getBindingId());
      this.epMetaData = epMetaData;
      this.executor = executor;
      this.type = type;
      this.mode = mode;
      initDispatch();
   }

   public DispatchImpl(ExecutorService executor, EndpointMetaData epMetaData, JAXBContext jbc, Mode mode)
   {
      this.bindingProvider = new BindingProviderImpl(epMetaData.getBindingId());
      this.epMetaData = epMetaData;
      this.executor = executor;
      this.type = Object.class;
      this.jaxbContext = jbc;
      this.mode = mode;
      initDispatch();
   }

   public T invoke(T obj)
   {
      T retObj = null;
      try
      {
         retObj = (T)invokeInternal(obj, getResponseContext());
      }
      catch (Exception ex)
      {
         handleInvokeException(ex);
      }
      return retObj;
   }

   private Object invokeInternal(Object obj, Map<String, Object> resContext) throws SOAPException
   {
      SOAPMessage reqMsg = getRequestMessage(obj);
      String targetAddress = epMetaData.getEndpointAddress();
      SOAPMessage resMsg = new SOAPConnectionImpl().call(reqMsg, targetAddress);
      Object retObj = getReturnObject(resMsg);
      return retObj;
   }

   public Response<T> invokeAsync(T msg)
   {
      ResponseImpl response = new ResponseImpl();
      Runnable task = new AsyncRunnable(response, null, msg);
      Future future = executor.submit(task);
      response.setFuture(future);
      return response;
   }

   public Future invokeAsync(T obj, AsyncHandler<T> handler)
   {
      ResponseImpl response = new ResponseImpl();
      Runnable task = new AsyncRunnable(response, handler, obj);
      Future future = executor.submit(task);
      response.setFuture(future);
      return response;
   }

   public void invokeOneWay(T msg)
   {
      SOAPMessage reqMsg = getRequestMessage(msg);
      try
      {
         String targetAddress = epMetaData.getEndpointAddress();
         new SOAPConnectionImpl().callOneWay(reqMsg, targetAddress);
      }
      catch (Exception ex)
      {
         handleInvokeException(ex);
      }
   }

   // 4.17. Conformance (Failed Dispatch.invoke): When an operation is invoked using an invoke method, an
   // implementation MUST throw a WebServiceException if there is any error in the configuration of the
   // Dispatch instance or a ProtocolException if an error occurs during the remote operation invocation.
   //
   // 4.19  Conformance (Failed Dispatch.invokeOneWay): When an operation is invoked using an invoke-
   // OneWay method, an implementation MUST throw a WebServiceException if there is any error in the
   // configuration of the Dispatch instance or if an error is detected1 during the remote operation invocation.
   private void handleInvokeException(Exception ex)
   {
      if (ex instanceof WebServiceException)
      {
         throw (WebServiceException)ex;
      }
      
      String msg = "Cannot dispatch message";
      log.error(msg, ex);
      throw new WebServiceException(msg, ex);
   }

   public Map<String, Object> getRequestContext()
   {
      return bindingProvider.getRequestContext();
   }

   public Map<String, Object> getResponseContext()
   {
      return bindingProvider.getResponseContext();
   }

   public Binding getBinding()
   {
      return bindingProvider.getBinding();
   }

   private void initDispatch()
   {
      if (SOAPMessage.class.isAssignableFrom(type) && mode == Mode.MESSAGE)
      {
         // accepted
      }
      else if (Source.class.isAssignableFrom(type))
      {
         // accepted
      }
      else if (jaxbContext != null && mode == Mode.PAYLOAD)
      {
         // accepted
      }
      else
      {
         throw new WebServiceException("Illegal argument combination [type=" + (type != null ? type.getName() : null) + ",mode=" + mode + "]");
      }
   }

   private SOAPMessage getRequestMessage(Object obj)
   {
      // jaxws/api/javax_xml_ws/Dispatch/Client.java#invokeTestJAXBNull
      if (obj == null)
         throw new SOAPFaultException("Request object cannot be null");
      
      SOAPMessage reqMsg = null;
      try
      {
         MessageFactory factory = MessageFactory.newInstance();
         if (SOAPMessage.class.isAssignableFrom(type))
         {
            reqMsg = (SOAPMessage)obj;
         }
         else if (Source.class.isAssignableFrom(type))
         {
            Source source = (Source)obj;
            if (mode == Mode.PAYLOAD)
            {
               reqMsg = factory.createMessage();
               SOAPBodyImpl soapBody = (SOAPBodyImpl)reqMsg.getSOAPBody();
               soapBody.setPayload(source);
            }
            if (mode == Mode.MESSAGE)
            {
               TransformerFactory tf = TransformerFactory.newInstance();
               ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
               tf.newTransformer().transform(source, new StreamResult(baos));
               reqMsg = factory.createMessage(null, new ByteArrayInputStream(baos.toByteArray()));
            }
         }
         else if (jaxbContext != null)
         {
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            marshaller.marshal(obj, baos);

            reqMsg = factory.createMessage();
            SOAPBodyImpl soapBody = (SOAPBodyImpl)reqMsg.getSOAPBody();
            StreamSource source = new StreamSource(new ByteArrayInputStream(baos.toByteArray()));
            soapBody.setPayload(source);
         }
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         throw new WebServiceException("Cannot create request message", ex);
      }

      if (reqMsg == null)
         throw new WebServiceException("Cannot create request message for: " + obj);

      return reqMsg;
   }

   private Object getReturnObject(SOAPMessage resMsg)
   {
      Object retObj = null;
      try
      {
         if (SOAPMessage.class.isAssignableFrom(type))
         {
            retObj = resMsg;
         }
         else if (Source.class.isAssignableFrom(type))
         {
            if (mode == Mode.PAYLOAD)
            {
               SOAPBodyImpl soapBody = (SOAPBodyImpl)resMsg.getSOAPBody();
               SOAPElement soapElement = (SOAPElement)soapBody.getChildElements().next();
               retObj = new DOMSource(soapElement);
            }
            if (mode == Mode.MESSAGE)
            {
               SOAPEnvelope soapEnvelope = resMsg.getSOAPPart().getEnvelope();
               String xmlMessage = DOMWriter.printNode(soapEnvelope, false);
               retObj = new StreamSource(new StringReader(xmlMessage));
            }
         }
         else if (jaxbContext != null)
         {
            SOAPBodyImpl soapBody = (SOAPBodyImpl)resMsg.getSOAPBody();
            SOAPElement soapElement = (SOAPElement)soapBody.getChildElements().next();

            log.debug("JAXB unmarshal: " + DOMWriter.printNode(soapElement, false));
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            retObj = unmarshaller.unmarshal(soapElement);
         }
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         throw new WebServiceException("Cannot process response message", ex);
      }
      return retObj;
   }

   class AsyncRunnable implements Runnable
   {
      private ResponseImpl response;
      private AsyncHandler handler;
      private Object payload;

      public AsyncRunnable(ResponseImpl response, AsyncHandler handler, Object payload)
      {
         this.response = response;
         this.handler = handler;
         this.payload = payload;
      }

      public void run()
      {
         try
         {
            Map<String, Object> resContext = response.getContext();
            Object result = invokeInternal(payload, resContext);
            response.set(result);

            // Call the handler if available
            if (handler != null)
               handler.handleResponse(response);
         }
         catch (Exception ex)
         {
            handleAsynInvokeException(ex);
         }
      }
      
      // 4.18 Conformance (Failed Dispatch.invokeAsync): When an operation is invoked using an invokeAsync
      // method, an implementation MUST throw a WebServiceException if there is any error in the configuration 
      // of the Dispatch instance. Errors that occur during the invocation are reported when the client
      // attempts to retrieve the results of the operation.
      private void handleAsynInvokeException(Exception ex)
      {
         String msg = "Cannot dispatch message";
         log.error(msg, ex);
         
         WebServiceException wsex;
         if (ex instanceof WebServiceException)
         {
            wsex = (WebServiceException)ex;
         }
         else
         {
            wsex = new WebServiceException(msg, ex);
         }
         response.setException(wsex);
      }
   }

   public EndpointReference getEndpointReference()
   {
      throw new NotImplementedException();
   }

   public <T extends EndpointReference> T getEndpointReference(Class<T> clazz)
   {
      throw new NotImplementedException();
   }
}
