/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.jbws;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.rpc.JAXRPCException;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.http.HTTPBinding;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.common.Constants;
import org.jboss.ws.common.DOMWriter;
import org.jboss.ws.common.IOUtils;
import org.jboss.ws.core.CommonBinding;
import org.jboss.ws.core.CommonBindingProvider;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.CommonSOAPFaultException;
import org.jboss.ws.core.HTTPMessageImpl;
import org.jboss.ws.core.MessageAbstraction;
import org.jboss.ws.core.MessageTrace;
import org.jboss.ws.core.binding.BindingException;
import org.jboss.ws.core.jaxrpc.handler.MessageContextJAXRPC;
import org.jboss.ws.core.jaxrpc.handler.SOAPMessageContextJAXRPC;
import org.jboss.ws.core.server.MimeHeaderSource;
import org.jboss.ws.core.server.ServiceEndpointInvoker;
import org.jboss.ws.core.server.ServletHeaderSource;
import org.jboss.ws.core.server.ServletRequestContext;
import org.jboss.ws.core.server.WSDLRequestHandler;
import org.jboss.ws.core.soap.MessageContextAssociation;
import org.jboss.ws.core.soap.MessageFactoryImpl;
import org.jboss.ws.core.soap.SOAPMessageImpl;
import org.jboss.ws.core.utils.ThreadLocalAssociation;
import org.jboss.ws.metadata.umdm.ServerEndpointMetaData;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.EndpointState;
import org.jboss.wsf.spi.invocation.InvocationContext;
import org.jboss.wsf.spi.invocation.RequestHandler;
import org.jboss.wsf.spi.management.EndpointMetrics;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.management.ServerConfigFactory;
import org.w3c.dom.Document;

/**
 * A request handler
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 25-Apr-2007
 */
public class RequestHandlerImpl implements RequestHandler
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(RequestHandlerImpl.class);
   // provide logging
   private static final Logger log = Logger.getLogger(RequestHandlerImpl.class);

   protected ServerConfig serverConfig;
   protected MessageFactoryImpl msgFactory;

   public RequestHandlerImpl()
   {
      final SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
      final ServerConfig serverConfig = spiProvider.getSPI(ServerConfigFactory.class).getServerConfig();
      
      this.init(serverConfig);
   }
   
   public RequestHandlerImpl(final ServerConfig serverConfig)
   {
      if (serverConfig == null) 
         throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "SERVER_CONFIG_CANNOT_BE_NULL"));
      
      this.init(serverConfig);
   }

   private void init(final ServerConfig serverConfig)
   {
      this.serverConfig = serverConfig;
      this.msgFactory = new MessageFactoryImpl();
   }
   
   public void handleHttpRequest(Endpoint endpoint, HttpServletRequest req, HttpServletResponse res, ServletContext context) throws ServletException, IOException
   {
      String method = req.getMethod();
      if (method.equals("POST"))
      {
         doPost(endpoint, req, res, context);
      }
      else if (method.equals("GET"))
      {
         doGet(endpoint, req, res, context);
      }
      else
      {
         throw new WSException(BundleUtils.getMessage(bundle, "UNSUPPORTED_METHOD",  method));
      }
   }

   private void doGet(Endpoint endpoint, HttpServletRequest req, HttpServletResponse res, ServletContext context) throws ServletException, IOException
   {
      // Process a WSDL request
      if (req.getParameter("wsdl") != null || req.getParameter("WSDL") != null)
      {
         res.setContentType("text/xml");
         ServletOutputStream out = res.getOutputStream();
         try
         {
            ServletRequestContext reqContext = new ServletRequestContext(context, req, res);
            handleWSDLRequest(endpoint, out, reqContext);
         }
         catch (Exception ex)
         {
            handleException(ex);
         }
         finally
         {
            try
            {
               out.close();
            }
            catch (IOException ioex)
            {
               if (log.isTraceEnabled() == true)
               {
                  log.trace("Cannot close output stream", ioex);
               }
               else
               {
                  log.debug("Cannot close output stream");
               }
            }
         }
      }
      else
      {
         res.setStatus(405);
         res.setContentType("text/plain");
         Writer out = res.getWriter();
         out.write("HTTP GET not supported");
         out.close();
      }
   }

   private void doPost(Endpoint endpoint, HttpServletRequest req, HttpServletResponse res, ServletContext context) throws ServletException, IOException
   {
      if (log.isDebugEnabled())
         log.debug("doPost: " + req.getRequestURI());

      ServletInputStream in = req.getInputStream();
      ServletOutputStream out = res.getOutputStream();

      ClassLoader classLoader = endpoint.getService().getDeployment().getRuntimeClassLoader();
      if (classLoader == null)
         throw new IllegalStateException(BundleUtils.getMessage(bundle, "NO_CLASSLOADER_ASSOCIATED"));

      // Set the thread context class loader
      ClassLoader ctxClassLoader = SecurityActions.getContextClassLoader();
      SecurityActions.setContextClassLoader(classLoader);
      try
      {
         ServletRequestContext reqContext = new ServletRequestContext(context, req, res);
         handleRequest(endpoint, in, out, reqContext);
      }
      catch (Exception ex)
      {
         handleException(ex);
      }
      finally
      {
         // Reset the thread context class loader
         SecurityActions.setContextClassLoader(ctxClassLoader);

         try
         {
            out.close();
         }
         catch (IOException ioex)
         {
            if (log.isTraceEnabled() == true)
            {
               log.trace("Cannot close output stream", ioex);
            }
            else
            {
               log.debug("Cannot close output stream");
            }
         }
      }
   }

   @SuppressWarnings("unchecked")
   public void handleRequest(Endpoint endpoint, InputStream inStream, OutputStream outStream, InvocationContext invContext)
   {
      if (log.isDebugEnabled())
         log.debug("handleRequest: " + endpoint.getName());

      ServerEndpointMetaData sepMetaData = endpoint.getAttachment(ServerEndpointMetaData.class);
      if (sepMetaData == null)
         throw new IllegalStateException(BundleUtils.getMessage(bundle, "CANNOT_OBTAIN_ENDPOINTMD"));

      // Build the message context
      CommonMessageContext msgContext = new SOAPMessageContextJAXRPC();
      invContext.addAttachment(javax.xml.rpc.handler.MessageContext.class, msgContext);

      // Set servlet specific properties
      HttpServletResponse httpResponse = null;
      ServletHeaderSource headerSource = null;
      if (invContext instanceof ServletRequestContext)
      {
         ServletRequestContext reqContext = (ServletRequestContext)invContext;

         ServletContext servletContext = reqContext.getServletContext();
         HttpServletRequest httpRequest = reqContext.getHttpServletRequest();
         httpResponse = reqContext.getHttpServletResponse();
         headerSource = new ServletHeaderSource(httpRequest, httpResponse);
         msgContext.put(MessageContextJAXRPC.SERVLET_CONTEXT, servletContext);
         msgContext.put(MessageContextJAXRPC.SERVLET_REQUEST, httpRequest);
         msgContext.put(MessageContextJAXRPC.SERVLET_RESPONSE, httpResponse);
      }

      // Associate a message context with the current thread
      MessageContextAssociation.pushMessageContext(msgContext);

      try
      {
         msgContext.setEndpointMetaData(sepMetaData);
         MessageAbstraction resMessage = processRequest(endpoint, headerSource, invContext, inStream);
         CommonMessageContext reqMsgContext = msgContext;

         boolean isFault = false;
         if (resMessage instanceof SOAPMessage)
         {
            SOAPPart part = ((SOAPMessage)resMessage).getSOAPPart();
            if (part == null)
               throw new SOAPException(BundleUtils.getMessage(bundle, "CANNOT_OBTAIN_SOAPPART"));

            // R1126 An INSTANCE MUST return a "500 Internal Server Error" HTTP status code
            // if the response envelope is a Fault.
            //
            // Also, a one-way operation must show up as empty content, and can be detected
            // by a null envelope.
            SOAPEnvelope soapEnv = part.getEnvelope();
            isFault = soapEnv != null && soapEnv.getBody().hasFault();
            if (isFault)
            {
               if (httpResponse != null)
               {
                  httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
               }
            }
         }

         if (outStream != null)
            sendResponse(endpoint, outStream, isFault);
         CommonMessageContext.cleanupAttachments(reqMsgContext);
      }
      catch (Exception ex)
      {
         WSException.rethrow(ex);
      }
      finally
      {
         // Cleanup outbound attachments
         CommonMessageContext.cleanupAttachments(MessageContextAssociation.peekMessageContext());

         // Reset the message context association
         MessageContextAssociation.popMessageContext();

         // clear thread local storage
         ThreadLocalAssociation.clear();
      }
   }

   private void sendResponse(Endpoint endpoint, OutputStream output, boolean isFault) throws SOAPException, IOException
   {
      CommonMessageContext msgContext = MessageContextAssociation.peekMessageContext();
      MessageAbstraction resMessage = msgContext.getMessageAbstraction();
      
      if (resMessage == null)
      {
         log.debug("Null response message");
         return;
      }

      resMessage.writeTo(output);
   }

   /**
    * Handle a request to this web service endpoint
    */
   private MessageAbstraction processRequest(Endpoint ep, MimeHeaderSource headerSource, InvocationContext reqContext, InputStream inputStream) throws BindingException
   {
      CommonMessageContext msgContext = MessageContextAssociation.peekMessageContext();

      ServerEndpointMetaData sepMetaData = ep.getAttachment(ServerEndpointMetaData.class);
      if (sepMetaData == null)
         throw new IllegalStateException(BundleUtils.getMessage(bundle, "CANNOT_OBTAIN_ENDPOINTMD"));

      long beginProcessing = 0;
      boolean debugEnabled = log.isDebugEnabled();
      try
      {
         EndpointState state = ep.getState();
         if (state != EndpointState.STARTED)
         {
            QName faultCode = Constants.SOAP11_FAULT_CODE_SERVER;
            String faultString = "Endpoint cannot handle requests in state: " + state;
            throw new CommonSOAPFaultException(faultCode, faultString);
         }

         if (debugEnabled)
            log.debug("BEGIN handleRequest: " + ep.getName());
         beginProcessing = initRequestMetrics(ep);

         MimeHeaders headers = (headerSource != null ? headerSource.getMimeHeaders() : null);

         MessageAbstraction reqMessage;

         String bindingID = sepMetaData.getBindingId();
         if (HTTPBinding.HTTP_BINDING.equals(bindingID))
         {
            reqMessage = new HTTPMessageImpl(headers, inputStream);
         }
         else
         {
            msgFactory.setStyle(sepMetaData.getStyle());

            reqMessage = (SOAPMessageImpl)msgFactory.createMessage(headers, inputStream);
         }

         // Associate current message with message context
         msgContext.setMessageAbstraction(reqMessage);

         // debug the incomming message
         MessageTrace.traceMessage("Incoming Request Message", reqMessage);

         // Get the Invoker
         ServiceEndpointInvoker epInvoker = ep.getAttachment(ServiceEndpointInvoker.class);
         if (epInvoker == null)
            throw new IllegalStateException(BundleUtils.getMessage(bundle, "CANNOT_OBTAIN_SEINVOKER"));

         // Invoke the service endpoint
         epInvoker.invoke(reqContext);

         // Get the response message context
         msgContext = MessageContextAssociation.peekMessageContext();

         // Get the response message
         MessageAbstraction resMessage = msgContext.getMessageAbstraction();
         if (resMessage != null)
            postProcessResponse(headerSource, resMessage);

         return resMessage;
      }
      catch (Exception ex)
      {
         MessageAbstraction resMessage = MessageContextAssociation.peekMessageContext().getMessageAbstraction();

         // In case we have an exception before the invoker is called
         // we create the fault message here.
         if (resMessage == null || resMessage.isFaultMessage() == false)
         {
            CommonBindingProvider bindingProvider = new CommonBindingProvider(sepMetaData);
            CommonBinding binding = bindingProvider.getCommonBinding();
            resMessage = binding.bindFaultMessage(ex);
         }

         if (resMessage != null)
            postProcessResponse(headerSource, resMessage);

         return resMessage;
      }
      finally
      {
         try
         {
            MessageAbstraction resMessage = MessageContextAssociation.peekMessageContext().getMessageAbstraction();
            if (resMessage != null)
            {
               if (resMessage.isFaultMessage())
               {
                  processFaultMetrics(ep, beginProcessing);
               }
               else
               {
                  processResponseMetrics(ep, beginProcessing);
               }
            }
         }
         catch (Exception ex)
         {
            log.error(BundleUtils.getMessage(bundle, "CANNOT_PROCESS_METRICS"),  ex);
         }

         if (debugEnabled)
            log.debug("END handleRequest: " + ep.getName());
      }
   }

   private long initRequestMetrics(Endpoint endpoint)
   {
      long beginTime = 0;

      EndpointMetrics metrics = endpoint.getEndpointMetrics();
      if (metrics != null)
         beginTime = metrics.processRequestMessage();

      return beginTime;
   }

   private void processResponseMetrics(Endpoint endpoint, long beginTime)
   {
      EndpointMetrics metrics = endpoint.getEndpointMetrics();
      if (metrics != null)
         metrics.processResponseMessage(beginTime);
   }

   private void processFaultMetrics(Endpoint endpoint, long beginTime)
   {
      EndpointMetrics metrics = endpoint.getEndpointMetrics();
      if (metrics != null)
         metrics.processFaultMessage(beginTime);
   }

   /** Set response mime headers
    */
   private void postProcessResponse(MimeHeaderSource headerSource, MessageAbstraction resMessage)
   {
      try
      {
         // Set the outbound headers
         if (headerSource != null && resMessage instanceof SOAPMessage)
         {
            ((SOAPMessage)resMessage).saveChanges();
            headerSource.setMimeHeaders(resMessage.getMimeHeaders());
         }

         // debug the outgoing message
         MessageTrace.traceMessage("Outgoing Response Message", resMessage);
      }
      catch (Exception ex)
      {
         WSException.rethrow("Faild to post process response message", ex);
      }
   }

   public void handleWSDLRequest(Endpoint endpoint, OutputStream outStream, InvocationContext context)
   {
      if (log.isDebugEnabled())
         log.debug("handleWSDLRequest: " + endpoint.getName());

      try
      {
         if (this.validInvocationContext(context))
         {
            final String resourcePath = this.getResourcePath(context);
            final URL requestURL = this.getRequestURL(endpoint, context);
            this.handleWSDLRequest(endpoint, outStream, resourcePath, requestURL);
         }
         else
         {
            final String epAddress = endpoint.getAddress();
            if (epAddress == null)
               throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "INVALID_ENDPOINT_ADDRESS",  epAddress));

            final URL wsdlUrl = new URL(epAddress + "?wsdl");
            IOUtils.copyStream(outStream, wsdlUrl.openStream());
         }
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (IOException ex)
      {
         throw new WSException(ex);
      }
   }

   private URL getRequestURL(Endpoint endpoint, InvocationContext context) throws MalformedURLException
   {
      URL requestURL = null;

      if (context instanceof ServletRequestContext)
      {
         ServletRequestContext reqContext = (ServletRequestContext)context;
         HttpServletRequest req = reqContext.getHttpServletRequest();
         requestURL = new URL(req.getRequestURL().toString());
      }
      else if (context.getProperty(Constants.NETTY_MESSAGE) != null)
      {
         requestURL = new URL(endpoint.getAddress());
      }
      
      return requestURL;
   }

   private String getResourcePath(final InvocationContext context)
   {
      String resourcePath = null;

      if (context instanceof ServletRequestContext)
      {
         ServletRequestContext reqContext = (ServletRequestContext)context;
         HttpServletRequest req = reqContext.getHttpServletRequest();
         resourcePath = (String)req.getParameter("resource");
      }
      else if (context.getProperty(Constants.NETTY_MESSAGE) != null)
      {
         return null;
         // TODO: implement resourcePath = getResourcePath(nettyMessage.getUri()); // i.e. parse it from query string
      }
      
      return resourcePath;
   }

   private boolean validInvocationContext(InvocationContext context)
   {
      if (context == null)
         return false;
      
      final boolean servletInvocationContext = context instanceof ServletRequestContext;
      final boolean nettyInvocationContext = context.getProperty(Constants.NETTY_MESSAGE) != null;

      return servletInvocationContext || nettyInvocationContext;
   }

   private void handleWSDLRequest(Endpoint endpoint, OutputStream outputStream, String resPath, URL reqURL) throws MalformedURLException, IOException
   {
      ServerEndpointMetaData epMetaData = endpoint.getAttachment(ServerEndpointMetaData.class);
      if (epMetaData == null)
         throw new IllegalStateException(BundleUtils.getMessage(bundle, "CANNOT_OBTAIN_ENDPOINTMD"));
      
      //The WSDLFilePublisher should set the location to an URL 
      URL wsdlLocation = epMetaData.getServiceMetaData().getWsdlLocation();
      String wsdlPublishLoc = epMetaData.getServiceMetaData().getWsdlPublishLocation();

      WSDLRequestHandler wsdlRequestHandler = new WSDLRequestHandler(wsdlLocation, wsdlPublishLoc, serverConfig);
      Document document = wsdlRequestHandler.getDocumentForPath(reqURL, resPath);

      OutputStreamWriter writer = new OutputStreamWriter(outputStream);
      new DOMWriter(writer, Constants.DEFAULT_XML_CHARSET).setPrettyprint(true).print(document);
   }

   private void handleException(Exception ex) throws ServletException
   {
      log.error(BundleUtils.getMessage(bundle, "ERROR_PROCESSING_WEB_SERVICE_REQUEST"),  ex);

      if (ex instanceof JAXRPCException)
         throw (JAXRPCException)ex;

      throw new ServletException(ex);
   }
}
