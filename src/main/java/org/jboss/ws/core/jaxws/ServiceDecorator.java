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
package org.jboss.ws.core.jaxws;

import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.core.StubExt;

import javax.xml.namespace.QName;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.spi.ServiceDelegate;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Acts as a wrapper between clients and the JAX-WS API.
 * Allows additional, propriatary API to be injected and core API calls
 * to be decorated.
 *
 * @author Heiko.Braun@jboss.org
 * @version $Id$
 * @since 21.12.2006
 */
public class ServiceDecorator extends Service {

   public final static String CLIENT_CONF_NAME = "org.jboss.ws.jaxws.client.configName";
   public final static String CLIENT_CONF_FILE = "org.jboss.ws.jaxws.client.configFile";

   private Map<String, Object> props = new HashMap<String, Object>();

   private ServiceDelegate serviceDelegate;

   private Map getProps()
   {
      return props;
   }

   public void setProperty(String key, Object value)
   {
      props.put(key, value);
   }

   public Object getProperty(String key)
   {
      return props.get(key);
   }

   public static ServiceDecorator newInstance(URL wsdlDocumentLocation, QName serviceName)
   {
      return new ServiceDecorator(wsdlDocumentLocation, serviceName);
   }

   // ----------------------------------------------------------------------------
   // intercepted javax.xml.ws.Service invocations

   protected ServiceDecorator(URL wsdlDocumentLocation, QName serviceName) {
      super(wsdlDocumentLocation, serviceName);
   }

   public <T> T getPort(Class<T> serviceEndpointInterface) {
      T port = super.getPort(serviceEndpointInterface);
      decorateConfig((StubExt)port);
      return port;
   }

   public <T> T getPort(QName portName, Class<T> serviceEndpointInterface) {
      T port = super.getPort(portName, serviceEndpointInterface);
      decorateConfig((StubExt)port);
      return port;
   }

   public <T> T getPort(QName portName, Class<T> serviceEndpointInterface, WebServiceFeature... features) {
      T port = super.getPort(portName, serviceEndpointInterface, features);
      decorateConfig((StubExt)port);
      return port;
   }

   public <T> T getPort(Class<T> serviceEndpointInterface, WebServiceFeature... features) {
      T port = super.getPort(serviceEndpointInterface, features);
      decorateConfig((StubExt)port);
      return port;
   }

   public <T> T getPort(EndpointReference endpointReference, Class<T> serviceEndpointInterface, WebServiceFeature... features) {
      T port = super.getPort(endpointReference, serviceEndpointInterface, features);
      decorateConfig((StubExt)port);
      return port;
   }

   private void decorateConfig(StubExt stub) {

      EndpointMetaData epMetaData = stub.getEndpointMetaData();

      if(props.containsKey(CLIENT_CONF_NAME))
         epMetaData.setConfigName((String)props.get(CLIENT_CONF_NAME));
      if(props.containsKey(CLIENT_CONF_FILE))
         epMetaData.setConfigFile((String)props.get(CLIENT_CONF_FILE));

   }
}
