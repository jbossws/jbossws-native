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

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.soap.FactoryLoader;
import javax.xml.transform.Source;
import javax.xml.ws.Endpoint;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.w3c.dom.Element;

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
         return (Provider)FactoryLoader.loadFactory(JAXWSPROVIDER_PROPERTY, DEFAULT_JAXWSPROVIDER);
      }
      catch (WebServiceException ex)
      {
         throw ex;
      }
      catch (Exception ex)
      {
         throw new WebServiceException("Unable to createEndpointReference Provider: " + ex.getMessage());
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

   /**
    * read an EndpointReference from the infoset contained in
    * <code>eprInfoset</code>.
    *
    * @returns the <code>EndpointReference</code> unmarshalled from
    * <code>eprInfoset</code>.  This method never returns <code>null</code>.
    *
    * @throws WebServiceException If there is an error creating the
    * <code>EndpointReference</code> from the specified <code>eprInfoset</code>.
    *
    * @throws NullPointerException If the <code>null</code>
    * <code>eprInfoset</code> value is given.
    *
    * @since JAX-WS 2.1
    **/
   public abstract EndpointReference readEndpointReference(javax.xml.transform.Source eprInfoset);

   /**
    * Create an EndpointReference for <code>serviceName</code>
    * service and <code>portName</code> port from the WSDL <code>wsdlDocumentLocation</code>. The instance
    * returned will be of type <code>clazz</code> and contain the <code>referenceParameters</code>
    * reference parameters. This method delegates to the vendor specific
    * implementation of the {@link javax.xml.ws.spi.Provider#createEndpointReference(Class<T>, javax.xml.namespace.QName, javax.xml.namespace.QName, javax.xml.transform.Source, org.w3c.dom.Element...)} method.
    *
    * @param clazz Specifies the type of <code>EndpointReference</code> that MUST be returned.
    * @param serviceName Qualified name of the service in the WSDL.
    * @param portName Qualified name of the endpoint in the WSDL.
    * @param wsdlDocumentLocation URL for the WSDL document location for the service.
    * @param referenceParameters Reference parameters to be associated with the
    * returned <code>EndpointReference</code> instance.
    *
    * @return the EndpointReference created from <code>serviceName</code>, <code>portName</code>,
    *          <code>wsdlDocumentLocation</code> and <code>referenceParameters</code>. This method
    *          never returns <code>null</code>.
    * @throws WebServiceException
    *         <UL>
    *             <li>If the <code>serviceName</code> service is not present in the WSDL.
    *             <li>If the <code>portName</code> port is not present in <code>serviceName</code> service in the WSDL.
    *             <li>If the <code>wsdlDocumentLocation</code> does not represent a valid WSDL.
    *             <li>If an error occurs while creating the <code>EndpointReference</code>.
    *             <li>If the Class <code>clazz</code> is not supported by this implementation.
    *         </UL>
    * @throws java.lang.IllegalArgumentException
    *     if any of the <code>clazz</code>, <code>serviceName</code>, <code>portName</code> and <code>wsdlDocumentLocation</code> is null.
    */
   public abstract <T extends EndpointReference> T createEndpointReference(Class<T> clazz, QName serviceName, QName portName, Source wsdlDocumentLocation,
         Element... referenceParameters);

   /**
    * The getPort method returns a proxy.  If there
    * are any reference parameters in the
    * <code>endpointReference</code>, then those reference
    * parameters MUST appear as SOAP headers, indicating them to be
    * reference parameters, on all messages sent to the endpoint.
    * The parameter  <code>serviceEndpointInterface</code> specifies
    * the service endpoint interface that is supported by the
    * returned proxy.
    * The parameter <code>endpointReference</code> specifies the
    * endpoint that will be invoked by the returned proxy.
    * In the implementation of this method, the JAX-WS
    * runtime system takes the responsibility of selecting a protocol
    * binding (and a port) and configuring the proxy accordingly from
    * the WSDL Metadata from the <code>EndpointReference</code>.
    *
    *
    * @param endpointReference the EndpointReference that will
    * be invoked by the returned proxy.
    * @param serviceEndpointInterface Service endpoint interface
    * @param features  A list of WebServiceFeatures to configure on the
    *                proxy.  Supported features not in the <code>features
    *                </code> parameter will have their default values.
    * @return Object Proxy instance that supports the
    *                  specified service endpoint interface
    * @throws WebServiceException
    *                  <UL>
    *                  <LI>If there is an error during creation
    *                      of the proxy
    *                  <LI>If there is any missing WSDL metadata
    *                      as required by this method
    *                  <LI>If this
    *                      <code>endpointReference</code>
    *                      is illegal
    *                  <LI>If an illegal
    *                      <code>serviceEndpointInterface</code>
    *                      is specified
    *                  <LI>If feature is enabled that is not compatible with
    *                      this port or is unsupported.
    *                   </UL>
    *
    * @see WebServiceFeature
    *
    * @since JAX-WS 2.1
    **/
   public abstract <T> T getPort(EndpointReference endpointReference, Class<T> serviceEndpointInterface, WebServiceFeature... features);

   /**
    * Factory method to create a <code>W3CEndpointReference</code>.
    *
    * <p>
    * This method can be used to create a <code>W3CEndpointReference</code>
    * for any endpoint by specifying the <code>address</code> property along
    * with any other desired properties.  This method
    * can also be used to create a <code>W3CEndpointReference</code> for
    * an endpoint that is published by the same Java EE application.
    * To do so the <code>address</code> property can be provided or this
    * method can automatically determine the <code>address</code> of 
    * an endpoint that is published by the same Java EE application and is
    * identified by the <code>serviceName</code> and 
    * <code>portName</code> propeties.  If the <code>address</code> is 
    * <code>null</code> and the <code>serviceName</code> and 
    * <code>portName</code> do not identify an endpoint published by the 
    * same Java EE application, a
    * <code>javax.lang.IllegalArgumentException</code> MUST be thrown.
    *
    * @param address Specifies the address of the target endpoint
    * @param serviceName Qualified name of the service in the WSDL.
    * @param portName Qualified name of the endpoint in the WSDL.
    * @param metadata A list of elements that should be added to the 
    * <code>W3CEndpointReference</code> instances <code>wsa:metadata</code> 
    * element.
    * @param wsdlDocumentLocation URL for the WSDL document location for 
    * the service.  
    * @param referenceParameters Reference parameters to be associated 
    * with the returned <code>EndpointReference</code> instance.
    *
    * @return the <code>W3CEndpointReference<code> created from 
    *          <code>serviceName</code>, <code>portName</code>,
    *          <code>metadata</code>, <code>wsdlDocumentLocation</code> 
    *          and <code>referenceParameters</code>. This method
    *          never returns <code>null</code>.
    *
    * @throws javax.lang.IllegalArgumentException
    *     <ul>
    *        <li>If the <code>address</code>, <code>serviceName</code> and
    *            <code>portName</code> are all <code>null</code>.
    *        <li>If the <code>serviceName</code> service is <code>null</code> and the
    *            <code>portName> is NOT <code>null</code>.
    *        <li>If the <code>address</code> property is <code>null</code> and
    *            the <code>serviceName</code> and <code>portName</code> do not
    *            specify a valid endpoint published by the same Java EE
    *            application.
    *        <li>If the <code>serviceName</code>is NOT <code>null</code>
    *             and is not present in the specified WSDL.
    *        <li>If the <code>portName</code> port is not <code>null<code> and it
    *             is not present in <code>serviceName</code> service in the WSDL.
    *        <li>If the <code>wsdlDocumentLocation</code> is NOT <code>null</code>
    *            and does not represent a valid WSDL.
    *     </ul>
    * @throws WebServiceException If an error occurs while creating the 
    *                             <code>W3CEndpointReference</code>.
    *
    * @since JAX-WS 2.1
    */
   public abstract W3CEndpointReference createW3CEndpointReference(String address, QName serviceName, QName portName, List<Element> metadata,
         String wsdlDocumentLocation, List<Element> referenceParameters);
}
