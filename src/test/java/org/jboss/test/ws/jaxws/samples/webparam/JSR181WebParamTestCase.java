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
package org.jboss.test.ws.jaxws.samples.webparam;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.ws.core.StubExt;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
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
   private String targetNS = "http://www.openuri.org/jsr181/WebParamExample";
   
   private static PingService port;

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(JSR181WebParamTestCase.class, "jaxws-samples-webparam.war");
   }

   public void setUp() throws Exception
   {
      if (port == null)
      {
         QName serviceName = new QName(targetNS, "PingServiceService");
         URL wsdlURL = new File("resources/jaxws/samples/webparam/META-INF/wsdl/PingService.wsdl").toURL();

         Service service = Service.create(wsdlURL, serviceName);
         port = service.getPort(PingService.class);
      }
   }

   public void testEcho() throws Exception
   {
      PingDocument doc = new PingDocument();
      doc.setContent("Hello Kermit");
      PingDocument retObj = port.echo(doc);
      assertEquals(doc.getContent(), retObj.getContent());
   }

   public void testPingOneWay() throws Exception
   {
      StubExt stub = (StubExt)port;
      EndpointMetaData epMetaData = stub.getEndpointMetaData();
      OperationMetaData opMetaData = epMetaData.getOperation(new QName(targetNS, "PingOneWay"));
      ParameterMetaData param = opMetaData.getParameter(new QName("Ping"));
      assertNotNull ("Expected param", param);

      PingDocument doc = new PingDocument();
      doc.setContent("Hello Kermit");
      port.pingOneWay(doc);
   }

   public void testPingTwoWay() throws Exception
   {
      PingDocument doc = new PingDocument();
      doc.setContent("Hello Kermit");
      Holder<PingDocument> holder = new Holder<PingDocument>(doc);

      port.pingTwoWay(holder);
      assertEquals("Hello Kermit Response", holder.value.getContent());
   }

   public void testSecurePing() throws Exception
   {
      StubExt stub = (StubExt)port;
      EndpointMetaData epMetaData = stub.getEndpointMetaData();
      OperationMetaData opMetaData = epMetaData.getOperation(new QName(targetNS, "SecurePing"));

      ParameterMetaData param1 = opMetaData.getParameter(new QName("Ping"));
      assertNotNull ("Expected param", param1);
      ParameterMetaData param2 = opMetaData.getParameter(new QName(targetNS, "SecHeader"));
      assertNotNull ("Expected param", param2);
      assertTrue ("Expected header param", param2.isInHeader());

      PingDocument doc = new PingDocument();
      doc.setContent("Hello Kermit");
      SecurityHeader secHeader = new SecurityHeader();
      secHeader.setValue("some secret");

      port.securePing(doc, secHeader);
   }
}