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

import java.util.Set;

import org.jboss.deployers.plugins.deployer.AbstractSimpleDeployer;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentUnit;
import org.jboss.kernel.spi.registry.KernelRegistry;
import org.jboss.kernel.spi.registry.KernelRegistryEntry;
import org.jboss.metadata.WebMetaData;
import org.jboss.ws.core.server.KernelLocator;
import org.jboss.ws.core.server.ServiceEndpointDeployer;
import org.jboss.ws.core.server.UnifiedDeploymentInfo;

/**
 * A deployer that starts and stops web service deployments
 *
 * @author Thomas.Diesler@jboss.org
 * @since 31-Oct-2006
 */
public class ServiceEndpointLifecycleDeployer extends AbstractSimpleDeployer
{
   /** Start the service endpoint  
    */
   @Override
   public void deploy(DeploymentUnit unit) throws DeploymentException
   {
      UnifiedDeploymentInfo udi = unit.getAttachment(UnifiedDeploymentInfo.class);
      if (udi != null && udi.name.equals(unit.getName()))
      {
         // Get the webapp context classloader and use it as the deploymet class loader
         Set<? extends WebMetaData> allMetaData = unit.getAllMetaData(WebMetaData.class);
         if (allMetaData.size() > 0)
         {
            WebMetaData webMetaData = allMetaData.iterator().next();
            udi.classLoader = webMetaData.getContextLoader();
         }

         log.debug("Start ServiceEndpoint: " + udi.getCanonicalName());
         getServiceEndpointDeployer().start(udi);
      }
   }

   /** Stop the service endpoint  
    */
   @Override
   public void undeploy(DeploymentUnit unit)
   {
      UnifiedDeploymentInfo udi = unit.getAttachment(UnifiedDeploymentInfo.class);
      if (udi != null && udi.name.equals(unit.getName()))
      {
         log.debug("Stop ServiceEndpoint: " + udi.getCanonicalName());
         getServiceEndpointDeployer().stop(udi);
      }
   }

   protected ServiceEndpointDeployer getServiceEndpointDeployer()
   {
      KernelRegistry registry = KernelLocator.getKernel().getRegistry();
      KernelRegistryEntry entry = registry.getEntry(ServiceEndpointDeployer.BEAN_NAME);
      return (ServiceEndpointDeployer)entry.getTarget();
   }
}
