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
// $Id$
package org.jboss.ws.metadata.builder.jaxrpc;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.core.UnifiedVirtualFile;
import org.jboss.ws.metadata.config.JBossWSConfigFactory;
import org.jboss.ws.metadata.config.jaxrpc.EndpointConfigJAXRPC;
import org.jboss.ws.metadata.j2ee.*;
import org.jboss.ws.metadata.jaxrpcmapping.JavaWsdlMapping;
import org.jboss.ws.metadata.jaxrpcmapping.ServiceEndpointInterfaceMapping;
import org.jboss.ws.metadata.umdm.EndpointMetaData.Type;
import org.jboss.ws.metadata.umdm.HandlerMetaData.HandlerType;
import org.jboss.ws.metadata.umdm.ServerEndpointMetaData;
import org.jboss.ws.metadata.umdm.ServiceMetaData;
import org.jboss.ws.metadata.umdm.UnifiedMetaData;
import org.jboss.ws.metadata.webservices.PortComponentMetaData;
import org.jboss.ws.metadata.webservices.WebserviceDescriptionMetaData;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.metadata.wsdl.WSDLEndpoint;
import org.jboss.ws.metadata.wsdl.WSDLService;
import org.jboss.ws.metadata.wsse.WSSecurityConfigFactory;
import org.jboss.ws.metadata.wsse.WSSecurityConfiguration;
import org.jboss.ws.metadata.wsse.WSSecurityOMFactory;

import javax.management.ObjectName;
import javax.xml.namespace.QName;
import java.net.URL;
import java.util.Set;

/**
 * A server side meta data builder that is based on webservices.xml.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 19-May-2005
 */
public class JAXRPCServerMetaDataBuilder extends JAXRPCMetaDataBuilder
{
   // provide logging
   final Logger log = Logger.getLogger(JAXRPCServerMetaDataBuilder.class);

   /**
    * Build from webservices.xml
    */
   public UnifiedMetaData buildMetaData(JAXRPCDeployment udi)
   {
      log.debug("START buildMetaData: [name=" + udi.getCanonicalName() + "]");
      try
      {
         // For every webservice-description build the ServiceMetaData         
         UnifiedMetaData wsMetaData = new UnifiedMetaData(udi.vfRoot);
         wsMetaData.setDeploymentName(udi.getCanonicalName());
         wsMetaData.setClassLoader(udi.classLoader);

         WebserviceDescriptionMetaData[] wsDescriptionArr = udi.getWebservicesMetaData().getWebserviceDescriptions();
         for (WebserviceDescriptionMetaData wsdMetaData : wsDescriptionArr)
         {
            ServiceMetaData serviceMetaData = new ServiceMetaData(wsMetaData, null);
            serviceMetaData.setWebserviceDescriptionName(wsdMetaData.getWebserviceDescriptionName());
            wsMetaData.addService(serviceMetaData);

            // Set wsdl location
            String wsdlFile = wsdMetaData.getWsdlFile();
            URL wsdlLocation = udi.getMetaDataFileURL(wsdlFile);

            // Unmarshall the WSDL
            serviceMetaData.setWsdlLocation(wsdlLocation);
            WSDLDefinitions wsdlDefinitions = serviceMetaData.getWsdlDefinitions();

            // Unmarshall the jaxrpc-mapping.xml
            String mappingFile = wsdMetaData.getJaxrpcMappingFile();
            serviceMetaData.setMappingLocation(udi.getMetaDataFileURL(mappingFile));
            JavaWsdlMapping javaWsdlMapping = serviceMetaData.getJavaWsdlMapping();
            if (javaWsdlMapping == null)
               throw new WSException("jaxrpc-mapping-file not configured from webservices.xml");

            // Build type mapping meta data
            setupTypesMetaData(serviceMetaData);

            // Assign the WS-Security configuration,
            WSSecurityConfigFactory wsseConfFactory = WSSecurityConfigFactory.newInstance();
            WSSecurityConfiguration securityConfiguration = wsseConfFactory.createConfiguration(
               wsMetaData.getRootFile(), WSSecurityOMFactory.SERVER_RESOURCE_NAME
            );
            serviceMetaData.setSecurityConfiguration(securityConfiguration);

            // For every port-component build the EndpointMetaData
            PortComponentMetaData[] pcMetaDataArr = wsdMetaData.getPortComponents();
            for (PortComponentMetaData pcMetaData : pcMetaDataArr)
            {
               QName portName = pcMetaData.getWsdlPort();

               // JBWS-722
               // <wsdl-port> in webservices.xml should be qualified
               if (portName.getNamespaceURI().length() == 0)
               {
                  String nsURI = wsdlDefinitions.getTargetNamespace();
                  portName = new QName(nsURI, portName.getLocalPart());
                  log.warn("Adding wsdl targetNamespace to: " + portName);
                  pcMetaData.setWsdlPort(portName);
               }

               WSDLEndpoint wsdlEndpoint = getWsdlEndpoint(wsdlDefinitions, portName);
               if (wsdlEndpoint == null)
                  throw new WSException("Cannot find port in wsdl: " + portName);

               // set service name
               serviceMetaData.setServiceName(wsdlEndpoint.getWsdlService().getQName());
               QName interfaceQName = wsdlEndpoint.getInterface().getQName();

               ServerEndpointMetaData sepMetaData = new ServerEndpointMetaData(serviceMetaData, portName, interfaceQName, Type.JAXRPC);
               sepMetaData.setPortComponentName(pcMetaData.getPortComponentName());
               String linkName = pcMetaData.getEjbLink() != null ? pcMetaData.getEjbLink() : pcMetaData.getServletLink();
               sepMetaData.setLinkName(linkName);
               serviceMetaData.addEndpoint(sepMetaData);

               initEndpointEncodingStyle(sepMetaData);

               initEndpointAddress(udi, sepMetaData, linkName);

               if (udi.metaData instanceof UnifiedApplicationMetaData)
               {
                  UnifiedApplicationMetaData apMetaData = (UnifiedApplicationMetaData)udi.metaData;
                  wsMetaData.setSecurityDomain(apMetaData.getSecurityDomain());

                  // Copy the wsdl publish location from jboss.xml
                  String wsdName = serviceMetaData.getWebserviceDescriptionName();
                  String wsdlPublishLocation = apMetaData.getWsdlPublishLocationByName(wsdName);
                  serviceMetaData.setWsdlPublishLocation(wsdlPublishLocation);

                  // Copy <port-component> meta data
                  UnifiedBeanMetaData beanMetaData = apMetaData.getBeanByEjbName(linkName);
                  if (beanMetaData == null)
                     throw new WSException("Cannot obtain UnifiedBeanMetaData for: " + linkName);

                  String configName = apMetaData.getConfigName();
                  String configFile = apMetaData.getConfigFile();
                  if (configName != null || configFile != null)
                     sepMetaData.setConfigName(configName, configFile);

                  UnifiedEjbPortComponentMetaData bpcMetaData = beanMetaData.getPortComponent();
                  if (bpcMetaData != null)
                  {
                     if (bpcMetaData.getAuthMethod() != null)
                     {
                        String authMethod = bpcMetaData.getAuthMethod();
                        sepMetaData.setAuthMethod(authMethod);
                     }
                     if (bpcMetaData.getTransportGuarantee() != null)
                     {
                        String transportGuarantee = bpcMetaData.getTransportGuarantee();
                        sepMetaData.setTransportGuarantee(transportGuarantee);
                     }

                     sepMetaData.setURLPattern(bpcMetaData.getURLPattern());
                  }
               }
               else if (udi.metaData instanceof UnifiedWebMetaData)
               {
                  UnifiedWebMetaData webMetaData = (UnifiedWebMetaData)udi.metaData;
                  wsMetaData.setSecurityDomain(webMetaData.getSecurityDomain());

                  String targetBean = webMetaData.getServletClassNames().get(linkName);
                  sepMetaData.setServiceEndpointImplName(targetBean);

                  // Copy the wsdl publish location from jboss-web.xml
                  String wsdName = serviceMetaData.getWebserviceDescriptionName();
                  String wsdlPublishLocation = webMetaData.getWsdlPublishLocationByName(wsdName);
                  serviceMetaData.setWsdlPublishLocation(wsdlPublishLocation);

                  String configName = webMetaData.getConfigName();
                  String configFile = webMetaData.getConfigFile();
                  if (configName != null || configFile != null)
                     sepMetaData.setConfigName(configName, configFile);

                  initTransportGuaranteeJSE(udi, sepMetaData, linkName);
               }

               // init service endpoint id
               ObjectName sepID = createServiceEndpointID(udi, sepMetaData);
               sepMetaData.setServiceEndpointID(sepID);

               replaceAddressLocation(sepMetaData);

               String seiName = pcMetaData.getServiceEndpointInterface();
               sepMetaData.setServiceEndpointInterfaceName(seiName);

               ServiceEndpointInterfaceMapping seiMapping = javaWsdlMapping.getServiceEndpointInterfaceMapping(seiName);
               if (seiMapping == null)
                  log.warn("Cannot obtain SEI mapping for: " + seiName);

               // process endpoint meta extension
               processEndpointMetaDataExtensions(sepMetaData, wsdlDefinitions);

               // Setup the endpoint operations
               setupOperationsFromWSDL(sepMetaData, wsdlEndpoint, seiMapping);

               // Add pre handlers
               UnifiedVirtualFile vfsRoot = sepMetaData.getRootFile();
               String configName = sepMetaData.getConfigName();
               String configFile = sepMetaData.getConfigFile();
               JBossWSConfigFactory factory = JBossWSConfigFactory.newInstance();
               EndpointConfigJAXRPC jaxrpcConfig = (EndpointConfigJAXRPC)factory.getConfig(vfsRoot, configName, configFile);
               sepMetaData.addHandlers(jaxrpcConfig.getHandlers(sepMetaData, HandlerType.PRE));

               // Setup the endpoint handlers
               for (UnifiedHandlerMetaData uhmd : pcMetaData.getHandlers())
               {
                  Set<String> portNames = uhmd.getPortNames();
                  if (portNames.size() == 0 || portNames.contains(portName.getLocalPart()))
                  {
                     sepMetaData.addHandler(uhmd.getHandlerMetaDataJAXRPC(sepMetaData, HandlerType.ENDPOINT));
                  }
               }

               // Add post handlers
               sepMetaData.addHandlers(jaxrpcConfig.getHandlers(sepMetaData, HandlerType.POST));
            }
         }

         log.debug("END buildMetaData: " + wsMetaData);
         return wsMetaData;
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

   private WSDLEndpoint getWsdlEndpoint(WSDLDefinitions wsdlDefinitions, QName portName)
   {
      WSDLEndpoint wsdlEndpoint = null;
      for (WSDLService wsdlService : wsdlDefinitions.getServices())
      {
         WSDLEndpoint auxEndpoint = wsdlService.getEndpoint(portName);
         if (auxEndpoint != null)
         {
            wsdlEndpoint = auxEndpoint;
            break;
         }
      }
      return wsdlEndpoint;
   }
}
