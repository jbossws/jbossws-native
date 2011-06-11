/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ws.core.jaxws.spi.http;

import java.io.File;
import java.net.UnknownHostException;
import java.util.List;

import org.jboss.ws.WSException;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.metadata.config.EndpointConfig;

/**
 * Netty HTTP server config that configures user temp 
 * directory to be used for contract publishing.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
final class NettyHttpServerConfig implements ServerConfig
{

   static final ServerConfig SINGLETON = new NettyHttpServerConfig();
   
   /** Temporary directory location. */
   private static File tmpDir;

   static
   {
      try
      {
         NettyHttpServerConfig.tmpDir = new File(System.getProperty("java.io.tmpdir"));
      }
      catch (SecurityException se)
      {
         throw new WSException(se);
      }
   }

   /**
    * Constructor.
    */
   public NettyHttpServerConfig()
   {
      super();
   }

   /**
    * @see ServerConfig#getServerHomeDir()
    * 
    * @return server home directory
    */
   public File getHomeDir()
   {
      return NettyHttpServerConfig.tmpDir;
   }

   /**
    * @see ServerConfig#getServerDataDir()
    * 
    * @return server data directory
    */
   public File getServerDataDir()
   {
      return NettyHttpServerConfig.tmpDir;
   }

   /**
    * @see ServerConfig#getServerTempDir()
    * 
    * @return server temp directory
    */
   public File getServerTempDir()
   {
      return NettyHttpServerConfig.tmpDir;
   }

   /**
    * @see ServerConfig#getWebServiceHost()
    * 
    * @return localhost
    */
   public String getWebServiceHost()
   {
      return "localhost";
   }

   /**
    * @see ServerConfig#isModifySOAPAddress()
    * 
    * @return always return false
    */
   public boolean isModifySOAPAddress()
   {
      return false;
   }

   // not implemented methods

   /**
    * Not implemented.
    * 
    * @return string
    */
   public String getImplementationTitle()
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Not implemented.
    * 
    * @return string
    */
   public String getImplementationVersion()
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Not implemented.
    * 
    * @return int
    */
   public int getWebServicePort()
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Not implemented.
    * 
    * @return int
    */
   public int getWebServiceSecurePort()
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Not implemented.
    * 
    * @param flag boolean
    */
   public void setModifySOAPAddress(final boolean flag)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Not implemented.
    * 
    * @param host string
    * @throws UnknownHostException never thrown
    */
   public void setWebServiceHost(final String host) throws UnknownHostException
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Not implemented.
    * 
    * @param port int
    */
   public void setWebServicePort(final int port)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Not implemented.
    * 
    * @param port int
    */
   public void setWebServiceSecurePort(final int port)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public void addEndpointConfig(EndpointConfig config)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public List<EndpointConfig> getEndpointConfigs()
   {
      throw new UnsupportedOperationException();
   }

}
