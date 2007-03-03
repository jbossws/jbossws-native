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

/**
 * Represents a <port-info> element in <service-ref>
 *
 * @author Thomas.Diesler@jboss.com
 */
public class PortInfo implements Serializable
{
   private static final long serialVersionUID = -5517739021682888778L;
   
   private UnifiedServiceRef serviceRef;
   private String serviceEndpointInterface;
   private QName portQName;
   private String configName;
   private String configFile;
   private List<NameValuePair> stubProperties = new ArrayList<NameValuePair>();

   public PortInfo(UnifiedServiceRef serviceRef)
   {
      this.serviceRef = serviceRef;
   }
   
   public UnifiedServiceRef getServiceRef()
   {
      return serviceRef;
   }

   public QName getPortQName()
   {
      return portQName;
   }

   public void setPortQName(QName portName)
   {
      this.portQName = portName;
   }

   public String getServiceEndpointInterface()
   {
      return serviceEndpointInterface;
   }

   public void setServiceEndpointInterface(String serviceEndpointInterface)
   {
      this.serviceEndpointInterface = serviceEndpointInterface;
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

   public List<NameValuePair> getStubProperties()
   {
      return stubProperties;
   }
}
