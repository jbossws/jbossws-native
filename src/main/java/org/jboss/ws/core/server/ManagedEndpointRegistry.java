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
package org.jboss.ws.core.server;

// $Id$

import java.util.ArrayList;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.ws.metadata.umdm.ServerEndpointMetaData;
import org.jboss.ws.metadata.umdm.UnifiedMetaData;
import org.jboss.wsintegration.spi.deployment.Endpoint;
import org.jboss.wsintegration.spi.management.BasicEndpointRegistry;

/**
 * A Service Endpoint Registry
 *
 * @author Thomas.Diesler@jboss.org
 * @since 04-May-2007
 */
public class ManagedEndpointRegistry extends BasicEndpointRegistry implements ManagedEndpointRegistryMBean
{
   // provide logging
   private static final Logger log = Logger.getLogger(ManagedEndpointRegistry.class);

   public String getImplementationTitle()
   {
      return getClass().getPackage().getImplementationTitle();
   }

   public String getImplementationVersion()
   {
      return UnifiedMetaData.getImplementationVersion();
   }

   /** Resolve a port-component-link, like:
    *
    *    [deployment.war]#PortComponentName
    *    [deployment.jar]#PortComponentName
    *
    */
   public Endpoint resolvePortComponentLink(String pcLink)
   {
      String pcName = pcLink;
      int hashIndex = pcLink.indexOf("#");
      if (hashIndex > 0)
      {
         pcName = pcLink.substring(hashIndex + 1);
      }

      Endpoint endpoint = null;
      for (ObjectName sepID : getEndpoints())
      {
         Endpoint auxEndpoint = getEndpoint(sepID);
         ServerEndpointMetaData sepMetaData = auxEndpoint.getAttachment(ServerEndpointMetaData.class);
         if (pcName.equals(sepMetaData.getPortComponentName()))
         {
            if (endpoint != null)
            {
               log.warn("Multiple service endoints found for: " + pcLink);
               endpoint = null;
               break;
            }
            endpoint = auxEndpoint;
         }
      }

      if (endpoint == null)
         log.warn("No ServiceEndpoint found for pcLink: " + pcLink);

      return endpoint;
   }

   public void create() throws Exception
   {
      log.info(getImplementationTitle());
      log.info(getImplementationVersion());
      MBeanServer server = getMBeanServer();
      if (server != null)
      {
         server.registerMBean(this, OBJECT_NAME);
      }
   }

   public void destroy() throws Exception
   {
      log.debug("Destroy service endpoint manager");
      MBeanServer server = getMBeanServer();
      if (server != null)
      {
         server.unregisterMBean(OBJECT_NAME);
      }
   }

   private MBeanServer getMBeanServer()
   {
      MBeanServer server = null;
      ArrayList servers = MBeanServerFactory.findMBeanServer(null);
      if (servers.size() > 0)
      {
         server = (MBeanServer)servers.get(0);
      }
      return server;
   }
}
