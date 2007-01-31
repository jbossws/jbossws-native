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
package org.jboss.ws.metadata.builder.jaxrpc;

//$Id$

import java.io.IOException;
import java.net.URL;
import java.util.Set;

import javax.xml.namespace.QName;

import org.jboss.logging.Logger;
import org.jboss.ws.Constants;
import org.jboss.ws.WSException;
import org.jboss.ws.core.UnifiedVirtualFile;
import org.jboss.ws.metadata.config.JBossWSConfigFactory;
import org.jboss.ws.metadata.config.jaxrpc.ClientConfigJAXRPC;
import org.jboss.ws.metadata.j2ee.UnifiedHandlerMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedServiceRefMetaData;
import org.jboss.ws.metadata.jaxrpcmapping.JavaWsdlMapping;
import org.jboss.ws.metadata.jaxrpcmapping.JavaWsdlMappingFactory;
import org.jboss.ws.metadata.jaxrpcmapping.ServiceEndpointInterfaceMapping;
import org.jboss.ws.metadata.umdm.ClientEndpointMetaData;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.ResourceLoaderAdapter;
import org.jboss.ws.metadata.umdm.ServiceMetaData;
import org.jboss.ws.metadata.umdm.UnifiedMetaData;
import org.jboss.ws.metadata.umdm.EndpointMetaData.Type;
import org.jboss.ws.metadata.umdm.HandlerMetaData.HandlerType;
import org.jboss.ws.metadata.wsdl.NCName;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.metadata.wsdl.WSDLEndpoint;
import org.jboss.ws.metadata.wsdl.WSDLService;
import org.jboss.ws.metadata.wsse.WSSecurityConfiguration;
import org.jboss.ws.metadata.wsse.WSSecurityOMFactory;

/**
 * A client side meta data builder.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 19-May-2005
 */
public class JAXRPCClientMetaDataBuilder extends JAXRPCMetaDataBuilder
{
   // provide logging
   private final Logger log = Logger.getLogger(JAXRPCClientMetaDataBuilder.class);

   /** Build from WSDL and jaxrpc-mapping.xml
    */
   public ServiceMetaData buildMetaData(QName serviceQName, URL wsdlURL, URL mappingURL, URL securityURL,
         UnifiedServiceRefMetaData serviceRefMetaData, ClassLoader loader)
   {
      try
      {
         JavaWsdlMapping javaWsdlMapping = null;
         if (mappingURL != null)
         {
            JavaWsdlMappingFactory mappingFactory = JavaWsdlMappingFactory.newInstance();
            javaWsdlMapping = mappingFactory.parse(mappingURL);
         }

         WSSecurityConfiguration securityConfig = null;
         if (securityURL != null)
         {
            securityConfig = WSSecurityOMFactory.newInstance().parse(securityURL);
         }

         return buildMetaData(serviceQName, wsdlURL, javaWsdlMapping, securityConfig, serviceRefMetaData, loader);
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         throw new WSException("Cannot build meta data: " + ex.getMessage(), ex);
      }
   }

   /** Build from WSDL and jaxrpc-mapping.xml
    */
   public ServiceMetaData buildMetaData(QName serviceQName, URL wsdlURL, JavaWsdlMapping javaWsdlMapping, WSSecurityConfiguration securityConfig,
         UnifiedServiceRefMetaData serviceRefMetaData, ClassLoader loader)
   {
      log.debug("START buildMetaData: [service=" + serviceQName + "]");
      try
      {
         ResourceLoaderAdapter vfsRoot = new ResourceLoaderAdapter(loader);

         UnifiedMetaData wsMetaData = new UnifiedMetaData(vfsRoot);
         wsMetaData.setClassLoader(loader);

         ServiceMetaData serviceMetaData = new ServiceMetaData(wsMetaData, serviceQName);
         wsMetaData.addService(serviceMetaData);

         serviceMetaData.setWsdlLocation(wsdlURL);
         WSDLDefinitions wsdlDefinitions = serviceMetaData.getWsdlDefinitions();

         if (javaWsdlMapping != null)
         {
            URL mappingURL = new URL(Constants.NS_JBOSSWS_URI + "/dummy-mapping-file");
            if (serviceRefMetaData != null && serviceRefMetaData.getMappingLocation() != null)
            {
               mappingURL = serviceRefMetaData.getMappingLocation();
            }
            wsMetaData.addMappingDefinition(mappingURL.toExternalForm(), javaWsdlMapping);
            serviceMetaData.setMappingLocation(mappingURL);
         }

         if (securityConfig != null)
         {
            serviceMetaData.setSecurityConfiguration(securityConfig);
            setupSecurity(securityConfig, wsMetaData.getRootFile());
         }

         buildMetaDataInternal(serviceMetaData, wsdlDefinitions, javaWsdlMapping, serviceRefMetaData);

         // eagerly initialize
         wsMetaData.eagerInitialize();

         log.debug("END buildMetaData: " + wsMetaData);
         return serviceMetaData;
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         throw new WSException("Cannot build meta data: " + ex.getMessage(), ex);
      }
   }

   private void buildMetaDataInternal(ServiceMetaData serviceMetaData, WSDLDefinitions wsdlDefinitions, JavaWsdlMapping javaWsdlMapping,
         UnifiedServiceRefMetaData serviceRefMetaData) throws IOException
   {
      QName serviceQName = serviceMetaData.getServiceName();

      // Get the WSDL service
      WSDLService wsdlService = null;
      if (serviceQName == null)
      {
         if (wsdlDefinitions.getServices().length != 1)
            throw new IllegalArgumentException("Expected a single service element");

         wsdlService = wsdlDefinitions.getServices()[0];
         serviceMetaData.setServiceName(wsdlService.getQName());
      }
      else
      {
         wsdlService = wsdlDefinitions.getService(new NCName(serviceQName.getLocalPart()));
      }
      if (wsdlService == null)
         throw new IllegalArgumentException("Cannot obtain wsdl service: " + serviceQName);

      // Build type mapping meta data
      setupTypesMetaData(serviceMetaData);

      // Build endpoint meta data
      for (WSDLEndpoint wsdlEndpoint : wsdlService.getEndpoints())
      {
         QName portName = wsdlEndpoint.getQName();
         QName interfaceQName = wsdlEndpoint.getInterface().getQName();
         ClientEndpointMetaData epMetaData = new ClientEndpointMetaData(serviceMetaData, portName, interfaceQName, Type.JAXRPC);
         epMetaData.setEndpointAddress(wsdlEndpoint.getAddress());
         serviceMetaData.addEndpoint(epMetaData);

         // config-name, config-file
         if (serviceRefMetaData != null)
         {
            String configName= serviceRefMetaData.getConfigName();
            String configFile = serviceRefMetaData.getConfigFile();
            if (configName != null || configFile != null)
               epMetaData.setConfigName(configName, configFile);
         }

         // Init the endpoint binding
         initEndpointBinding(wsdlEndpoint, epMetaData);

         // Init the service encoding style
         initEndpointEncodingStyle(epMetaData);

         ServiceEndpointInterfaceMapping seiMapping = null;
         if (javaWsdlMapping != null)
         {
            QName portType = wsdlEndpoint.getInterface().getQName();
            seiMapping = javaWsdlMapping.getServiceEndpointInterfaceMappingByPortType(portType);
            if (seiMapping != null)
            {
               epMetaData.setServiceEndpointInterfaceName(seiMapping.getServiceEndpointInterface());
            }
            else
            {
               log.warn("Cannot obtain the SEI mapping for: " + portType);
            }
         }

         processEndpointMetaDataExtensions(epMetaData, wsdlDefinitions);
         setupOperationsFromWSDL(epMetaData, wsdlEndpoint, seiMapping);
         setupHandlers(serviceRefMetaData, portName, epMetaData);
      }
   }

   private void setupHandlers(UnifiedServiceRefMetaData serviceRefMetaData, QName portName, EndpointMetaData epMetaData)
   {
      // Add pre handlers
      UnifiedVirtualFile vfsRoot = epMetaData.getRootFile();
      String configName = epMetaData.getConfigName();
      String configFile = epMetaData.getConfigFile();
      JBossWSConfigFactory factory = JBossWSConfigFactory.newInstance();
      ClientConfigJAXRPC jaxrpcConfig = (ClientConfigJAXRPC)factory.getConfig(vfsRoot, configName, configFile);
      epMetaData.addHandlers(jaxrpcConfig.getHandlers(epMetaData, HandlerType.PRE));

      // Setup the endpoint handlers
      if (serviceRefMetaData != null)
      {
         for (UnifiedHandlerMetaData uhmd : serviceRefMetaData.getHandlers())
         {
            Set<String> portNames = uhmd.getPortNames();
            if (portNames.size() == 0 || portNames.contains(portName.getLocalPart()))
            {
               epMetaData.addHandler(uhmd.getHandlerMetaDataJAXRPC(epMetaData, HandlerType.ENDPOINT));
            }
         }
      }

      // Add post handlers
      epMetaData.addHandlers(jaxrpcConfig.getHandlers(epMetaData, HandlerType.POST));
   }

   private void setupSecurity(WSSecurityConfiguration securityConfig, UnifiedVirtualFile vfsRoot)
   {
      if (securityConfig.getKeyStoreFile() != null)
      {
         try {
            UnifiedVirtualFile child = vfsRoot.findChild( securityConfig.getKeyStoreFile() );
            securityConfig.setKeyStoreURL(child.toURL());
         } catch (IOException e) {
            // ignore
         }
      }

      if (securityConfig.getTrustStoreFile() != null)
      {
         try {
            UnifiedVirtualFile child = vfsRoot.findChild( securityConfig.getTrustStoreFile() );
            securityConfig.setTrustStoreURL(child.toURL());
         } catch (IOException e) {
            // Ignore
         }
      }
   }
}
