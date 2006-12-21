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

// $Id$

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.jboss.deployers.plugins.structure.AbstractDeploymentContext;
import org.jboss.deployers.spi.deployment.MainDeployer;
import org.jboss.deployers.spi.structure.DeploymentContext;
import org.jboss.deployers.spi.structure.DeploymentState;
import org.jboss.logging.Logger;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;
import org.jboss.ws.core.server.ServiceEndpointPublisher;
import org.jboss.ws.core.server.UnifiedDeploymentInfo;

/**
 * Publish the HTTP service endpoint to Tomcat 
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 12-May-2006
 */
public class JBoss50ServiceEndpointPublisher extends ServiceEndpointPublisher
{
   // provide logging
   private static Logger log = Logger.getLogger(JBoss50ServiceEndpointPublisher.class);
   
   private MainDeployer mainDeployer;
   private Map<String, DeploymentContext> contextMap = new HashMap<String, DeploymentContext>();
   
   public MainDeployer getMainDeployer()
   {
      return mainDeployer;
   }

   public void setMainDeployer(MainDeployer mainDeployer)
   {
      this.mainDeployer = mainDeployer;
   }

   public String publishServiceEndpoint(UnifiedDeploymentInfo udi) throws Exception
   {
      URL warURL = udi.expandedWebApp;
      log.debug("publishServiceEndpoint: " + warURL);
      
      rewriteWebXml(warURL);
      DeploymentContext context = createDeploymentContext(warURL);
      
      mainDeployer.addDeploymentContext(context);
      mainDeployer.process();
      
      contextMap.put(warURL.toExternalForm(), context);
      return "OK";
   }

   public String destroyServiceEndpoint(UnifiedDeploymentInfo udi) throws Exception
   {
      URL warURL = udi.expandedWebApp;
      log.debug("destroyServiceEndpoint: " + warURL);
      
      DeploymentContext context = contextMap.get(warURL.toExternalForm());
      if (context != null)
      {
         context.setState(DeploymentState.UNDEPLOYING);
         mainDeployer.process();
         mainDeployer.removeDeploymentContext(context.getName());
         
         contextMap.remove(warURL.toExternalForm());
      }
      return "OK";
   }

   private DeploymentContext createDeploymentContext(URL warURL) throws Exception
   {
      VirtualFile file = VFS.getRoot(warURL);
      return new AbstractDeploymentContext(file);
   }
}
