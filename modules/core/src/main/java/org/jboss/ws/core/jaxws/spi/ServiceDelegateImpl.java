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
package org.jboss.ws.core.jaxws.spi;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.spi.ServiceDelegate;

import org.jboss.logging.Logger;
import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.common.ResourceLoaderAdapter;
import org.jboss.ws.common.utils.DelegateClassLoader;
import org.jboss.ws.core.StubExt;
import org.jboss.ws.core.jaxws.client.ClientImpl;
import org.jboss.ws.core.jaxws.client.ClientProxy;
import org.jboss.ws.core.jaxws.client.DispatchImpl;
import org.jboss.ws.core.jaxws.client.serviceref.NativeServiceObjectFactoryJAXWS;
import org.jboss.ws.core.jaxws.handler.HandlerResolverImpl;
import org.jboss.ws.metadata.builder.jaxws.JAXWSClientMetaDataBuilder;
import org.jboss.ws.metadata.builder.jaxws.JAXWSMetaDataBuilder;
import org.jboss.ws.metadata.umdm.ClientEndpointMetaData;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.EndpointMetaData.Type;
import org.jboss.ws.metadata.umdm.FeatureAwareClientEndpointMetaDataAdapter;
import org.jboss.ws.metadata.umdm.FeatureAwareEndpointMetaData;
import org.jboss.ws.metadata.umdm.HandlerMetaDataJAXWS;
import org.jboss.ws.metadata.umdm.ServiceMetaData;
import org.jboss.ws.metadata.umdm.UnifiedMetaData;
import org.jboss.ws.metadata.wsdl.WSDLUtils;
import org.jboss.wsf.spi.deployment.UnifiedVirtualFile;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerChainMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerChainsMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerMetaData.HandlerType;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedPortComponentRefMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedServiceRefMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedStubPropertyMetaData;

/**
 * Service delegates are used internally by Service objects to allow pluggability of JAX-WS implementations.
 *
 * Every Service object has its own delegate, created using the javax.xml.ws.Provider#createServiceDelegate method.
 * A Service object delegates all of its instance methods to its delegate.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 03-May-2006
 */
public class ServiceDelegateImpl extends ServiceDelegate
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(ServiceDelegateImpl.class);
   // provide logging
   private static final Logger log = Logger.getLogger(ServiceDelegateImpl.class);

   // Lock to ensure only one thread can initialise the defaultExecutor.
   private static final Object DEFAULT_EXECUTOR_LOCK = new Object();
   // The executor service
   private static ExecutorService defaultExecutor = null;
         
   // The service meta data that is associated with this JAXWS Service
   private ServiceMetaData serviceMetaData;
   // The ServiceRefMetaData supplied by the ServiceObjectFactory 
   private UnifiedServiceRefMetaData usRef;
   // The handler resolver
   private HandlerResolver handlerResolver;
   // The executor service
   private ExecutorService executor;
   // The features
   private WebServiceFeature[] features;

   // A list of annotated ports
   private List<QName> annotatedPorts = new ArrayList<QName>();

   public ServiceDelegateImpl(URL wsdlURL, QName serviceName, Class serviceClass, WebServiceFeature[] features)
   {      
      this(wsdlURL, serviceName, serviceClass);
      this.features = features;      
   }

   public ServiceDelegateImpl(URL wsdlURL, QName serviceName, Class serviceClass)
   {
      // If this Service was constructed through the ServiceObjectFactory
      // this thread local association should be available
      usRef = NativeServiceObjectFactoryJAXWS.getServiceRefAssociation();
      UnifiedVirtualFile vfsRoot = (usRef != null ? vfsRoot = usRef.getVfsRoot() : new ResourceLoaderAdapter());

      // Verify wsdl access if this is not a generic Service
      if (wsdlURL != null && serviceClass != Service.class)
      {
         try
         {
            InputStream is = wsdlURL.openStream();
            is.close();
         }
         catch (IOException e)
         {
            log.warn(BundleUtils.getMessage(bundle, "CANNOT_ACCESS_WSDLURL",  wsdlURL));
            wsdlURL = null;
         }
      }

      if (wsdlURL != null)
      {
         JAXWSClientMetaDataBuilder builder = new JAXWSClientMetaDataBuilder();
         serviceMetaData = builder.buildMetaData(serviceName, wsdlURL, vfsRoot);
      }
      else
      {
         ClassLoader cl = serviceClass.getClassLoader();
         UnifiedMetaData wsMetaData = cl == null ? new UnifiedMetaData(vfsRoot) : new UnifiedMetaData(vfsRoot, cl);
         serviceMetaData = new ServiceMetaData(wsMetaData, serviceName);
         wsMetaData.addService(serviceMetaData);
      }

      handlerResolver = new HandlerResolverImpl();

      String filename = null;
      if (usRef != null)
      {
         serviceMetaData.setServiceRefName(usRef.getServiceRefName());

         // Setup the service handlers
         if (usRef.getHandlerChain() != null)
         {
            filename = usRef.getHandlerChain();
         }
      }
      
      if (serviceClass != null && serviceClass.getAnnotation(HandlerChain.class) != null)
      {
         HandlerChain anHandlerChain = (HandlerChain)serviceClass.getAnnotation(HandlerChain.class);
         if (anHandlerChain != null && anHandlerChain.file().length() > 0) {
            filename = anHandlerChain.file();
            try
            {
               new URL(filename);
            }
            catch (MalformedURLException ex)
            {
               filename = serviceClass.getPackage().getName().replace('.', '/') + "/" + filename;
            }         
         }     
      }
      if (filename != null) {
         UnifiedHandlerChainsMetaData handlerChainsMetaData = JAXWSMetaDataBuilder.getHandlerChainsMetaData(serviceClass, filename);
         for (UnifiedHandlerChainMetaData UnifiedHandlerChainMetaData : handlerChainsMetaData.getHandlerChains())
         {
            for (UnifiedHandlerMetaData uhmd : UnifiedHandlerChainMetaData.getHandlers())
            {
               HandlerMetaDataJAXWS hmd = HandlerMetaDataJAXWS.newInstance(uhmd, HandlerType.ENDPOINT);
               serviceMetaData.addHandler(hmd);
            }
         }
         ((HandlerResolverImpl)handlerResolver).initServiceHandlerChain(serviceMetaData);
      }
   }

   private <T> QName getPortTypeName(Class<T> seiClass)
   {
      if (!seiClass.isAnnotationPresent(WebService.class))
         throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "CANNOT_FIND_WEBSERVICE",  seiClass.getName()));

      WebService anWebService = seiClass.getAnnotation(WebService.class);
      String localPart = anWebService.name();
      if (localPart.length() == 0)
         localPart = WSDLUtils.getJustClassName(seiClass);

      String nsURI = anWebService.targetNamespace();
      if (nsURI.length() == 0)
         nsURI = WSDLUtils.getTypeNamespace(seiClass);

      QName portType = new QName(nsURI, localPart);
      return portType;
   }

   private <T> T getPortInternal(EndpointMetaData epMetaData, Class<T> seiClass)
   {
      QName portName = epMetaData.getPortName();

      // Adjust the endpoint meta data according to the annotations
      if (annotatedPorts.contains(portName) == false)
      {
         synchronized (epMetaData)
         {
            if (annotatedPorts.contains(portName) == false)
            {
               JAXWSClientMetaDataBuilder metaDataBuilder = new JAXWSClientMetaDataBuilder();
               metaDataBuilder.rebuildEndpointMetaData(epMetaData, seiClass);
               annotatedPorts.add(portName);
            }
         }
      }

      return (T)createProxy(seiClass, epMetaData);
   }

   private void assertSEIConstraints(Class seiClass)
   {
      if (seiClass == null)
         throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "SEI_CANNOT_BE_NULL"));

      if (!seiClass.isAnnotationPresent(WebService.class))
         throw new WebServiceException(BundleUtils.getMessage(bundle, "MISSING_WEBSERVICE_ANNO",  seiClass));
   }

   @Override
   /**
    * Creates a new port for the service.
    * Ports created in this way contain no WSDL port type information
    * and can only be used for creating Dispatchinstances.
    */
   public void addPort(QName portName, String bindingId, String epAddress)
   {
      EndpointMetaData epMetaData = serviceMetaData.getEndpoint(portName);
      if (epMetaData == null)
      {
         epMetaData = new ClientEndpointMetaData(serviceMetaData, portName, null, Type.JAXWS);
         serviceMetaData.addEndpoint(epMetaData);
      }
      epMetaData.setBindingId(bindingId);
      epMetaData.setEndpointAddress(epAddress);
   }

   @Override
   public <T> Dispatch<T> createDispatch(QName portName, Class<T> type, Mode mode)
   {
      ExecutorService executor = (ExecutorService)getExecutor();
      EndpointMetaData epMetaData = getEndpointMetaData(portName);
      FeatureAwareClientEndpointMetaDataAdapter clientMetaDataAdapter = new FeatureAwareClientEndpointMetaDataAdapter((ClientEndpointMetaData)epMetaData);

      return new DispatchImpl(executor, clientMetaDataAdapter, type, mode);
   }

   @Override
   public Dispatch<Object> createDispatch(QName portName, JAXBContext jbc, Mode mode)
   {
      ExecutorService executor = (ExecutorService)getExecutor();
      EndpointMetaData epMetaData = getEndpointMetaData(portName);
      FeatureAwareClientEndpointMetaDataAdapter clientMetaDataAdapter = new FeatureAwareClientEndpointMetaDataAdapter((ClientEndpointMetaData)epMetaData);

      return new DispatchImpl(executor, clientMetaDataAdapter, jbc, mode);
   }

   private EndpointMetaData getEndpointMetaData(QName portName)
   {
      EndpointMetaData epMetaData = serviceMetaData.getEndpoint(portName);
      if (epMetaData == null)
         throw new WebServiceException(BundleUtils.getMessage(bundle, "CANNOT_FIND_PORT",  portName));

      return epMetaData;
   }

   /** Gets the name of this service. */
   @Override
   public QName getServiceName()
   {
      return serviceMetaData.getServiceName();
   }

   /** Returns an Iterator for the list of QNames of service endpoints grouped by this service */
   @Override
   public Iterator<QName> getPorts()
   {
      ArrayList<QName> portNames = new ArrayList<QName>();
      for (EndpointMetaData epMetaData : serviceMetaData.getEndpoints())
      {
         portNames.add(epMetaData.getPortName());
      }
      return portNames.iterator();
   }

   @Override
   public URL getWSDLDocumentLocation()
   {
      return serviceMetaData.getWsdlLocation();
   }

   @Override
   public HandlerResolver getHandlerResolver()
   {
      return handlerResolver;
   }

   @Override
   public void setHandlerResolver(HandlerResolver handlerResolver)
   {
      this.handlerResolver = handlerResolver;
   }

   private ExecutorService getDefaultExecutor()
   {
      if (defaultExecutor == null)
      {
         synchronized (DEFAULT_EXECUTOR_LOCK)
         {
            if (defaultExecutor == null)
            {
               defaultExecutor = Executors.newCachedThreadPool();
               if (log.isTraceEnabled())
               {
                  log.trace("Created new defaultExecutor", new Throwable("Call Trace"));
               }
            }
         }
      }

      return defaultExecutor;
   }
   
   @Override
   public Executor getExecutor()
   {
      if (executor == null)
      {
         executor = getDefaultExecutor();
      }
      return executor;
   }

   @Override
   public void setExecutor(Executor executor)
   {
      if ((executor instanceof ExecutorService) == false)
         throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "EXECUTORS_MUST_IMPLEMENT",  ExecutorService.class.getName()));

      this.executor = (ExecutorService)executor;
   }

   private <T> T createProxy(Class<T> seiClass, EndpointMetaData epMetaData) throws WebServiceException
   {
      if (seiClass == null)
         throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "SEI_CLASS_CANNOT_BE_NULL"));

      try
      {
         ExecutorService executor = (ExecutorService)getExecutor();
         FeatureAwareClientEndpointMetaDataAdapter clientMetaDataAdapter = new FeatureAwareClientEndpointMetaDataAdapter((ClientEndpointMetaData)epMetaData);
         ClientProxy handler = new ClientProxy(executor, new ClientImpl(clientMetaDataAdapter, handlerResolver));
         ClassLoader cl = epMetaData.getClassLoader();
         try
         {
            cl.loadClass(ProviderImpl.class.getName());
         }
         catch (Exception e)
         {
            ClassLoader clientCl = ProviderImpl.class.getClassLoader();
            cl = new DelegateClassLoader(clientCl, cl);
         }

         T proxy;
         try
         {
            proxy = (T)Proxy.newProxyInstance(cl, new Class[] { seiClass, BindingProvider.class, StubExt.class, FeatureAwareEndpointMetaData.class }, handler);
         }
         catch (RuntimeException rte)
         {
            URL codeLocation = seiClass.getProtectionDomain().getCodeSource().getLocation();
            log.error(BundleUtils.getMessage(bundle, "CANNOT_CREATE_PROXY_FOR_SEI", new Object[]{ seiClass.getName() ,  codeLocation}));
            throw rte;
         }

         // Configure the stub
         configureStub((StubExt)proxy);

         return proxy;
      }
      catch (WebServiceException ex)
      {
         throw ex;
      }
      catch (Exception ex)
      {
         throw new WebServiceException(BundleUtils.getMessage(bundle, "CANNOT_CREATE_PROXY"),  ex);
      }
   }
   
   

   private void configureStub(StubExt stub)
   {
      EndpointMetaData epMetaData = stub.getEndpointMetaData();
      String seiName = epMetaData.getServiceEndpointInterfaceName();
      QName portName = epMetaData.getPortName();

      if (usRef == null)
      {
         log.debugf("No port configuration for: %s", portName);
         return;
      }

      String configFile = usRef.getConfigFile();
      String configName = usRef.getConfigName();

      UnifiedPortComponentRefMetaData pcref = usRef.getPortComponentRef(seiName, portName);
      if (pcref != null)
      {
         if (pcref.getConfigFile() != null)
            configFile = pcref.getConfigFile();
         if (pcref.getConfigName() != null)
            configName = pcref.getConfigName();

         BindingProvider bp = (BindingProvider)stub;
         Map<String, Object> reqCtx = bp.getRequestContext();
         for (UnifiedStubPropertyMetaData prop : pcref.getStubProperties())
         {
            log.debugf("Set stub property: %s", prop);
            reqCtx.put(prop.getPropName(), prop.getPropValue());
         }
      }

      if (configName != null || configFile != null)
      {
         log.debugf("Configure Stub: [configName=%s,configFile=%s]", configName, configFile);
         stub.setConfigName(configName, configFile);
      }
   }

   @Override
   public <T> Dispatch<T> createDispatch(QName portName, Class<T> type, Mode mode, WebServiceFeature... features)
   {
      return createDispatch(portName, type, mode);
   }

   @Override
   public <T> Dispatch<T> createDispatch(EndpointReference epr, Class<T> type, Mode mode, WebServiceFeature... features)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public Dispatch<Object> createDispatch(QName portName, JAXBContext context, Mode mode, WebServiceFeature... features)
   {
      return createDispatch(portName, context, mode);
   }

   @Override
   public Dispatch<Object> createDispatch(EndpointReference epr, JAXBContext context, Mode mode, WebServiceFeature... features)
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public <T> T getPort(QName portName, Class<T> sei, WebServiceFeature... features)
   {
      return getPort(portName, sei);
   }

   @Override
   public <T> T getPort(EndpointReference epr, Class<T> sei, WebServiceFeature... features)
   {
      return getPort(sei);
   }

   @Override
   public <T> T getPort(Class<T> sei, WebServiceFeature... features)
   {
      return getPort(sei);
   }

   /**
    * The getPort method returns a stub. A service client uses this stub to invoke operations on the target service endpoint.
    * The serviceEndpointInterface specifies the service endpoint interface that is supported by the created dynamic proxy or stub instance.
    */
   @Override
   public <T> T getPort(QName portName, Class<T> seiClass)
   {
      assertSEIConstraints(seiClass);

      if (serviceMetaData == null)
         throw new WebServiceException(BundleUtils.getMessage(bundle, "SERVICE_META_DATA_NOT_AVAILABLE"));

      // com/sun/ts/tests/jaxws/api/javax_xml_ws/Service#GetPort1NegTest1WithWsdl
      EndpointMetaData epMetaData = serviceMetaData.getEndpoint(portName);
      if (epMetaData == null && serviceMetaData.getEndpoints().size() > 0)
         throw new WebServiceException(BundleUtils.getMessage(bundle, "CANNOT_GET_PORT_META_DATA",  portName));

      // This is the case when the service could not be created from wsdl
      if (serviceMetaData.getEndpoints().size() == 0)
      {
         log.warn(BundleUtils.getMessage(bundle, "CANNOT_GET_PORT_META_DATA",  portName));

         QName portType = getPortTypeName(seiClass);
         epMetaData = new ClientEndpointMetaData(serviceMetaData, portName, portType, Type.JAXWS);
      }
      
      String seiClassName = seiClass.getName();
      epMetaData.setServiceEndpointInterfaceName(seiClassName);

      return getPortInternal(epMetaData, seiClass);
   }

   /**
    * The getPort method returns a stub. A service client uses this stub to invoke operations on the target service endpoint.
    * The serviceEndpointInterface specifies the service endpoint interface that is supported by the created dynamic proxy or stub instance.
    */
   @Override
   public <T> T getPort(Class<T> seiClass)
   {
      assertSEIConstraints(seiClass);

      if (serviceMetaData == null)
         throw new WebServiceException(BundleUtils.getMessage(bundle, "SERVICE_META_DATA_NOT_AVAILABLE"));

      String seiClassName = seiClass.getName();
      EndpointMetaData epMetaData = serviceMetaData.getEndpointByServiceEndpointInterface(seiClassName);

      if (epMetaData == null && serviceMetaData.getEndpoints().size() == 1)
      {
         epMetaData = serviceMetaData.getEndpoints().get(0);
         epMetaData.setServiceEndpointInterfaceName(seiClassName);
      }
      else
      {
         QName portTypeName = getPortTypeName(seiClass);
         for (EndpointMetaData epmd : serviceMetaData.getEndpoints())
         {
            if (portTypeName.equals(epmd.getPortTypeName()))
            {
               epmd.setServiceEndpointInterfaceName(seiClass.getName());
               epMetaData = epmd;
               break;
            }
         }
      }

      if (epMetaData == null)
         throw new WebServiceException(BundleUtils.getMessage(bundle, "CANNOT_GET_PORT_META_DATA",  seiClassName));

      return getPortInternal(epMetaData, seiClass);
   }
}
