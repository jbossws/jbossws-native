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
package org.jboss.ws.metadata.umdm;

// $Id$

import org.jboss.ws.metadata.umdm.HandlerMetaData.HandlerType;
import org.jboss.ws.metadata.config.ConfigurationProvider;

import javax.management.ObjectName;
import javax.xml.namespace.QName;

/**
 * Client side endpoint meta data.
 *
 * @author Thomas.Diesler@jboss.org
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @since 12-May-2005
 */
public class ServerEndpointMetaData extends EndpointMetaData
{
   public static final String SEPID_DOMAIN = "jboss.ws";
   public static final String SEPID_PROPERTY_CONTEXT = "context";
   public static final String SEPID_PROPERTY_ENDPOINT = "endpoint";

   // The REQUIRED link name
   private String linkName;
   // Legacy JSR-109 port component name
   private String portComponentName;
   // The endpoint implementation bean
   private String implName;
   // The unique service endpointID
   private ObjectName sepID;
   // The HTTP context root
   private String contextRoot;
   // The HTTP virtual hosts 
   private String[] virtualHosts;
   // The HTTP url parttern
   private String urlPattern;
   // The optional transport guarantee
   private String transportGuarantee;
   // The optional secure wsdl access 
   private boolean secureWSDLAccess = true;
   // The bean that registers with the ServiceEndpointManager
   private String managedEndpointBean = "org.jboss.ws.core.server.ServiceEndpoint";

   public ServerEndpointMetaData(ServiceMetaData service, QName portName, QName portTypeName, Type type)
   {
      super(service, portName, portTypeName, type);
   }

   public String getLinkName()
   {
      return linkName;
   }

   public void setLinkName(String linkName)
   {
      this.linkName = linkName;
   }

   public String getPortComponentName()
   {
      return portComponentName;
   }

   public void setPortComponentName(String portComponentName)
   {
      this.portComponentName = portComponentName;
   }

   public String getServiceEndpointImplName()
   {
      return implName;
   }

   public void setServiceEndpointImplName(String endpointImpl)
   {
      this.implName = endpointImpl;
   }

   public ObjectName getServiceEndpointID()
   {
      return sepID;
   }

   public void setServiceEndpointID(ObjectName endpointID)
   {
      this.sepID = endpointID;
   }

   public String getContextRoot()
   {
      return contextRoot;
   }

   public void setContextRoot(String contextRoot)
   {
      if (contextRoot != null && !(contextRoot.startsWith("/")))
         throw new IllegalArgumentException("context root should start with '/'");

      this.contextRoot = contextRoot;
   }

   public String[] getVirtualHosts()
   {
      return virtualHosts;
   }

   public void setVirtualHosts(String[] virtualHosts)
   {
      this.virtualHosts = virtualHosts;
   }

   public String getURLPattern()
   {
      return urlPattern;
   }

   public void setURLPattern(String urlPattern)
   {
      if (urlPattern != null && !urlPattern.startsWith("/"))
         throw new IllegalArgumentException("url pattern should start with '/'");

      this.urlPattern = urlPattern;
   }

   public String getTransportGuarantee()
   {
      return transportGuarantee;
   }

   public void setTransportGuarantee(String transportGuarantee)
   {
      this.transportGuarantee = transportGuarantee;
   }

   public boolean isSecureWSDLAccess()
   {
      return secureWSDLAccess;
   }

   public void setSecureWSDLAccess(boolean secureWSDLAccess)
   {
      this.secureWSDLAccess = secureWSDLAccess;
   }

   public String getManagedEndpointBean()
   {
      return managedEndpointBean;
   }

   public void setManagedEndpointBean(String managedEndpointBean)
   {
      this.managedEndpointBean = managedEndpointBean;
   }

   public String getConfigName() {
      String configName = super.getConfigName();
      if (configName == null)
      {
         configName = ConfigurationProvider.DEFAULT_ENDPOINT_CONFIG_NAME;
         setConfigName(configName);
      }
      return configName;
   }

   public String getConfigFile() {
      String configFile = super.getConfigFile();

      if (configFile == null)
      {
         if (getType() == Type.JAXRPC)
         {
            configFile = ConfigurationProvider.DEFAULT_JAXRPC_ENDPOINT_CONFIG_FILE;
         }
         else
         {
            configFile = ConfigurationProvider.DEFAULT_JAXWS_ENDPOINT_CONFIG_FILE;
         }
         setConfigFile(configFile);
      }
      return configFile;
   }

   public String toString()
   {
      StringBuilder buffer = new StringBuilder("\nServerEndpointMetaData:");
      buffer.append("\n type=").append(getType());
      buffer.append("\n qname=").append(getPortName());
      buffer.append("\n id=").append(getServiceEndpointID().getCanonicalName());
      buffer.append("\n address=").append(getEndpointAddress());
      buffer.append("\n binding=").append(getBindingId());
      buffer.append("\n linkName=").append(getLinkName());
      buffer.append("\n implName=").append(getServiceEndpointImplName());
      buffer.append("\n seiName=").append(getServiceEndpointInterfaceName());
      buffer.append("\n serviceMode=").append(getServiceMode());
      buffer.append("\n portComponentName=").append(getPortComponentName());
      buffer.append("\n contextRoot=").append(getContextRoot());
      buffer.append("\n urlPattern=").append(getURLPattern());
      buffer.append("\n configFile=").append(getConfigFile());
      buffer.append("\n configName=").append(getConfigName());
      buffer.append("\n authMethod=").append(getAuthMethod());
      buffer.append("\n transportGuarantee=").append(getTransportGuarantee());
      buffer.append("\n secureWSDLAccess=").append(isSecureWSDLAccess());
      buffer.append("\n properties=").append(getProperties());

      for (OperationMetaData opMetaData : getOperations())
      {
         buffer.append("\n").append(opMetaData);
      }
      for (HandlerMetaData hdlMetaData : getHandlerMetaData(HandlerType.ALL))
      {
         buffer.append("\n").append(hdlMetaData);
      }
      return buffer.toString();
   }
}
