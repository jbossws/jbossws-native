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
package org.jboss.wsintegration.stack.jbws.deployment;

//$Id$

import org.jboss.ws.metadata.builder.jaxrpc.JAXRPCServerMetaDataBuilder;
import org.jboss.ws.metadata.builder.jaxws.JAXWSMetaDataBuilderEJB3;
import org.jboss.ws.metadata.builder.jaxws.JAXWSMetaDataBuilderJSE;
import org.jboss.ws.metadata.umdm.UnifiedMetaData;
import org.jboss.wsintegration.spi.deployment.AbstractDeployer;
import org.jboss.wsintegration.spi.deployment.Deployment;
import org.jboss.wsintegration.spi.deployment.JAXRPCDeployment;
import org.jboss.wsintegration.spi.deployment.UnifiedDeploymentInfo;
import org.jboss.wsintegration.spi.deployment.Deployment.DeploymentType;

/**
 * A deployer that builds the UnifiedDeploymentInfo 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 25-Apr-2007
 */
public class UnifiedMetaDataDeployer extends AbstractDeployer
{
   @Override
   public void create(Deployment dep)
   {
      UnifiedMetaData umd = dep.getContext().getAttachment(UnifiedMetaData.class);
      if (umd == null)
      {
         UnifiedDeploymentInfo udi = dep.getContext().getAttachment(UnifiedDeploymentInfo.class);
         if (udi == null)
            throw new IllegalStateException("Cannot obtain unified deployment info");
         
         if (udi.type == DeploymentType.JAXRPC_JSE)
         {
            JAXRPCServerMetaDataBuilder builder = new JAXRPCServerMetaDataBuilder();
            umd = builder.buildMetaData((JAXRPCDeployment)udi);
         }
         else if (udi.type == DeploymentType.JAXRPC_EJB21)
         {
            JAXRPCServerMetaDataBuilder builder = new JAXRPCServerMetaDataBuilder();
            umd = builder.buildMetaData((JAXRPCDeployment)udi);
         }
         else if (udi.type == DeploymentType.JAXWS_JSE)
         {
            JAXWSMetaDataBuilderJSE builder = new JAXWSMetaDataBuilderJSE();
            umd = builder.buildMetaData(udi);
         }
         else if (udi.type == DeploymentType.JAXWS_EJB3)
         {
            JAXWSMetaDataBuilderEJB3 builder = new JAXWSMetaDataBuilderEJB3();
            umd = builder.buildMetaData(udi);
         }
         else
         {
            throw new IllegalStateException("Invalid type:  " + udi.type);
         }
         
         dep.getContext().addAttachment(UnifiedMetaData.class, umd);
      }
   }
}