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
package org.jboss.ws.integration.jboss42;

//$Id$

import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.MainDeployerMBean;
import org.jboss.deployment.SubDeployerInterceptorSupport;
import org.jboss.kernel.spi.registry.KernelRegistry;
import org.jboss.kernel.spi.registry.KernelRegistryEntry;
import org.jboss.metadata.WebMetaData;
import org.jboss.mx.server.Invocation;
import org.jboss.mx.util.MBeanProxy;
import org.jboss.ws.core.server.KernelLocator;
import org.jboss.ws.core.server.ServiceEndpointDeployer;
import org.jboss.ws.core.server.ServiceEndpointPublisher;
import org.jboss.ws.core.server.UnifiedDeploymentInfo;

/**
 * A deployer service that manages WS4EE compliant Web Services
 *
 * This service is called from the {@see org.jboss.ws.metadata.WebServiceInterceptor}
 *
 * @author Thomas.Diesler@jboss.org
 * @author Dimitris.Andreadis@jboss.org
 * @since 15-Jan-2005
 */
public abstract class DeployerInterceptor extends SubDeployerInterceptorSupport
{
   // The main deployer
   private MainDeployerMBean mainDeployer;

   /** Create the deployer service
    */
   protected void createService() throws Exception
   {
      mainDeployer = (MainDeployerMBean)MBeanProxy.get(MainDeployerMBean.class, MainDeployerMBean.OBJECT_NAME, server);
      super.attach();
   }

   /** Destroy the deployer service
    */
   protected void destroyService()
   {
      super.detach();
   }

   /** Overwrite to create the webservice
    */
   protected Object create(Invocation invocation, DeploymentInfo di) throws Throwable
   {
      log.debug("create: " + di.url);

      Object retn = invokeNext(invocation);

      if (isWebserviceDeployment(di))
      {
         UnifiedDeploymentInfo udi = createUnifiedDeploymentInfo(di);
         di.context.put(UnifiedDeploymentInfo.class.getName(), udi);
         getServiceEndpointDeployer().create(udi);
      }

      return retn;
   }

   /** Overwrite to start the webservice
    */
   protected Object start(Invocation invocation, DeploymentInfo di) throws Throwable
   {
      log.debug("start: " + di.url);

      Object retn = invokeNext(invocation);

      UnifiedDeploymentInfo udi = getServiceEndpointDeployment(di);
      if (udi != null)
      {
         // late initialization of the web context loader
         if (di.metaData instanceof WebMetaData)
         {
            ClassLoader classLoader = ((WebMetaData)di.metaData).getContextLoader();
            udi.classLoader = classLoader;
         }

         getServiceEndpointDeployer().start(udi);
      }

      return retn;
   }

   /** Overwrite to stop the webservice
    */
   protected Object stop(Invocation invocation, DeploymentInfo di) throws Throwable
   {
      log.debug("stop: " + di.url);

      UnifiedDeploymentInfo udi = getServiceEndpointDeployment(di);
      if (udi != null)
      {
         getServiceEndpointDeployer().stop(udi);
      }

      return invokeNext(invocation);
   }

   /** Overwrite to destroy the webservice
    */
   protected Object destroy(Invocation invocation, DeploymentInfo di) throws Throwable
   {
      log.debug("destroy: " + di.url);

      UnifiedDeploymentInfo udi = getServiceEndpointDeployment(di);
      if (udi != null)
      {
         getServiceEndpointDeployer().destroy(udi);
      }

      return invokeNext(invocation);
   }

   protected ServiceEndpointDeployer getServiceEndpointDeployer()
   {
      KernelRegistry registry = KernelLocator.getKernel().getRegistry();
      KernelRegistryEntry entry = registry.getEntry(ServiceEndpointDeployer.BEAN_NAME);
      return (ServiceEndpointDeployer)entry.getTarget();
   }

   protected ServiceEndpointPublisher getServiceEndpointPublisher()
   {
      KernelRegistry registry = KernelLocator.getKernel().getRegistry();
      KernelRegistryEntry entry = registry.getEntry(ServiceEndpointPublisher.BEAN_NAME);
      return (ServiceEndpointPublisher)entry.getTarget();
   }
   
   /** Return true if the deployment contains a web service endpoint
    */
   protected abstract boolean isWebserviceDeployment(DeploymentInfo di);

   protected abstract UnifiedDeploymentInfo createUnifiedDeploymentInfo(DeploymentInfo di) throws Throwable;

   protected UnifiedDeploymentInfo getServiceEndpointDeployment(DeploymentInfo di)
   {
      return (UnifiedDeploymentInfo)di.context.get(UnifiedDeploymentInfo.class.getName());
   }

   /** Handle all webservice deployment exceptions.
    * You can either simply logs the problem and keep the EJB/WAR module
    * alive or undeploy properly.
    */
   protected void handleStartupException(DeploymentInfo di, Throwable th)
   {
      log.error("Cannot startup webservice for: " + di.shortName, th);
      mainDeployer.undeploy(di);
   }

   /** Handle all webservice deployment exceptions.
    *
    * You can either simply logs the problem and keep the EJB/WAR module
    * alive or undeploy properly.
    */
   protected void handleShutdownException(String moduleName, Throwable th)
   {
      log.error("Cannot shutdown webservice for: " + moduleName, th);
   }
}
