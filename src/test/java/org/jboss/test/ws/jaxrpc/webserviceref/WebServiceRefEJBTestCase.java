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
package org.jboss.test.ws.jaxrpc.webserviceref;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import javax.xml.rpc.Service;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.ws.core.jaxrpc.ServiceFactoryImpl;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.tools.wsdl.WSDLDefinitionsFactory;

/**
 * Test the JAXRPC <service-ref>
 *
 * @author Thomas.Diesler@jboss.com
 * @since 23-Oct-2005
 */
public class WebServiceRefEJBTestCase extends JBossWSTest
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxrpc-webserviceref";
   
   /*
   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(WebServiceRefEJBTestCase.class, "jaxrpc-webserviceref.war, jaxrpc-webserviceref-ejb-client.jar");
   }

   public void testWSDLAccess() throws MalformedURLException
   {
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
      WSDLDefinitionsFactory factory = WSDLDefinitionsFactory.newInstance();
      WSDLDefinitions wsdlDefinitions = factory.parse(wsdlURL);
      assertNotNull(wsdlDefinitions);
   }
   */
   
   public void testDynamicProxy() throws Exception
   {
      
      System.out.println("FIXME: [JBAS-3817] Fix EJB2.1 deployments");
      if (true) return;
      
      URL wsdlURL = new File("resources/jaxrpc/webserviceref/META-INF/wsdl/TestEndpoint.wsdl").toURL();
      URL mappingURL = new File("resources/jaxrpc/webserviceref/META-INF/jaxrpc-mapping.xml").toURL();
      QName qname = new QName("http://org.jboss.ws/wsref", "TestEndpointService");
      Service service = new ServiceFactoryImpl().createService(wsdlURL, qname, mappingURL);
      TestEndpoint port = (TestEndpoint)service.getPort(TestEndpoint.class);

      String helloWorld = "Hello World!";
      Object retObj = port.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   public void testEJBClient() throws Exception
   {
      
      System.out.println("FIXME: [JBAS-3817] Fix EJB2.1 deployments");
      if (true) return;
      
      InitialContext iniCtx = getInitialContext();
      EJBRemoteHome ejbHome = (EJBRemoteHome)iniCtx.lookup("/ejb/EJBClient");
      EJBRemote ejbRemote = ejbHome.create();

      String helloWorld = "Hello World!";
      Object retObj = ejbRemote.echo(helloWorld);
      assertEquals(helloWorld, retObj);
      
   }
}
