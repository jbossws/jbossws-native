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
package org.jboss.ws.core.deployment;

//$Id$

import javax.management.ObjectName;

import org.jboss.ws.integration.Endpoint;
import org.jboss.ws.integration.deployment.AbstractDeployer;
import org.jboss.ws.integration.deployment.Deployment;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.ServerEndpointMetaData;
import org.jboss.ws.metadata.umdm.ServiceMetaData;
import org.jboss.ws.metadata.umdm.UnifiedMetaData;

/**
 * A deployer that assigns the EndpointMetaData to the Endpoint 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 25-Apr-2007
 */
public class UnifiedMetaDataAssociationDeployer extends AbstractDeployer
{
   @Override
   public void create(Deployment dep)
   {
      UnifiedMetaData umd = dep.getContext().getAttachment(UnifiedMetaData.class);
      if (umd == null)
         throw new IllegalStateException("Cannot obtain unified meta data");

      for (Endpoint ep : dep.getService().getEndpoints())
      {
         ServerEndpointMetaData sepMetaData = ep.getMetaData(ServerEndpointMetaData.class);
         if (sepMetaData == null)
         {
            sepMetaData = getEndpointMetaData(umd, ep.getName());
            sepMetaData.setServiceEndpointImplName(ep.getEndpointImpl().getName());
            ep.addMetaData(ServerEndpointMetaData.class, sepMetaData);
         }
      }
   }

   private ServerEndpointMetaData getEndpointMetaData(UnifiedMetaData umd, ObjectName epName)
   {
      String propEndpoint = epName.getKeyProperty(Endpoint.SEPID_PROPERTY_ENDPOINT);

      ServerEndpointMetaData epMetaData = null;
      for (ServiceMetaData serviceMetaData : umd.getServices())
      {
         for (EndpointMetaData aux : serviceMetaData.getEndpoints())
         {
            String linkName = ((ServerEndpointMetaData)aux).getLinkName();
            if (propEndpoint.equals(linkName))
            {
               epMetaData = (ServerEndpointMetaData)aux;
               break;
            }
         }
      }

      if (epMetaData == null)
         throw new IllegalStateException("Cannot find endpoint meta data for: " + epName);

      return epMetaData;
   }
}