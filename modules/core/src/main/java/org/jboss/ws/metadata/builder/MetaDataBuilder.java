/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ws.metadata.builder;

import static org.jboss.ws.common.integration.WSHelper.isJseEndpoint;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.management.ObjectName;
import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Port;
import javax.wsdl.extensions.http.HTTPAddress;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.xml.namespace.QName;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.common.Constants;
import org.jboss.ws.common.ObjectNameFactory;
import org.jboss.ws.core.jaxrpc.UnqualifiedFaultException;
import org.jboss.ws.core.soap.Use;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.FaultMetaData;
import org.jboss.ws.metadata.umdm.OperationMetaData;
import org.jboss.ws.metadata.umdm.ServerEndpointMetaData;
import org.jboss.ws.metadata.umdm.TypeMappingMetaData;
import org.jboss.ws.metadata.umdm.TypesMetaData;
import org.jboss.ws.metadata.wsdl.WSDLBinding;
import org.jboss.ws.metadata.wsdl.WSDLBindingOperation;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.metadata.wsdl.WSDLEndpoint;
import org.jboss.ws.metadata.wsdl.WSDLInterface;
import org.jboss.ws.metadata.wsdl.WSDLInterfaceFault;
import org.jboss.ws.metadata.wsdl.WSDLInterfaceOperation;
import org.jboss.ws.metadata.wsdl.WSDLInterfaceOperationOutfault;
import org.jboss.ws.metadata.wsdl.WSDLService;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.HttpEndpoint;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.management.ServerConfigFactory;
import org.jboss.wsf.spi.metadata.j2ee.JSEArchiveMetaData;
import org.jboss.wsf.spi.metadata.j2ee.JSESecurityMetaData;
import org.jboss.wsf.spi.metadata.j2ee.JSESecurityMetaData.JSEResourceCollection;

/** An abstract meta data builder.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 19-May-2005
 */
public abstract class MetaDataBuilder
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(MetaDataBuilder.class);
   // provide logging
   private final static Logger log = Logger.getLogger(MetaDataBuilder.class);

   /** Inititialize the endpoint binding */
   protected void initEndpointBinding(WSDLEndpoint wsdlEndpoint, EndpointMetaData epMetaData)
   {
      WSDLDefinitions wsdlDefinitions = wsdlEndpoint.getWsdlService().getWsdlDefinitions();
      WSDLInterface wsdlInterface = wsdlEndpoint.getInterface();
      WSDLBinding wsdlBinding = wsdlDefinitions.getBindingByInterfaceName(wsdlInterface.getName());
      initEndpointBinding(wsdlBinding, epMetaData);
   }

   protected void initEndpointBinding(WSDLBinding wsdlBinding, EndpointMetaData epMetaData)
   {
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
            if (epMetaData.getPortName().equals(wsdlEndpoint.getName()))
            {
               QName bindQName = wsdlEndpoint.getBinding();
               WSDLBinding wsdlBinding = wsdlDefinitions.getBinding(bindQName);
               if (wsdlBinding == null)
                  throw new WSException(BundleUtils.getMessage(bundle, "CANNOT_OBTAIN_BINDING",  bindQName));

               for (WSDLBindingOperation wsdlBindingOperation : wsdlBinding.getOperations())
               {
                  String encStyle = wsdlBindingOperation.getEncodingStyle();
                  epMetaData.setEncodingStyle(Use.valueOf(encStyle));
               }
            }
         }
      }
   }

   protected void initEndpointAddress(Deployment dep, ServerEndpointMetaData sepMetaData)
   {
      String contextRoot = dep.getService().getContextRoot();
      String urlPattern = null;

      // Get the URL pattern from the endpoint
      String linkName = sepMetaData.getLinkName();
      if (linkName != null)
      {
         Endpoint endpoint = dep.getService().getEndpointByName(linkName);
         if (endpoint != null)
            urlPattern = ((HttpEndpoint)endpoint).getURLPattern();
      }

      // Endpoint API hack
      Integer port = (Integer)dep.getService().getProperty("port");
      if (port == null)
      {
         port = -1;
      }

      // If not, derive the context root from the deployment
      if (contextRoot == null)
      {
         String simpleName = dep.getSimpleName();
         contextRoot = simpleName.substring(0, simpleName.indexOf('.'));
         if (dep instanceof ArchiveDeployment)
         {
            if (((ArchiveDeployment)dep).getParent() != null)
            {
               simpleName = ((ArchiveDeployment)dep).getParent().getSimpleName();
               simpleName = simpleName.substring(0, simpleName.indexOf('.'));
               contextRoot = simpleName + "-" + contextRoot;
            }
         }
      }

      // Default to "/*" 
      if (urlPattern == null)
         urlPattern = "/*";

      if (contextRoot.startsWith("/") == false)
         contextRoot = "/" + contextRoot;
      if (urlPattern.startsWith("/") == false)
         urlPattern = "/" + urlPattern;

      sepMetaData.setContextRoot(contextRoot);
      sepMetaData.setURLPattern(urlPattern);

      String servicePath = contextRoot + urlPattern;
      sepMetaData.setEndpointAddress(getServiceEndpointAddress(null, servicePath, port, dep.getAttachment(ServerConfig.class)));
   }

   public static ObjectName createServiceEndpointID(Deployment dep, ServerEndpointMetaData sepMetaData)
   {
      String linkName = sepMetaData.getLinkName();
      String context = sepMetaData.getContextRoot();
      if (context.startsWith("/"))
         context = context.substring(1);

      StringBuilder idstr = new StringBuilder(ServerEndpointMetaData.SEPID_DOMAIN + ":");
      idstr.append(ServerEndpointMetaData.SEPID_PROPERTY_CONTEXT + "=" + context);
      idstr.append("," + ServerEndpointMetaData.SEPID_PROPERTY_ENDPOINT + "=" + linkName);

      return ObjectNameFactory.create(idstr.toString());
   }

   /** Get the web service address for a given path
    */
   public static String getServiceEndpointAddress(String uriScheme, String servicePath, int servicePort, ServerConfig config)
   {
      if (servicePath == null || servicePath.length() == 0)
         throw new WSException(BundleUtils.getMessage(bundle, "SERVICE_PATH_CANNOT_BE_NULL"));

      if (servicePath.endsWith("/*"))
         servicePath = servicePath.substring(0, servicePath.length() - 2);

      if (uriScheme == null)
         uriScheme = "http";

      if (config == null)
      {
         SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
         config = spiProvider.getSPI(ServerConfigFactory.class).getServerConfig();
      }

      String host = config.getWebServiceHost();

      int port = servicePort;
      if (servicePort == -1)
      {
         if ("https".equals(uriScheme))
         {
            port = config.getWebServiceSecurePort();
         }
         else
         {
            port = config.getWebServicePort();
         }
      }

      // Reset port if using the default for the scheme.
      if (("http".equals(uriScheme) && port == 80) || ("https".equals(uriScheme) && port == 443))
      {
         port = -1;
      }

      URL url = null;
      try
      {
         if (port > -1)
         {
            url = new URL(uriScheme, host, port, servicePath);
         }
         else
         {
            url = new URL(uriScheme, host, servicePath);
         }
         return url.toExternalForm();
      }
      catch (MalformedURLException e)
      {
         throw new WSException(BundleUtils.getMessage(bundle, "MALFORMED_URL_DETAIL", new Object[]{ uriScheme ,  host ,  port ,  servicePath }),  e);
      }
   }

   /**
    * Read the transport guarantee from web.xml
    */
   protected void initTransportGuaranteeJSE(Deployment dep, ServerEndpointMetaData sepMetaData, String servletLink) throws IOException
   {
      String transportGuarantee = null;
      JSEArchiveMetaData webMetaData = dep.getAttachment(JSEArchiveMetaData.class);
      if (webMetaData != null && isJseEndpoint(sepMetaData.getEndpoint()))
      {
         Map<String, String> servletMappings = webMetaData.getServletMappings();
         String urlPattern = servletMappings.get(servletLink);

         if (urlPattern == null)
            throw new WSException(BundleUtils.getMessage(bundle, "CANNOT_FIND_URL_PATTERN",  servletLink));

         List<JSESecurityMetaData> securityList = webMetaData.getSecurityMetaData();
         for (JSESecurityMetaData currentSecurity : securityList)
         {
            if (currentSecurity.getTransportGuarantee() != null && currentSecurity.getTransportGuarantee().length() > 0)
            {
               for (JSEResourceCollection currentCollection : currentSecurity.getWebResources())
               {
                  for (String currentUrlPattern : currentCollection.getUrlPatterns())
                  {
                     if (urlPattern.equals(currentUrlPattern) || "/*".equals(currentUrlPattern))
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
            QName wsdlPortName = wsdlEndpoint.getName();
            if (wsdlPortName.equals(portName))
            {
               endpointFound = true;

               String orgAddress = wsdlEndpoint.getAddress();
               String uriScheme = getUriScheme(orgAddress);

               String transportGuarantee = sepMetaData.getTransportGuarantee();
               if ("CONFIDENTIAL".equals(transportGuarantee))
                  uriScheme = "https";

               ServerConfig config = sepMetaData.getEndpoint().getService().getDeployment().getAttachment(ServerConfig.class);
               if (requiresRewrite(orgAddress, uriScheme, config))
               {
                  String servicePath = sepMetaData.getContextRoot() + sepMetaData.getURLPattern();
                  String serviceEndpointURL = getServiceEndpointAddress(uriScheme, servicePath, -1, config);

                  if (log.isDebugEnabled())
                     log.debug("Replace service endpoint address '" + orgAddress + "' with '" + serviceEndpointURL + "'");
                  wsdlEndpoint.setAddress(serviceEndpointURL);
                  sepMetaData.setEndpointAddress(serviceEndpointURL);

                  // modify the wsdl-1.1 definition
                  if (wsdlDefinitions.getWsdlOneOneDefinition() != null)
                     replaceWSDL11PortAddress(wsdlDefinitions, portName, serviceEndpointURL);
               }
               else
               {
                  if (log.isDebugEnabled())
                     log.debug("Don't replace service endpoint address '" + orgAddress + "'");
                  try
                  {
                     sepMetaData.setEndpointAddress(new URL(orgAddress).toExternalForm());
                  }
                  catch (MalformedURLException e)
                  {
                     log.warn(BundleUtils.getMessage(bundle, "MALFORMED_URL",  orgAddress));
                     sepMetaData.setEndpointAddress(orgAddress);
                  }
               }
            }
         }
      }

      if (endpointFound == false)
         throw new WSException(BundleUtils.getMessage(bundle, "CANNOT_FIND_PORT_IN_WSDL",  portName));
   }
   
   private static boolean requiresRewrite(String orgAddress, String uriScheme, ServerConfig config)
   {
      if (uriScheme != null)
      {
         if (!uriScheme.toLowerCase().startsWith("http"))
         {
            //perform rewrite on http/https addresses only
            return false;
         }
      }
      if (config == null)
      {
         SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
         config = spiProvider.getSPI(ServerConfigFactory.class).getServerConfig();
      }
      boolean alwaysModify = config.isModifySOAPAddress();
      
      return (alwaysModify || uriScheme == null || orgAddress.indexOf("REPLACE_WITH_ACTUAL_URL") >= 0);
   }

   private static void replaceWSDL11PortAddress(WSDLDefinitions wsdlDefinitions, QName portQName, String serviceEndpointURL)
   {
      Definition wsdlOneOneDefinition = wsdlDefinitions.getWsdlOneOneDefinition();
      String tnsURI = wsdlOneOneDefinition.getTargetNamespace();

      // search for matching portElement and replace the address URI
      if (modifyPortAddress(tnsURI, portQName, serviceEndpointURL, wsdlOneOneDefinition.getServices()))
      {
         return;
      }

      // recursively process imports if none can be found
      if (!wsdlOneOneDefinition.getImports().isEmpty())
      {

         Iterator imports = wsdlOneOneDefinition.getImports().values().iterator();
         while (imports.hasNext())
         {
            List l = (List)imports.next();
            Iterator importsByNS = l.iterator();
            while (importsByNS.hasNext())
            {
               Import anImport = (Import)importsByNS.next();
               if (modifyPortAddress(anImport.getNamespaceURI(), portQName, serviceEndpointURL, anImport.getDefinition().getServices()))
               {
                  return;
               }
            }
         }
      }
      
      throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "CANNOT_FIND_PORT",  portQName ));
   }

   /**
    * Try matching the port and modify it. Return true if the port is actually matched.
    * 
    * @param tnsURI
    * @param portQName
    * @param serviceEndpointURL
    * @param services
    * @return
    */
   private static boolean modifyPortAddress(String tnsURI, QName portQName, String serviceEndpointURL, Map services)
   {
      Iterator itServices = services.values().iterator();
      while (itServices.hasNext())
      {
         javax.wsdl.Service wsdlOneOneService = (javax.wsdl.Service)itServices.next();
         Map wsdlOneOnePorts = wsdlOneOneService.getPorts();
         Iterator itPorts = wsdlOneOnePorts.keySet().iterator();
         while (itPorts.hasNext())
         {
            String portLocalName = (String)itPorts.next();
            if (portQName.equals(new QName(tnsURI, portLocalName)))
            {
               Port wsdlOneOnePort = (Port)wsdlOneOnePorts.get(portLocalName);
               List extElements = wsdlOneOnePort.getExtensibilityElements();
               for (Object extElement : extElements)
               {
                  if (extElement instanceof SOAPAddress)
                  {
                     SOAPAddress address = (SOAPAddress)extElement;
                     address.setLocationURI(serviceEndpointURL);
                  }
                  else if (extElement instanceof SOAP12Address)
                  {
                     SOAP12Address address = (SOAP12Address)extElement;
                     address.setLocationURI(serviceEndpointURL);
                  }
                  else if (extElement instanceof HTTPAddress)
                  {
                     HTTPAddress address = (HTTPAddress)extElement;
                     address.setLocationURI(serviceEndpointURL);
                  }
               }
               return true;
            }
         }
      }
      return false;
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

   protected void buildFaultMetaData(OperationMetaData opMetaData, WSDLInterfaceOperation wsdlOperation)
   {
      TypesMetaData typesMetaData = opMetaData.getEndpointMetaData().getServiceMetaData().getTypesMetaData();

      WSDLInterface wsdlInterface = wsdlOperation.getWsdlInterface();
      for (WSDLInterfaceOperationOutfault outFault : wsdlOperation.getOutfaults())
      {
         QName ref = outFault.getRef();

         WSDLInterfaceFault wsdlFault = wsdlInterface.getFault(ref);
         QName xmlName = wsdlFault.getElement();
         QName xmlType = wsdlFault.getXmlType();
         String javaTypeName = null;

         if (xmlType == null)
         {
            log.warn(BundleUtils.getMessage(bundle, "CANNOT_OBTAIN_FAULT_TYPE",  xmlName));
            xmlType = xmlName;
         }

         TypeMappingMetaData tmMetaData = typesMetaData.getTypeMappingByXMLType(xmlType);
         if (tmMetaData != null)
            javaTypeName = tmMetaData.getJavaTypeName();

         if (javaTypeName == null)
         {
            log.warn(BundleUtils.getMessage(bundle, "CANNOT_OBTAIN_JAVA_TYPE_MAPPING",  xmlType));
            javaTypeName = new UnqualifiedFaultException(xmlType).getClass().getName();
         }

         FaultMetaData faultMetaData = new FaultMetaData(opMetaData, xmlName, xmlType, javaTypeName);
         opMetaData.addFault(faultMetaData);
      }
   }
}
