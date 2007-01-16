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
package org.jboss.ws.metadata.builder;

// $Id$

import org.jboss.logging.Logger;
import org.jboss.ws.Constants;
import org.jboss.ws.WSException;
import org.jboss.ws.core.jaxrpc.Use;
import org.jboss.ws.core.server.ServiceEndpointManager;
import org.jboss.ws.core.server.ServiceEndpointManagerFactory;
import org.jboss.ws.core.server.UnifiedDeploymentInfo;
import org.jboss.ws.core.utils.ObjectNameFactory;
import org.jboss.ws.metadata.j2ee.*;
import org.jboss.ws.metadata.j2ee.UnifiedWebSecurityMetaData.UnifiedWebResourceCollection;
import org.jboss.ws.metadata.umdm.ClientEndpointMetaData;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.ServerEndpointMetaData;
import org.jboss.ws.metadata.wsdl.*;
import org.w3c.dom.Element;

import javax.management.ObjectName;
import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** An abstract meta data builder.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 19-May-2005
 */
public abstract class MetaDataBuilder
{
   // provide logging
   private final static Logger log = Logger.getLogger(MetaDataBuilder.class);

   /** Inititialize the endpoint binding */
   protected void initEndpointBinding(WSDLEndpoint wsdlEndpoint, ClientEndpointMetaData epMetaData)
   {
      WSDLDefinitions wsdlDefinitions = wsdlEndpoint.getWsdlService().getWsdlDefinitions();
      WSDLInterface wsdlInterface = wsdlEndpoint.getInterface();
      WSDLBinding wsdlBinding = wsdlDefinitions.getBindingByInterfaceName(wsdlInterface.getQName());
      String bindingType = wsdlBinding.getType();
      if (Constants.NS_SOAP11.equals(bindingType))
         epMetaData.setBindingId(Constants.SOAP11HTTP_BINDING);
      else if (Constants.NS_SOAP12.equals(bindingType))
         epMetaData.setBindingId(Constants.SOAP12HTTP_BINDING);
   }

   /** Initialize the endpoint encoding style from the binding operations
    */
   protected void initEndpointEncodingStyle(EndpointMetaData epMetaData)
   {
      WSDLDefinitions wsdlDefinitions = epMetaData.getServiceMetaData().getWsdlDefinitions();
      for (WSDLService wsdlService : wsdlDefinitions.getServices())
      {
         for (WSDLEndpoint wsdlEndpoint : wsdlService.getEndpoints())
         {
            if (epMetaData.getPortName().equals(wsdlEndpoint.getQName()))
            {
               QName bindQName = wsdlEndpoint.getBinding();
               NCName ncName = new NCName(bindQName.getLocalPart());
               WSDLBinding wsdlBinding = wsdlDefinitions.getBinding(ncName);
               if (wsdlBinding == null)
                  throw new WSException("Cannot obtain binding: " + ncName);

               for (WSDLBindingOperation wsdlBindingOperation : wsdlBinding.getOperations())
               {
                  String encStyle = wsdlBindingOperation.getEncodingStyle();
                  epMetaData.setEncodingStyle(Use.valueOf(encStyle));
               }
            }
         }
      }
   }


   public static void initEndpointAddress(UnifiedDeploymentInfo udi, ServerEndpointMetaData sepMetaData, String linkName)
   {
      String contextRoot = sepMetaData.getContextRoot();
      String urlPattern = sepMetaData.getURLPattern();

      if (udi.metaData instanceof UnifiedWebMetaData)
      {
         UnifiedWebMetaData webMetaData = (UnifiedWebMetaData)udi.metaData;
         String jbwebContextRoot = webMetaData.getContextRoot();
         if (jbwebContextRoot != null)
            contextRoot = jbwebContextRoot;

         Map<String, String> servletMappings = webMetaData.getServletMappings();
         urlPattern = (String)servletMappings.get(linkName);
         if (urlPattern == null)
            throw new WSException("Cannot obtain url pattern for servlet name: " + linkName);
      }

      if (udi.metaData instanceof UnifiedApplicationMetaData)
      {
         UnifiedApplicationMetaData applMetaData = (UnifiedApplicationMetaData)udi.metaData;
         UnifiedBeanMetaData beanMetaData = (UnifiedBeanMetaData)applMetaData.getBeanByEjbName(linkName);
         if (beanMetaData == null)
            throw new WSException("Cannot obtain meta data for ejb link: " + linkName);

         String wsContextRoot = applMetaData.getWebServiceContextRoot();
         if (wsContextRoot != null)
            contextRoot = wsContextRoot;

         UnifiedEjbPortComponentMetaData ejbpcMetaData = beanMetaData.getPortComponent();
         if (ejbpcMetaData != null && ejbpcMetaData.getPortComponentURI() != null)
         {
            String pcUrlPattern = ejbpcMetaData.getPortComponentURI();
            if (pcUrlPattern != null)
               urlPattern = pcUrlPattern;
         }
      }

      // If not, derive the context root from the deployment
      if (contextRoot == null)
      {
         contextRoot = "/";
         if (udi.parent != null)
         {
            String shortName = udi.parent.simpleName;
            shortName = shortName.substring(0, shortName.indexOf('.'));
            contextRoot += shortName + "-";
         }
         String shortName = udi.simpleName;
         shortName = shortName.substring(0, shortName.indexOf('.'));
         contextRoot += shortName;
      }

      if (contextRoot.startsWith("/") == false)
         contextRoot = "/" + contextRoot;
      if (urlPattern == null)
         urlPattern = "/" + linkName;
      if (urlPattern.startsWith("/") == false)
         urlPattern = "/" + urlPattern;

      sepMetaData.setContextRoot(contextRoot);
      sepMetaData.setURLPattern(urlPattern);

      String servicePath = contextRoot + urlPattern;
      sepMetaData.setEndpointAddress(getServiceEndpointAddress(null, servicePath));
   }

   public static ObjectName createServiceEndpointID(UnifiedDeploymentInfo udi, ServerEndpointMetaData sepMetaData)
   {
      String linkName = sepMetaData.getLinkName();
      String context = sepMetaData.getContextRoot();
      if (context.startsWith("/"))
         context = context.substring(1);

      StringBuilder idstr = new StringBuilder(ServerEndpointMetaData.SEPID_DOMAIN + ":");
      idstr.append(ServerEndpointMetaData.SEPID_PROPERTY_CONTEXT + "=" + context);
      idstr.append("," + ServerEndpointMetaData.SEPID_PROPERTY_ENDPOINT + "=" + linkName);

      // Add JMS destination JNDI name for MDB endpoints
      if (udi.metaData instanceof UnifiedApplicationMetaData)
      {
         String ejbName = sepMetaData.getLinkName();
         if (ejbName == null)
            throw new WSException("Cannot obtain ejb-link from port component");

         UnifiedApplicationMetaData applMetaData = (UnifiedApplicationMetaData)udi.metaData;
         UnifiedBeanMetaData beanMetaData = (UnifiedBeanMetaData)applMetaData.getBeanByEjbName(ejbName);
         if (beanMetaData == null)
            throw new WSException("Cannot obtain ejb meta data for: " + ejbName);

         if (beanMetaData instanceof UnifiedMessageDrivenMetaData)
         {
            UnifiedMessageDrivenMetaData mdMetaData = (UnifiedMessageDrivenMetaData)beanMetaData;
            String jndiName = mdMetaData.getDestinationJndiName();
            idstr.append(",jms=" + jndiName);
         }
      }

      return ObjectNameFactory.create(idstr.toString());
   }


   /** Get the web service address for a given path
    */
   public static String getServiceEndpointAddress(String uriScheme, String servicePath)
   {
      if (servicePath == null || servicePath.length() == 0)
         throw new WSException("Service path cannot be null");

      if (servicePath.endsWith("/*"))
         servicePath = servicePath.substring(0, servicePath.length() - 2);

      if (uriScheme == null)
         uriScheme = "http";

      ServiceEndpointManagerFactory factory = ServiceEndpointManagerFactory.getInstance();
      ServiceEndpointManager epManager = factory.getServiceEndpointManager();
      String host = epManager.getWebServiceHost();
      int port = epManager.getWebServicePort();
      if ("https".equals(uriScheme))
         port = epManager.getWebServiceSecurePort();

      String urlStr = uriScheme + "://" + host + ":" + port + servicePath;
      try
      {
         return new URL(urlStr).toExternalForm();
      }
      catch (MalformedURLException e)
      {
         throw new WSException("Malformed URL: " + urlStr);
      }
   }

   /**
    * Read the transport guarantee from web.xml
    */
   protected void initTransportGuaranteeJSE(UnifiedDeploymentInfo udi, ServerEndpointMetaData sepMetaData, String servletLink) throws IOException
   {
      String transportGuarantee = null;
      if (udi.metaData instanceof UnifiedWebMetaData)
      {
         UnifiedWebMetaData webMetaData = (UnifiedWebMetaData)udi.metaData;
         Map<String, String> servletMappings = webMetaData.getServletMappings();
         String urlPattern = servletMappings.get(servletLink);

         if (urlPattern == null)
            throw new WSException("Cannot find <url-pattern> for servlet-name: " + servletLink);

         List<UnifiedWebSecurityMetaData> securityList = webMetaData.getSecurityMetaData();
         for (UnifiedWebSecurityMetaData currentSecurity : securityList)
         {
            if (currentSecurity.getTransportGuarantee() != null && currentSecurity.getTransportGuarantee().length() > 0)
            {
               for (UnifiedWebResourceCollection currentCollection : currentSecurity.getWebResources())
               {
                  for (String currentUrlPattern : currentCollection.getUrlPatterns())
                  {
                     if (urlPattern.equals(currentUrlPattern))
                     {
                        transportGuarantee = currentSecurity.getTransportGuarantee();
                     }
                  }
               }
            }
         }
      }
      sepMetaData.setTransportGuarantee(transportGuarantee);
   }
   
   /** Replace the address locations for a given port component.
    */
   public static void replaceAddressLocation(ServerEndpointMetaData sepMetaData)
   {
      WSDLDefinitions wsdlDefinitions = sepMetaData.getServiceMetaData().getWsdlDefinitions();
      QName portName = sepMetaData.getPortName();

      boolean endpointFound = false;
      for (WSDLService wsdlService : wsdlDefinitions.getServices())
      {
         for (WSDLEndpoint wsdlEndpoint : wsdlService.getEndpoints())
         {
            QName wsdlPortName = wsdlEndpoint.getQName();
            if (wsdlPortName.equals(portName))
            {
               endpointFound = true;

               String orgAddress = wsdlEndpoint.getAddress();
               String uriScheme = getUriScheme(orgAddress);

               String transportGuarantee = sepMetaData.getTransportGuarantee();
               if ("CONFIDENTIAL".equals(transportGuarantee))
                  uriScheme = "https";

               String servicePath = sepMetaData.getContextRoot() + sepMetaData.getURLPattern();
               String serviceEndpointURL = getServiceEndpointAddress(uriScheme, servicePath);

               ServiceEndpointManagerFactory factory = ServiceEndpointManagerFactory.getInstance();
               ServiceEndpointManager epManager = factory.getServiceEndpointManager();
               boolean alwaysModify = epManager.isAlwaysModifySOAPAddress();

               if (alwaysModify || uriScheme == null || orgAddress.indexOf("REPLACE_WITH_ACTUAL_URL") >= 0)
               {
                  log.debug("Replace service endpoint address '" + orgAddress + "' with '" + serviceEndpointURL + "'");
                  wsdlEndpoint.setAddress(serviceEndpointURL);
                  sepMetaData.setEndpointAddress(serviceEndpointURL);

                  // modify the wsdl-1.1 definition
                  if (wsdlDefinitions.getWsdlOneOneDefinition() != null)
                     replaceWSDL11SOAPAddress(wsdlDefinitions, portName, serviceEndpointURL);
               }
               else
               {
                  log.debug("Don't replace service endpoint address '" + orgAddress + "'");
                  try
                  {
                     sepMetaData.setEndpointAddress(new URL(orgAddress).toExternalForm());
                  }
                  catch (MalformedURLException e)
                  {
                     throw new WSException("Malformed URL: " + orgAddress);
                  }
               }
            }
         }
      }

      if (endpointFound == false)
         throw new WSException("Cannot find port in wsdl: " + portName);
   }

   private static void replaceWSDL11SOAPAddress(WSDLDefinitions wsdlDefinitions, QName portQName, String serviceEndpointURL)
   {
      Definition wsdlOneOneDefinition = wsdlDefinitions.getWsdlOneOneDefinition();
      String tnsURI = wsdlOneOneDefinition.getTargetNamespace();

      // search for matching portElement and replace the address URI
      Port wsdlOneOnePort = modifySOAPAddress(tnsURI, portQName, serviceEndpointURL, wsdlOneOneDefinition.getServices());

      // recursivly process imports if none can be found
      if (wsdlOneOnePort == null && !wsdlOneOneDefinition.getImports().isEmpty())
      {

         Iterator imports = wsdlOneOneDefinition.getImports().values().iterator();
         while (imports.hasNext())
         {
            List l = (List)imports.next();
            Iterator importsByNS = l.iterator();
            while (importsByNS.hasNext())
            {
               Import anImport = (Import)importsByNS.next();
               wsdlOneOnePort = modifySOAPAddress(anImport.getNamespaceURI(), portQName, serviceEndpointURL, anImport.getDefinition().getServices());
            }
         }
      }

      // if it still doesn't exist something is wrong
      if (wsdlOneOnePort == null)
         throw new IllegalArgumentException("Cannot find port with name '" + portQName + "' in wsdl document");
   }

   private static Port modifySOAPAddress(String tnsURI, QName portQName, String serviceEndpointURL, Map services)
   {
      QName SOAP12_ADDRESS = new QName(Constants.NS_SOAP12, "address");

      Port wsdlOneOnePort = null;
      Iterator itServices = services.values().iterator();
      while (itServices.hasNext())
      {
         Service wsdlOneOneService = (Service)itServices.next();
         Map wsdlOneOnePorts = wsdlOneOneService.getPorts();
         Iterator itPorts = wsdlOneOnePorts.keySet().iterator();
         while (itPorts.hasNext())
         {
            String portLocalName = (String)itPorts.next();
            if (portQName.equals(new QName(tnsURI, portLocalName)))
            {
               wsdlOneOnePort = (Port)wsdlOneOnePorts.get(portLocalName);
               List extElements = wsdlOneOnePort.getExtensibilityElements();
               for (Object extElement : extElements)
               {
                  QName elementType = ((ExtensibilityElement)extElement).getElementType();
                  if (extElement instanceof SOAPAddress)
                  {
                     SOAPAddress address = (SOAPAddress)extElement;
                     address.setLocationURI(serviceEndpointURL);
                  }
                  else if (SOAP12_ADDRESS.equals(elementType))
                  {
                     Element domElement = ((UnknownExtensibilityElement)extElement).getElement();
                     domElement.setAttribute("location", serviceEndpointURL);
                  }
               }
            }
         }
      }

      return wsdlOneOnePort;
   }

   private static String getUriScheme(String addrStr)
   {
      try
      {
         URI addrURI = new URI(addrStr);
         String scheme = addrURI.getScheme();
         return scheme;
      }
      catch (URISyntaxException e)
      {
         return null;
      }
   }
}
