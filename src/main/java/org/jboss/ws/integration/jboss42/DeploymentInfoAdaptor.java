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

// $Id$

import org.jboss.deployment.DeploymentInfo;
import org.jboss.metadata.ApplicationMetaData;
import org.jboss.metadata.WebMetaData;
import org.jboss.ws.core.server.UnifiedDeploymentInfo;

/**
 * Build container independent deployment info. 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 05-May-2006
 */
public class DeploymentInfoAdaptor
{
   public static UnifiedDeploymentInfo buildDeploymentInfo(UnifiedDeploymentInfo udi, DeploymentInfo di)
   {
      if (di.parent != null)
      {
         udi.parent = new UnifiedDeploymentInfo(null);
         buildDeploymentInfo(udi.parent, di.parent);
      }

      udi.simpleName = di.shortName;
      udi.url = di.url;
      udi.localUrl = di.localUrl;
      udi.metaData = buildMetaData(di.metaData);
      udi.annotationsCl = di.annotationsCl;
      udi.localCl = di.localCl;
      udi.ucl = di.ucl;
      udi.deployedObject = di.deployedObject;

      return udi;
   }

   private static Object buildMetaData(Object metaData)
   {
      Object retMetaData = null;
      if (metaData instanceof WebMetaData)
      {
         retMetaData = WebMetaDataAdaptor.buildUnifiedWebMetaData((WebMetaData)metaData);
      }
      else if (metaData instanceof ApplicationMetaData)
      {
         retMetaData = ApplicationMetaDataAdaptor.buildUnifiedApplicationMetaData((ApplicationMetaData)metaData);
      }
      return retMetaData;
   }
}
