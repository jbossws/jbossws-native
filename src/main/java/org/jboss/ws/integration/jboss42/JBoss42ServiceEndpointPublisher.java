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

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.management.MBeanServer;

import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.MainDeployerMBean;
import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanProxy;
import org.jboss.mx.util.MBeanProxyCreationException;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.ws.core.server.ServiceEndpointPublisher;
import org.jboss.ws.core.server.UnifiedDeploymentInfo;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.ServerEndpointMetaData;
import org.jboss.ws.metadata.umdm.ServiceMetaData;
import org.jboss.ws.metadata.umdm.UnifiedMetaData;

/**
 * Publish the HTTP service endpoint to Tomcat 
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 12-May-2006
 */
public class JBoss42ServiceEndpointPublisher extends ServiceEndpointPublisher
{
   // provide logging
   private static Logger log = Logger.getLogger(JBoss42ServiceEndpointPublisher.class);
   
   public String publishServiceEndpoint(UnifiedDeploymentInfo udi) throws Exception
   {
      URL warURL = udi.localUrl;
      DeploymentInfo di = (DeploymentInfo)udi.context.get(DeploymentInfo.class.getName());
      if (di == null)
         throw new IllegalStateException("Cannot obtain DeploymentInfo from context");

      rewriteWebXml(udi);

      // Preserve the repository config
      DeploymentInfo auxdi = new DeploymentInfo(warURL, null, MBeanServerLocator.locateJBoss());
      auxdi.repositoryConfig = di.getTopRepositoryConfig();
      getMainDeployer().deploy(auxdi);
      return "OK";
   }

   public String destroyServiceEndpoint(UnifiedDeploymentInfo udi) throws Exception
   {
      getMainDeployer().undeploy(udi.localUrl);
      return "OK";
   }
   
   public Set<String> getWebServiceServletLinks(UnifiedDeploymentInfo udi)
   {
     Set<String> links = new HashSet<String>();
     
     UnifiedMetaData wsMetaData = getServiceEndpointDeployer().getUnifiedMetaData(udi);

     for (ServiceMetaData serviceMetaData : wsMetaData.getServices())
     {
        for (EndpointMetaData epMetaData : serviceMetaData.getEndpoints())
        {
           ServerEndpointMetaData sepMetaData = (ServerEndpointMetaData)epMetaData;
           links.add(sepMetaData.getLinkName());
           
        }
     }     
     
     return links;
   }
   
   private MainDeployerMBean getMainDeployer() throws MBeanProxyCreationException
   {
      MBeanServer server = MBeanServerLocator.locateJBoss();
      MainDeployerMBean mainDeployer = (MainDeployerMBean)MBeanProxy.get(MainDeployerMBean.class, MainDeployerMBean.OBJECT_NAME, server);
      return mainDeployer;
   }
}
