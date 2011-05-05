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
package org.jboss.ws.metadata.umdm;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.handler.PortInfo;

import org.jboss.ws.core.jaxws.JAXBContextCache;
import org.jboss.ws.core.jaxws.wsaddressing.NativeEndpointReference;
import org.jboss.ws.core.soap.Style;
import org.jboss.ws.core.soap.Use;
import org.jboss.ws.metadata.config.Configurable;
import org.jboss.ws.api.binding.BindingCustomization;
import org.jboss.wsf.spi.deployment.UnifiedVirtualFile;
import org.jboss.wsf.spi.metadata.config.CommonConfig;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedPortComponentRefMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerMetaData.HandlerType;

/**
 * Feature aware client endpoint meta data adapter.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class FeatureAwareClientEndpointMetaDataAdapter extends ClientEndpointMetaData implements FeatureAwareEndpointMetaData
{
   
   private final ClientEndpointMetaData delegee;
   private final Set<WebServiceFeature> features = new HashSet<WebServiceFeature>();
   
   public FeatureAwareClientEndpointMetaDataAdapter(final ClientEndpointMetaData delegee)
   {
      this.delegee = delegee;
   }

   @SuppressWarnings("unchecked")
   @Override
   public <T extends WebServiceFeature> T getFeature(Class<T> key)
   {
      for (WebServiceFeature feature : this.features)
      {
         if (key == feature.getClass())
         {
            return (T)feature;
         }
      }
      
      return this.delegee.getFeature(key);
   }

   @Override
   public void setFeature(WebServiceFeature feature)
   {
      this.features.add(feature);
   }

   @Override
   public FeatureSet getFeatures()
   {
      final FeatureSet retVal = this.delegee.getFeatures();
      
      for (WebServiceFeature feature : this.features)
      {
         retVal.addFeature(feature);
      }
      
      return retVal;
   }

   @Override
   public void addFeature(WebServiceFeature feature)
   {
      this.setFeature(feature);
   }

   @Override
   public <T extends WebServiceFeature> boolean isFeatureEnabled(final Class<T> key)
   {
      final T feature = this.getFeature(key);
      
      return (feature != null ? feature.isEnabled() : false);
   }

   // ********************************************
   // just delegate to non feature related methods
   // ********************************************
   
   @Override
   public EndpointConfigMetaData createEndpointConfigMetaData(String configName, String configFile)
   {
      return this.delegee.createEndpointConfigMetaData(configName, configFile);
   }

   @Override
   public String getEndpointAddress()
   {
      return this.delegee.getEndpointAddress();
   }

   @Override
   public PortInfo getPortInfo()
   {
      return this.delegee.getPortInfo();
   }

   @Override
   public void setEndpointAddress(String endpointAddress)
   {
      this.delegee.setEndpointAddress(endpointAddress);
   }

   @Override
   public String toString()
   {
      return this.delegee.toString();
   }

   @Override
   public void addHandler(HandlerMetaData handler)
   {
      this.delegee.addHandler(handler);
   }

   @Override
   public void addHandlers(List<HandlerMetaData> configHandlers)
   {
      this.delegee.addHandlers(configHandlers);
   }

   @Override
   public void addOperation(OperationMetaData opMetaData)
   {
      this.delegee.addOperation(opMetaData);
   }

   @Override
   public void clearHandlers()
   {
      this.delegee.clearHandlers();
   }

   @Override
   public void clearOperations()
   {
      this.delegee.clearOperations();
   }

   @Override
   public void configure(Configurable configurable)
   {
      this.delegee.configure(configurable);
   }

   @Override
   public void eagerInitialize()
   {
      this.delegee.eagerInitialize();
   }

   @Override
   public String getAuthMethod()
   {
      return this.delegee.getAuthMethod();
   }

   @Override
   public Collection<BindingCustomization> getBindingCustomizations()
   {
      return this.delegee.getBindingCustomizations();
   }

   @Override
   public String getBindingId()
   {
      return this.delegee.getBindingId();
   }

   @Override
   public ClassLoader getClassLoader()
   {
      return this.delegee.getClassLoader();
   }

   @Override
   public CommonConfig getConfig()
   {
      return this.delegee.getConfig();
   }

   @Override
   public String getConfigFile()
   {
      return this.delegee.getConfigFile();
   }

   @Override
   public String getConfigName()
   {
      return this.delegee.getConfigName();
   }

   @Override
   public String getDocumentation()
   {
      return this.delegee.getDocumentation();
   }

   @Override
   public Use getEncodingStyle()
   {
      return this.delegee.getEncodingStyle();
   }

   @Override
   public EndpointConfigMetaData getEndpointConfigMetaData()
   {
      return this.delegee.getEndpointConfigMetaData();
   }

   @Override
   public NativeEndpointReference getEndpointReference()
   {
      return this.delegee.getEndpointReference();
   }

   @Override
   public List<HandlerMetaData> getHandlerMetaData(HandlerType type)
   {
      return this.delegee.getHandlerMetaData(type);
   }

   @Override
   public JAXBContextCache getJaxbCache()
   {
      return this.delegee.getJaxbCache();
   }

   @Override
   public OperationMetaData getOperation(QName xmlName)
   {
      return this.delegee.getOperation(xmlName);
   }

   @Override
   public OperationMetaData getOperation(Method method)
   {
      return this.delegee.getOperation(method);
   }

   @Override
   public List<OperationMetaData> getOperations()
   {
      return this.delegee.getOperations();
   }

   @Override
   public ParameterStyle getParameterStyle()
   {
      return this.delegee.getParameterStyle();
   }

   @Override
   public QName getPortName()
   {
      return this.delegee.getPortName();
   }

   @Override
   public QName getPortTypeName()
   {
      return this.delegee.getPortTypeName();
   }

   @Override
   public Properties getProperties()
   {
      return this.delegee.getProperties();
   }

   @Override
   public List<Class> getRegisteredTypes()
   {
      return this.delegee.getRegisteredTypes();
   }

   @Override
   public UnifiedVirtualFile getRootFile()
   {
      return this.delegee.getRootFile();
   }

   @Override
   public Class getServiceEndpointInterface()
   {
      return this.delegee.getServiceEndpointInterface();
   }

   @Override
   public String getServiceEndpointInterfaceName()
   {
      return this.delegee.getServiceEndpointInterfaceName();
   }

   @Override
   public ServiceMetaData getServiceMetaData()
   {
      return this.delegee.getServiceMetaData();
   }

   @Override
   public Mode getServiceMode()
   {
      return this.delegee.getServiceMode();
   }

   @Override
   public List<UnifiedPortComponentRefMetaData> getServiceRefContrib()
   {
      return this.delegee.getServiceRefContrib();
   }

   @Override
   public Style getStyle()
   {
      return this.delegee.getStyle();
   }

   @Override
   public Type getType()
   {
      return this.delegee.getType();
   }

   @Override
   public void initEndpointConfig()
   {
      this.delegee.initEndpointConfig();
   }

   @Override
   public boolean isHandlersInitialized()
   {
      return this.delegee.isHandlersInitialized();
   }

   @Override
   public boolean matches(UnifiedPortComponentRefMetaData pcRef)
   {
      return this.delegee.matches(pcRef);
   }

   @Override
   public void registerConfigObserver(Configurable observer)
   {
      this.delegee.registerConfigObserver(observer);
   }

   @Override
   public void setAuthMethod(String authMethod)
   {
      this.delegee.setAuthMethod(authMethod);
   }

   @Override
   public void setBindingId(String bindingId)
   {
      this.delegee.setBindingId(bindingId);
   }

   @Override
   public void setConfigName(String configName)
   {
      this.delegee.setConfigName(configName);
   }

   @Override
   public void setConfigName(String configName, String configFile)
   {
      this.delegee.setConfigName(configName, configFile);
   }

   @Override
   public void setDocumentation(String documentation)
   {
      this.delegee.setDocumentation(documentation);
   }

   @Override
   public void setEncodingStyle(Use value)
   {
      this.delegee.setEncodingStyle(value);
   }

   @Override
   public void setEndpointReference(NativeEndpointReference epr)
   {
      this.delegee.setEndpointReference(epr);
   }

   @Override
   public void setHandlersInitialized(boolean flag)
   {
      this.delegee.setHandlersInitialized(flag);
   }

   @Override
   public void setParameterStyle(ParameterStyle value)
   {
      this.delegee.setParameterStyle(value);
   }

   @Override
   public void setPortName(QName portName)
   {
      this.delegee.setPortName(portName);
   }

   @Override
   public void setProperties(Properties properties)
   {
      this.delegee.setProperties(properties);
   }

   @Override
   public void setServiceEndpointInterfaceName(String seiName)
   {
      this.delegee.setServiceEndpointInterfaceName(seiName);
   }

   @Override
   public void setServiceMode(Mode serviceMode)
   {
      this.delegee.setServiceMode(serviceMode);
   }

   @Override
   public void setStyle(Style value)
   {
      this.delegee.setStyle(value);
   }

   @Override
   public void validate()
   {
      this.delegee.validate();
   }

   @Override
   public void addExtension(MetaDataExtension ext)
   {
      this.delegee.addExtension(ext);
   }

   @Override
   public MetaDataExtension getExtension(String namespace)
   {
      return this.delegee.getExtension(namespace);
   }

   @Override
   public Map<String, MetaDataExtension> getExtensions()
   {
      return this.delegee.getExtensions();
   }

   @Override
   public boolean equals(Object obj)
   {
      return this.delegee.equals(obj);
   }

   @Override
   public int hashCode()
   {
      return this.delegee.hashCode();
   }

}
