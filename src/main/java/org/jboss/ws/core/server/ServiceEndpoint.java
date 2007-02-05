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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;

import org.jboss.logging.Logger;
import org.jboss.ws.Constants;
import org.jboss.ws.core.CommonBinding;
import org.jboss.ws.core.CommonBindingProvider;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.jaxrpc.binding.BindingException;
import org.jboss.ws.core.soap.MessageContextAssociation;
import org.jboss.ws.core.soap.MessageFactoryImpl;
import org.jboss.ws.core.soap.SOAPElementImpl;
import org.jboss.ws.core.soap.SOAPElementWriter;
import org.jboss.ws.core.soap.SOAPMessageImpl;
import org.jboss.ws.core.utils.DOMWriter;
import org.jboss.ws.extensions.xop.XOPContext;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.ServerEndpointMetaData;
import org.jboss.ws.metadata.umdm.UnifiedMetaData;
import org.w3c.dom.Document;

/**
 * This object registered with the ServiceEndpointManager service.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 16-Jan-2005
 */
public class ServiceEndpoint
{
   // provide logging
   private static Logger log = Logger.getLogger(ServiceEndpoint.class);
   private static Logger msgLog = Logger.getLogger("jbossws.SOAPMessage");

   /** Endpoint type enum */
   public enum State
   {
      CREATED, STARTED, STOPED, DESTROYED
   }

   // The deployment info for this endpoint
   protected ServiceEndpointInfo seInfo;
   // Some metrics for this endpoint
   protected ServiceEndpointMetrics seMetrics;

   public ServiceEndpoint(ServiceEndpointInfo seInfo)
   {
      this.seInfo = seInfo;
      this.seInfo.setState(State.CREATED);
      this.seMetrics = new ServiceEndpointMetrics(seInfo.getServiceEndpointID());
   }

   public State getState()
   {
      return seInfo.getState();
   }
   
   public ServiceEndpointInfo getServiceEndpointInfo()
   {
      return seInfo;
   }

   public ServiceEndpointMetrics getServiceEndpointMetrics()
   {
      return seMetrics;
   }

   public void create() throws Exception
   {
      seInfo.setState(State.CREATED);
   }

   public void start() throws Exception
   {
      // eagerly initialize the UMDM
      ServerEndpointMetaData epMetaData = seInfo.getServerEndpointMetaData();
      UnifiedMetaData wsMetaData = epMetaData.getServiceMetaData().getUnifiedMetaData();
      wsMetaData.eagerInitialize();
      
      seMetrics.start();
      seInfo.setState(State.STARTED);
   }

   public void stop()
   {
      seMetrics.stop();
      seInfo.setState(State.STOPED);
      if(log.isDebugEnabled()) log.debug("Stop Endpoint" + seMetrics);
   }

   public void destroy()
   {
      seInfo.setState(State.DESTROYED);
   }

   /** Handle a WSDL request or a request for an included resource
    */
   public void handleWSDLRequest(OutputStream outStream, URL reqURL, String resPath) throws IOException
   {
      ServiceEndpointInfo sepInfo = getServiceEndpointInfo();
      EndpointMetaData epMetaData = sepInfo.getServerEndpointMetaData();

      //String wsdlHost = reqURL.getHost();
      String wsdlHost = reqURL.getProtocol() + "://" + reqURL.getHost() + ":" + reqURL.getPort();

      ServiceEndpointManagerFactory factory = ServiceEndpointManagerFactory.getInstance();
      ServiceEndpointManager epManager = factory.getServiceEndpointManager();
      if (epManager.getWebServiceHost().equals(ServiceEndpointManager.UNDEFINED_HOSTNAME) == false)
      {
         wsdlHost = epManager.getWebServiceHost();
      }
      if(log.isDebugEnabled()) log.debug("WSDL request, using host: " + wsdlHost);

      WSDLRequestHandler wsdlRequestHandler = new WSDLRequestHandler(epMetaData);
      Document document = wsdlRequestHandler.getDocumentForPath(reqURL, wsdlHost, resPath);

      OutputStreamWriter writer = new OutputStreamWriter(outStream);
      new DOMWriter(writer).setPrettyprint(true).print(document.getDocumentElement());
      outStream.flush();
      outStream.close();
   }

   /**
    * Handle a request to this web service endpoint
    */
   public SOAPMessage handleRequest(HeaderSource headerSource, EndpointContext context, InputStream inputStream) throws BindingException
   {
      CommonMessageContext msgContext = MessageContextAssociation.peekMessageContext();
      ServerEndpointMetaData epMetaData = seInfo.getServerEndpointMetaData();

      long beginProcessing = 0;
      ClassLoader ctxClassLoader = Thread.currentThread().getContextClassLoader();
      try
      {
         State state = seInfo.getState();
         if (state != State.STARTED)
         {
            QName faultCode = Constants.SOAP11_FAULT_CODE_SERVER;
            String faultString = "Endpoint cannot handle requests in state: " + state;
            throw new SOAPFaultException(faultCode, faultString, null, null);
         }

         if(log.isDebugEnabled()) log.debug("BEGIN handleRequest: " + seInfo.getServiceEndpointID());
         beginProcessing = seMetrics.processRequestMessage();

         MessageFactoryImpl msgFactory = new MessageFactoryImpl();
         msgFactory.setServiceMode(epMetaData.getServiceMode());
         msgFactory.setStyle(epMetaData.getStyle());

         MimeHeaders headers = (headerSource != null ? headerSource.getMimeHeaders() : null);
         SOAPMessageImpl reqMessage = (SOAPMessageImpl)msgFactory.createMessage(headers, inputStream);

         // Associate current message with message context
         msgContext.setSOAPMessage(reqMessage);

         // debug the incomming message
         if (msgLog.isTraceEnabled())
         {
            SOAPEnvelope soapEnv = reqMessage.getSOAPPart().getEnvelope();
            String envStr = SOAPElementWriter.writeElement((SOAPElementImpl)soapEnv, true);
            msgLog.trace("Incoming SOAPMessage\n" + envStr);
         }

         // Set the thread context class loader
         ClassLoader classLoader = epMetaData.getClassLoader();
         Thread.currentThread().setContextClassLoader(classLoader);

         // Invoke the service endpoint
         ServiceEndpointInvoker seInvoker = seInfo.getInvoker();
         SOAPMessage resMessage = seInvoker.invoke(seInfo, context);

         if (resMessage != null)
            postProcessResponse(headerSource, resMessage);

         return resMessage;
      }
      catch (Exception ex)
      {
         SOAPMessage resMessage = msgContext.getSOAPMessage();

         // In case we have an exception before the invoker is called
         // we create the fault message here.
         if (resMessage == null || ((SOAPMessageImpl)resMessage).isFaultMessage() == false)
         {
            CommonBindingProvider bindingProvider = getCommonBindingProvider();
            CommonBinding binding = bindingProvider.getCommonBinding();
            resMessage = (SOAPMessage)binding.bindFaultMessage(ex);
         }

         if (resMessage != null)
            postProcessResponse(headerSource, resMessage);

         return resMessage;
      }
      finally
      {
         try
         {
            SOAPMessage soapMessage = msgContext.getSOAPMessage();
            if (soapMessage != null && soapMessage.getSOAPPart().getEnvelope() != null)
            {
               if (soapMessage.getSOAPPart().getEnvelope().getBody().getFault() != null)
               {
                  seMetrics.processFaultMessage(beginProcessing);
               }
               else
               {
                  seMetrics.processResponseMessage(beginProcessing);
               }
            }
         }
         catch (Exception ex)
         {
            log.error("Cannot process metrics", ex);
         }

         // Reset the thread context class loader
         Thread.currentThread().setContextClassLoader(ctxClassLoader);
         if(log.isDebugEnabled()) log.debug("END handleRequest: " + seInfo.getServiceEndpointID());
      }
   }

   /** Set response mime headers
    */
   private void postProcessResponse(HeaderSource headerSource, SOAPMessage resMessage)
   {
      try
      {
         // Set the outbound headers
         if (headerSource != null)
         {
            XOPContext.eagerlyCreateAttachments();
            resMessage.saveChanges();
            headerSource.setMimeHeaders(resMessage.getMimeHeaders());
         }

         // debug the outgoing message
         if (msgLog.isTraceEnabled())
         {
            resMessage.saveChanges();
            SOAPEnvelope soapEnv = resMessage.getSOAPPart().getEnvelope();
            if (soapEnv != null)
            {
               String envStr = SOAPElementWriter.writeElement((SOAPElementImpl)soapEnv, true);
               msgLog.trace("Outgoing SOAPMessage\n" + envStr);
            }
         }
      }
      catch (Exception ex)
      {
         throw new JAXRPCException("Faild to post process response message", ex);
      }
   }

   private CommonBindingProvider getCommonBindingProvider()
   {
      return new CommonBindingProvider(seInfo.getServerEndpointMetaData());
   }

   /**
    * Returns a string representation of the object.
    */
   public String toString()
   {
      StringBuilder buffer = new StringBuilder(seInfo.toString());
      buffer.append("\n state=" + seInfo.getState());
      return buffer.toString();
   }
}
