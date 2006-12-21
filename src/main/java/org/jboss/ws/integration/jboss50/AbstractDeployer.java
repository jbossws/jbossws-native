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

//$Id$

import org.jboss.deployers.plugins.deployer.AbstractSimpleDeployer;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentUnit;
import org.jboss.kernel.spi.registry.KernelRegistry;
import org.jboss.kernel.spi.registry.KernelRegistryEntry;
import org.jboss.ws.core.server.ServiceEndpointPublisher;
import org.jboss.ws.core.server.KernelLocator;
import org.jboss.ws.core.server.ServiceEndpointDeployer;
import org.jboss.ws.core.server.UnifiedDeploymentInfo;
import org.jboss.ws.core.server.UnifiedDeploymentInfo.DeploymentType;

/**
 * An abstract web service deployer.
 * 
 *    deploy(unit) 
 *      if(isWebServiceDeployment)
 *        deployServiceEndoint  
 *          udi = createUnifiedDeploymentInfo()
 *          ServiceEndpointDeployer.create(udi)
 *
 *    undeploy(unit) 
 *      undeployServiceEndoint  
 *        ServiceEndpointDeployer.destroy(udi)
 *
 * @author Thomas.Diesler@jboss.org
 * @since 31-Oct-2006
 */
public abstract class AbstractDeployer extends AbstractSimpleDeployer
{
   /** Depending on the type of deployment, this method should return true
    *  if the deployment contains web service endpoints.
    */
   public abstract boolean isWebServiceDeployment(DeploymentUnit unit);

   /** Deploy the web service endpoints if there are any  
    */
   @Override
   public void deploy(DeploymentUnit unit) throws DeploymentException
   {
      boolean isComponent = unit.getDeploymentContext().isComponent();
      if (isComponent == false && isWebServiceDeployment(unit))
         deployInternal(unit);
   }

   /** 
    * Called when the deployment contains web service endpoints.
    * Is private and handles recovery of failed deployments 
    */
   private final void deployInternal(DeploymentUnit unit) throws DeploymentException
   {
      try
      {
         deployServiceEndpoint(unit);
      }
      catch (Exception ex)
      {
         UnifiedDeploymentInfo udi = getUnifiedDeploymentInfo(unit);
         if (udi != null)
            undeployInternal(unit, udi);
         
         DeploymentException.rethrowAsDeploymentException(ex.getMessage(), ex);
      }
   }

   /** Undeploy the web service endpoints if there are any  
    */
   @Override
   public void undeploy(DeploymentUnit unit)
   {
      boolean isComponent = unit.getDeploymentContext().isComponent();
      UnifiedDeploymentInfo udi = getUnifiedDeploymentInfo(unit);
      if (isComponent == false && udi != null)
         undeployInternal(unit, udi);
   }

   private void undeployInternal(DeploymentUnit unit, UnifiedDeploymentInfo udi)
   {
      undeployServiceEndpoint(unit, udi);
   }

   /** Create the unified deployment info from the deployment unit
    */
   protected abstract UnifiedDeploymentInfo createUnifiedDeploymentInfo(DeploymentUnit unit) throws Exception;

   /** Create the unified deployment info and create the service endpoints 
    *  through the ServiceEndpointDeployer 
    */
   protected void deployServiceEndpoint(DeploymentUnit unit) throws Exception
   {
      UnifiedDeploymentInfo udi = createUnifiedDeploymentInfo(unit);
      unit.addAttachment(UnifiedDeploymentInfo.class, udi);
      createServiceEndpoint(udi, unit);
   }

   /** Stop and destroy the service endpoints through the ServiceEndpointDeployer 
    */
   protected void undeployServiceEndpoint(DeploymentUnit unit, UnifiedDeploymentInfo udi)
   {
      destroyServiceEndpoint(udi, unit);
   }

   protected void createServiceEndpoint(UnifiedDeploymentInfo udi, DeploymentUnit unit) throws Exception
   {
      log.debug("Create ServiceEndpoint: " + udi.getCanonicalName());
      getServiceEndpointDeployer().create(udi);
   }

   protected void destroyServiceEndpoint(UnifiedDeploymentInfo udi, DeploymentUnit unit)
   {
      log.debug("Destroy ServiceEndpoint: " + udi.getCanonicalName());
      getServiceEndpointDeployer().destroy(udi);
   }

   /** Override to provide the deployment type
    */
   protected abstract DeploymentType getDeploymentType();

   protected UnifiedDeploymentInfo getUnifiedDeploymentInfo(DeploymentUnit unit)
   {
      UnifiedDeploymentInfo udi = unit.getAttachment(UnifiedDeploymentInfo.class);
      return (udi != null && udi.type == getDeploymentType() ? udi : null);
   }

   protected ServiceEndpointDeployer getServiceEndpointDeployer()
   {
      KernelRegistry registry = KernelLocator.getKernel().getRegistry();
      KernelRegistryEntry entry = registry.getEntry(ServiceEndpointDeployer.BEAN_NAME);
      return (ServiceEndpointDeployer)entry.getTarget();
   }

   protected JBoss50ServiceEndpointPublisher getServiceEndpointPublisher()
   {
      KernelRegistry registry = KernelLocator.getKernel().getRegistry();
      KernelRegistryEntry entry = registry.getEntry(ServiceEndpointPublisher.BEAN_NAME);
      return (JBoss50ServiceEndpointPublisher)entry.getTarget();
   }
}
