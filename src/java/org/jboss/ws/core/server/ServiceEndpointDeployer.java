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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.metadata.builder.jaxrpc.JAXRPCDeployment;
import org.jboss.ws.metadata.builder.jaxrpc.JAXRPCServerMetaDataBuilder;
import org.jboss.ws.metadata.builder.jaxws.JAXWSMetaDataBuilderEJB3;
import org.jboss.ws.metadata.builder.jaxws.JAXWSMetaDataBuilderJSE;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.ServerEndpointMetaData;
import org.jboss.ws.metadata.umdm.ServiceMetaData;
import org.jboss.ws.metadata.umdm.UnifiedMetaData;

/**
 * The POJO deployer for web service endpoints. This Deployer is already decoupled from the target
 * container (i.e. JBoss, Tomcat). The containers deployer architecture should be used to populate
 * the UnifiedDeploymentInfo object.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 12-May-2006
 */
public class ServiceEndpointDeployer
{
   // logging support
   private static Logger log = Logger.getLogger(ServiceEndpointDeployer.class);

   // default bean name
   public static final String BEAN_NAME = "ServiceEndpointDeployer";

   // The servlet init param in web.xml that is the service endpoint class
   public static final String INIT_PARAM_SERVICE_ENDPOINT_IMPL = "ServiceEndpointImpl";

   // The ServiceEndpointManger injected by the kernel
   private ServiceEndpointManager epManager;

   // Maps the deployment url to UMDM
   private Map<String, UnifiedMetaData> metaDataMap = new ConcurrentHashMap<String, UnifiedMetaData>();

   // Injected by the Microkernel
   public void setServiceEndpointManager(ServiceEndpointManager epManager)
   {
      this.epManager = epManager;
   }

   public void create(UnifiedDeploymentInfo udi)
   {
      if(log.isDebugEnabled()) log.debug("create: " + udi.name);
      try
      {
         UnifiedMetaData wsMetaData;
         if (udi.type == UnifiedDeploymentInfo.DeploymentType.JAXRPC_JSE)
         {
            JAXRPCServerMetaDataBuilder builder = new JAXRPCServerMetaDataBuilder();
            wsMetaData = builder.buildMetaData((JAXRPCDeployment)udi);
         }
         else if (udi.type == UnifiedDeploymentInfo.DeploymentType.JAXRPC_EJB21)
         {
            JAXRPCServerMetaDataBuilder builder = new JAXRPCServerMetaDataBuilder();
            wsMetaData = builder.buildMetaData((JAXRPCDeployment)udi);
         }
         else if (udi.type == UnifiedDeploymentInfo.DeploymentType.JAXWS_JSE)
         {
            JAXWSMetaDataBuilderJSE builder = new JAXWSMetaDataBuilderJSE();
            wsMetaData = builder.buildMetaData(udi);
         }
         else if (udi.type == UnifiedDeploymentInfo.DeploymentType.JAXWS_EJB3)
         {
            JAXWSMetaDataBuilderEJB3 builder = new JAXWSMetaDataBuilderEJB3();
            wsMetaData = builder.buildMetaData(udi);
         }
         else
         {
            throw new IllegalStateException("Invalid type:  " + udi.type);
         }

         metaDataMap.put(udi.name, wsMetaData);

         for (ServiceMetaData serviceMetaData : wsMetaData.getServices())
         {
            for (EndpointMetaData epMetaData : serviceMetaData.getEndpoints())
            {
               ServiceEndpointInfo seInfo = new ServiceEndpointInfo(udi, (ServerEndpointMetaData)epMetaData);
               epManager.createServiceEndpoint(seInfo);
            }
         }
      }
      catch (Exception ex)
      {
         log.error("Cannot create service endpoint", ex);
         if (ex instanceof RuntimeException)
            throw (RuntimeException)ex;

         throw new WSException(ex);
      }
   }

   public void start(UnifiedDeploymentInfo udi)
   {
      if(log.isDebugEnabled()) log.debug("start: " + udi.name);
      try
      {
         UnifiedMetaData wsMetaData = getUnifiedMetaData(udi);
         if (wsMetaData != null)
         {
            // late initialization of the web context loader
            if (wsMetaData.getClassLoader() != udi.classLoader)
               wsMetaData.setClassLoader(udi.classLoader);

            // Publish the WSDL file
            WSDLFilePublisher wsdlfp = new WSDLFilePublisher(udi);
            wsdlfp.publishWsdlFiles(wsMetaData);
            for (ServiceMetaData serviceMetaData : wsMetaData.getServices())
            {
               for (EndpointMetaData epMetaData : serviceMetaData.getEndpoints())
               {
                  ObjectName sepID = ((ServerEndpointMetaData)epMetaData).getServiceEndpointID();
                  epManager.startServiceEndpoint(sepID);
               }
            }
         }
      }
      catch (Exception ex)
      {
         log.error("Cannot start service endpoint", ex);
         if (ex instanceof RuntimeException)
            throw (RuntimeException)ex;

         throw new WSException(ex);
      }
   }

   public void stop(UnifiedDeploymentInfo udi)
   {
      if(log.isDebugEnabled()) log.debug("stop: " + udi.name);
      try
      {
         UnifiedMetaData wsMetaData = getUnifiedMetaData(udi);
         if (wsMetaData != null)
         {
            // Stop the service endpoints
            for (ServiceMetaData serviceMetaData : wsMetaData.getServices())
            {
               for (EndpointMetaData epMetaData : serviceMetaData.getEndpoints())
               {
                  ObjectName sepID = ((ServerEndpointMetaData)epMetaData).getServiceEndpointID();
                  epManager.stopServiceEndpoint(sepID);
               }
            }

            // Unpublish the WSDL file
            WSDLFilePublisher wsdlfp = new WSDLFilePublisher(udi);
            wsdlfp.unpublishWsdlFiles();
         }
      }
      catch (Exception ex)
      {
         log.error("Cannot stop service endpoint", ex);
         if (ex instanceof RuntimeException)
            throw (RuntimeException)ex;

         throw new WSException(ex);
      }
   }

   public void destroy(UnifiedDeploymentInfo udi)
   {
      if(log.isDebugEnabled()) log.debug("destroy: " + udi.name);
      try
      {
         UnifiedMetaData wsMetaData = getUnifiedMetaData(udi);
         if (wsMetaData != null)
         {
            // Destroy the service endpoints
            for (ServiceMetaData serviceMetaData : wsMetaData.getServices())
            {
               for (EndpointMetaData epMetaData : serviceMetaData.getEndpoints())
               {
                  ObjectName sepID = ((ServerEndpointMetaData)epMetaData).getServiceEndpointID();
                  epManager.destroyServiceEndpoint(sepID);
               }
            }
         }
      }
      catch (Exception ex)
      {
         log.error("Cannot destroy service endpoint", ex);
         if (ex instanceof RuntimeException)
            throw (RuntimeException)ex;

         throw new WSException(ex);
      }
   }

   public UnifiedMetaData getUnifiedMetaData(UnifiedDeploymentInfo udi)
   {
      UnifiedMetaData wsMetaData = metaDataMap.get(udi.name);
      return wsMetaData;
   }
}