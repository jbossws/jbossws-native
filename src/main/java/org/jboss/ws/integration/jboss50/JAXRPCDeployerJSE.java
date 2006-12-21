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
import java.util.Set;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentUnit;
import org.jboss.metadata.WebMetaData;
import org.jboss.virtual.VirtualFile;
import org.jboss.ws.core.server.UnifiedDeploymentInfo;
import org.jboss.ws.core.server.UnifiedDeploymentInfo.DeploymentType;
import org.jboss.ws.metadata.builder.jaxrpc.JAXRPCDeployment;

/**
 * A deployer JAXRPC JSE Endpoints
 *
 * @author Thomas.Diesler@jboss.org
 * @since 31-Oct-2005
 */
public class JAXRPCDeployerJSE extends AbstractJSEDeployer
{
   @Override
   protected DeploymentType getDeploymentType()
   {
      return DeploymentType.JAXRPC_JSE;
   }

   @Override
   public boolean isWebServiceDeployment(DeploymentUnit unit)
   {
      boolean hasWebMetaData = unit.getAllMetaData(WebMetaData.class).size() > 0;
      return hasWebMetaData && getWebServicesURL(unit) != null;
   }

   @Override
   protected UnifiedDeploymentInfo createUnifiedDeploymentInfo(DeploymentUnit unit) throws DeploymentException
   {
      URL webservicesUrl = getWebServicesURL(unit);
      UnifiedDeploymentInfo udi = new JAXRPCDeployment(getDeploymentType(), webservicesUrl);
      DeploymentInfoAdaptor.buildDeploymentInfo(udi, unit);
      return udi;
   }

   private URL getWebServicesURL(DeploymentUnit unit)
   {
      URL webservicesURL = null;
      VirtualFile vfile = unit.getMetaDataFile("webservices.xml");
      if (vfile != null)
      {
         try
         {
            webservicesURL = vfile.toURL();
         }
         catch (Exception ex)
         {
            // ignore
         }
      }
      return webservicesURL;
   }
}
