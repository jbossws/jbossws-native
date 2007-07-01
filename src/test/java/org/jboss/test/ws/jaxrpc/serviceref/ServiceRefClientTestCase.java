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
package org.jboss.test.ws.jaxrpc.serviceref;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.ServiceFactory;

import junit.framework.Test;

import org.jboss.ws.core.jaxrpc.client.ServiceFactoryImpl;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.tools.wsdl.WSDLDefinitionsFactory;
import org.jboss.wsf.spi.test.JBossWSTest;
import org.jboss.wsf.spi.test.JBossWSTestSetup;

/**
 * Test the JAXRPC <service-ref>
 *
 * @author Thomas.Diesler@jboss.com
 * @since 23-Oct-2005
 */
public class ServiceRefClientTestCase extends JBossWSTest
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxrpc-serviceref";

   public static Test suite()
   {
      return new JBossWSTestSetup(ServiceRefClientTestCase.class, "jaxrpc-serviceref.war, jaxrpc-serviceref-client.jar");
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
      URL wsdlURL = new File("resources/jaxrpc/serviceref/META-INF/wsdl/TestEndpoint.wsdl").toURL();
      URL mappingURL = new File("resources/jaxrpc/serviceref/META-INF/jaxrpc-mapping.xml").toURL();
      QName qname = new QName("http://org.jboss.ws/wsref", "TestEndpointService");
      Service service = new ServiceFactoryImpl().createService(wsdlURL, qname, mappingURL);
      TestEndpoint port = (TestEndpoint)service.getPort(TestEndpoint.class);

      String helloWorld = "Hello World!";
      Object retObj = port.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   public void testDynamicProxyNeg() throws Exception
   {
      URL wsdlURL = new File("resources/jaxrpc/serviceref/META-INF/wsdl/TestEndpoint.wsdl").toURL();
      QName qname = new QName("http://org.jboss.ws/wsref", "TestEndpointService");
      Service service = ServiceFactory.newInstance().createService(wsdlURL, qname);
      try
      {
         TestEndpoint port = (TestEndpoint)service.getPort(java.rmi.activation.Activator.class);
         fail("Expected ServiceException, but got: " + port);
      }
      catch (ServiceException ex)
      {
         // this is tested by the CTS
      }
      catch (Exception ex)
      {
         fail("Expected ServiceException, but got: " + ex);
      }
   }

   public void testApplicationClient() throws Exception
   {
      String helloWorld = "Hello World!";
      ApplicationClient.encCtx = getInitialContext();
      ApplicationClient.main(new String[] { helloWorld });
      assertEquals(helloWorld, ApplicationClient.retStr);
   }
}
