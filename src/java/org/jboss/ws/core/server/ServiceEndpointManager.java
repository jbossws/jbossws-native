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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.activation.DataHandler;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.JAXWSAConstants;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.MessageAbstraction;
import org.jboss.ws.core.jaxrpc.handler.MessageContextJAXRPC;
import org.jboss.ws.core.jaxrpc.handler.SOAPMessageContextJAXRPC;
import org.jboss.ws.core.jaxws.handler.MessageContextJAXWS;
import org.jboss.ws.core.jaxws.handler.SOAPMessageContextJAXWS;
import org.jboss.ws.core.soap.MessageContextAssociation;
import org.jboss.ws.core.soap.SOAPConnectionImpl;
import org.jboss.ws.core.utils.ThreadLocalAssociation;
import org.jboss.ws.extensions.addressing.AddressingConstantsImpl;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.HandlerMetaData;
import org.jboss.ws.metadata.umdm.ServerEndpointMetaData;
import org.jboss.ws.metadata.umdm.UnifiedMetaData;
import org.jboss.ws.metadata.umdm.EndpointMetaData.Type;
import org.jboss.ws.metadata.umdm.HandlerMetaData.HandlerType;

/**
 * A service that manages JBossWS endpoints.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 15-Jan-2005
 */
public class ServiceEndpointManager implements ServiceEndpointManagerMBean
{
   // provide logging
   private static final Logger log = Logger.getLogger(ServiceEndpointManager.class);

   // Default bean name
   public static final String BEAN_NAME = "ServiceEndpointManager";
   // The host name that is returned if there is no other defined
   public static String UNDEFINED_HOSTNAME = "jbossws.undefined.host";

   // maps serviceID to EndpointInfo
   private Map<ObjectName, ServiceEndpoint> registry = new ConcurrentHashMap<ObjectName, ServiceEndpoint>();

   // The webservice host name that will be used when updating the wsdl
   private String webServiceHost = UNDEFINED_HOSTNAME;
   // The webservice port that will be used when updating the wsdl
   private int webServicePort;
   // The webservice port that will be used when updating the wsdl
   private int webServiceSecurePort;
   // Whether we should always modify the soap address to the deployed endpoing location
   private boolean alwaysModifySOAPAddress;
   // The name of the invoker that handles invocations on JSE endpoints
   private String serviceEndpointInvokerJSE;
   // The name of the invoker that handles invocations on EJB2.1 endpoints
   private String serviceEndpointInvokerEJB21;
   // The name of the invoker that handles invocations on EJB3 endpoints
   private String serviceEndpointInvokerEJB3;
   // The name of the invoker that handles invocations on MDB endpoints
   private String serviceEndpointInvokerMDB;

   public String getWebServiceHost()
   {
      return webServiceHost;
   }

   public int getWebServicePort()
   {
      if (webServicePort == 0)
      {
         ServerConfigFactory factory = ServerConfigFactory.getInstance();
         ServerConfig config = factory.getServerConfig();
         webServicePort = config.getWebServicePort();
         log.debug("Using WebServicePort: " + webServicePort);
      }
      return webServicePort;
   }

   public int getWebServiceSecurePort()
   {
      if (webServiceSecurePort == 0)
      {
         ServerConfigFactory factory = ServerConfigFactory.getInstance();
         ServerConfig config = factory.getServerConfig();
         webServiceSecurePort = config.getWebServiceSecurePort();
         log.debug("Using WebServiceSecurePort: " + webServiceSecurePort);
      }
      return webServiceSecurePort;
   }

   public boolean isAlwaysModifySOAPAddress()
   {
      return alwaysModifySOAPAddress;
   }

   public void setWebServiceHost(String host) throws UnknownHostException
   {
      if (host == null || host.trim().length() == 0)
      {
         log.debug("Using undefined host: " + UNDEFINED_HOSTNAME);
         host = UNDEFINED_HOSTNAME;
      }
      if ("0.0.0.0".equals(host))
      {
         InetAddress localHost = InetAddress.getLocalHost();
         log.debug("Using local host: " + localHost.getHostName());
         host = localHost.getHostName();
      }
      this.webServiceHost = host;
   }

   public void setWebServicePort(int port)
   {
      this.webServicePort = port;
   }

   public void setWebServiceSecurePort(int port)
   {
      this.webServiceSecurePort = port;
   }

   public void setAlwaysModifySOAPAddress(boolean modify)
   {
      this.alwaysModifySOAPAddress = modify;
   }

   public String getServiceEndpointInvokerEJB21()
   {
      return serviceEndpointInvokerEJB21;
   }

   public void setServiceEndpointInvokerEJB21(String invoker)
   {
      this.serviceEndpointInvokerEJB21 = invoker;
   }

   public String getServiceEndpointInvokerEJB3()
   {
      return serviceEndpointInvokerEJB3;
   }

   public void setServiceEndpointInvokerEJB3(String invoker)
   {
      this.serviceEndpointInvokerEJB3 = invoker;
   }

   public String getServiceEndpointInvokerMDB()
   {
      return serviceEndpointInvokerMDB;
   }

   public void setServiceEndpointInvokerMDB(String invoker)
   {
      this.serviceEndpointInvokerMDB = invoker;
   }

   public String getServiceEndpointInvokerJSE()
   {
      return serviceEndpointInvokerJSE;
   }

   public void setServiceEndpointInvokerJSE(String invoker)
   {
      this.serviceEndpointInvokerJSE = invoker;
   }

   public String getImplementationVersion()
   {
      return UnifiedMetaData.getImplementationVersion();
   }

   public List<ObjectName> getServiceEndpoints()
   {
      ArrayList<ObjectName> list = new ArrayList<ObjectName>();
      list.addAll(registry.keySet());
      return list;
   }

   /** Get service endpoint for a given serviceID
    *
    * The keys into the registry are:
    *
    *    [deploment.ear]/[deployment.war]#WsdlService/PortName
    *    [deploment.ear]/[deployment.jar]#ServiceName/PortName
    *
    */
   public ServiceEndpoint getServiceEndpointByID(ObjectName sepID)
   {
      ServiceEndpoint wsEndpoint = (ServiceEndpoint)registry.get(sepID);
      if (wsEndpoint == null)
         log.warn("No ServiceEndpoint found for serviceID: " + sepID);

      return wsEndpoint;
   }

   /** Resolve a port-component-link, like:
    *
    *    [deployment.war]#PortComponentName
    *    [deployment.jar]#PortComponentName
    *
    */
   public ServiceEndpoint resolvePortComponentLink(String pcLink)
   {
      String pcName = pcLink;
      int hashIndex = pcLink.indexOf("#");
      if (hashIndex > 0)
      {
         pcName = pcLink.substring(hashIndex + 1);
      }

      ServiceEndpoint serviceEndpoint = null;
      for (ObjectName sepID : registry.keySet())
      {
         ServiceEndpoint auxEndpoint = registry.get(sepID);
         ServiceEndpointInfo sepInfo = auxEndpoint.getServiceEndpointInfo();
         if (pcName.equals(sepInfo.getServerEndpointMetaData().getPortComponentName()))
         {
            if (serviceEndpoint != null)
            {
               log.warn("Multiple service endoints found for: " + pcLink);
               serviceEndpoint = null;
               break;
            }
            serviceEndpoint = auxEndpoint;
         }
      }

      if (serviceEndpoint == null)
         log.warn("No ServiceEndpoint found for pcLink: " + pcLink);

      return serviceEndpoint;
   }

   /** Show the registered webservices
    */
   public String showServiceEndpointTable(URL requestURL) throws java.net.MalformedURLException
   {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);

      pw.println("<h3>Registered Service Endpoints</h3>");

      pw.println("<table>");
      pw.println("<tr><td>ServiceEndpointID</td><td>ServiceEndpointAddress</td><td>&nbsp;</td></tr>");
      Iterator it = registry.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry entry = (Map.Entry)it.next();
         ObjectName sepID = (ObjectName)entry.getKey();
         ServiceEndpoint wsEndpoint = (ServiceEndpoint)entry.getValue();
         ServiceEndpointInfo seInfo = wsEndpoint.getServiceEndpointInfo();
         String displayAddress = getDisplayAddress(seInfo, requestURL);
         pw.println("<tr><td>" + sepID.getCanonicalName() + "</td><td><a href='" + displayAddress + "?wsdl'>" + displayAddress + "?wsdl</a></td></tr>");
      }
      pw.println("</table>");
      pw.close();

      return sw.toString();
   }

   public List<ServiceEndpointDTO> getRegisteredEndpoints(URL requestURL) throws java.net.MalformedURLException
   {
      List<ServiceEndpointDTO> registered = new ArrayList<ServiceEndpointDTO>();
      Iterator it = registry.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry entry = (Map.Entry)it.next();
         ObjectName sepID = (ObjectName)entry.getKey();
         ServiceEndpoint wsEndpoint = (ServiceEndpoint)entry.getValue();
         ServiceEndpointInfo seInfo = wsEndpoint.getServiceEndpointInfo();
         String displayAddress = getDisplayAddress(seInfo, requestURL);

         try
         {
            ServiceEndpointDTO dto = new ServiceEndpointDTO();
            dto.setSepID(sepID);
            dto.setAddress(displayAddress);
            dto.setSeMetrics((ServiceEndpointMetrics)wsEndpoint.getServiceEndpointMetrics().clone());
            dto.setState(wsEndpoint.getState());
            registered.add(dto);
         }
         catch (CloneNotSupportedException e)
         {
         }
      }

      return registered;
   }

   private String getDisplayAddress(ServiceEndpointInfo seInfo, URL requestURL) throws MalformedURLException
   {
      String endpointAddress = seInfo.getServerEndpointMetaData().getEndpointAddress();
      URL displayURL = new URL(endpointAddress);
      String endPointPath = displayURL.getPath();
      if (this.getWebServiceHost().equals(ServiceEndpointManager.UNDEFINED_HOSTNAME) == true)
      {
         displayURL = requestURL;
      }
      String displayAddress = displayURL.getProtocol() + "://" + displayURL.getHost() + ":" + displayURL.getPort() + endPointPath;
      return displayAddress;
   }

   /** Get the endpoint metrics
    */
   public ServiceEndpointMetrics getServiceEndpointMetrics(ObjectName sepID)
   {
      ServiceEndpoint wsEndpoint = getServiceEndpointByID(sepID);
      return (wsEndpoint != null ? wsEndpoint.getServiceEndpointMetrics() : null);
   }

   /** Show endpoint metrics
    */
   public String showServiceEndpointMetrics(ObjectName sepID)
   {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);

      ServiceEndpointMetrics seMetrics = getServiceEndpointMetrics(sepID);
      if (seMetrics != null)
      {
         pw.println("<h3>Service Endpoint Metrics</h3>");

         pw.println("<table>");
         pw.println("<tr><td>EndpointID</td><td>" + seMetrics.getEndpointID() + "</td></tr>");
         pw.println("<tr><td>Start Time</td><td>" + seMetrics.getStartTime() + "</td></tr>");
         pw.println("<tr><td>Stop Time</td><td>" + seMetrics.getStopTime() + "</td></tr>");
         pw.println("<tr><td>Request Count</td><td>" + seMetrics.getRequestCount() + "</td></tr>");
         pw.println("<tr><td>Response Count</td><td>" + seMetrics.getRequestCount() + "</td></tr>");
         pw.println("<tr><td>Fault Count</td><td>" + seMetrics.getResponseCount() + "</td></tr>");
         pw.println("<tr><td>Max Processing Time</td><td>" + seMetrics.getMaxProcessingTime() + "</td></tr>");
         pw.println("<tr><td>Min Processing Time</td><td>" + seMetrics.getMinProcessingTime() + "</td></tr>");
         pw.println("<tr><td>Avg Processing Time</td><td>" + seMetrics.getAverageProcessingTime() + "</td></tr>");
         pw.println("<tr><td>Total Processing Time</td><td>" + seMetrics.getTotalProcessingTime() + "</td></tr>");
         pw.println("</table>");
         pw.close();
      }
      return sw.toString();
   }

   public void processWSDLRequest(ObjectName sepID, OutputStream outStream, URL requestURL, String resourcePath) throws Exception
   {
      ServiceEndpoint wsEndpoint = getServiceEndpointByID(sepID);
      if (wsEndpoint == null)
         throw new WSException("Cannot obtain endpoint for: " + sepID);

      wsEndpoint.handleWSDLRequest(outStream, requestURL, resourcePath);
   }

   public void processRequest(ObjectName sepID, InputStream inStream, OutputStream outStream, ServletRequestContext context) throws Exception
   {
      ServiceEndpoint wsEndpoint = getServiceEndpointByID(sepID);
      if (wsEndpoint == null)
         throw new WSException("Cannot obtain endpoint for: " + sepID);

      // Get the type of the endpoint
      ServerEndpointMetaData sepMetaData = wsEndpoint.getServiceEndpointInfo().getServerEndpointMetaData();
      Type type = sepMetaData.getType();

      ServletContext servletContext = context.getServletContext();
      HttpServletRequest httpRequest = context.getHttpServletRequest();
      HttpServletResponse httpResponse = context.getHttpServletResponse();
      ServletHeaderSource headerSource = new ServletHeaderSource(httpRequest, httpResponse);

      // Associate a message context with the current thread
      CommonMessageContext msgContext;
      if (type == EndpointMetaData.Type.JAXRPC)
      {
         msgContext = new SOAPMessageContextJAXRPC();
         msgContext.put(MessageContextJAXRPC.SERVLET_CONTEXT, servletContext);
         msgContext.put(MessageContextJAXRPC.SERVLET_REQUEST, httpRequest);
         msgContext.put(MessageContextJAXRPC.SERVLET_RESPONSE, httpResponse);
      }
      else
      {
         msgContext = new SOAPMessageContextJAXWS();
         msgContext.put(MessageContextJAXWS.MESSAGE_OUTBOUND_PROPERTY, new Boolean(false));
         msgContext.put(MessageContextJAXWS.INBOUND_MESSAGE_ATTACHMENTS, new HashMap<String, DataHandler>());
         msgContext.put(MessageContextJAXWS.HTTP_REQUEST_HEADERS, headerSource.getHeaderMap());
         msgContext.put(MessageContextJAXWS.HTTP_REQUEST_METHOD, httpRequest.getMethod());
         msgContext.put(MessageContextJAXWS.QUERY_STRING, httpRequest.getQueryString());
         msgContext.put(MessageContextJAXWS.PATH_INFO, httpRequest.getPathInfo());
         msgContext.put(MessageContextJAXWS.SERVLET_CONTEXT, servletContext);
         msgContext.put(MessageContextJAXWS.SERVLET_REQUEST, httpRequest);
         msgContext.put(MessageContextJAXWS.SERVLET_RESPONSE, httpResponse);
      }
      msgContext.setEndpointMetaData(sepMetaData);

      MessageContextAssociation.pushMessageContext(msgContext);
      try
      {
         MessageAbstraction resMessage = wsEndpoint.processRequest(headerSource, context, inStream);

         // Replace the message context with the response context
         msgContext = MessageContextAssociation.peekMessageContext();

         Map<String, List<String>> headers = (Map<String, List<String>>)msgContext.get(MessageContextJAXWS.HTTP_RESPONSE_HEADERS);
         if (headers != null)
            headerSource.setHeaderMap(headers);

         Integer code = (Integer)msgContext.get(MessageContextJAXWS.HTTP_RESPONSE_CODE);
         if (code != null)
            httpResponse.setStatus(code.intValue());

         boolean isFault = false;
         if (resMessage instanceof SOAPMessage)
         {
            SOAPPart part = ((SOAPMessage)resMessage).getSOAPPart();
            if (part == null)
               throw new SOAPException("Cannot obtain SOAPPart from response message");

            // R1126 An INSTANCE MUST return a "500 Internal Server Error" HTTP status code
            // if the response envelope is a Fault.
            //
            // Also, a one-way operation must show up as empty content, and can be detected
            // by a null envelope.
            SOAPEnvelope soapEnv = part.getEnvelope();
            isFault = soapEnv != null && soapEnv.getBody().hasFault();
            if (isFault && httpResponse != null)
            {
               httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
         }

         sendResponse(outStream, msgContext, isFault);
      }
      finally
      {
         outStream.flush();
         outStream.close();

         // Reset the message context association
         MessageContextAssociation.popMessageContext();

         // clear thread local storage
         ThreadLocalAssociation.clear();
      }
   }

   private void sendResponse(OutputStream outputStream, CommonMessageContext msgContext, boolean isFault) throws SOAPException, IOException
   {
      MessageAbstraction resMessage = msgContext.getMessageAbstraction();
      String wsaTo = null;

      // Get the destination from the AddressingProperties
      AddressingProperties outProps = (AddressingProperties)msgContext.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_OUTBOUND);
      if (outProps != null && outProps.getTo() != null)
      {
         AddressingConstantsImpl ADDR = new AddressingConstantsImpl();
         wsaTo = outProps.getTo().getURI().toString();
         if (wsaTo.equals(ADDR.getAnonymousURI()))
            wsaTo = null;
      }
      if (wsaTo != null)
      {
         log.debug("Sending response to addressing destination: " + wsaTo);
         new SOAPConnectionImpl().callOneWay((SOAPMessage)resMessage, wsaTo);
      }
      else
      {
         resMessage.writeTo(outputStream);
      }
   }

   /** Process the given SOAPRequest and return the corresponding SOAPResponse
    */
   public String processRequest(ObjectName sepID, String inMessage) throws Exception
   {
      log.debug("processSOAPRequest: " + sepID);

      ByteArrayInputStream inputStream = new ByteArrayInputStream(inMessage.getBytes("UTF-8"));
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream(512);

      processRequest(sepID, inputStream, outputStream, null);

      String outMsg = new String(outputStream.toByteArray());
      return outMsg;
   }

   /** Get the ServiceEndpointInvoker for this type of service endpoint
    */
   private ServiceEndpointInvoker getServiceEndpointInvoker(ServiceEndpointInfo seInfo) throws ClassNotFoundException, InstantiationException, IllegalAccessException
   {
      ServiceEndpointInvoker seInvoker = null;

      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      if (seInfo.getType() == ServiceEndpointInfo.EndpointType.JSE)
      {
         Class seInvokerClass = cl.loadClass(serviceEndpointInvokerJSE);
         seInvoker = (ServiceEndpointInvoker)seInvokerClass.newInstance();
      }
      else if (seInfo.getType() == ServiceEndpointInfo.EndpointType.SLSB21)
      {
         Class seInvokerClass = cl.loadClass(serviceEndpointInvokerEJB21);
         seInvoker = (ServiceEndpointInvoker)seInvokerClass.newInstance();
      }
      else if (seInfo.getType() == ServiceEndpointInfo.EndpointType.SLSB30)
      {
         Class seInvokerClass = cl.loadClass(serviceEndpointInvokerEJB3);
         seInvoker = (ServiceEndpointInvoker)seInvokerClass.newInstance();
      }
      else if (seInfo.getType() == ServiceEndpointInfo.EndpointType.MDB21)
      {
         Class seInvokerClass = cl.loadClass(serviceEndpointInvokerMDB);
         seInvoker = (ServiceEndpointInvoker)seInvokerClass.newInstance();
      }

      if (seInvoker == null)
         throw new WSException("Cannot obtain service endpoint invoker");

      return seInvoker;
   }

   /** Get the list of HandlerInfos associated with a given service endpoint
    */
   public List<HandlerMetaData> getHandlerMetaData(ObjectName sepID)
   {
      ServiceEndpoint wsEndpoint = getServiceEndpointByID(sepID);
      if (wsEndpoint == null)
         throw new WSException("Cannot find service endpoint: " + sepID);

      List<HandlerMetaData> handlers = null;
      if (wsEndpoint != null)
      {
         ServerEndpointMetaData sepMetaData = wsEndpoint.getServiceEndpointInfo().getServerEndpointMetaData();
         handlers = sepMetaData.getHandlerMetaData(HandlerType.ALL);
      }
      return handlers;
   }

   /**
    * Dynamically change the list of handlers associated with a given service endpoint
    * The endpoint is expected to be in STOPED state
    */
   public void setHandlerMetaData(ObjectName sepID, List<HandlerMetaData> handlers)
   {
      ServiceEndpoint wsEndpoint = getServiceEndpointByID(sepID);
      if (wsEndpoint == null)
         throw new WSException("Cannot find service endpoint: " + sepID);

      ServiceEndpointInfo sepInfo = wsEndpoint.getServiceEndpointInfo();
      if (sepInfo.getState() != ServiceEndpoint.State.STOPED)
         throw new WSException("Endpoint expected to be in STOPED state");

      ServerEndpointMetaData sepMetaData = wsEndpoint.getServiceEndpointInfo().getServerEndpointMetaData();
      sepMetaData.clearHandlers();

      for (HandlerMetaData handlerMetaData : handlers)
      {
         handlerMetaData.setEndpointMetaData(sepMetaData);
         sepMetaData.addHandler(handlerMetaData);
      }
   }

   /** Create a service endpoint
    */
   public void createServiceEndpoint(ServiceEndpointInfo seInfo) throws Exception
   {
      ObjectName sepID = seInfo.getServiceEndpointID();
      if (registry.get(sepID) != null)
         throw new WSException("Service already registerd: " + sepID);

      ServiceEndpointInvoker seInvoker = getServiceEndpointInvoker(seInfo);
      seInvoker.init(seInfo);
      seInfo.setInvoker(seInvoker);

      // Load/Create the service endpoint impl
      ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
      ServerEndpointMetaData epMetaData = seInfo.getServerEndpointMetaData();
      String managedEndpointBean = epMetaData.getManagedEndpointBean();
      Class seClass = ctxLoader.loadClass(managedEndpointBean);
      Constructor ctor = seClass.getConstructor(new Class[] { ServiceEndpointInfo.class });
      ServiceEndpoint wsEndpoint = (ServiceEndpoint)ctor.newInstance(new Object[] { seInfo });
      wsEndpoint.create();

      // Register the endpoint with the MBeanServer
      registry.put(sepID, wsEndpoint);

      log.debug("WebService created: " + sepID);
   }

   /** Start a service endpoint
    */
   public void startServiceEndpoint(ObjectName sepID) throws Exception
   {
      ServiceEndpoint wsEndpoint = getServiceEndpointByID(sepID);
      if (wsEndpoint == null)
         throw new WSException("Cannot find service endpoint: " + sepID);

      wsEndpoint.start();

      ServiceEndpointInfo seInfo = wsEndpoint.getServiceEndpointInfo();
      log.info("WebService started: " + seInfo.getServerEndpointMetaData().getEndpointAddress());
   }

   /** Stop a service endpoint
    */
   public void stopServiceEndpoint(ObjectName sepID) throws Exception
   {
      ServiceEndpoint wsEndpoint = getServiceEndpointByID(sepID);
      if (wsEndpoint == null)
      {
         log.error("Cannot find service endpoint: " + sepID);
         return;
      }

      wsEndpoint.stop();

      ServiceEndpointInfo seInfo = wsEndpoint.getServiceEndpointInfo();
      log.info("WebService stopped: " + seInfo.getServerEndpointMetaData().getEndpointAddress());
   }

   /** Destroy a service endpoint
    */
   public void destroyServiceEndpoint(ObjectName sepID) throws Exception
   {
      ServiceEndpoint wsEndpoint = getServiceEndpointByID(sepID);
      if (wsEndpoint == null)
      {
         log.error("Cannot find service endpoint: " + sepID);
         return;
      }

      wsEndpoint.destroy();

      // Remove the endpoint from the MBeanServer
      registry.remove(sepID);

      ServiceEndpointInfo seInfo = wsEndpoint.getServiceEndpointInfo();
      log.debug("WebService destroyed: " + seInfo.getServerEndpointMetaData().getEndpointAddress());
   }

   public void create() throws Exception
   {
      log.info(getImplementationVersion());
      MBeanServer server = getJMXServer();
      if (server != null)
      {
         server.registerMBean(this, OBJECT_NAME);
      }
   }

   public void destroy() throws Exception
   {
      log.debug("Destroy service endpoint manager");
      MBeanServer server = getJMXServer();
      if (server != null)
      {
         server.unregisterMBean(OBJECT_NAME);
      }
   }

   private MBeanServer getJMXServer()
   {
      MBeanServer server = null;
      ArrayList servers = MBeanServerFactory.findMBeanServer(null);
      if (servers.size() > 0)
      {
         server = (MBeanServer)servers.get(0);
      }
      return server;
   }
}
