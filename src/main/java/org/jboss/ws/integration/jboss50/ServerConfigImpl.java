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

import java.io.File;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.ws.core.server.ServerConfig;
import org.jboss.ws.core.utils.ObjectNameFactory;

/**
 * JBoss specific implementation of a ServerConfig 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 08-May-2006
 */
public class ServerConfigImpl implements ServerConfig
{
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
}
