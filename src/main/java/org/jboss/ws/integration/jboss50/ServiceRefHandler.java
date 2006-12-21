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
package org.jboss.ws.integration.jboss50;

// $Id$

import java.net.URL;
import java.util.Iterator;

import javax.management.MBeanServer;
import javax.naming.Context;
import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.xml.rpc.JAXRPCException;

import org.jboss.deployers.spi.deployer.DeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.naming.Util;
import org.jboss.webservice.metadata.serviceref.ServiceRefMetaData;
import org.jboss.ws.WSException;
import org.jboss.ws.core.jaxrpc.ServiceReferenceable;
import org.jboss.ws.metadata.builder.jaxrpc.JAXRPCClientDeployment;
import org.jboss.ws.metadata.j2ee.UnifiedServiceRefMetaData;
import org.jboss.ws.metadata.jaxrpcmapping.JavaWsdlMapping;
import org.jboss.ws.metadata.jaxrpcmapping.JavaWsdlMappingFactory;
import org.jboss.ws.core.server.UnifiedDeploymentInfo;
import org.jboss.ws.tools.wsdl.WSDL11DefinitionFactory;

/**
 * Binds a JAXRPC Service object in the client's ENC for every service-ref element in the
 * deployment descriptor.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 04-Nov-2006
 */
public class ServiceRefHandler implements ServiceRefHandlerMBean
{
   // logging support
   private static Logger log = Logger.getLogger(ServiceRefHandler.class);

   /**
    * This binds a jaxrpc Service into the callers ENC for every service-ref element
    *
    * @param envCtx      ENC to bind the javax.rpc.xml.Service object to
    * @param serviceRefs An iterator of the service-ref elements in the client deployment descriptor
    * @param deployment  The client deployment 
    * @throws org.jboss.deployment.DeploymentException if it goes wrong
    */
   public void setupServiceRefEnvironment(Context envCtx, Iterator serviceRefs, Object deployment)
   {
      String className = (deployment != null ? deployment.getClass().getName() : null);
      if ((deployment instanceof DeploymentUnit) == false)
         throw new IllegalArgumentException("Expected " + DeploymentUnit.class.getName() + ", but was: " + className);

      DeploymentUnit unit = (DeploymentUnit)deployment;
      try
      {
         while (serviceRefs.hasNext())
         {
            ServiceRefMetaData serviceRef = (ServiceRefMetaData)serviceRefs.next();
            String serviceRefName = serviceRef.getServiceRefName();

            // Build the container independent deployment info
            UnifiedDeploymentInfo udi = new JAXRPCClientDeployment(UnifiedDeploymentInfo.DeploymentType.JAXRPC_Client);
            DeploymentInfoAdaptor.buildDeploymentInfo(udi, unit);

            // Convert wsdlFile to URL
            String wsdlFile = serviceRef.getWsdlFile();
            if (wsdlFile != null)
               serviceRef.setWsdlFile(udi.getMetaDataFile(wsdlFile).toExternalForm());
               
            // Convert mappingFile to URL
            String mappingFile = serviceRef.getMappingFile();
            if (mappingFile != null)
               serviceRef.setMappingFile(udi.getMetaDataFile(mappingFile).toExternalForm());
               
            UnifiedServiceRefMetaData wsServiceRef = ServiceRefMetaDataAdaptor.buildUnifiedServiceRefMetaData(serviceRef);

            JavaWsdlMapping javaWsdlMapping = getJavaWsdlMapping(wsServiceRef);
            wsServiceRef.setJavaWsdlMapping(javaWsdlMapping);

            Definition wsdlDefinition = getWsdlDefinition(wsServiceRef);
            wsServiceRef.setWsdlDefinition(wsdlDefinition);

            ServiceReferenceable ref = new ServiceReferenceable(wsServiceRef, udi);
            Util.bind(envCtx, serviceRefName, ref);

            log.debug("<service-ref> bound to: java:comp/env/" + serviceRefName);
         }
      }
      catch (Exception ex)
      {
         throw new WSException("Cannot bind webservice to client environment", ex);
      }
   }

   private JavaWsdlMapping getJavaWsdlMapping(UnifiedServiceRefMetaData serviceRef)
   {
      JavaWsdlMapping javaWsdlMapping = null;
      URL mappingURL = serviceRef.getMappingLocation();
      if (mappingURL != null)
      {
         try
         {
            // setup the XML binding Unmarshaller
            JavaWsdlMappingFactory mappingFactory = JavaWsdlMappingFactory.newInstance();
            javaWsdlMapping = mappingFactory.parse(mappingURL);
         }
         catch (Exception e)
         {
            throw new JAXRPCException("Cannot unmarshal jaxrpc-mapping-file: " + mappingURL, e);
         }
      }
      return javaWsdlMapping;
   }

   private Definition getWsdlDefinition(UnifiedServiceRefMetaData serviceRef)
   {
      Definition wsdlDefinition = null;
      {
         URL wsdlOverride = serviceRef.getWsdlOverride();
         URL wsdlURL = serviceRef.getWsdlLocation();
         if (wsdlOverride == null && wsdlURL != null)
         {
            try
            {
               WSDL11DefinitionFactory factory = WSDL11DefinitionFactory.newInstance();
               wsdlDefinition = factory.parse(wsdlURL);
            }
            catch (WSDLException e)
            {
               throw new WSException("Cannot unmarshall wsdl, cause: " + e.toString());
            }
         }
      }
      return wsdlDefinition;
   }

   public void create() throws Exception
   {
      MBeanServer server = MBeanServerLocator.locateJBoss();
      if (server != null)
         server.registerMBean(this, OBJECT_NAME);
   }

   public void destroy() throws Exception
   {
      MBeanServer server = MBeanServerLocator.locateJBoss();
      if (server != null)
         server.unregisterMBean(OBJECT_NAME);
   }
}
