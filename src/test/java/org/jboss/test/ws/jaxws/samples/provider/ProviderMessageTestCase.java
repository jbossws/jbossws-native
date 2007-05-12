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
package org.jboss.test.ws.jaxws.samples.provider;

// $Id$

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.soap.SOAPBinding;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.tools.wsdl.WSDLDefinitionsFactory;
import org.jboss.ws.utils.DOMUtils;

/**
 * Test a Provider<SOAPMessage>
 *
 * @author Thomas.Diesler@jboss.org
 * @since 29-Jun-2006
 */
public class ProviderMessageTestCase extends JBossWSTest
{
   private String msgString =
      "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
      "  <env:Header/>" +
      "  <env:Body>" +
      "    <ns1:somePayload xmlns:ns1='http://org.jboss.ws/provider'/>" +
      "  </env:Body>" +
      "</env:Envelope>";

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(ProviderMessageTestCase.class, "jaxws-samples-provider-message.war");
   }

   public void testWSDLAccess() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-samples-provider-message?wsdl");
      WSDLDefinitionsFactory factory = WSDLDefinitionsFactory.newInstance();
      WSDLDefinitions wsdlDefinitions = factory.parse(wsdlURL);
      assertNotNull(wsdlDefinitions);
   }

   public void testProviderDispatch() throws Exception
   {
      Dispatch<SOAPMessage> dispatch = createDispatch("ProviderEndpoint");
      SOAPMessage reqMsg = getRequestMessage();

      SOAPMessage resMsg = dispatch.invoke(reqMsg);
      SOAPEnvelope resEnv = resMsg.getSOAPPart().getEnvelope();
      
      assertEquals(DOMUtils.parse(msgString), resEnv);
   }

   private Dispatch<SOAPMessage> createDispatch(String target) throws MalformedURLException, JAXBException
   {
      String targetNS = "http://org.jboss.ws/provider";
      QName serviceName = new QName(targetNS, "ProviderService");
      QName portName = new QName(targetNS, "ProviderPort");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-samples-provider-message/" + target + "?wsdl");

      Service service = Service.create(serviceName);
      service.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, wsdlURL.toExternalForm());
      
      Dispatch<SOAPMessage> dispatch = service.createDispatch(portName, SOAPMessage.class, Mode.MESSAGE);
      return dispatch;
   }

   public void testProviderMessage() throws Exception
   {
      SOAPMessage reqMsg = getRequestMessage();
      SOAPEnvelope reqEnv = reqMsg.getSOAPPart().getEnvelope();

      URL epURL = new URL("http://" + getServerHost() + ":8080/jaxws-samples-provider-message");
      SOAPConnection con = SOAPConnectionFactory.newInstance().createConnection();
      SOAPMessage resMsg = con.call(reqMsg, epURL);
      SOAPEnvelope resEnv = resMsg.getSOAPPart().getEnvelope();

      assertEquals(reqEnv, resEnv);
   }

   private SOAPMessage getRequestMessage() throws SOAPException, IOException
   {
      MessageFactory msgFactory = MessageFactory.newInstance();
      SOAPMessage reqMsg = msgFactory.createMessage(null, new ByteArrayInputStream(msgString.getBytes()));
      return reqMsg;
   }
}
