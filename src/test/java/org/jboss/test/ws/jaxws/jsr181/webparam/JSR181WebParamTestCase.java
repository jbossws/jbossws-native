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
package org.jboss.test.ws.jaxws.jsr181.webparam;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.Service;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.ws.core.jaxrpc.CallImpl;
import org.jboss.ws.core.jaxrpc.ServiceFactoryImpl;
import org.jboss.ws.metadata.umdm.OperationMetaData;
import org.jboss.ws.metadata.umdm.ParameterMetaData;

/**
 * Test the JSR-181 annotation: javax.jws.WebParam
 *
 * @author Thomas.Diesler@jboss.org
 * @since 07-Oct-2005
 */
public class JSR181WebParamTestCase extends JBossWSTest
{
   private String endpointURL = "http://" + getServerHost() + ":8080/jaxws-jsr181-webparam/TestService";
   private String targetNS = "http://www.openuri.org/jsr181/WebParamExample";

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(JSR181WebParamTestCase.class, "jaxws-jsr181-webparam.war");
   }

   public void testEcho() throws Exception
   {
      QName serviceName = new QName(targetNS, "PingServiceService");
      QName portName = new QName(targetNS, "PingServicePort");
      URL wsdlURL = new URL(endpointURL + "?wsdl");

      File mappingFile = new File("resources/jaxws/jsr181/webparam/jaxrpc-mapping.xml");
      assertTrue(mappingFile.exists());

      ServiceFactoryImpl factory = new ServiceFactoryImpl();
      Service service = factory.createService(wsdlURL, serviceName, mappingFile.toURL());
      CallImpl call = (CallImpl)service.createCall(portName, "echo");

      PingDocument doc = new PingDocument("Hello Kermit");
      Object retObj = call.invoke(new Object[]{doc});
      assertEquals(doc, retObj);
   }

   public void testPingOneWay() throws Exception
   {
      QName serviceName = new QName(targetNS, "PingServiceService");
      QName portName = new QName(targetNS, "PingServicePort");
      URL wsdlURL = new URL(endpointURL + "?wsdl");

      File mappingFile = new File("resources/jaxws/jsr181/webparam/jaxrpc-mapping.xml");
      assertTrue(mappingFile.exists());

      ServiceFactoryImpl factory = new ServiceFactoryImpl();
      Service service = factory.createService(wsdlURL, serviceName, mappingFile.toURL());
      CallImpl call = (CallImpl)service.createCall(portName, "PingOneWay");

      OperationMetaData opMetaData = call.getOperationMetaData();
      ParameterMetaData param = opMetaData.getParameter(new QName("Ping"));
      assertNotNull ("Expected param", param);

      PingDocument doc = new PingDocument("Hello Kermit!");
      Object retObj = call.invoke(new Object[]{doc});
      assertNull("Expected null return", retObj);
   }

   public void testPingTwoWay() throws Exception
   {
      QName serviceName = new QName(targetNS, "PingServiceService");
      QName portName = new QName(targetNS, "PingServicePort");
      URL wsdlURL = new URL(endpointURL + "?wsdl");

      File mappingFile = new File("resources/jaxws/jsr181/webparam/jaxrpc-mapping.xml");
      assertTrue(mappingFile.exists());

      ServiceFactoryImpl factory = new ServiceFactoryImpl();
      Service service = factory.createService(wsdlURL, serviceName, mappingFile.toURL());
      CallImpl call = (CallImpl)service.createCall(portName, "PingTwoWay");

      OperationMetaData opMetaData = call.getOperationMetaData();
      ParameterMetaData param = opMetaData.getParameter(new QName("Ping"));
      assertNotNull ("Expected param", param);
      assertEquals (ParameterMode.INOUT, param.getMode());

      PingDocument doc = new PingDocument("Hello Kermit");
      PingDocumentHolder holder = new PingDocumentHolder(doc);

      Object retObj = call.invoke(new Object[]{holder});
      assertNull("Expected null return", retObj);
      assertEquals("Hello Kermit Response", holder.value.getContent());
   }

   public void testSecurePing() throws Exception
   {
      QName serviceName = new QName(targetNS, "PingServiceService");
      QName portName = new QName(targetNS, "PingServicePort");
      URL wsdlURL = new URL(endpointURL + "?wsdl");

      File mappingFile = new File("resources/jaxws/jsr181/webparam/jaxrpc-mapping.xml");
      assertTrue(mappingFile.exists());

      ServiceFactoryImpl factory = new ServiceFactoryImpl();
      Service service = factory.createService(wsdlURL, serviceName, mappingFile.toURL());
      CallImpl call = (CallImpl)service.createCall(portName, "SecurePing");

      OperationMetaData opMetaData = call.getOperationMetaData();
      ParameterMetaData param1 = opMetaData.getParameter(new QName("Ping"));
      assertNotNull ("Expected param", param1);
      ParameterMetaData param2 = opMetaData.getParameter(new QName(targetNS, "SecHeader"));
      assertNotNull ("Expected param", param2);
      assertTrue ("Expected header param", param2.isInHeader());

      PingDocument doc = new PingDocument("Hello Kermit");
      SecurityHeader secHeader = new SecurityHeader("some secret");

      Object retObj = call.invoke(new Object[]{doc, secHeader});
      assertNull("Expected null return", retObj);
   }
}