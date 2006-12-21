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
package org.jboss.ws.integration.tomcat;

//$Id: ServiceEndpointManagerFactoryImpl.java 294 2006-05-08 16:33:42Z thomas.diesler@jboss.com $

import java.io.File;
import java.io.IOException;

import org.jboss.ws.core.server.ServerConfig;

/**
 * Tomcat specific implementation of a ServerConfig 
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
         File tmpdir = File.createTempFile("jbossws", null).getParentFile();
         return tmpdir;
      }
      catch (IOException e)
      {
         return null;
      }
   }

   public File getServerDataDir()
   {
      try
      {
         File tmpdir = File.createTempFile("jbossws", null).getParentFile();
         return tmpdir;
      }
      catch (IOException e)
      {
         return null;
      }
   }
}
