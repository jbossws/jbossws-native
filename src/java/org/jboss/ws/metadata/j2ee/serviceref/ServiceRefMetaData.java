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
package org.jboss.ws.metadata.j2ee.serviceref;

// $Id$

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.core.UnifiedVirtualFile;

/**
 * The metdata data from service-ref element in web.xml, ejb-jar.xml, and
 * application-client.xml.
 * 
 * @author Thomas.Diesler@jboss.org
 */
public class ServiceRefMetaData implements Serializable
{
   // provide logging
   private static Logger log = Logger.getLogger(ServiceRefMetaData.class);

   private UnifiedVirtualFile vfsRoot;
   
   // Standard properties 

   // The required <service-ref-name> element
   private String serviceRefName;
   // The JAXRPC required <service-interface> element
   private String serviceInterface;
   // service-res-type
   private String serviceRefType;
   // The optional <wsdl-file> element
   private String wsdlFile;
   // The optional <jaxrpc-mapping-file> element
   private String mappingFile;
   // The optional <service-qname> element
   private QName serviceQName;
   // The LinkedHashMap<String, PortComponentRefMetaData> for <port-component-ref> elements
   private Map<String, PortComponentRefMetaData> portComponentRefs = new LinkedHashMap<String, PortComponentRefMetaData>();
   // The optional <handler> elements. JAX-RPC handlers declared in the standard J2EE1.4 descriptor
   private List<HandlerMetaData> handlers = new ArrayList<HandlerMetaData>();
   // The optional <handler-chains> elements. JAX-WS handlers declared in the standard JavaEE5 descriptor
   private HandlerChainsMetaData handlerChains;

   // JBoss properties 

   // The optional <service-impl-class> element
   private String serviceImplClass;
   // The optional JBossWS config-name
   private String configName;
   // The optional JBossWS config-file
   private String configFile;
   // The optional URL of the actual WSDL to use, <wsdl-override> 
   private String wsdlOverride;
   // The optional <handler-chain> element. JAX-WS handler chain declared in the JBoss JavaEE5 descriptor
   private String handlerChain;
   // Arbitrary proxy properties given by <call-property> 
   private List<CallPropertyMetaData> callProperties = new ArrayList<CallPropertyMetaData>();

   public UnifiedVirtualFile getVfsRoot()
   {
      return vfsRoot;
   }

   public void setVfsRoot(UnifiedVirtualFile vfsRoot)
   {
      this.vfsRoot = vfsRoot;
   }

   public String getServiceRefName()
   {
      return serviceRefName;
   }

   public void setServiceRefName(String serviceRefName)
   {
      this.serviceRefName = serviceRefName;
   }

   public String getMappingFile()
   {
      return mappingFile;
   }

   public void setMappingFile(String mappingFile)
   {
      this.mappingFile = mappingFile;
   }

   public URL getMappingLocation()
   {
      URL mappingURL = null;
      if (mappingFile != null)
      {
         try
         {
            mappingURL = vfsRoot.findChild(mappingFile).toURL();
         }
         catch (Exception e)
         {
            throw new WSException("Cannot find jaxrcp-mapping-file: " + mappingFile, e);
         }
      }
      return mappingURL;
   }

   public Collection<PortComponentRefMetaData> getPortComponentRefs()
   {
      return portComponentRefs.values();
   }

   public PortComponentRefMetaData getPortComponentRef(String seiName)
   {
      PortComponentRefMetaData ref = portComponentRefs.get(seiName);
      return ref;
   }

   public void addPortComponentRef(PortComponentRefMetaData pcRef)
   {
      portComponentRefs.put(pcRef.getServiceEndpointInterface(), pcRef);
   }

   public List<HandlerMetaData> getHandlers()
   {
      return handlers;
   }

   public void addHandler(HandlerMetaData handler)
   {
      handlers.add(handler);
   }

   public String getServiceInterface()
   {
      return serviceInterface;
   }

   public void setServiceInterface(String serviceInterface)
   {
      this.serviceInterface = serviceInterface;
   }

   public String getServiceImplClass()
   {
      return serviceImplClass;
   }

   public void setServiceImplClass(String serviceImplClass)
   {
      this.serviceImplClass = serviceImplClass;
   }

   public QName getServiceQName()
   {
      return serviceQName;
   }

   public void setServiceQName(QName serviceQName)
   {
      this.serviceQName = serviceQName;
   }

   public String getServiceRefType()
   {
      return serviceRefType;
   }

   public void setServiceRefType(String serviceResType)
   {
      this.serviceRefType = serviceResType;
   }

   public String getWsdlFile()
   {
      return wsdlFile;
   }

   public void setWsdlFile(String wsdlFile)
   {
      this.wsdlFile = wsdlFile;
   }

   public URL getWsdlLocation()
   {
      URL wsdlLocation = null;
      if (wsdlOverride != null)
      {
         try
         {
            wsdlLocation = new URL(wsdlOverride);
         }
         catch (MalformedURLException e1)
         {
            try
            {
               wsdlLocation = vfsRoot.findChild(wsdlOverride).toURL();
            }
            catch (Exception e)
            {
               throw new WSException("Cannot find wsdl-override: " + wsdlOverride, e);
            }
         }
      }
      
      if (wsdlLocation == null && wsdlFile != null)
      {
         try
         {
            wsdlLocation = vfsRoot.findChild(wsdlFile).toURL();
         }
         catch (Exception e)
         {
            throw new WSException("Cannot find wsdl-file: " + wsdlFile, e);
         }
      }
      
      return wsdlLocation;
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

   public String getWsdlOverride()
   {
      return wsdlOverride;
   }

   public void setWsdlOverride(String wsdlOverride)
   {
      this.wsdlOverride = wsdlOverride;
   }

   public List<CallPropertyMetaData> getCallProperties()
   {
      return callProperties;
   }

   public void setCallProperties(List<CallPropertyMetaData> callProps)
   {
      callProperties = callProps;
   }

   public void addCallProperty(CallPropertyMetaData callProp)
   {
      callProperties.add(callProp);
   }

   public HandlerChainsMetaData getHandlerChains()
   {
      return handlerChains;
   }

   public void setHandlerChains(HandlerChainsMetaData handlerChains)
   {
      this.handlerChains = handlerChains;
   }

   public String getHandlerChain()
   {
      return handlerChain;
   }

   public void setHandlerChain(String handlerChain)
   {
      this.handlerChain = handlerChain;
   }
}
