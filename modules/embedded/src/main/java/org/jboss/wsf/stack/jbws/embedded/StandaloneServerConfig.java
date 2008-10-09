/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.wsf.stack.jbws.embedded;

import org.jboss.wsf.spi.management.ServerConfig;

import java.io.File;
import java.net.UnknownHostException;

/**
 * @author Heiko.Braun <heiko.braun@jboss.com>
 */
public class StandaloneServerConfig implements ServerConfig
{

   public String getImplementationTitle()
   {
      return "JBossWS Standalone";
   }

   public String getImplementationVersion()
   {
      return "3.0.0";
   }
   
   public File getHomeDir()
   {
      return new File("/tmp"); // TODO: change to default
   }

   public File getServerTempDir()
   {
      return new File("/tmp"); // TODO: change to default
   }

   public File getServerDataDir()
   {
      return new File("/tmp");  //To change body of implemented methods use File | Settings | File Templates.
   }

   public String getWebServiceHost()
   {
      return "localhost";  //To change body of implemented methods use File | Settings | File Templates.
   }

   public void setWebServiceHost(String host) throws UnknownHostException
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }

   public int getWebServicePort()
   {
      return 20000;  // introduce constant
   }

   public void setWebServicePort(int port)
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }

   public int getWebServiceSecurePort()
   {
      return 20001; // TODO: constant
   }

   public void setWebServiceSecurePort(int port)
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }

   public boolean isModifySOAPAddress()
   {
      return true;
   }

   public void setModifySOAPAddress(boolean flag)
   {
      
   }
}
