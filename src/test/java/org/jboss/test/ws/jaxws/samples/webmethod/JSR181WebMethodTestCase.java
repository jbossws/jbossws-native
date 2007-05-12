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
package org.jboss.test.ws.jaxws.samples.webmethod;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.ws.core.soap.NameImpl;
import org.jboss.ws.utils.DOMUtils;
import org.w3c.dom.Element;

/**
 * Test the JSR-181 annotation: javax.jws.webmethod
 *
 * @author Thomas.Diesler@jboss.org
 * @since 07-Oct-2005
 */
public class JSR181WebMethodTestCase extends JBossWSTest
{
   private String endpointURL = "http://" + getServerHost() + ":8080/jaxws-samples-webmethod/TestService";
   private String targetNS = "http://webmethod.samples.jaxws.ws.test.jboss.org/";

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(JSR181WebMethodTestCase.class, "jaxws-samples-webmethod.war");
   }

   public void testLegalAccess() throws Exception
   {
      URL wsdlURL = new URL(endpointURL + "?wsdl");
      QName serviceName = new QName(targetNS, "TestEndpointService");

      Service service = Service.create(wsdlURL, serviceName);
      TestEndpoint port = (TestEndpoint)service.getPort(TestEndpoint.class);

      Object retObj = port.echo("Hello");
      assertEquals("Hello", retObj);
   }

   public void testLegalMessageAccess() throws Exception
   {
      MessageFactory msgFactory = MessageFactory.newInstance();
      SOAPConnection con = SOAPConnectionFactory.newInstance().createConnection();

      String reqEnv =
      "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
      " <env:Header/>" +
      " <env:Body>" +
      "  <ns1:echoString xmlns:ns1='" + targetNS + "'>" +
      "   <arg0>Hello</arg0>" +
      "  </ns1:echoString>" +
      " </env:Body>" +
      "</env:Envelope>";
      SOAPMessage reqMsg = msgFactory.createMessage(null, new ByteArrayInputStream(reqEnv.getBytes()));

      URL epURL = new URL(endpointURL);
      SOAPMessage resMsg = con.call(reqMsg, epURL);

      NameImpl name = new NameImpl(new QName(targetNS, "echoStringResponse"));
      SOAPElement soapElement = (SOAPElement)resMsg.getSOAPBody().getChildElements(name).next();
      soapElement = (SOAPElement)soapElement.getChildElements(new NameImpl("return")).next();
      assertEquals("Hello", soapElement.getValue());
   }

   public void testIllegalMessageAccess() throws Exception
   {
      MessageFactory msgFactory = MessageFactory.newInstance();
      SOAPConnection con = SOAPConnectionFactory.newInstance().createConnection();

      String reqEnv =
      "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
      " <env:Header/>" +
      " <env:Body>" +
      "  <ns1:noWebMethod xmlns:ns1='" + targetNS + "'>" +
      "   <String_1>Hello</String_1>" +
      "  </ns1:noWebMethod>" +
      " </env:Body>" +
      "</env:Envelope>";
      SOAPMessage reqMsg = msgFactory.createMessage(null, new ByteArrayInputStream(reqEnv.getBytes()));

      URL epURL = new URL(endpointURL);
      SOAPMessage resMsg = con.call(reqMsg, epURL);
      SOAPFault soapFault = resMsg.getSOAPBody().getFault();
      assertNotNull("Expected SOAPFault", soapFault);

      String faultString = soapFault.getFaultString();
      assertTrue(faultString, faultString.indexOf("noWebMethod") > 0);
   }

   public void testIllegalCallAccess() throws Exception
   {
      URL wsdlURL = new URL(endpointURL + "?wsdl");
      QName serviceName = new QName(targetNS, "TestEndpointService");
      QName portName = new QName(targetNS, "TestEndpointPort");

      String reqPayload =
         "<ns1:noWebMethod xmlns:ns1='" + targetNS + "'>" +
         " <String_1>Hello</String_1>" +
         "</ns1:noWebMethod>";

      String expPayload =
         "<env:Fault xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
         " <faultcode>env:Client</faultcode>" +
         " <faultstring>Endpoint {http://webmethod.samples.jaxws.ws.test.jboss.org/}TestEndpointPort does not contain operation meta data for: {http://webmethod.samples.jaxws.ws.test.jboss.org/}noWebMethod</faultstring>" +
         "</env:Fault>";

      Service service = Service.create(wsdlURL, serviceName);
      Dispatch dispatch = service.createDispatch(portName, StreamSource.class, Mode.PAYLOAD);
      Source retSource = (Source)dispatch.invoke(new StreamSource(new StringReader(reqPayload)));
      Element retEl = getElementFromSource(retSource);
      
      assertEquals(DOMUtils.parse(expPayload), retEl);
   }
}