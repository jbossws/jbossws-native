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
package javax.xml.ws.spi;

// $Id$

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.WebServiceException;

/**
 * Service provider for ServiceDelegate and Endpoint objects.
 *  
 * @author Thomas.Diesler@jboss.com
 * @since 03-May-2006
 */
public abstract class Provider
{
   public static final String JAXWSPROVIDER_PROPERTY = "javax.xml.ws.spi.Provider";
   private static final String DEFAULT_JAXWSPROVIDER = "org.jboss.ws.core.jaxws.spi.ProviderImpl";

   /**
    * Creates a new instance of Provider
    */
   protected Provider()
   {
   }

   /**
    *
    * Creates a new provider object.
    * <p>
    * The algorithm used to locate the provider subclass to use consists
    * of the following steps:
    * <p>
    * <ul>
    * <li>
    *   If a resource with the name of
    *   <code>META-INF/services/javax.xml.ws.spi.Provider</code>
    *   exists, then its first line, if present, is used as the UTF-8 encoded
    *   name of the implementation class.
    * </li>
    * <li>
    *   If the $java.home/lib/jaxws.properties file exists and it is readable by
    *   the <code>java.util.Properties.load(InputStream)</code> method and it contains
    *   an entry whose key is <code>javax.xml.ws.spi.Provider</code>, then the value of
    *   that entry is used as the name of the implementation class.
    * </li>
    * <li>
    *   If a system property with the name <code>javax.xml.ws.spi.Provider</code>
    *   is defined, then its value is used as the name of the implementation class.
    * </li>
    * <li>
    *   Finally, a default implementation class name is used.
    * </li>
    * </ul>
    *
    */
   public static Provider provider()
   {
      try
      {
         return (Provider)ProviderLoader.loadProvider(DEFAULT_JAXWSPROVIDER);
      }
      catch (RuntimeException ex)
      {
         throw ex;
      }
      catch (Exception ex)
      {
         throw new WebServiceException("Unable to load Provider: " + ex.getMessage(), ex);
      }

   }

   /**
    * Creates a service delegate object.
    * <p>
    * @param wsdlDocumentLocation A URL pointing to the WSDL document
    *        for the service, or <code>null</code> if there isn't one.
    * @param serviceName The qualified name of the service.
    * @param serviceClass The service class, which MUST be either
    *        <code>javax.xml.ws.Service</code> or a subclass thereof.
    * @return The newly created service delegate.
    */
   public abstract ServiceDelegate createServiceDelegate(java.net.URL wsdlDocumentLocation, QName serviceName, Class serviceClass);

   /**
    *
    * Creates an endpoint object with the provided binding and implementation
    * object.
    *
    * @param bindingId A URI specifying the desired binding (e.g. SOAP/HTTP)
    * @param implementor A service implementation object to which
    *        incoming requests will be dispatched. The corresponding
    *        class MUST be annotated with all the necessary Web service
    *        annotations.
    * @return The newly created endpoint.
    */
   public abstract Endpoint createEndpoint(String bindingId, Object implementor);

   /**
    * Creates and publishes an endpoint object with the specified
    * address and implementation object.
    *
    * @param address A URI specifying the address and transport/protocol
    *        to use. A http: URI MUST result in the SOAP 1.1/HTTP
    *        binding being used. Implementations may support other
    *        URI schemes.
    * @param implementor A service implementation object to which
    *        incoming requests will be dispatched. The corresponding
    *        class MUST be annotated with all the necessary Web service
    *        annotations.
    * @return The newly created endpoint.
    */
   public abstract Endpoint createAndPublishEndpoint(String address, Object implementor);

}
