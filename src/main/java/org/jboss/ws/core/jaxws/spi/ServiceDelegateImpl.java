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
package org.jboss.ws.core.jaxws.spi;

// $Id$

import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jws.WebService;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.spi.ServiceDelegate;

import org.jboss.logging.Logger;
import org.jboss.util.NotImplementedException;
import org.jboss.ws.Constants;
import org.jboss.ws.core.jaxws.StubExt;
import org.jboss.ws.core.jaxws.client.ClientImpl;
import org.jboss.ws.core.jaxws.client.ClientProxy;
import org.jboss.ws.core.jaxws.client.DispatchImpl;
import org.jboss.ws.core.jaxws.handler.HandlerResolverImpl;
import org.jboss.ws.metadata.builder.jaxws.JAXWSClientMetaDataBuilder;
import org.jboss.ws.metadata.umdm.ClientEndpointMetaData;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.ServiceMetaData;
import org.jboss.ws.metadata.umdm.EndpointMetaData.Type;
import org.jboss.ws.metadata.umdm.HandlerMetaData.HandlerType;

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
   // provide logging
   private final Logger log = Logger.getLogger(ServiceDelegateImpl.class);

   // The executor service
   private static ExecutorService defaultExecutor = Executors.newCachedThreadPool();

   // The service meta data that is associated with this JAXWS Service
   private ServiceMetaData serviceMetaData;
   // The ports known by this service
   private HashMap<QName, Port> ports = new HashMap<QName,Port>();
   // The handler resolver
   private HandlerResolver handlerResolver = new HandlerResolverImpl();
   // The executor service
   private ExecutorService executor;

   // A list of annotated ports
   private List<QName> annotatedPorts = new ArrayList<QName>();

   public ServiceDelegateImpl(URL wsdlURL, QName serviceName)
   {
      JAXWSClientMetaDataBuilder builder = new JAXWSClientMetaDataBuilder();

      serviceMetaData = builder.buildMetaData(serviceName, wsdlURL);

      for (EndpointMetaData epMetaData : serviceMetaData.getEndpoints())
      {
         QName portName = epMetaData.getQName();
         String bindingId = epMetaData.getBindingId();
         String epAddress = epMetaData.getEndpointAddress();
         Port port = new Port (portName, bindingId, epAddress);
         ports.put(portName, port);
      }
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
         throw new WebServiceException("Service meta data not available");

      EndpointMetaData epMetaData = serviceMetaData.getEndpoint(portName);
      if (epMetaData == null)
         throw new WebServiceException("Cannot get port meta data for: " + portName);

      String seiClassName = seiClass.getName();
      epMetaData.setServiceEndpointInterfaceName(seiClassName);

      return getPortInternal(epMetaData, seiClass);
   }

   @Override
   /**
    * The getPort method returns a stub. A service client uses this stub to invoke operations on the target service endpoint.
    * The serviceEndpointInterface specifies the service endpoint interface that is supported by the created dynamic proxy or stub instance.
    */
   public <T> T getPort(Class<T> seiClass)
   {
      assertSEIConstraints(seiClass);

      if (serviceMetaData == null)
         throw new WebServiceException("Service meta data not available");

      String seiClassName = seiClass.getName();
      EndpointMetaData epMetaData = serviceMetaData.getEndpointByServiceEndpointInterface(seiClassName);

      if (epMetaData == null && serviceMetaData.getEndpoints().size() == 1)
      {
         epMetaData = serviceMetaData.getEndpoints().get(0);
         epMetaData.setServiceEndpointInterfaceName(seiClassName);
      }
      else
      {
         // resolve PortType by name
         WebService ws = seiClass.getAnnotation(WebService.class);
         String ns = ws.targetNamespace();
         String name = ws.name();

         // TODO: default name mapping when annotations not used
         QName portTypeName = new QName(ns, name);

         for(EndpointMetaData epmd : serviceMetaData.getEndpoints())
         {
            QName interfaceQName = epmd.getInterfaceQName(); // skip namespaces here
            if( interfaceQName.getLocalPart().equals( portTypeName.getLocalPart() ) )
            {
               epmd.setServiceEndpointInterfaceName(seiClass.getName());
               epMetaData = epmd;
               break;
            }
         }
      }

      if (epMetaData == null)
         throw new WebServiceException("Cannot get port meta data for: " + seiClassName);

      return getPortInternal(epMetaData, seiClass);
   }

   private <T> T getPortInternal(EndpointMetaData epMetaData, Class<T> seiClass)
   {
      QName portName = epMetaData.getQName();

      // Adjust the endpoint meta data according to the annotations
      if (annotatedPorts.contains(portName) == false)
      {
         JAXWSClientMetaDataBuilder metaDataBuilder = new JAXWSClientMetaDataBuilder();
         metaDataBuilder.rebuildEndpointMetaData(epMetaData, seiClass);
      }

      return (T)createProxy(seiClass, epMetaData);
   }

   private void assertSEIConstraints(Class seiClass) {

      if (seiClass == null)
         throw new IllegalArgumentException("Service endpoint interface cannot be null");

      if(!seiClass.isAnnotationPresent(WebService.class))
         throw new WebServiceException("SEI is missing @WebService annotation: " + seiClass);
   }

   @Override
   /**
    * Creates a new port for the service.
    * Ports created in this way contain no WSDL port type information
    * and can only be used for creating Dispatchinstances.
    */

   public void addPort(QName portName, String bindingId, String epAddress)
   {
      Port port = new Port (portName, bindingId, epAddress);
      ports.put(portName, port);
   }

   @Override
   public <T> Dispatch<T> createDispatch(QName portName, Class<T> type, Mode mode)
   {
      ExecutorService executor = (ExecutorService)getExecutor();
      EndpointMetaData epMetaData = getEndpointMetaData(portName);
      DispatchImpl dispatch = new DispatchImpl(executor, epMetaData, type, mode);
      return dispatch;
   }

   @Override
   public Dispatch<Object> createDispatch(QName portName, JAXBContext jbc, Mode mode)
   {
      ExecutorService executor = (ExecutorService)getExecutor();
      EndpointMetaData epMetaData = getEndpointMetaData(portName);
      DispatchImpl dispatch = new DispatchImpl(executor, epMetaData, jbc, mode);
      return dispatch;
   }

   private EndpointMetaData getEndpointMetaData(QName portName)
   {
      Port port = ports.get(portName);
      if (port == null)
         throw new WebServiceException("Cannot find port: " + portName);

      // Create an anonymous endpoint
      EndpointMetaData epMetaData = serviceMetaData.getEndpoint(portName);
      if (epMetaData == null)
      {
         epMetaData = new ClientEndpointMetaData(serviceMetaData, new QName(Constants.NS_JBOSSWS_URI, "AnonymousPort"), new QName(Constants.NS_JBOSSWS_URI, "Anonymous"), Type.JAXWS);
         epMetaData.setEndpointAddress(port.getEndpointAddress());
         epMetaData.setBindingId(port.getBindingId());
      }
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
      for (Port port : ports.values())
      {
         portNames.add(port.getQName());
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

   @Override
   public Executor getExecutor()
   {
      if (executor == null)
      {
         executor = defaultExecutor;
      }
      return executor;
   }

   @Override
   public void setExecutor(Executor executor)
   {
      if ((executor instanceof ExecutorService) == false)
         throw new IllegalArgumentException("Supported executors must implement " + ExecutorService.class.getName());

      this.executor = (ExecutorService)executor;
   }

   private <T> T createProxy(Class<T> seiClass, EndpointMetaData epMetaData) throws WebServiceException
   {
      try
      {
         if (handlerResolver instanceof HandlerResolverImpl)
         {
            HandlerResolverImpl impl = ((HandlerResolverImpl)handlerResolver);
            impl.initHandlerChain(epMetaData, HandlerType.PRE);
            impl.initHandlerChain(epMetaData, HandlerType.ENDPOINT);
            impl.initHandlerChain(epMetaData, HandlerType.POST);
         }

         ExecutorService executor = (ExecutorService)getExecutor();
         ClientProxy handler = new ClientProxy(executor, new ClientImpl(epMetaData, handlerResolver));
         ClassLoader cl = epMetaData.getClassLoader();
         T proxy = (T)Proxy.newProxyInstance(cl, new Class[] { seiClass, BindingProvider.class, StubExt.class }, handler);
         return proxy;
      }
      catch (WebServiceException ex)
      {
         throw ex;
      }
      catch (Exception ex)
      {
         throw new WebServiceException("Cannot create proxy", ex);
      }
   }

   private static class Port
   {
      private QName qname;
      private String bindingId;
      private String endpointAddress;

      public Port(QName qname, String bindingId, String endpointAddress)
      {
         this.qname = qname;
         this.bindingId = bindingId;
         this.endpointAddress = endpointAddress;
      }

      public String getBindingId()
      {
         return bindingId;
      }

      public String getEndpointAddress()
      {
         return endpointAddress;
      }

      public QName getQName()
      {
         return qname;
      }
   }

   @Override
   public <T> Dispatch<T> createDispatch(QName portName, Class<T> type, Mode mode, WebServiceFeature... features)
   {
      throw new NotImplementedException();
   }

   @Override
   public <T> Dispatch<T> createDispatch(EndpointReference endpointReference, Class<T> type, Mode mode, WebServiceFeature... features)
   {
      throw new NotImplementedException();
   }

   @Override
   public Dispatch<Object> createDispatch(QName portName, JAXBContext context, Mode mode, WebServiceFeature... features)
   {
      throw new NotImplementedException();
   }

   @Override
   public Dispatch<Object> createDispatch(EndpointReference endpointReference, JAXBContext context, Mode mode, WebServiceFeature... features)
   {
      throw new NotImplementedException();
   }

   @Override
   public <T> T getPort(QName portName, Class<T> serviceEndpointInterface, WebServiceFeature... features)
   {
      throw new NotImplementedException();
   }

   @Override
   public <T> T getPort(EndpointReference endpointReference, Class<T> serviceEndpointInterface, WebServiceFeature... features)
   {
      throw new NotImplementedException();
   }

   @Override
   public <T> T getPort(Class<T> serviceEndpointInterface, WebServiceFeature... features)
   {
      throw new NotImplementedException();
   }
}
