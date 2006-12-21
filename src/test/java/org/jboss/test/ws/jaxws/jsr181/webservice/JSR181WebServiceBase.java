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
package org.jboss.test.ws.jaxws.jsr181.webservice;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.tools.wsdl.WSDLDefinitionsFactory;

// $Id$

/**
 * Base testing class for &#064;WebService
 *
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @version $Revision$
 */
public class JSR181WebServiceBase extends JBossWSTest
{

   private EndpointInterface getPort() throws MalformedURLException
   {
      QName serviceName = new QName("http://www.openuri.org/2004/04/HelloWorld", "TestService");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jsr181?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      return service.getPort(EndpointInterface.class);
   }

   private EndpointInterface03 getPort03() throws MalformedURLException
   {
      QName serviceName = new QName("http://www.openuri.org/2004/04/HelloWorld", "TestService");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jsr181?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      return service.getPort(EndpointInterface03.class);
   }

   private void sayHello() throws RemoteException, MalformedURLException
   {
      assertWSDLAccess();
      String helloWorld = "Hello world!";
      Object retObj = getPort().echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   public void webServiceTest() throws Exception
   {
      sayHello();
   }

   public void webServiceWsdlLocationTest() throws Exception
   {
      sayHello();
   }

   public void webServiceEndpointInterfaceTest() throws Exception
   {
      assertWSDLAccess();
      String helloWorld = "Hello Interface!";
      Object retObj = getPort03().echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   private void assertWSDLAccess() throws MalformedURLException
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jsr181?wsdl");
      WSDLDefinitionsFactory factory = WSDLDefinitionsFactory.newInstance();
      WSDLDefinitions wsdlDefinitions = factory.parse(wsdlURL);
      assertNotNull(wsdlDefinitions);
   }
}