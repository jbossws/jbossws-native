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

// $Id$

import java.io.IOException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceFeature;

import org.jboss.ws.WSException;
import org.jboss.ws.core.ConfigProvider;
import org.jboss.ws.core.StubExt;
import org.jboss.ws.core.UnifiedVirtualFile;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.ServiceMetaData;
import org.jboss.ws.metadata.wsse.WSSecurityConfigFactory;
import org.jboss.ws.metadata.wsse.WSSecurityConfiguration;

/**
 * Extends the standard JAXWS Service 
 *
 * @author Thomas.Diesler@jboss.com
 * @since 18-Jan-2007
 */
public class ServiceExt extends Service implements ConfigProvider
{
   // The config name for all created ports 
   private String configName;
   // The config file for all created ports 
   private String configFile;
   // The WS-Security config
   private String securityConfig;

   public ServiceExt(URL wsdlDocumentLocation, QName serviceName)
   {
      super(wsdlDocumentLocation, serviceName);
   }

   /** 
    * Get the port configuration file for newly created ports 
    */
   public String getConfigFile()
   {
      return configFile;
   }

   /** 
    * Get the port configuration name for newly created ports 
    */
   public String getConfigName()
   {
      return configName;
   }

   /** 
    * Set the port configuration name for newly created ports 
    */
   public void setConfigName(String configName)
   {
      this.configName = configName;
   }

   /** 
    * Set the port configuration name for newly created ports 
    */
   public void setConfigName(String configName, String configFile)
   {
      this.configName = configName;
      this.configFile = configFile;
   }

   /** 
    * Get the WS-Security configuration  
    */
   public String getSecurityConfig()
   {
      return securityConfig;
   }

   /** 
    * Set the WS-Security configuration  
    */
   public void setSecurityConfig(String securityConfig)
   {
      this.securityConfig = securityConfig;
   }

   @Override
   public <T> T getPort(Class<T> serviceEndpointInterface, WebServiceFeature... features)
   {
      T port = super.getPort(serviceEndpointInterface, features);
      configurePort(port);
      return port;
   }

   @Override
   public <T> T getPort(Class<T> serviceEndpointInterface)
   {
      T port = super.getPort(serviceEndpointInterface);
      configurePort(port);
      return port;
   }

   @Override
   public <T> T getPort(EndpointReference endpointReference, Class<T> serviceEndpointInterface, WebServiceFeature... features)
   {
      T port = super.getPort(endpointReference, serviceEndpointInterface, features);
      configurePort(port);
      return port;
   }

   @Override
   public <T> T getPort(QName portName, Class<T> serviceEndpointInterface, WebServiceFeature... features)
   {
      T port = super.getPort(portName, serviceEndpointInterface, features);
      configurePort(port);
      return port;
   }

   @Override
   public <T> T getPort(QName portName, Class<T> serviceEndpointInterface)
   {
      T port = super.getPort(portName, serviceEndpointInterface);
      configurePort(port);
      return port;
   }

   private void configurePort(Object port)
   {
      ConfigProvider cp = (ConfigProvider)port;
      if (configName != null || configFile != null)
         cp.setConfigName(configName, configFile);

      if (securityConfig != null)
      {
         EndpointMetaData epMetaData = ((StubExt)port).getEndpointMetaData();
         ServiceMetaData serviceMetaData = epMetaData.getServiceMetaData();
         if (serviceMetaData.getSecurityConfiguration() == null)
         {
            try
            {
               WSSecurityConfigFactory wsseConfFactory = WSSecurityConfigFactory.newInstance();
               UnifiedVirtualFile vfsRoot = serviceMetaData.getUnifiedMetaData().getRootFile();
               WSSecurityConfiguration config = wsseConfFactory.createConfiguration(vfsRoot, securityConfig);
               serviceMetaData.setSecurityConfiguration(config);
            }
            catch (IOException ex)
            {
               WSException.rethrow("Cannot set security config", ex);
            }
         }
      }
   }
}
