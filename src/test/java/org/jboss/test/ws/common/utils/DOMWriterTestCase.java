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
package org.jboss.test.ws.common.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.common.DOMUtils;
import org.jboss.wsf.common.DOMWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test the DOMWriter
 *
 * @author Thomas.Diesler@jboss.org
 * @since 10-Aug-2006
 */
public class DOMWriterTestCase extends JBossWSTest
{
   /** The element does not contain the required ns declaration.
    */
   public void testNamespaceCompletionOne() throws Exception
   {
      String inStr = 
         "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
          "<env:Body>" +
           "<env:Fault>" +
            "<faultcode>env:Client</faultcode>" +
            "<faultstring>Endpoint {http://webmethod.jsr181.ws.test.jboss.org/jaws}TestEndpointPort does not contain operation meta data for: {http://webmethod.jsr181.ws.test.jboss.org/jaws}noWebMethod</faultstring>" +
           "</env:Fault>" +
          "</env:Body>" +
         "</env:Envelope>";
      
      Element env = DOMUtils.parse(inStr);
      Element body = DOMUtils.getFirstChildElement(env);
      Element fault = DOMUtils.getFirstChildElement(body);
      
      String expStr = 
         "<env:Fault xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
          "<faultcode>env:Client</faultcode>" +
          "<faultstring>Endpoint {http://webmethod.jsr181.ws.test.jboss.org/jaws}TestEndpointPort does not contain operation meta data for: {http://webmethod.jsr181.ws.test.jboss.org/jaws}noWebMethod</faultstring>" +
         "</env:Fault>";
      
      String wasStr = DOMWriter.printNode(fault, false);
      assertEquals(expStr, wasStr);
   }
   
   /** The element already contains the required ns declaration.
    */
   public void testNamespaceCompletionTwo() throws Exception
   {
      String inStr = 
         "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
          "<env:Body>" +
           "<ns1:rpc xmlns:ns1='http://somens'>" +
            "<ns1:param1/>" +
            "<ns1:param2/>" +
           "</ns1:rpc>" +
          "</env:Body>" +
         "</env:Envelope>";
      
      Element env = DOMUtils.parse(inStr);
      Element body = DOMUtils.getFirstChildElement(env);
      Element rpc = DOMUtils.getFirstChildElement(body);
      
      String expStr = 
         "<ns1:rpc xmlns:ns1='http://somens'>" +
          "<ns1:param1/>" +
          "<ns1:param2/>" +
         "</ns1:rpc>";
      
      String wasStr = DOMWriter.printNode(rpc, false);
      assertEquals(expStr, wasStr);
   }
   
   /** The element does not contain the required ns declaration, the child does.
    */
   public void testNamespaceCompletionThree() throws Exception
   {
      String inStr = 
         "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
          "<env:Body>" +
           "<ns1:rpc xmlns:ns1='http://somens'>" +
            "<ns1:param1/>" +
            "<ns1:param2/>" +
           "</ns1:rpc>" +
          "</env:Body>" +
         "</env:Envelope>";
      
      Element env = DOMUtils.parse(inStr);
      Element body = DOMUtils.getFirstChildElement(env);
      
      String expStr = 
         "<env:Body xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
          "<ns1:rpc xmlns:ns1='http://somens'>" +
           "<ns1:param1/>" +
           "<ns1:param2/>" +
          "</ns1:rpc>" +
         "</env:Body>";
      
      String wasStr = DOMWriter.printNode(body, false);
      assertEquals(expStr, wasStr);
   }
   
   /** The element does not contain the required attribute ns declaration.
    */
   public void testNamespaceCompletionAttribute() throws Exception
   {
      String inStr = 
         "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
          "<env:Header>" +
           "<someHeader env:mustUnderstand='1' xml:lang='en'/>" +
          "</env:Header>" +
          "<env:Body/>" +
         "</env:Envelope>";
      
      Element env = DOMUtils.parse(inStr);
      Element header = DOMUtils.getFirstChildElement(env);
      Element headerElement = DOMUtils.getFirstChildElement(header);
      
      String expStr = 
         "<someHeader env:mustUnderstand='1' xml:lang='en' xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'/>";
      
      String wasStr = DOMWriter.printNode(headerElement, false);
      assertEquals(expStr, wasStr);
   }
   
   public void testEnvelopeWriter() throws Exception
   {
      String expEnv = "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>"
         + "  <env:Header xmlns:wsa='http://www.w3.org/2005/08/addressing'>"
         + "    <wsa:To>http://fabrikam123.example/Purchasing</wsa:To>"
         + "    <wsa:ReplyTo>"
         + "      <wsa:Address>http://business456.example/client1</wsa:Address>"
         + "      <wsa:ReferenceParameters>"
         + "        <ns1:sessionid xmlns:ns1='http://somens'>someuniqueid</ns1:sessionid>"
         + "      </wsa:ReferenceParameters>"
         + "    </wsa:ReplyTo>"
         + "    <wsa:Action>http://fabrikam123.example/SubmitPO</wsa:Action>"
         + "    <wsa:MessageID>uuid:6B29FC40-CA47-1067-B31D-00DD010662DA</wsa:MessageID>"
         + "  </env:Header>" 
         + "  <env:Body/>" 
         + "</env:Envelope>";

      MessageFactory factory = MessageFactory.newInstance();
      SOAPMessage soapMessage = factory.createMessage(null, new ByteArrayInputStream(expEnv.getBytes()));

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      soapMessage.writeTo(baos);
      String wasEnv = new String(baos.toByteArray());
      
      assertEquals(DOMUtils.parse(expEnv), DOMUtils.parse(wasEnv));
   }
   
   public void testEntity() throws Exception
   {
      String expStr = 
         "<xsd:simpleType name='MailRelayConfiguration' xmlns:xsd='http://www.w3.org/2001/XMLSchema'>" +
          "<xsd:restriction base='xsd:string'>" +
           "<xsd:enumeration value='Incoming &amp; Outgoing'/>" +
           "<xsd:enumeration value='None'/>" +
          "</xsd:restriction>" +
         "</xsd:simpleType>";


      Element domEl = DOMUtils.parse(expStr);
      String wasStr = DOMWriter.printNode(domEl, false);
      
      assertEquals(expStr, wasStr);
   }

   // [JBWS-762] DOMUtils.parse skips peer comments on Document node
   public void testDocumentComments() throws Exception
   {
      String expStr = 
         "<?xml version='1.0' encoding='UTF-8'?>" +
         "<!-- Some root comment -->" +
         "<root>" +
          "<!-- Some element comment -->" +
          "<element>some value</element>" +
         "</root>";


      Document doc = DOMUtils.parse(expStr).getOwnerDocument();
      StringWriter strwr = new StringWriter();
      new DOMWriter(strwr, "UTF-8").print(doc);
      String wasStr = strwr.toString();
      
      assertEquals(expStr, wasStr);
   }

   public void testElementNamespaceURIElementNS() throws Exception
   {
      String xmlIn = "<Hello xmlns='http://somens'><Sub>World</Sub></Hello>";
      
      Element root = DOMUtils.createElement(new QName("http://somens", "Hello"));
      assertEquals("http://somens", root.getNamespaceURI());
      Element child = (Element)root.appendChild(DOMUtils.createElement(new QName("Sub")));
      child.appendChild(DOMUtils.createTextNode("World"));

      String xmlOut = DOMWriter.printNode(root, false);
      assertEquals(xmlIn, xmlOut);
   }

   public void testElementNamespaceURIDocumentParse() throws Exception
   {
      String xmlIn = "<Hello xmlns='http://somens'><Sub>World</Sub></Hello>";
      
      Element root = DOMUtils.parse(xmlIn);
      assertEquals("http://somens", root.getNamespaceURI());

      String xmlOut = DOMWriter.printNode(root, false);
      assertEquals(xmlIn, xmlOut);
   }

   public void testElementNamespaceURITransformer() throws Exception
   {
      String xmlIn = "<Hello xmlns='http://somens'><Sub>World</Sub></Hello>";
      StreamSource source = new StreamSource(new ByteArrayInputStream(xmlIn.getBytes()));

      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      DOMResult result = new DOMResult();
      transformer.transform(source, result);
      
      Element root = ((Document)result.getNode()).getDocumentElement();
      assertEquals("http://somens", root.getNamespaceURI());

      String xmlOut = DOMWriter.printNode(root, false);
      assertEquals(xmlIn, xmlOut);
   }
}
