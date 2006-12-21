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

import java.io.IOException;
import java.net.URL;

import org.jboss.deployment.DeploymentInfo;
import org.jboss.mx.server.Invocation;
import org.jboss.ws.core.server.UnifiedDeploymentInfo;
import org.jboss.ws.metadata.umdm.UnifiedMetaData;

/**
 * A deployer service that manages WS4EE compliant Web-Services for EJB Endpoints
 *
 * @author Thomas.Diesler@jboss.org
 * @since 15-Jan-2005
 */
public abstract class DeployerInterceptorEJB extends DeployerInterceptor
{
   protected Object create(Invocation invocation, DeploymentInfo di) throws Throwable
   {
      Object retObj = super.create(invocation, di);

      UnifiedDeploymentInfo udi = getServiceEndpointDeployment(di);
      if (udi != null)
      {
         UnifiedMetaData wsMetaData = getServiceEndpointDeployer().getUnifiedMetaData(udi);
         udi.localUrl = generateWebDeployment(di, wsMetaData);
         udi.context.put(DeploymentInfo.class.getName(), di);
         getServiceEndpointPublisher().publishServiceEndpoint(udi);
      }

      return retObj;
   }

   protected abstract URL generateWebDeployment(DeploymentInfo di, UnifiedMetaData wsMetaData) throws IOException;

   protected Object destroy(Invocation invocation, DeploymentInfo di) throws Throwable
   {
      UnifiedDeploymentInfo udi = getServiceEndpointDeployment(di);
      if (udi != null)
      {
         getServiceEndpointPublisher().destroyServiceEndpoint(udi);
      }

      return super.destroy(invocation, di);
   }
}
