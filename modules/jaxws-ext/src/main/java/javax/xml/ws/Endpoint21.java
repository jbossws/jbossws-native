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

import org.w3c.dom.Element;

/**
 * @author Heiko.Braun@jboss.com
 */
public abstract class Endpoint21 extends Endpoint
{
   /**
    * Returns the <code>EndpointReference</code> associated with
    * this <code>Endpoint</code> instance.
    * <p>
    * If the Binding for this <code>bindingProvider</code> is
    * either SOAP1.1/HTTP or SOAP1.2/HTTP, then a
    * <code>W3CEndpointReference</code> MUST be returned.
    * If the returned <code>EndpointReference</code> is a
    * <code>W3CEndpointReference</code> it MUST contain
    * the <code>wsaw:ServiceName</code> element and the
    * <code>wsaw:EndpointName</code> attribute on the
    * <code>wsaw:ServiceName</code>. It SHOULD contain
    * the embedded WSDL in the <code>wsa:Metadata</code> element
    * if there is an associated WSDL. The
    * <code>wsaw:InterfaceName</code> MAY also be present.
    * <br>
    * See <a href="http://www.w3.org/TR/2006/CR-ws-addr-wsdl-20060529/">
    * WS-Addressing - WSDL 1.0</a>.
    *
    * @param referenceParameters Reference parameters to be associated with the
    * returned <code>EndpointReference</code> instance.
    * @return EndpointReference of this <code>Endpoint</code> instance.
    * If the returned <code>EndpointReference</code> is of type
    * <code>W3CEndpointReference</code> then it MUST contain the
    * the specified <code>referenceParameters</code>.

    * @throws WebServiceException If any error in the creation of
    * the <code>EndpointReference</code> or if the <code>Endpoint</code> is
    * not in the published state.
    * @throws UnsupportedOperationException If this <code>BindingProvider</code>
    * uses the XML/HTTP binding.
    *
    * @see javax.xml.ws.wsaddressing.W3CEndpointReference
    *
    * @since JAX-WS 2.1
    **/
   public abstract EndpointReference getEndpointReference(Element... referenceParameters);

   /**
    * Returns the <code>EndpointReference</code> associated with
    * this <code>Endpoint</code> instance.
    * <p>
    * If the returned <code>EndpointReference</code> is a
    * <code>W3CEndpointReference</code> it MUST contain
    * the <code>wsaw:ServiceName</code> element and the
    * <code>wsaw:EndpointName</code> attribute on the
    * <code>wsaw:ServiceName</code>. It SHOULD contain
    * the embedded WSDL in the <code>wsa:Metadata</code> element
    * if there is an associated WSDL. The
    * <code>wsaw:InterfaceName</code> MAY also be present.
    * <br>
    * See <a href="http://www.w3.org/TR/2006/CR-ws-addr-wsdl-20060529/">
    * WS-Addressing - WSDL 1.0</a>.
    *
    * @param referenceParameters Reference parameters to be associated with the
    * returned <code>EndpointReference</code> instance.
    * @return EndpointReference of type <code>clazz</code> of this
    * <code>Endpoint<code> instance.
    * If the returned <code>EndpointReference</code> is of type
    * <code>W3CEndpointReference</code> then it MUST contain the
    * the specified <code>referenceParameters</code>.

    * @throws WebServiceException If any error in the creation of
    * the <code>EndpointReference</code> or if the <code>Endpoint</code> is
    * not in the published state or if the <code>clazz</code> is not a supported
    * <code>EndpointReference</code> type.
    * @throws UnsupportedOperationException If this <code>BindingProvider</code>
    * uses the XML/HTTP binding.
    *
    *
    * @since JAX-WS 2.1
    **/
   public abstract <T extends EndpointReference> T getEndpointReference(Class<T> clazz, Element... referenceParameters);
}
