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
package org.jboss.test.ws.jaxws.epr;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.Service21;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-1844] Implement Provider.createW3CEndpointReference()
 * 
 * http://jira.jboss.org/jira/browse/JBWS-1844
 *
 * @author Thomas.Diesler@jboss.com
 * @since 25-Feb-2007
 */
public class EndpointReferenceTestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSTestSetup(EndpointReferenceTestCase.class, "jaxws-epr.jar");
   }

   public void _testSimple() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-epr/TestEndpointImpl?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/epr", "TestEndpointService");
      Service service = Service.create(wsdlURL, serviceName);
      TestEndpoint port = service.getPort(TestEndpoint.class);
      String retStr = port.echo("hello");
      assertEquals("hello", retStr);
   }

   public void testEndpointReference() throws Exception
   {
      String address = "http://" + getServerHost() + ":8080/jaxws-epr/TestEndpointImpl";
      QName serviceName = new QName("http://org.jboss.ws/epr", "TestEndpointService");
      
      W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
      builder = builder.address(address);
      builder = builder.serviceName(serviceName);
      W3CEndpointReference epr = builder.build();
      
      Service21 service = Service21.create(serviceName);
      TestEndpoint port = service.getPort(epr, TestEndpoint.class);
      String retStr = port.echo("hello");
      assertEquals("hello", retStr);
   }
}
