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
package org.jboss.test.ws.jaxws.endpoint;

// $Id$

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestHelper;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.tools.wsdl.WSDLDefinitionsFactory;

/**
 * Test JAXWS Endpoint deployment
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 29-Apr-2005
 */
public class EndpointTestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new TestSetup(JBossWSTestSetup.newTestSetup(EndpointTestCase.class, "jaxws-endpoint-servlet.war")) {

         private Boolean useJBossWebLoader;
         
         protected void setUp() throws Exception
         {
            MBeanServerConnection server = JBossWSTestHelper.getServer();
            useJBossWebLoader = (Boolean)server.getAttribute(new ObjectName("jboss.web:service=WebServer"), "UseJBossWebLoader");
            server.setAttribute(new ObjectName("jboss.web:service=WebServer"), new Attribute("UseJBossWebLoader", new Boolean(true)));
            super.setUp();
         }

         protected void tearDown() throws Exception
         {
            super.tearDown();
            MBeanServerConnection server = JBossWSTestHelper.getServer();
            server.setAttribute(new ObjectName("jboss.web:service=WebServer"), new Attribute("UseJBossWebLoader", useJBossWebLoader));
         }
      };
   }

   public void testWSDLAccess() throws MalformedURLException
   {
      if (isTargetJBoss50())
      {
         System.out.println("FIXME: [JBWEB-68] Servlet.init(ServletConfig) not called");
         return;
      }
      
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-endpoint?wsdl");
      WSDLDefinitionsFactory factory = WSDLDefinitionsFactory.newInstance();
      WSDLDefinitions wsdlDefinitions = factory.parse(wsdlURL);
      assertNotNull(wsdlDefinitions);
   }

   public void testClientAccess() throws Exception
   {
      if (isTargetJBoss50())
      {
         System.out.println("FIXME: [JBWEB-68] Servlet.init(ServletConfig) not called");
         return;
      }
      
      // Create the port
      URL wsdlURL = new File("resources/jaxws/endpoint/WEB-INF/wsdl/TestService.wsdl").toURL();
      QName qname = new QName("http://org.jboss.ws/jaxws/endpoint", "TestService");
      Service service = Service.create(wsdlURL, qname);
      EndpointInterface port = (EndpointInterface)service.getPort(EndpointInterface.class);

      String helloWorld = "Hello world!";
      Object retObj = port.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   public void testServletAccess() throws Exception
   {
      if (isTargetJBoss50())
      {
         System.out.println("FIXME: [JBWEB-68] Servlet.init(ServletConfig) not called");
         return;
      }

      URL url = new URL("http://" + getServerHost() + ":8080/jaxws-endpoint-servlet?param=hello-world");
      BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
      assertEquals("hello-world", br.readLine());

   }
}
