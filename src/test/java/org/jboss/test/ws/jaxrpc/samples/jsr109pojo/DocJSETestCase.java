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
package org.jboss.test.ws.jaxrpc.samples.jsr109pojo;

import java.io.File;
import java.net.URL;

import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.Stub;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.ws.core.jaxrpc.client.ServiceFactoryImpl;

/**
 * Test JSE endpoint for a document style service.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 02-Jun-2005
 */
public class DocJSETestCase extends JBossWSTest
{
   private static JaxRpcTestService port;

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(DocJSETestCase.class, "jaxrpc-samples-jsr109pojo-doc.war, jaxrpc-samples-jsr109pojo-doc-client.jar");
   }

   protected void setUp() throws Exception
   {
      super.setUp();

      if (port == null)
      {
         if (isTargetJBoss())
         {
            InitialContext iniCtx = getInitialContext();
            Service service = (Service)iniCtx.lookup("java:comp/env/service/TestServiceJSE");
            port = (JaxRpcTestService)service.getPort(JaxRpcTestService.class);
         }
         else
         {
            ServiceFactoryImpl factory = new ServiceFactoryImpl();
            URL wsdlURL = new File("resources/jaxrpc/samples/jsr109pojo/doclit/WEB-INF/wsdl/TestService.wsdl").toURL();
            URL mappingURL = new File("resources/jaxrpc/samples/jsr109pojo/doclit/WEB-INF/jaxrpc-mapping.xml").toURL();
            QName qname = new QName("http://org.jboss.ws/samples/jsr109pojo", "TestService");
            Service service = factory.createService(wsdlURL, qname, mappingURL);
            port = (JaxRpcTestService)service.getPort(JaxRpcTestService.class);
            ((Stub)port)._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, "http://" + getServerHost() + ":8080/jaxrpc-samples-jsr109pojo-doc");
         }
      }
   }

   public void testEchoString() throws Exception
   {
      String hello = "Hello";
      String world = "world!";
      Object retObj = port.echoString(hello, world);
      assertEquals(hello + world, retObj);
   }

   public void testEchoSimpleUserType() throws Exception
   {
      String hello = "Hello";
      SimpleUserType userType = new SimpleUserType(1, 2);
      Object retObj = port.echoSimpleUserType(hello, userType);
      assertEquals(userType, retObj);
   }
}
