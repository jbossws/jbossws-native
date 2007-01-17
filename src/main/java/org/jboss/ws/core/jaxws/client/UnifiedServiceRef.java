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
package org.jboss.ws.core.jaxws.client;

//$Id$

import java.io.Serializable;

import org.jboss.ws.core.server.UnifiedVirtualFile;

/**
 * Represents a <service-ref> element of the jboss.xml, jboss-web.xml, jboss-client.xml deployment descriptor 
 * for the 5.0 schema
 *
 * @author Thomas.Diesler@jboss.com
 */
public class UnifiedServiceRef implements Serializable
{
   private static final long serialVersionUID = -5518998734737147195L;
   
   private UnifiedVirtualFile vfsRoot;
   private String encName;
   private String wsdlLocation;
   private String configName;
   private String configFile;

   public UnifiedServiceRef(UnifiedVirtualFile vfsRoot, String name)
   {
      this.vfsRoot = vfsRoot;
      this.encName = name;
   }

   public UnifiedVirtualFile getRootFile()
   {
      return vfsRoot;
   }

   public String getEncName()
   {
      return encName;
   }

   public String getWsdlLocation()
   {
      return wsdlLocation;
   }

   public void setWsdlLocation(String wsdlLocation)
   {
      this.wsdlLocation = wsdlLocation;
   }

   public String getConfigFile()
   {
      return configFile;
   }

   public void setConfigFile(String configFile)
   {
      this.configFile = configFile;
   }

   public String getConfigName()
   {
      return configName;
   }

   public void setConfigName(String configName)
   {
      this.configName = configName;
   }

   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      sb.append("name=").append(encName);
      sb.append(",config-name=").append(configName);
      sb.append(",config-file=").append(configFile);
      sb.append(",wsdl=").append(wsdlLocation);
      sb.append("]");
      return sb.toString();
   }
}
