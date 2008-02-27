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
package javax.xml.ws;

// $Id$

import javax.xml.namespace.QName;
import javax.xml.bind.JAXBContext;
import java.net.URL;

/**
 * @author Heiko.Braun@jboss.com
 * @version $Revision$
 */
public class Service21 extends Service
{
   
   protected Service21(URL wsdlDocumentLocation, QName serviceName)
   {
      super(wsdlDocumentLocation, serviceName);
   }

   /**
    * Create a <code>Service</code> instance.
    *
    * The specified WSDL document location and service qualified name MUST
    * uniquely identify a <code>wsdl:service</code> element.
    *
    * @param wsdlLocation URL for the WSDL document location
    *                             for the service
    * @param serviceName QName for the service
    * @throws WebServiceException If any error in creation of the
    *                    specified service.
    **/
   public static Service21 create(URL wsdlLocation, QName serviceName)
   {
      return new Service21(wsdlLocation, serviceName);
   }

   /**
    * Create a <code>Service</code> instance.
    *
    * @param serviceName QName for the service
    * @throws WebServiceException If any error in creation of the
    *                    specified service
    */
   public static Service21 create(QName serviceName)
   {
      return create(null, serviceName);
   }
   
   /**
       * Creates a <code>Dispatch</code> instance for use with objects of
       * the users choosing. If there
       * are any reference parameters in the
       * <code>endpointReference</code>, then those reference
       * parameters MUST appear as SOAP headers, indicating them to be
       * reference parameters, on all messages sent to the endpoint.
       * The <code>endpointReference's</code> address MUST be used
       * for invocations on the endpoint.
       * In the implementation of this method, the JAX-WS
       * runtime system takes the responsibility of selecting a protocol
       * binding (and a port) and configuring the dispatch accordingly from
       * the WSDL associated with this <code>Service</code> instance or
       * from the WSDL Metadata from the <code>endpointReference</code>.
       * If this <code>Service</code> instance has a WSDL and
       * the <code>endpointReference</code>
       * also has a WSDL, then the WSDL from this instance will be used.
       * If this <code>Service</code> instance does not have a WSDL and
       * the <code>endpointReference</code> does have a WSDL, then the
       * WSDL from the <code>endpointReference</code> will be used.
       * <p>
       * This method behaves the same as calling
       * <pre>
       * dispatch = service.createDispatch(portName, type, mode, features);
       * </pre>
       * where the <code>portName</code> is retrieved from the
       * <code>wsaw:EndpointName</code> attribute of the <code>wsaw:ServiceName</code>
       * element in the
       * metadata of the <code>endpointReference</code>.
       * <br>
       * See <a href="http://www.w3.org/TR/2006/CR-ws-addr-wsdl-20060529/">WS-Addressing - WSDL 1.0
       * </a>.
       *
       * @param endpointReference  The <code>EndpointReference</code>
       * for the target service endpoint that will be invoked by the
       * returned <code>Dispatch</code> object.
       * @param type The class of object used to messages or message
       * payloads. Implementations are required to support
       * javax.xml.transform.Source and javax.xml.soap.SOAPMessage.
       * @param mode Controls whether the created dispatch instance is message
       * or payload oriented, i.e. whether the user will work with complete
       * protocol messages or message payloads. E.g. when using the SOAP
       * protocol, this parameter controls whether the user will work with
       * SOAP messages or the contents of a SOAP body. Mode MUST be MESSAGE
       * when type is SOAPMessage.
       * @param features  An array of WebServiceFeatures to configure on the
       *                proxy.  Supported features not in the <code>features
       *                </code> parameter will have their default values.
       *
       * @return Dispatch instance
       * @throws WebServiceException
       *                  <UL>
       *                    <LI>If there is any missing WSDL metadata
       *                      as required by this method.
       *                    <li>If the <code>wsaw:ServiceName</code> element
       *                    or the <code>wsaw:EndpointName</code> attribute
       *                    is missing in the metdata of the
       *                    <code>endpointReference</code>.
       *                    <li>If the <code>wsaw:ServiceName</code> does not
       *                    match the <code>serviceName</code> of this instance.
       *                    <li>If the <code>wsaw:EndpointName</code> does not
       *                    match a valid wsdl:Port in the WSDL metadata.
       *                    <li>If any error in the creation of
       *                     the <code>Dispatch</code> object.
       *                    <li>if a feature is enabled that is not
       *                    compatible with this port or is unsupported.
       *                  </UL>
       *
       * @see javax.xml.transform.Source
       * @see javax.xml.soap.SOAPMessage
       * @see WebServiceFeature;
       *
       * @since JAX-WS 2.1
       **/
      public <T> Dispatch<T> createDispatch(EndpointReference endpointReference, Class<T> type, Service.Mode mode, WebServiceFeature... features)
      {
         return delegate.createDispatch(endpointReference, type, mode, features);
      }

    /**
    * Creates a <code>Dispatch</code> instance for use with JAXB
    * generated objects. If there
    * are any reference parameters in the
    * <code>endpointReference</code>, then those reference
    * parameters MUST appear as SOAP headers, indicating them to be
    * reference parameters, on all messages sent to the endpoint.
    * The <code>endpointReference's</code> address MUST be used
    * for invocations on the endpoint.
    * In the implementation of this method, the JAX-WS
    * runtime system takes the responsibility of selecting a protocol
    * binding (and a port) and configuring the dispatch accordingly from
    * the WSDL associated with this <code>Service</code> instance or
    * from the WSDL Metadata from the <code>endpointReference</code>.
    * If this <code>Service</code> instance has a WSDL and
    * the <code>endpointReference</code>
    * also has a WSDL, then the WSDL from this instance will be used.
    * If this <code>Service</code> instance does not have a WSDL and
    * the <code>endpointReference</code> does have a WSDL, then the
    * WSDL from the <code>endpointReference</code> will be used.
    * <p>
    * This method behavies the same as calling
    * <pre>
    * dispatch = service.createDispatch(portName, context, mode, features);
    * </pre>
    * where the <code>portName</code> is retrieved from the
    * <code>wsaw:EndpointName</code> attribute of the <code>wsaw:ServiceName</code>
    * element in the
    * metadata of the <code>endpointReference</code>.
    * <br>
    * See <a href="http://www.w3.org/TR/2006/CR-ws-addr-wsdl-20060529/">WS-Addressing - WSDL 1.0
    * </a>.
    *
    * @param endpointReference  The <code>EndpointReference</code>
    * for the target service endpoint that will be invoked by the
    * returned <code>Dispatch</code> object.
    * @param context The JAXB context used to marshall and unmarshall
    * messages or message payloads.
    * @param mode Controls whether the created dispatch instance is message
    * or payload oriented, i.e. whether the user will work with complete
    * protocol messages or message payloads. E.g. when using the SOAP
    * protocol, this parameter controls whether the user will work with
    * SOAP messages or the contents of a SOAP body.
    * @param features  An array of WebServiceFeatures to configure on the
    *                proxy.  Supported features not in the <code>features
    *                </code> parameter will have their default values.
    *
    * @return Dispatch instance
    * @throws WebServiceException
    * @throws WebServiceException
    *                  <UL>
    *                    <li>If there is any missing WSDL metadata
    *                      as required by this method.
    *                    <li>If the <code>wsaw:ServiceName</code> element
    *                    or the <code>wsaw:EndpointName</code> attribute
    *                    is missing in the metdata of the
    *                    <code>endpointReference</code>.
    *                    <li>If the <code>wsaw:ServiceName</code> does not
    *                    match the <code>serviceName</code> of this instance.
    *                    <li>If the <code>wsaw:EndpointName</code> does not
    *                    match a valid wsdl:Port in the WSDL metadata.
    *                    <li>If any error in the creation of
    *                  the <code>Dispatch</code> object.
    *                    <li>if a feature is enabled that is not
    *                    compatible with this port or is unsupported.
    *                  </UL>
    *
    * @see javax.xml.bind.JAXBContext
    * @see WebServiceFeature
    *
    * @since JAX-WS 2.1
    **/
   public Dispatch<Object> createDispatch(EndpointReference endpointReference, JAXBContext context, Service.Mode mode, WebServiceFeature... features)
   {
      return delegate.createDispatch(endpointReference, context, mode, features);
   }

   /**
    * Creates a <code>Dispatch</code> instance for use with objects of
    * the users choosing.
    *
    * @param portName  Qualified name for the target service endpoint
    * @param type The class of object used for messages or message
    * payloads. Implementations are required to support
    * javax.xml.transform.Source and javax.xml.soap.SOAPMessage.
    * @param mode Controls whether the created dispatch instance is message
    * or payload oriented, i.e. whether the user will work with complete
    * protocol messages or message payloads. E.g. when using the SOAP
    * protocol, this parameter controls whether the user will work with
    * SOAP messages or the contents of a SOAP body. Mode MUST be MESSAGE
    * when type is SOAPMessage.
    * @param features  A list of WebServiceFeatures to configure on the
    *                proxy.  Supported features not in the <code>features
    *                </code> parameter will have their default values.
    *
    * @return Dispatch instance.
    * @throws WebServiceException If any error in the creation of
    *                  the <code>Dispatch</code> object or if a
    *                  feature is enabled that is not compatible with
    *                  this port or is unsupported.
    *
    * @see javax.xml.transform.Source
    * @see javax.xml.soap.SOAPMessage
    * @see WebServiceFeature
    *
    * @since JAX-WS 2.1
    **/
   public <T> Dispatch<T> createDispatch(QName portName, Class<T> type, Service.Mode mode, WebServiceFeature... features)
   {
      return delegate.createDispatch(portName, type, mode, features);
   }

   /**
    * Creates a <code>Dispatch</code> instance for use with JAXB
    * generated objects.
    *
    * @param portName  Qualified name for the target service endpoint
    * @param context The JAXB context used to marshall and unmarshall
    * messages or message payloads.
    * @param mode Controls whether the created dispatch instance is message
    * or payload oriented, i.e. whether the user will work with complete
    * protocol messages or message payloads. E.g. when using the SOAP
    * protocol, this parameter controls whether the user will work with
    * SOAP messages or the contents of a SOAP body.
    * @param features  A list of WebServiceFeatures to configure on the
    *                proxy.  Supported features not in the <code>features
    *                </code> parameter will have their default values.
    *
    * @return Dispatch instance.
    * @throws WebServiceException If any error in the creation of
    *                  the <code>Dispatch</code> object or if a
    *                  feature is enabled that is not compatible with
    *                  this port or is unsupported.
    *
    * @see javax.xml.bind.JAXBContext
    * @see WebServiceFeature
    *
    * @since JAX-WS 2.1
    **/
   public Dispatch<Object> createDispatch(QName portName, JAXBContext context, Service.Mode mode, WebServiceFeature... features)
   {
      return delegate.createDispatch(portName, context, mode, features);
   }


   /**
    * The getPort method returns a proxy. The parameter
    * <code>serviceEndpointInterface</code> specifies the service
    * endpoint interface that is supported by the returned proxy.
    * In the implementation of this method, the JAX-WS
    * runtime system takes the responsibility of selecting a protocol
    * binding (and a port) and configuring the proxy accordingly.
    * The returned proxy should not be reconfigured by the client.
    *
    * @param serviceEndpointInterface Service endpoint interface.
    * @param features  A list of WebServiceFeatures to configure on the
    *                proxy.  Supported features not in the <code>features
    *                </code> parameter will have their default values.
    * @return Object instance that supports the
    *                  specified service endpoint interface.
    * @throws WebServiceException
    *                  <UL>
    *                  <LI>If there is an error during creation
    *                      of the proxy.
    *                  <LI>If there is any missing WSDL metadata
    *                      as required by this method.
    *                  <LI>If an illegal
    *                      <code>serviceEndpointInterface</code>
    *                      is specified.
    *                  <LI>If a feature is enabled that is not compatible
    *                      with this port or is unsupported.
    *                  </UL>
    *
    * @see WebServiceFeature
    *
    * @since JAX-WS 2.1
    **/
   public <T> T getPort(Class<T> serviceEndpointInterface, WebServiceFeature... features)
   {
      return delegate.getPort(serviceEndpointInterface, features);
   }

   /**
    * The getPort method returns a proxy.
    * The parameter <code>endpointReference</code> specifies the
    * endpoint that will be invoked by the returned proxy.  If there
    * are any reference parameters in the
    * <code>endpointReference</code>, then those reference
    * parameters MUST appear as SOAP headers, indicating them to be
    * reference parameters, on all messages sent to the endpoint.
    * The <code>endpointReference's</code> address MUST be used
    * for invocations on the endpoint.
    * The parameter <code>serviceEndpointInterface</code> specifies
    * the service endpoint interface that is supported by the
    * returned proxy.
    * In the implementation of this method, the JAX-WS
    * runtime system takes the responsibility of selecting a protocol
    * binding (and a port) and configuring the proxy accordingly from
    * the WSDL associated with this <code>Service</code> instance or
    * from the WSDL Metadata from the <code>endpointReference</code>.
    * If this <code>Service</code> instance has a WSDL and
    * the <code>endpointReference</code>
    * also has a WSDL, then the WSDL from this instance will be used.
    * If this <code>Service</code> instance does not have a WSDL and
    * the <code>endpointReference</code> does have a WSDL, then the
    * WSDL from the <code>endpointReference</code> will be used.
    * The returned proxy should not be reconfigured by the client.
    * If this <code>Service</code> instance has a known proxy
    * port that matches the information contained in
    * the WSDL,
    * then that proxy is returned, otherwise a WebServiceException
    * is thrown.
    * <p>
    * Calling this method has the same behavior as the following
    * <pre>
    * port = service.getPort(portName, serviceEndpointInterface);
    * </pre>
    * where the <code>portName</code> is retrieved from the
    * <code>wsaw:EndpontName</code> attribute of the
    * <code>wsaw:ServiceName</code> element in the
    * metadata of the <code>endpointReference</code> or from the
    * <code>serviceEndpointInterface</code> and the WSDL
    * associated with this <code>Service</code> instance.
    * <br>
    * See <a href="http://www.w3.org/TR/2006/CR-ws-addr-wsdl-20060529/">WS-Addressing - WSDL 1.0
    * </a>.
    *
    * @param endpointReference  The <code>EndpointReference</code>
    * for the target service endpoint that will be invoked by the
    * returned proxy.
    * @param serviceEndpointInterface Service endpoint interface.
    * @param features  An array of WebServiceFeatures to configure on the
    *                proxy.  Supported features not in the <code>features
    *                </code> parameter will have their default values.
    * @return Object Proxy instance that supports the
    *                  specified service endpoint interface.
    * @throws WebServiceException
    *                  <UL>
    *                  <LI>If there is an error during creation
    *                      of the proxy.
    *                  <LI>If there is any missing WSDL metadata
    *                      as required by this method.
    *                  <LI>If the <code>wsaw:EndpointName</code> is
    *                      missing from the <code>endpointReference</code>
    *                      or does not match a wsdl:Port
    *                      in the WSDL metadata.
    *                  <LI>If the <code>wsaw:ServiceName</code> in the
    *                      <code>endpointReference</code> metadata does not
    *                      match the <code>serviceName</code> of this
    *                      <code>Service</code> instance.
    *                  <LI>If an invalid
    *                      <code>endpointReference</code>
    *                      is specified.
    *                  <LI>If an invalid
    *                      <code>serviceEndpointInterface</code>
    *                      is specified.
    *                  <LI>If a feature is enabled that is not compatible
    *                      with this port or is unsupported.
    *                  </UL>
    *
    *  @since JAX-WS 2.1
    **/
   public <T> T getPort(EndpointReference endpointReference, Class<T> serviceEndpointInterface, WebServiceFeature... features)
   {
      return delegate.getPort(endpointReference, serviceEndpointInterface, features);
   }

   /**
    * The getPort method returns a proxy. A service client
    * uses this proxy to invoke operations on the target
    * service endpoint. The <code>serviceEndpointInterface</code>
    * specifies the service endpoint interface that is supported by
    * the created dynamic proxy instance.
    *
    * @param portName  Qualified name of the service endpoint in
    *                  the WSDL service description.
    * @param serviceEndpointInterface Service endpoint interface
    *                  supported by the dynamic proxy instance.
    * @param features  A list of WebServiceFeatures to configure on the
    *                proxy.  Supported features not in the <code>features
    *                </code> parameter will have their default values.
    * @return Object Proxy instance that
    *                supports the specified service endpoint
    *                interface.
    * @throws WebServiceException This exception is thrown in the
    *                  following cases:
    *                  <UL>
    *                  <LI>If there is an error in creation of
    *                      the proxy.
    *                  <LI>If there is any missing WSDL metadata
    *                      as required by this method.
    *                  <LI>If an illegal
    *                      <code>serviceEndpointInterface</code>
    *                      or <code>portName</code> is specified.
    *                  <LI>If a feature is enabled that is not compatible
    *                      with this port or is unsupported.
    *                  </UL>
    * @see java.lang.reflect.Proxy
    * @see java.lang.reflect.InvocationHandler
    * @see WebServiceFeature
    *
    * @since JAX-WS 2.1
    **/
   public <T> T getPort(QName portName, Class<T> serviceEndpointInterface, WebServiceFeature... features)
   {
      return delegate.getPort(portName, serviceEndpointInterface, features);
   }
}
