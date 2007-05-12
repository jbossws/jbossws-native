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
package org.jboss.test.ws.jaxws.samples.httpbinding;

// $Id$

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.http.HTTPBinding;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.tools.wsdl.WSDLDefinitionsFactory;

/**
 * Test HTTP Binding
 *
 * @author Thomas.Diesler@jboss.org
 * @since 02-Apr-2007
 */
public class HttpJAXBTestCase extends JBossWSTest
{
   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(HttpJAXBTestCase.class, "jaxws-samples-httpbinding-jaxb.war");
   }

   public void testWSDLAccess() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-samples-httpbinding-jaxb/ProviderEndpoint?wsdl");
      WSDLDefinitionsFactory factory = WSDLDefinitionsFactory.newInstance();
      WSDLDefinitions wsdlDefinitions = factory.parse(wsdlURL);
      assertNotNull(wsdlDefinitions);
   }

   public void testProviderDispatch() throws Exception
   {
      Dispatch<Object> dispatch = createDispatch("ProviderEndpoint");

      UserType user = new UserType();
      user.setString("Kermit");
      user.setQname(new QName("TheFrog"));
      UserType userRes = (UserType)dispatch.invoke(user);
      assertEquals(user.getString(), userRes.getString());
      assertEquals(user.getQname(), userRes.getQname());
   }

   private Dispatch<Object> createDispatch(String target) throws MalformedURLException, JAXBException
   {
      String targetNS = "http://org.jboss.ws/httpbinding";
      QName serviceName = new QName(targetNS, "ProviderService");
      QName portName = new QName(targetNS, "ProviderPort");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-samples-httpbinding-jaxb/" + target + "?wsdl");

      Service service = Service.create(serviceName);
      service.addPort(portName, HTTPBinding.HTTP_BINDING, wsdlURL.toExternalForm());

      JAXBContext jbc = JAXBContext.newInstance(new Class[] { UserType.class });
      Dispatch<Object> dispatch = service.createDispatch(portName, jbc, Mode.PAYLOAD);
      return dispatch;
   }
}