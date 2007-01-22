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
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.jboss.ws.core.server.UnifiedVirtualFile;

/**
 * Represents a <service-ref> element of the jboss.xml, jboss-web.xml, jboss-client.xml
 *
 * @author Thomas.Diesler@jboss.com
 * @since 16-Dec-2006
 */
public class UnifiedServiceRef implements Serializable
{
   private static final long serialVersionUID = -6242639118713373752L;
   
   private UnifiedVirtualFile vfsRoot;
   private String serviceRefName;
   private String serviceClassName;
   private QName serviceQName;
   private String configName;
   private String configFile;
   private String handlerChain;
   private List<PortInfo> portInfos = new ArrayList<PortInfo>();
   private String wsdlLocation;

   public UnifiedVirtualFile getRootFile()
   {
      return vfsRoot;
   }
   
   public void setRootFile(UnifiedVirtualFile vfsRoot)
   {
      this.vfsRoot = vfsRoot;
   }
   
   public String getServiceRefName()
   {
      return serviceRefName;
   }

   public void setServiceRefName(String name)
   {
      this.serviceRefName = name;
   }

   public String getServiceClassName()
   {
      return serviceClassName;
   }

   public void setServiceClassName(String serviceClassName)
   {
      this.serviceClassName = serviceClassName;
   }

   public QName getServiceQName()
   {
      return serviceQName;
   }

   public void setServiceQName(QName serviceQName)
   {
      this.serviceQName = serviceQName;
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

   public String getHandlerChain()
   {
      return handlerChain;
   }

   public void setHandlerChain(String handlerChain)
   {
      this.handlerChain = handlerChain;
   }

   public List<PortInfo> getPortInfos()
   {
      return portInfos;
   }

   public String getWsdlLocation()
   {
      return wsdlLocation;
   }

   public void setWsdlLocation(String wsdlLocation)
   {
      this.wsdlLocation = wsdlLocation;
   }

   public String toString()
   {
      StringBuffer sb = new StringBuffer(100);
      sb.append("[");
      sb.append("name=").append(serviceRefName);
      sb.append("]");
      return sb.toString();
   }
}
