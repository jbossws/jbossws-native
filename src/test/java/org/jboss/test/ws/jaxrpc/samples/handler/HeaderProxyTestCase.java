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
package org.jboss.test.ws.jaxrpc.samples.handler;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.holders.StringHolder;

import junit.framework.Test;

import org.jboss.ws.Constants;
import org.jboss.ws.core.StubExt;
import org.jboss.ws.core.jaxrpc.client.ServiceFactoryImpl;
import org.jboss.ws.core.jaxrpc.client.ServiceImpl;
import org.jboss.wsf.spi.test.JBossWSTest;
import org.jboss.wsf.spi.test.JBossWSTestSetup;

/**
 * Test bound and unbound headers
 *
 * @author Thomas.Diesler@jboss.org
 * @since 27-Jan-2005
 */
public class HeaderProxyTestCase extends JBossWSTest
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxrpc-samples-handler";
   private static final String NAMESPACE_URI = "http://org.jboss.ws/samples/handler";

   private static HeaderTestService endpoint;

   public static Test suite()
   {
      return new JBossWSTestSetup(HeaderProxyTestCase.class, "jaxrpc-samples-handler.war");
   }

   protected void setUp() throws Exception
   {
      super.setUp();

      if (endpoint == null)
      {
         File javaWsdlMappingFile = new File("resources/jaxrpc/samples/handler/WEB-INF/jaxrpc-mapping.xml");
         assertTrue(javaWsdlMappingFile.exists());

         QName serviceName = new QName(NAMESPACE_URI, "TestService");
         ServiceFactoryImpl factory = new ServiceFactoryImpl();
         URL wsdlLocation = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
         ServiceImpl service = (ServiceImpl)factory.createService(wsdlLocation, serviceName, javaWsdlMappingFile.toURL());
         endpoint = (HeaderTestService)service.getPort(HeaderTestService.class);
      }
   }

   public void testBoundInHeader() throws Exception
   {
      endpoint.testInHeader("Hello world!", "IN header message");
   }

   public void testBoundInOutHeader() throws Exception
   {
      StringHolder holder = new StringHolder("INOUT header message");
      endpoint.testInOutHeader("Hello world!", holder);
      assertEquals("INOUT header message - response", holder.value);
   }

   public void testBoundOutHeader() throws Exception
   {
      StringHolder holder = new StringHolder();
      endpoint.testOutHeader("Hello world!", holder);
      assertEquals("OUT header message", holder.value);
   }

   public void testUnboundInHeader() throws Exception
   {
      // Add a header to the stub
      StubExt stub = (StubExt)endpoint;
      QName xmlName = new QName("http://otherns", "HeaderValue");
      stub.addUnboundHeader(xmlName, Constants.TYPE_LITERAL_STRING, String.class, ParameterMode.IN);
      stub.setUnboundHeaderValue(xmlName, "Unbound IN header message");

      endpoint.testInHeader("Hello world!", "IN header message");
      
      String unboundRet = (String)stub.getUnboundHeaderValue(xmlName);
      assertEquals("Unbound OUT header message", unboundRet);
   }

   public void testUnboundInOutHeader() throws Exception
   {
      // Add a header to the stub
      StubExt stub = (StubExt)endpoint;
      QName xmlName = new QName("http://otherns", "HeaderValue");
      stub.addUnboundHeader(xmlName, Constants.TYPE_LITERAL_STRING, String.class, ParameterMode.INOUT);
      stub.setUnboundHeaderValue(xmlName, "Unbound INOUT header message");

      StringHolder holder = new StringHolder("INOUT header message");
      endpoint.testInOutHeader("Hello world!", holder);
      assertEquals("INOUT header message - response", holder.value);
      
      String unboundRet = (String)stub.getUnboundHeaderValue(xmlName);
      assertEquals("Unbound OUT header message", unboundRet);
   }

   public void testUnboundOutHeader() throws Exception
   {
      // Add a header to the stub
      StubExt stub = (StubExt)endpoint;
      QName xmlName = new QName("http://otherns", "HeaderValue");
      stub.addUnboundHeader(xmlName, Constants.TYPE_LITERAL_STRING, String.class, ParameterMode.OUT);

      StringHolder holder = new StringHolder();
      endpoint.testOutHeader("Hello world!", holder);
      assertEquals("OUT header message", holder.value);
      
      String unboundRet = (String)stub.getUnboundHeaderValue(xmlName);
      assertEquals("Unbound OUT header message", unboundRet);
   }
}
