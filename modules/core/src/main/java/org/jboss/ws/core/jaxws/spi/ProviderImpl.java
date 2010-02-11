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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.BindingType;
import javax.xml.ws.Endpoint;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.spi.Invoker;
import javax.xml.ws.spi.Provider;
import javax.xml.ws.spi.ServiceDelegate;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.jboss.logging.Logger;
import org.jboss.ws.core.jaxws.wsaddressing.EndpointReferenceUtil;
import org.jboss.ws.core.jaxws.wsaddressing.NativeEndpointReference;
import org.jboss.wsf.common.DOMUtils;
import org.w3c.dom.Element;

/**
 * Service provider for ServiceDelegate and Endpoint objects.
 *  
 * @author Thomas.Diesler@jboss.com
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 * @see javax.xml.ws.spi.Provider
 */
public final class ProviderImpl extends Provider
{

   private static final Logger log = Logger.getLogger(ProviderImpl.class);

   public ProviderImpl()
   {
   }

   public Endpoint createAndPublishEndpoint(final String address, final Object implementor)
   {
      return this.createAndPublishEndpoint(address, implementor, (WebServiceFeature[])null);
   }

   public Endpoint createAndPublishEndpoint(final String address, final Object implementor, final WebServiceFeature... features)
   {
      final String bindingId = getBindingFromAddress(address);
      final EndpointImpl endpoint = (EndpointImpl)createEndpoint(bindingId, implementor, features);
      endpoint.publish(address);

      return endpoint;
   }

   public Endpoint createEndpoint(final String bindingId, final Object implementor)
   {
      return this.createEndpoint(bindingId, implementor, (WebServiceFeature[])null);
   }

   public Endpoint createEndpoint(final String bindingId, final Object implementor, final WebServiceFeature... features)
   {
      final String nonNullBindingId = this.getBindingId(bindingId, implementor.getClass());
      
      return new EndpointImpl(nonNullBindingId, implementor, features);
   }

   public Endpoint createEndpoint(final String bindingId, final Class<?> implementorClass, final Invoker invoker,
         final WebServiceFeature... features)
   {
      // JAX-WS Endpoint API is broken by design and reveals many implementation details 
      // of JAX-WS RI that are not portable cross different application servers :(
      log.warn("createEndpoint(String,Class,Invoker,WebServiceFeature...) not implemented"); // TODO implement?

      return null;
   }
   
   /*
   public ServiceDelegate createServiceDelegate(final URL wsdlLocation, final QName serviceName, final Class<? extends Service> serviceClass)
   {
      return this.createServiceDelegate(wsdlLocation, serviceName, serviceClass, (WebServiceFeature[])null);
   }
   */

   @SuppressWarnings("unchecked")
   public ServiceDelegate createServiceDelegate(final URL wsdlLocation, final QName serviceName, final Class serviceClass, 
         final WebServiceFeature... features)
   {
      try
      {
         ServiceDelegateImpl delegate = new ServiceDelegateImpl(wsdlLocation, serviceName, serviceClass, features);
         DOMUtils.clearThreadLocals();
         return delegate;
      }
      catch (RuntimeException e)
      {
         throw new WebServiceException(e);
      }
   }

   @SuppressWarnings("unchecked")
   public ServiceDelegate createServiceDelegate(URL wsdlLocation, QName serviceName,
         Class serviceClass)
   {
      return this.createServiceDelegate(wsdlLocation, serviceName, serviceClass, (WebServiceFeature[])null);
   }

   public W3CEndpointReference createW3CEndpointReference(final String address, final QName interfaceName, 
         final QName serviceName, final QName portName, final List<Element> metadata, final String wsdlDocumentLocation, 
         final List<Element> referenceParameters, final List<Element> elements, final Map<QName, String> attributes)
   {
      if ((serviceName == null) && (address == null) && (portName == null))
         throw new IllegalStateException();
      if ((portName != null) && (serviceName == null))
         throw new IllegalStateException();
      
      final NativeEndpointReference epr = new NativeEndpointReference();
      epr.setAddress(address);
      epr.setServiceName(serviceName);
      epr.setEndpointName(portName);
      epr.setInterfaceName(interfaceName);
      epr.setMetadata(metadata);
      epr.setWsdlLocation(wsdlDocumentLocation);
      epr.setReferenceParameters(referenceParameters);

      return EndpointReferenceUtil.transform(W3CEndpointReference.class, epr);
   }

   public W3CEndpointReference createW3CEndpointReference(final String address, final QName serviceName, 
         final QName portName, final List<Element> metadata, final String wsdlDocumentLocation,
         final List<Element> referenceParameters)
   {
      return createW3CEndpointReference(address, null, serviceName, portName, metadata, wsdlDocumentLocation, referenceParameters, null, null);
   }

   public <T> T getPort(final EndpointReference epr, final Class<T> sei, final WebServiceFeature... features)
   {
      final NativeEndpointReference nepr = EndpointReferenceUtil.transform(NativeEndpointReference.class, epr);
      final URL wsdlLocation = nepr.getWsdlLocation();
      final QName serviceName = nepr.getServiceName();
      final ServiceDelegate delegate = createServiceDelegate(wsdlLocation, serviceName, Service.class);

      return delegate.getPort(epr, sei, features);
   }

   public EndpointReference readEndpointReference(final Source eprInfoset)
   {
      if (eprInfoset == null)
         throw new NullPointerException("Provided eprInfoset cannot be null");

      try
      {
         //we currently support W3CEndpointReference only
         return new W3CEndpointReference(eprInfoset);
      }
      catch (Exception e)
      {
         throw new WebServiceException(e);
      }
   }

   private String getBindingFromAddress(final String address)
   {
      try
      {
         final URL url = new URL(address);
         final String protocol = url.getProtocol();

         if (protocol.toLowerCase().startsWith("http"))
         {
            return SOAPBinding.SOAP11HTTP_BINDING;
         }
      }
      catch (MalformedURLException e)
      {
         throw new IllegalArgumentException("Invalid endpoint address: " + address);
      }

      throw new IllegalArgumentException("Unsupported protocol: " + address);
   }

   private String getBindingId(final String bindingId, final Class<?> implementorClass)
   {
      if (bindingId != null)
      {
         return bindingId;
      }
      else
      {
         final BindingType bindingType = implementorClass.getAnnotation(BindingType.class);
         return (bindingType != null) ? bindingType.value() : SOAPBinding.SOAP11HTTP_BINDING;
      }
   }

}
