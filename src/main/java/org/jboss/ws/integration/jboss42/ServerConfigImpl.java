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

import java.io.File;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.ws.core.server.ServerConfig;
import org.jboss.ws.core.utils.ObjectNameFactory;

/**
 * JBoss specific implementation of a ServerConfig 
 *
 * @author Thomas.Diesler@jboss.org
 * @author darran.lofthouse@jboss.com
 * @since 08-May-2006
 */
public class ServerConfigImpl implements ServerConfig
{

   private static final Logger log = Logger.getLogger(ServerConfigImpl.class);

   public File getServerTempDir()
   {
      try
      {
         MBeanServer server = MBeanServerLocator.locateJBoss();
         ObjectName oname = ObjectNameFactory.create("jboss.system:type=ServerConfig");
         File tmpdir = (File)server.getAttribute(oname, "ServerTempDir");
         return tmpdir;
      }
      catch (JMException e)
      {
         return null;
      }
   }

   public File getServerDataDir()
   {
      try
      {
         MBeanServer server = MBeanServerLocator.locateJBoss();
         ObjectName oname = ObjectNameFactory.create("jboss.system:type=ServerConfig");
         File tmpdir = (File)server.getAttribute(oname, "ServerDataDir");
         return tmpdir;
      }
      catch (JMException e)
      {
         return null;
      }
   }

   public int getWebServicePort()
   {
      int port = getConnectorPort("HTTP/1.1", false);
      if (port > -1)
      {
         return port;
      }

      log.warn("Unable to calculate 'WebServicePort', using default '8080'");
      return 8080;
   }

   public int getWebServiceSecurePort()
   {
      int port = getConnectorPort("HTTP/1.1", true);
      if (port > -1)
      {
         return port;
      }

      log.warn("Unable to calculate 'WebServiceSecurePort', using default '8443'");
      return 8443;
   }

   private int getConnectorPort(final String protocol, final boolean secure)
   {
      int port = -1;

      try
      {
         MBeanServer server = MBeanServerLocator.locateJBoss();
         ObjectName connectors = new ObjectName("jboss.web:type=Connector,*");

         Set connectorNames = server.queryNames(connectors, null);
         for (Object current : connectorNames)
         {
            ObjectName currentName = (ObjectName)current;

            try
            {
               int connectorPort = (Integer)server.getAttribute(currentName, "port");
               boolean connectorSecure = (Boolean)server.getAttribute(currentName, "secure");
               String connectorProtocol = (String)server.getAttribute(currentName, "protocol");

               if (protocol.equals(connectorProtocol) && secure == connectorSecure)
               {
                  if (port > -1)
                  {
                     log.warn("Found multiple connectors for protocol='" + protocol + "' and secure='" + secure + "', using first port found '" + port + "'");
                  }
                  else
                  {
                     port = connectorPort;
                  }
               }
            }
            catch (AttributeNotFoundException ignored)
            {
            }
         }

         return port;
      }
      catch (JMException e)
      {
         return -1;
      }

   }
}
