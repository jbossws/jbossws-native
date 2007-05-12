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
package org.jboss.test.ws.interop.wsse;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestHelper;
import org.jboss.ws.core.StubExt;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.BindingProvider;
import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;

/**
 * @author Heiko.Braun@jboss.org
 * @version $Id$
 * @since 25.01.2007
 */
public abstract class AbstractWSSEBase extends JBossWSTest {

   protected IPingService port;

   protected void setUp() throws Exception
   {
      super.setUp();

      if (port == null)
      {
         URL wsdlLocation = new File("resources/interop/wsse/shared/WEB-INF/wsdl/WsSecurity10.wsdl").toURL();
         QName serviceName = new QName("http://tempuri.org/", "PingService10");
         Service service = Service.create(wsdlLocation, serviceName);
         port = service.getPort(IPingService.class);

         ((BindingProvider)port).getRequestContext().put(
            BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
            getEndpointURL()
         );
         configureClient();
      }

      defaultSetup(port);
   }

   abstract String getEndpointURL();


   protected void defaultSetup(IPingService port) {
      ((StubExt)port).setConfigName("Standard WSSecurity Client");

      System.setProperty("org.jboss.ws.wsse.keyStore", "resources/interop/wsse/shared/META-INF/alice.jks");
      System.setProperty("org.jboss.ws.wsse.trustStore", "resources/interop/wsse/shared/META-INF/wsse10.truststore");
      System.setProperty("org.jboss.ws.wsse.keyStorePassword", "password");
      System.setProperty("org.jboss.ws.wsse.trustStorePassword", "password");
      System.setProperty("org.jboss.ws.wsse.keyStoreType", "jks");
      System.setProperty("org.jboss.ws.wsse.trustStoreType", "jks");
   }

   protected void configureClient() {

      /*InteropConfigFactory factory = InteropConfigFactory.newInstance();
      ClientScenario scenario = factory.createClientScenario(System.getProperty("client.scenario"));
      if(scenario!=null)
      {
         log.info("Using scenario: " + scenario);
         ((Stub)port)._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, scenario.getTargetEndpoint().toString());
      }
      else
      {
         throw new IllegalStateException("Failed to load client scenario");
      }
      */
   }

   protected static void prepareClientClasspath(String s) {
      try
      {
         // wrap the classload upfront to allow inclusion of the client.jar
         JBossWSTestHelper helper = new JBossWSTestHelper();
         ClassLoader parent = Thread.currentThread().getContextClassLoader();
         URLClassLoader replacement = new URLClassLoader(new URL[] {helper.getArchiveURL(s)}, parent);
         Thread.currentThread().setContextClassLoader(replacement);

      }
      catch (MalformedURLException e)
      {
         throw new IllegalStateException(e);
      }
   }
}
