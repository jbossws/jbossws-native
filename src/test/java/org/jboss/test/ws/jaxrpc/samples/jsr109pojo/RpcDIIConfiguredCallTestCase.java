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
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.rpc.Call;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.ws.core.jaxrpc.ServiceFactoryImpl;
import org.jboss.ws.core.jaxrpc.ServiceImpl;

/**
 * Test the Dynamic Invocation Interface (DII) on a Call that is preconfigured from WSDL
 *
 * @author Thomas.Diesler@jboss.org
 * @since 06-Jan-2005
 */
public class RpcDIIConfiguredCallTestCase extends JBossWSTest
{
   private final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxrpc-samples-jsr109pojo-rpc";
   private static final String TARGET_NAMESPACE = "http://org.jboss.ws/samples/jsr109pojo";

   private static Call call;

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(RpcDIIConfiguredCallTestCase.class, "jaxrpc-samples-jsr109pojo-rpc.war");
   }

   protected void setUp() throws Exception
   {
      super.setUp();

      if (call == null)
      {
         File javaWsdlMappingFile = new File("resources/jaxrpc/samples/jsr109pojo/rpclit/WEB-INF/jaxrpc-mapping.xml");
         assertTrue(javaWsdlMappingFile.exists());

         ServiceFactoryImpl factory = new ServiceFactoryImpl();
         URL wsdlLocation = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
         QName serviceName = new QName(TARGET_NAMESPACE, "TestService");
         ServiceImpl service = (ServiceImpl)factory.createService(wsdlLocation, serviceName, javaWsdlMappingFile.toURL());
         call = service.createCall();
      }
   }

   public void testEchoString() throws Exception
   {
      QName operationName = new QName(TARGET_NAMESPACE, "echoString");
      call.setOperationName(operationName);
      assertFalse(call.isParameterAndReturnSpecRequired(operationName));

      String hello = "Hello";
      String world = "world!";
      Object retObj = call.invoke(new Object[]{hello, world});
      assertEquals(hello + world, retObj);

      List outValues = call.getOutputValues();
      assertEquals(0, outValues.size());

      Map outParams = call.getOutputParams();
      assertEquals(0, outParams.size());
   }

   public void testEchoSimpleUserType() throws Exception
   {
      QName operationName = new QName(TARGET_NAMESPACE, "echoSimpleUserType");
      call.setOperationName(operationName);

      String hello = "Hello";
      SimpleUserType userType = new SimpleUserType(1, 2);
      Object retObj = call.invoke(new Object[]{hello, userType});
      assertEquals(userType, retObj);
   }
}
