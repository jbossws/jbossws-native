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
package org.jboss.test.ws.jaxws.jbws944;

import java.net.MalformedURLException;
import java.net.URL;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.tools.wsdl.WSDLDefinitionsFactory;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * EJB3 jmx name is incorrectly derrived
 * 
 * http://jira.jboss.org/jira/browse/JBWS-944
 *
 * @author Thomas.Diesler@jboss.org
 * @author Jason.Greene@jboss.com
 * @since 29-Apr-2005
 */
public class JBWS944TestCase extends JBossWSTest
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-jbws944/FooBean01";

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS944TestCase.class, "jaxws-jbws944.jar");
   }

   public void testRemoteAccess() throws Exception
   {
      InitialContext iniCtx = getInitialContext();
      EJB3RemoteBusinessInterface ejb3Remote = (EJB3RemoteBusinessInterface)iniCtx.lookup("/FooBean01/remote");

      String helloWorld = "Hello world!";
      Object retObj = ejb3Remote.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   // This tests whether the remote proxy also implements
   // the home interface and that it can be narrowed to it.
   public void testNarrowedRemoteAccess() throws Exception
   {
      if (true)
      {
         System.out.println("FIXME: [JBCTS-540] EJB3 proxy does not implement the Home interface");
         return;
      }
      
      InitialContext iniCtx = getInitialContext();
      Object obj = iniCtx.lookup("/FooBean01/remote");
      EJB3RemoteHome ejb3Home = (EJB3RemoteHome)PortableRemoteObject.narrow(obj, EJB3RemoteHome.class);
      EJB3RemoteInterface ejb3Remote = ejb3Home.create();

      String helloWorld = "Hello world!";
      Object retObj = ejb3Remote.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   public void testWebService() throws Exception
   {
      assertWSDLAccess();

      String helloWorld = "Hello world!";
      QName serviceName = new QName("http://org.jboss.ws/jbws944", "EJB3BeanService");
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      EndpointInterface port = (EndpointInterface)service.getPort(EndpointInterface.class);
      String retObj = port.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   private void assertWSDLAccess() throws MalformedURLException
   {
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
      WSDLDefinitionsFactory factory = WSDLDefinitionsFactory.newInstance();
      WSDLDefinitions wsdlDefinitions = factory.parse(wsdlURL);
      assertNotNull(wsdlDefinitions);
   }
}
