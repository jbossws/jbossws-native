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

import java.net.URL;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentUnit;
import org.jboss.ws.core.server.UnifiedDeploymentInfo;
import org.jboss.ws.metadata.umdm.UnifiedMetaData;

/**
 * An abstract deployer for EJB Endpoints
 *
 * @author Thomas.Diesler@jboss.org
 * @since 31-Oct-2006
 */
public abstract class AbstractEJBDeployer extends AbstractDeployer 
{
   @Override
   protected void createServiceEndpoint(UnifiedDeploymentInfo udi, DeploymentUnit unit) throws Exception
   {
      // Create the service endpoint
      super.createServiceEndpoint(udi, unit);
      
      // Generate the webapp and publish through th publisher
      try
      {
         UnifiedMetaData wsMetaData = getServiceEndpointDeployer().getUnifiedMetaData(udi);
         URL warURL = new ServiceEndpointGeneratorEJB3().generatWebDeployment(wsMetaData, unit);
         unit.addAttachment(ServiceEndpointWebApp.class, new ServiceEndpointWebApp(warURL));
         getServiceEndpointPublisher().publishServiceEndpoint(udi);
      }
      catch (Exception ex)
      {
         DeploymentException.rethrowAsDeploymentException(ex.getMessage(), ex);
      }
   }
   
   @Override
   protected void destroyServiceEndpoint(UnifiedDeploymentInfo udi, DeploymentUnit unit) 
   {
      // Destroy the webapp
      URL warURL = unit.getAttachment(ServiceEndpointWebApp.class).getWarURL();
      try
      {
         getServiceEndpointPublisher().destroyServiceEndpoint(udi);
      }
      catch (Exception ex)
      {
         log.error("Cannot destroy service endpoint: " + warURL, ex);
      }
      
      // Destroy the service endpoint
      super.destroyServiceEndpoint(udi, unit);
   }

   static class ServiceEndpointWebApp
   {
      private URL warURL;
      
      ServiceEndpointWebApp(URL warURL)
      {
         this.warURL = warURL;
      }

      public URL getWarURL()
      {
         return warURL;
      }
   }
}
