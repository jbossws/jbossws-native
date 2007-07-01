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
package org.jboss.test.ws.jaxws.samples.webserviceref;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.ejb3.client.ClientLauncher;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.tools.wsdl.WSDLDefinitionsFactory;
import org.jboss.wsf.spi.test.JBossWSTest;
import org.jboss.wsf.spi.test.JBossWSTestSetup;

/**
 * Test the JAXWS annotation: javax.xml.ws.WebServiceRef
 *
 * @author Thomas.Diesler@jboss.com
 * @since 23-Oct-2005
 */
public class StubPropertyTestCase extends JBossWSTest
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-samples-webserviceref-secure";

   public static Test suite()
   {
      return new JBossWSTestSetup(StubPropertyTestCase.class, "jaxws-samples-webserviceref-secure.jar, jaxws-samples-webserviceref-secure-client.jar");
   }

   public void testWSDLAccess() throws MalformedURLException
   {
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
      WSDLDefinitionsFactory factory = WSDLDefinitionsFactory.newInstance();
      WSDLDefinitions wsdlDefinitions = factory.parse(wsdlURL);
      assertNotNull(wsdlDefinitions);
   }

   public void testDynamicProxy() throws Exception
   {
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
      QName qname = new QName("http://org.jboss.ws/wsref", "SecureEndpointService");
      Service service = Service.create(wsdlURL, qname);
      SecureEndpoint port = (SecureEndpoint)service.getPort(SecureEndpoint.class);

      BindingProvider bindingProvider = (BindingProvider)port;
      bindingProvider.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "kermit");
      bindingProvider.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "thefrog");

      String helloWorld = "Hello World";
      Object retObj = port.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   public void testUnconfiguredStub() throws Throwable
   {
      String reqMsg = "Hello World";
      new ClientLauncher().launch(SecureEndpointClient.class.getName(), "jbossws-client", new String[] { reqMsg, "kermit", "thefrog" });
      assertEquals("Hello World|Hello World|Hello World", SecureEndpointClient.retStr);
   }

   public void testConfiguredStub() throws Throwable
   {
      String reqMsg = "Hello World";
      new ClientLauncher().launch(SecureEndpointClient.class.getName(), "jbossws-client", new String[] { reqMsg });
      assertEquals("Hello World|Hello World|Hello World", SecureEndpointClient.retStr);
   }
}
