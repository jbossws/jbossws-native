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

import java.net.URL;
import java.net.URLClassLoader;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentUnit;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.logging.Logger;
import org.jboss.metadata.WebMetaData;
import org.jboss.ws.core.server.UnifiedDeploymentInfo;

// $Id$


/**
 * Build container independent deployment info.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 05-May-2006
 */
public class DeploymentInfoAdaptor
{
   // logging support
   private static Logger log = Logger.getLogger(DeploymentInfoAdaptor.class);

   public static void buildDeploymentInfo(UnifiedDeploymentInfo udi, DeploymentUnit unit) throws DeploymentException  
   {
      try
      {
         if (unit.getDeploymentContext().getParent() != null)
         {
            udi.parent = new UnifiedDeploymentInfo(null);
            buildDeploymentInfo(udi.parent, unit.getDeploymentContext().getParent().getDeploymentUnit());
         }

         udi.name = unit.getName();
         udi.simpleName = unit.getSimpleName();
         udi.url = unit.getDeploymentContext().getRoot().toURL();
         udi.metaData = buildMetaData(unit);

         // Since we create temporary classes, we need to create a delegate loader
         // This prevents CCE problems where the parent loader is available at deploy time,
         // and a child loader is available at start time.
         udi.classLoader = new URLClassLoader(new URL[]{}, unit.getClassLoader());
         
         log.debug("UnifiedDeploymentInfo:\n" + udi);
      }
      catch (Exception ex)
      {
         DeploymentException.rethrowAsDeploymentException(ex.getMessage(), ex);
      }
   }

   private static Object buildMetaData(DeploymentUnit unit)
   {
      Object metaData = null;
      if (unit.getAttachment(Ejb3Deployment.class) != null)
      {
         metaData = ApplicationMetaDataAdaptor.buildUnifiedApplicationMetaData(unit);
      }
      else
      {
         if (unit.getAllMetaData(WebMetaData.class).size() > 0)
         {
            metaData = WebMetaDataAdaptor.buildUnifiedWebMetaData(unit);
         }
      }
      return metaData;
   }
}
