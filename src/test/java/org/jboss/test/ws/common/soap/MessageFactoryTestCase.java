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
package org.jboss.test.ws.common.soap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.ws.Constants;
import org.jboss.ws.core.jaxrpc.Style;
import org.jboss.ws.core.soap.MessageFactoryImpl;
import org.jboss.wsintegration.spi.utils.DOMUtils;
import org.jboss.wsintegration.spi.utils.DOMWriter;
import org.w3c.dom.Element;

/**
 * Test the MessageFactory
 *
 * @author Thomas.Diesler@jboss.org
 * @since 21-Mar-2006
 */
public class MessageFactoryTestCase extends JBossWSTest
{
   public void testEnvelopeBuilder() throws Exception
   {
      String envStr = 
         "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
         " <env:Body>" +
         "  <businessList generic='2.0' operator='JBOSS' xmlns='urn:uddi-org:api_v2'>" + 
         "   <businessInfos>" + 
         "    <businessInfo businessKey='892ac280-c16b-11d5-85ad-801eef211111'>" + 
         "     <name xml:lang='en'>Demi Credit</name>" + 
         "     <description xml:lang='en'>A smaller demo app used for illustrating UDDI inquiry.</description>" + 
         "     <serviceInfos>" + 
         "      <serviceInfo businessKey='9a26b6e0-c15f-11d5-85a3-801eef208714' serviceKey='860eca90-c16d-11d5-85ad-801eef208714'>" + 
         "       <name xml:lang='en'>DCAmail</name>" + 
         "      </serviceInfo>" + 
         "     </serviceInfos>" + 
         "    </businessInfo>" + 
         "   </businessInfos>" + 
         "  </businessList>" + 
         " </env:Body>" +
         "</env:Envelope>";

      ByteArrayInputStream inputStream = new ByteArrayInputStream(envStr.getBytes());

      MessageFactory factory = new MessageFactoryImpl();
      SOAPMessage soapMsg = factory.createMessage(null, inputStream);
      SOAPEnvelope env = soapMsg.getSOAPPart().getEnvelope();

      assertEquals("env:Envelope", env.getNodeName());
      assertEquals(Constants.NS_SOAP11_ENV, env.getNamespaceURI());
      
      SOAPBodyElement soapBodyElement = (SOAPBodyElement)env.getBody().getChildElements().next();
      assertEquals("urn:uddi-org:api_v2", soapBodyElement.getNamespaceURI());
   }

   // [JBWS-745] SAAJ:SOAPBodyElement.addNamespaceDeclaration should allow empty prefix
   public void testAddNamespaceDeclaration() throws Exception
   {
      MessageFactory factory = new MessageFactoryImpl();
      SOAPMessage soapMsg = factory.createMessage();
      SOAPEnvelope env = soapMsg.getSOAPPart().getEnvelope();

      assertEquals("env:Envelope", env.getNodeName());
      assertEquals(Constants.NS_SOAP11_ENV, env.getNamespaceURI());
      
      SOAPBody soapBody = env.getBody();
      SOAPBodyElement soapBodyElement = (SOAPBodyElement)soapBody.addChildElement("businessList");
      soapBodyElement.addNamespaceDeclaration("", "urn:uddi-org:api_v2");
      
      String expEnvStr = 
         "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" + 
         " <env:Header/>" + 
         " <env:Body>" + 
         "  <businessList xmlns='urn:uddi-org:api_v2'/>" + 
         " </env:Body>" + 
         "</env:Envelope>";
      
      Element expEnv = DOMUtils.parse(expEnvStr);
      assertEquals(expEnv, env);
   }
   
   // [JBWS-1407] Premature end of File exception on createMessage
   public void testPrematureEndOfFile() throws Exception
   {
      File envFile = new File("resources/common/soap/MessageFactory/jbws1407.xml");
      assertTrue("File exists: " + envFile, envFile.exists());

      FileInputStream inputStream = new FileInputStream(envFile);

      MessageFactory factory = new MessageFactoryImpl();
      SOAPMessage soapMsg = factory.createMessage(null, inputStream);
      SOAPEnvelope env = soapMsg.getSOAPPart().getEnvelope();

      assertEquals("SOAP-ENV:Envelope", env.getNodeName());
      assertEquals(Constants.NS_SOAP11_ENV, env.getNamespaceURI());
   }
   
   public void testSetContentOnSOAPPart_StreamSource() throws Exception
   {
      String expMsg = 
         "<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/'>" +
         " <soapenv:Header/>" +
         " <soapenv:Body>" +
         "  <HelloResponse xmlns='http://helloservice.org/types\'>" +
         "   <argument>responseBean</argument>" +
         "  </HelloResponse>" +
         " </soapenv:Body>" +
         "</soapenv:Envelope>";
      
      MessageFactory factory = new MessageFactoryImpl();
      ByteArrayInputStream inputStream = new ByteArrayInputStream(expMsg.getBytes());
      StreamSource source = new StreamSource(inputStream);
      SOAPMessage message = factory.createMessage();
      message.getSOAPPart().setContent(source);
      message.saveChanges();
      
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      message.writeTo(baos);
      String wasMsg = new String(baos.toByteArray());
      
      assertEquals(DOMUtils.parse(expMsg), DOMUtils.parse(wasMsg));
   }
   
   public void testSetContentOnSOAPPart_DOMSource() throws Exception
   {
      String expMsg = 
         "<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/'>" +
         " <soapenv:Header/>" +
         " <soapenv:Body>" +
         "  <HelloResponse xmlns='http://helloservice.org/types\'>" +
         "   <argument>responseBean</argument>" +
         "  </HelloResponse>" +
         " </soapenv:Body>" +
         "</soapenv:Envelope>";
      
      MessageFactory factory = new MessageFactoryImpl();
      
      ByteArrayInputStream inputStream = new ByteArrayInputStream(expMsg.getBytes());
      SOAPMessage soapMsg = factory.createMessage(null, inputStream);
            
      SOAPMessage message = factory.createMessage();
      message.getSOAPPart().setContent(soapMsg.getSOAPPart().getContent());
      message.saveChanges();
      
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      message.writeTo(baos);
      String wasMsg = new String(baos.toByteArray());
      
      assertEquals(DOMUtils.parse(expMsg), DOMUtils.parse(wasMsg));
   }
   
   // [JBWS-1511] MessageFactory does not preserve comments
   public void testPreserveComments() throws Exception
   {
      String expMsg = 
         "<soapenv:Envelope xmlns:ns1='http://somens.org' xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/'>" +
         " <!-- header -->" +
         " <soapenv:Header>" +
         "  <!-- header element -->" +
         "  <ns1:header>kermit</ns1:header>" +
         " </soapenv:Header>" +
         " <!-- body -->" +
         " <soapenv:Body>" +
         "  <!-- body element -->" +
         "  <Hello>" +
         "   <!-- element -->" +
         "   <argument>kermit</argument>" +
         "  </Hello>" +
         " </soapenv:Body>" +
         "</soapenv:Envelope>";
      
      // Verify that DOM parse/write do not modify the message
      Element expEnv = DOMUtils.parse(expMsg);
      assertEquals(expMsg, DOMWriter.printNode(expEnv, false));
      
      doPreserveCommentsPerStyle(expMsg, Style.DOCUMENT);
      doPreserveCommentsPerStyle(expMsg, Style.RPC);
      doPreserveCommentsPerStyle(expMsg, null);
   }

   private void doPreserveCommentsPerStyle(String expXML, Style style) throws IOException, SOAPException
   {
      MessageFactoryImpl factory = new MessageFactoryImpl();
      factory.setStyle(style);
      
      ByteArrayInputStream inputStream = new ByteArrayInputStream(expXML.getBytes());
      SOAPMessage soapMsg = factory.createMessage(null, inputStream);
      SOAPEnvelope wasEnv = soapMsg.getSOAPPart().getEnvelope();
      
      String wasXML = DOMWriter.printNode(wasEnv, false);
      //System.out.println(expXML);
      //System.out.println(wasXML);
      assertEquals(expXML, wasXML);
   }
}