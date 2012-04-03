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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.WebServiceFeature;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.common.Constants;
import org.jboss.ws.common.JavaUtils;
import org.jboss.ws.core.binding.TypeMappingImpl;
import org.jboss.ws.core.jaxrpc.binding.JBossXBDeserializerFactory;
import org.jboss.ws.core.jaxrpc.binding.JBossXBSerializerFactory;
import org.jboss.ws.core.jaxrpc.binding.SOAPArrayDeserializerFactory;
import org.jboss.ws.core.jaxrpc.binding.SOAPArraySerializerFactory;
import org.jboss.ws.core.soap.Style;
import org.jboss.ws.core.soap.Use;
import org.jboss.ws.metadata.accessor.AccessorFactory;
import org.jboss.ws.metadata.accessor.AccessorFactoryCreator;
import org.jboss.ws.metadata.config.Configurable;
import org.jboss.ws.metadata.config.ConfigurationProvider;
import org.jboss.ws.metadata.config.JBossWSConfigFactory;
import org.jboss.wsf.spi.deployment.UnifiedVirtualFile;
import org.jboss.wsf.spi.metadata.config.CommonConfig;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerMetaData.HandlerType;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedPortComponentRefMetaData;

/**
 * A Service component describes a set of endpoints.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 12-May-2005
 */
public abstract class EndpointMetaData extends ExtensibleMetaData implements ConfigurationProvider, InitalizableMetaData
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(EndpointMetaData.class);
   // provide logging
   private static Logger log = Logger.getLogger(EndpointMetaData.class);

   public static final Set<String> SUPPORTED_BINDINGS = new HashSet<String>();
   static
   {
      SUPPORTED_BINDINGS.add(Constants.SOAP11HTTP_BINDING);
      SUPPORTED_BINDINGS.add(Constants.SOAP12HTTP_BINDING);
      SUPPORTED_BINDINGS.add(Constants.HTTP_BINDING);
   }

   // The parent meta data.
   private ServiceMetaData serviceMetaData;

   // The REQUIRED configuration meta data.
   private EndpointConfigMetaData configMetaData;
   // The REQUIRED name
   private QName portName;
   // The REQUIRED binding id
   private String bindingId;
   // The REQUIRED name of the WSDL interface/portType
   private QName portTypeName;
   // The endpoint interface name
   private String seiName;
   // The endpoint interface
   private Class seiClass;
   // The optional authentication method
   private String authMethod;
   // Arbitrary properties given by <call-property>
   private Properties properties;
   // The SOAPBinding style
   private Style style;
   // The SOAPBinding use
   private Use use;
   // The SOAPBinding parameter style
   private ParameterStyle parameterStyle;
   // The JAXWS ServiceMode
   private Mode serviceMode;
   // The list of service meta data
   private List<OperationMetaData> operations = new ArrayList<OperationMetaData>();
   // Maps the java method to the operation meta data
   private Map<Method, OperationMetaData> opMetaDataCache = new HashMap<Method, OperationMetaData>();
   // The features defined for this endpoint
   private FeatureSet features = new FeatureSet();

   private ConfigObservable configObservable = new ConfigObservable();

   private List<UnifiedPortComponentRefMetaData> serviceRefContrib = new ArrayList<UnifiedPortComponentRefMetaData>();

   EndpointMetaData()
   {
   }

   public EndpointMetaData(ServiceMetaData service, QName portName, QName portTypeName)
   {
      this.serviceMetaData = service;
      this.portName = portName;
      this.portTypeName = portTypeName;
      this.bindingId = Constants.SOAP11HTTP_BINDING;
   }

   public ServiceMetaData getServiceMetaData()
   {
      return serviceMetaData;
   }

   public QName getPortName()
   {
      return portName;
   }

   public void setPortName(QName portName)
   {
      this.portName = portName;
   }

   public QName getPortTypeName()
   {
      return portTypeName;
   }

   public abstract String getEndpointAddress();

   public abstract void setEndpointAddress(String endpointAddress);

   public String getBindingId()
   {
      return bindingId;
   }

   public void setBindingId(String bindingId)
   {
      if (SUPPORTED_BINDINGS.contains(bindingId) == false)
         throw new WSException(BundleUtils.getMessage(bundle, "UNSUPPORTED_BINDING",  bindingId));

      this.bindingId = bindingId;
   }

   public String getServiceEndpointInterfaceName()
   {
      return seiName;
   }

   public void setServiceEndpointInterfaceName(String seiName)
   {
      this.seiName = seiName;
      this.seiClass = null;

      UnifiedMetaData wsMetaData = serviceMetaData.getUnifiedMetaData();
      if (wsMetaData.isEagerInitialized())
      {
         if (UnifiedMetaData.isFinalRelease() == false)
            log.warn(BundleUtils.getMessage(bundle, "SET_SEI_NAME_AFTER_EAGER_INIT"));

         // reinitialize
         initializeInternal();
      }
   }

   /** Get the class loader associated with the endpoint meta data */
   public ClassLoader getClassLoader()
   {
      return getServiceMetaData().getUnifiedMetaData().getClassLoader();
   }

   /**
    * Load the service endpoint interface.
    * It should only be cached during eager initialization.
    */
   public Class getServiceEndpointInterface()
   {
      Class tmpClass = seiClass;
      if (tmpClass == null && seiName != null)
      {
         try
         {
            ClassLoader classLoader = getClassLoader();
            tmpClass = classLoader.loadClass(seiName);
            if (serviceMetaData.getUnifiedMetaData().isEagerInitialized())
            {
               log.warn(BundleUtils.getMessage(bundle, "LOADING_SEI_AFTER_EAGER_INIT"));
               seiClass = tmpClass;
            }
         }
         catch (ClassNotFoundException ex)
         {
            throw new WSException(BundleUtils.getMessage(bundle, "CANNOT_LOAD_SEI",  seiName),  ex);
         }
      }
      return tmpClass;
   }

   public Use getEncodingStyle()
   {
      if (use == null)
      {
         use = Use.getDefaultUse();
         if (log.isDebugEnabled())
            log.debug("Using default encoding style: " + use);
      }
      return use;
   }

   public void setEncodingStyle(Use value)
   {
      if (value != null && use != null && !use.equals(value))
         throw new WSException(BundleUtils.getMessage(bundle, "MIXED_STYLES_NOT_SUPPORTED"));

      log.trace("setEncodingStyle: " + value);
      this.use = value;
   }

   public Style getStyle()
   {
      if (style == null)
      {
         style = Style.getDefaultStyle();
         if (log.isDebugEnabled())
            log.debug("Using default style: " + style);
      }
      return style;
   }

   public void setStyle(Style value)
   {
      if (value != null && style != null && !style.equals(value))
         throw new WSException(BundleUtils.getMessage(bundle, "MIXED_STYLES_NOT_SUPPORTED"));

      if (log.isTraceEnabled())
         log.trace("setStyle: " + value);
      this.style = value;
   }

   public ParameterStyle getParameterStyle()
   {
      if (parameterStyle == null)
      {
         parameterStyle = ParameterStyle.WRAPPED;
         if (log.isDebugEnabled())
            log.debug("Using default parameter style: " + parameterStyle);
      }
      return parameterStyle;
   }

   public void setParameterStyle(ParameterStyle value)
   {
      if (value != null && parameterStyle != null && !parameterStyle.equals(value))
         throw new WSException(BundleUtils.getMessage(bundle, "MIXED_SOAP_PARAMETER_STYLES_NOT_SUPPORTED"));

      if (log.isDebugEnabled())
         log.debug("setParameterStyle: " + value);
      this.parameterStyle = value;
   }

   public Mode getServiceMode()
   {
      return serviceMode;
   }

   public void setServiceMode(Mode serviceMode)
   {
      this.serviceMode = serviceMode;
   }

   public String getAuthMethod()
   {
      return authMethod;
   }

   public void setAuthMethod(String authMethod)
   {
      this.authMethod = authMethod;
   }

   public Properties getProperties()
   {
      if (null == this.properties)
         this.properties = new Properties();
      return this.properties;
   }

   public void setProperties(Properties properties)
   {
      this.properties = properties;
   }

   public <T extends WebServiceFeature> T getFeature(Class<T> key)
   {
      return features.getFeature(key);
   }

   public <T extends WebServiceFeature> boolean isFeatureEnabled(Class<T> key)
   {
      return features.isFeatureEnabled(key);
   }

   public FeatureSet getFeatures()
   {
      return features;
   }

   public void addFeature(WebServiceFeature feature)
   {
      this.features.addFeature(feature);
   }

   public List<OperationMetaData> getOperations()
   {
      return new ArrayList<OperationMetaData>(operations);
   }

   public OperationMetaData getOperation(QName xmlName)
   {
      OperationMetaData opMetaData = null;
      for (OperationMetaData aux : operations)
      {
         QName opQName = aux.getQName();
         String javaName = aux.getJavaName();
         if (opQName.equals(xmlName) && !javaName.endsWith(Constants.ASYNC_METHOD_SUFFIX))
         {
            if (opMetaData == null)
            {
               opMetaData = aux;
            }
            else
            {
               throw new WSException(BundleUtils.getMessage(bundle, "CANNOT_UNIQUELY_INDENTIFY_OP",  xmlName));
            }
         }
      }

      if (opMetaData == null && getStyle() == Style.DOCUMENT)
      {
         for (OperationMetaData auxOperation : operations)
         {
            ParameterMetaData paramMetaData = null;
            for (ParameterMetaData auxParam : auxOperation.getParameters())
            {
               ParameterMode mode = auxParam.getMode();
               if (!auxParam.isInHeader() && mode != ParameterMode.OUT)
               {
                  paramMetaData = auxParam;
                  break;
               }
            }
            if (paramMetaData != null && paramMetaData.getXmlName().equals(xmlName))
            {
               if (opMetaData == null)
               {
                  opMetaData = auxOperation;
               }
               else
               {
                  throw new WSException(BundleUtils.getMessage(bundle, "CANNOT_UNIQUELY_INDENTIFY_OP",  xmlName));
               }
            }
         }
      }

      return opMetaData;
   }

   public OperationMetaData getOperation(Method method)
   {
      if (opMetaDataCache.size() == 0)
      {
         // This can happen when the SEI mapping was not found
         log.warn(BundleUtils.getMessage(bundle, "ACCESS_TO_EMPTY_OP_META_DATA_CACHE"));
         initializeInternal();
      }

      OperationMetaData opMetaData = opMetaDataCache.get(method);
      if (opMetaData == null)
      {
         for (OperationMetaData aux : operations)
         {
            boolean doesMatch = aux.getJavaMethod().equals(method);

            // fallback for async methods
            if (!doesMatch && method.getName().endsWith(Constants.ASYNC_METHOD_SUFFIX))
            {
               String name = method.getName();
               name = name.substring(0, name.length() - 5);
               doesMatch = aux.getJavaName().equals(name);
            }

            if (doesMatch)
            {
               opMetaDataCache.put(method, aux);
               opMetaData = aux;
               break;
            }

         }
      }
      return opMetaData;
   }

   public void addOperation(OperationMetaData opMetaData)
   {
      operations.add(opMetaData);
   }

   public void clearOperations()
   {
      operations.clear();
   }

   public void addHandlers(List<HandlerMetaData> configHandlers)
   {
      getEndpointConfigMetaData().addHandlers(configHandlers);
   }

   public void addHandler(HandlerMetaData handler)
   {
      getEndpointConfigMetaData().addHandler(handler);
   }

   public void clearHandlers()
   {
      getEndpointConfigMetaData().clearHandlers();
   }

   public List<HandlerMetaData> getHandlerMetaData(HandlerType type)
   {
      return getEndpointConfigMetaData().getHandlerMetaData(type);
   }

   public boolean isHandlersInitialized()
   {
      return getEndpointConfigMetaData().isHandlersInitialized();
   }

   public void setHandlersInitialized(boolean flag)
   {
      getEndpointConfigMetaData().setHandlersInitialized(flag);
   }

   public void validate()
   {
      for (OperationMetaData opMetaData : operations)
         opMetaData.validate();
   }

   /**
    * @see UnifiedMetaData#eagerInitialize()
    */
   public void eagerInitialize()
   {
      initializeInternal();
   }

   private void initializeInternal()
   {
      // reset sei class
      seiClass = null;

      getEndpointConfigMetaData().initializeInternal();

      eagerInitializeOperations();
      eagerInitializeTypes();
      eagerInitializeAccessors();
   }

   private void eagerInitializeOperations()
   {
      seiClass = getServiceEndpointInterface();
      if (seiClass != null)
      {
         List<Method> unsynchronizedMethods = new ArrayList<Method>();
         unsynchronizedMethods.addAll(Arrays.asList(seiClass.getMethods()));

         for (OperationMetaData opMetaData : operations)
         {
            opMetaData.eagerInitialize(unsynchronizedMethods);
            Method method = opMetaData.getJavaMethod();
            if (method != null)
            {
               opMetaDataCache.put(method, opMetaData);
               unsynchronizedMethods.remove(method);
            }
         }
      }
   }

   private void eagerInitializeTypes()
   {
      TypeMappingImpl typeMapping = serviceMetaData.getTypeMapping();
      List<TypeMappingMetaData> typeMappings = serviceMetaData.getTypesMetaData().getTypeMappings();
      for (TypeMappingMetaData tmMetaData : typeMappings)
      {
         String javaTypeName = tmMetaData.getJavaTypeName();
         QName xmlType = tmMetaData.getXmlType();
         if (xmlType != null)
         {
            List<Class> types = typeMapping.getJavaTypes(xmlType);

            try
            {
               ClassLoader classLoader = getClassLoader();
               Class javaType = JavaUtils.loadJavaType(javaTypeName, classLoader);

               if (JavaUtils.isPrimitive(javaTypeName))
                  javaType = JavaUtils.getWrapperType(javaType);

               if (getEncodingStyle() == Use.ENCODED && javaType.isArray())
               {
                  typeMapping.register(javaType, xmlType, new SOAPArraySerializerFactory(), new SOAPArrayDeserializerFactory());
               }
               else
               {
                  typeMapping.register(javaType, xmlType, new JBossXBSerializerFactory(), new JBossXBDeserializerFactory());
               }
            }
            catch (ClassNotFoundException e)
            {
               log.warn(BundleUtils.getMessage(bundle, "CANNOT_LOAD_CLASS", new Object[]{ xmlType,  javaTypeName}));
            }
         }
      }
   }

   private void eagerInitializeAccessors()
   {
      // Collect the list of all used types
      List<Class> types = new ArrayList<Class>();
      for (OperationMetaData opMetaData : operations)
      {
         for (ParameterMetaData paramMetaData : opMetaData.getParameters())
         {
            types.add(paramMetaData.getJavaType());
         }

         ParameterMetaData retParam = opMetaData.getReturnParameter();
         if (retParam != null)
         {
            types.add(retParam.getJavaType());
         }
      }

      // Create the accessors using a shared JAXBContext 
      for (OperationMetaData opMetaData : operations)
      {
         for (ParameterMetaData paramMetaData : opMetaData.getParameters())
         {
            createAccessor(paramMetaData);
         }

         ParameterMetaData retParam = opMetaData.getReturnParameter();
         if (retParam != null)
            createAccessor(retParam);
      }

   }

   private void createAccessor(ParameterMetaData paramMetaData)
   {
      AccessorFactoryCreator factoryCreator = paramMetaData.getAccessorFactoryCreator();
      if (paramMetaData.getWrappedParameters() != null)
      {
         AccessorFactory factory = factoryCreator.create(paramMetaData);
         for (WrappedParameter wParam : paramMetaData.getWrappedParameters())
            wParam.setAccessor(factory.create(wParam));
      }
   }

   // ---------------------------------------------------------------
   // Configuration provider impl

   /**
    * Callback for components that require configuration through jbossws-dd
    */
   public void configure(Configurable configurable)
   {
      CommonConfig config = getConfig();
      // TODO: remove this method
   }

   public UnifiedVirtualFile getRootFile()
   {
      return getServiceMetaData().getUnifiedMetaData().getRootFile();
   }

   public void registerConfigObserver(Configurable observer)
   {
      configObservable.addObserver(observer);
   }

   public String getConfigFile()
   {
      return getEndpointConfigMetaData().getConfigFile();
   }

   public String getConfigName()
   {
      return getEndpointConfigMetaData().getConfigName();
   }

   public EndpointConfigMetaData getEndpointConfigMetaData()
   {
      if (configMetaData == null)
         configMetaData = new EndpointConfigMetaData(this);

      return this.configMetaData;
   }

   public CommonConfig getConfig()
   {
      EndpointConfigMetaData ecmd = getEndpointConfigMetaData();
      CommonConfig config = ecmd.getConfig();

      // Make sure we have a configuration
      if (config == null)
      {
         // No base configuration. 
         initEndpointConfigMetaData(ecmd);
         config = ecmd.getConfig();
      }

      return config;
   }

   public void setConfigName(String configName)
   {
      setConfigNameInternal(configName, null);
   }

   public void setConfigName(String configName, String configFile)
   {
      setConfigNameInternal(configName, configFile);
   }

   private void setConfigNameInternal(String configName, String configFile)
   {
      if (configName == null)
         throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "CONFIG_NAME_CANNOT_BE_NULL"));

      if (configFile == null)
      {
         configFile = getEndpointConfigMetaData().getConfigFile();
      }

      if (configName.equals(getEndpointConfigMetaData().getConfigName()) == false || configFile.equals(getEndpointConfigMetaData().getConfigFile()) == false)
      {
         if (log.isDebugEnabled())
            log.debug("Reconfiguration forced, new config is '" + configName + "' file is '" + configFile + "'");

         this.configMetaData = createEndpointConfigMetaData(configName, configFile);
         configObservable.doNotify(configName);
      }
   }

   /**
    * The factory method to create and initialise a new EndpointConfigMetaData, the current 
    * EndpointConfigMetaData will be used as the base to backup the RMMD.
    * 
    * This method does not set the EndpointConfigMetaData as it can be used by clients to create 
    * a local configuration not stored in the EndpointMetaData.
    */
   protected EndpointConfigMetaData createEndpointConfigMetaData(String configName, String configFile)
   {
      EndpointConfigMetaData ecmd = new EndpointConfigMetaData(this);
      ecmd.setConfigName(configName);
      ecmd.setConfigFile(configFile);

      initEndpointConfigMetaData(ecmd);

      return ecmd;
   }

   public void initEndpointConfig()
   {
      EndpointConfigMetaData ecmd = getEndpointConfigMetaData();
      initEndpointConfigMetaData(ecmd);
   }

   /**
    * Initialise the EndpointConfigMeta.
    * 
    * @param toInitialise - The EndpointConfigMetaData to initialise.
    */
   private void initEndpointConfigMetaData(EndpointConfigMetaData toInitialise)
   {
      String configName = toInitialise.getConfigName();
      String configFile = toInitialise.getConfigFile();

      if (log.isDebugEnabled())
         log.debug("Create new config [name=" + configName + ",file=" + configFile + "]");

      JBossWSConfigFactory factory = JBossWSConfigFactory.newInstance(getClassLoader());
      CommonConfig config = factory.getConfig(getRootFile(), configName, configFile);
      toInitialise.setConfig(config);

      toInitialise.configHandlerMetaData();
   }

   class ConfigObservable extends Observable
   {

      private ReferenceQueue<WeakReference<Observer>> queue = new ReferenceQueue<WeakReference<Observer>>();
      private List<WeakReference<Observer>> observer = new ArrayList<WeakReference<Observer>>();

      public void doNotify(Object object)
      {
         setChanged();
         notifyObservers(object);
      }

      public synchronized void addObserver(Observer o)
      {
         clearCollected();
         observer.add(new WeakReference(o, queue));
      }

      public synchronized void deleteObserver(Observer o)
      {
         clearCollected();
         for (WeakReference<Observer> w : observer)
         {
            Observer tmp = w.get();
            if (tmp != null && tmp.equals(o))
            {
               observer.remove(o);
               break;
            }

         }
      }

      public void notifyObservers()
      {
         notifyObservers(null);
      }

      public void notifyObservers(Object arg)
      {
         clearCollected();
         if (hasChanged())
         {
            for (WeakReference<Observer> w : observer)
            {
               Observer tmp = w.get();
               if (tmp != null)
               {
                  tmp.update(this, arg);
               }
            }
         }
      }

      private void clearCollected()
      {
         Reference ref;
         while ((ref = queue.poll()) != null)
         {
            observer.remove(ref);
         }

      }
   }

   public List<UnifiedPortComponentRefMetaData> getServiceRefContrib()
   {
      return serviceRefContrib;
   }

   public boolean matches(UnifiedPortComponentRefMetaData pcRef)
   {
      String seiName = pcRef.getServiceEndpointInterface();
      QName portName = pcRef.getPortQName();

      boolean match;
      if (seiName != null && portName != null)
      {
         match = getServiceEndpointInterfaceName().equals(seiName) && getPortName().equals(portName);
      }
      else
      {
         match = getServiceEndpointInterfaceName().equals(seiName) || getPortName().equals(portName);
      }
      return match;
   }
}
