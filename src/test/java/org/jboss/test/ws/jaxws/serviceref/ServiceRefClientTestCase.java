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
package org.jboss.test.ws.jaxws.serviceref;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPBinding;
import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.tools.wsdl.WSDLDefinitionsFactory;

/**
 * Test the JAXWS <service-ref>
 *
 * @author Thomas.Diesler@jboss.com
 * @since 123-Mar-2007
 */
public class ServiceRefClientTestCase extends JBossWSTest
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-serviceref";

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(ServiceRefClientTestCase.class, "jaxws-serviceref.war, jaxws-serviceref-client.jar");
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
      URL wsdlURL = new File("resources/jaxws/serviceref/META-INF/wsdl/TestEndpoint.wsdl").toURL();
      QName qname = new QName("http://serviceref.jaxws.ws.test.jboss.org/", "TestEndpointService");
      Service service = Service.create(wsdlURL, qname);
      TestEndpoint port = (TestEndpoint)service.getPort(TestEndpoint.class);

      String request = "testDynamicProxy";
      String response = port.echo(request);
      assertEquals(request, response);
   }

   public void testApplicationClient() throws Exception
   {
      InitialContext iniCtx = getInitialContext("jbossws-client");
      TestEndpoint port = ((Service)iniCtx.lookup("java:comp/env/service2")).getPort(TestEndpoint.class);
      assertNotNull(port);

      BindingProvider bp = (BindingProvider)port;
      boolean mtomEnabled = ((SOAPBinding)bp.getBinding()).isMTOMEnabled();
      assertTrue("MTOM should be enabled on port", mtomEnabled);

      String request = "testApplicationClient";
      String response = port.echo(request);
      assertEquals(response, request);

   }

}
