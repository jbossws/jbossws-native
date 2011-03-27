/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
import java.io.File;
import java.io.InputStream;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPMessage;

import org.jboss.wsf.test.JBossWSTest;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Test the SOAPElement
 *
 * @author Thomas.Diesler@jboss.org
 * @since 22-Aug-2005
 */
public class SOAPElementTestCase extends JBossWSTest
{
   private SOAPFactory soapFactory;

   protected void setUp() throws Exception
   {
      super.setUp();
      soapFactory = SOAPFactory.newInstance();
   }

   public void testAddChildElement() throws Exception
   {
      MessageFactory msgFactory = MessageFactory.newInstance();
      SOAPMessage soapMessage = msgFactory.createMessage();
      SOAPEnvelope env = soapMessage.getSOAPPart().getEnvelope();
      SOAPBody body = env.getBody();

      Name name = soapFactory.createName("MyChild1");
      SOAPElement se = body.addChildElement(name);
      assertNotNull("Expected an element", se);

      assertEquals("Expected 1 child element", 1, getIteratorCount(body.getChildElements()));

      SOAPElement se2 = (SOAPElement)body.getChildElements().next();
      assertEquals(se, se2);
   }

   public void testAddChildElementNS() throws Exception
   {
      MessageFactory msgFactory = MessageFactory.newInstance();
      SOAPMessage soapMessage = msgFactory.createMessage();
      SOAPEnvelope env = soapMessage.getSOAPPart().getEnvelope();
      SOAPBody body = env.getBody();

      String s = "MyName1";
      String p = "MyPrefix1";
      String u = "myURI";

      body.addNamespaceDeclaration(p, u);
      assertEquals(u, body.getNamespaceURI(p));

      SOAPElement se = body.addChildElement(s, p);
      assertNotNull("Expected an element", se);

      assertEquals("Expected 1 child element", 1, getIteratorCount(body.getChildElements()));

      SOAPElement se2 = (SOAPElement)body.getChildElements().next();
      assertEquals(se, se2);
   }
   
   //JBWS-2346
   public void testGetElementByTagNameNS() throws Exception
   {
      InputStream is = getResourceURL("common/soap/jbws2346.xml").openStream();
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(is);
      MessageFactory factory = MessageFactory.newInstance();
      SOAPMessage msg = factory.createMessage();
      msg.getSOAPBody().addDocument(doc);
      SOAPBody body = msg.getSOAPBody();
      NodeList list = body.getElementsByTagNameNS("http://org.jboss.ws/testNS", "elementA");
      assertEquals(1, list.getLength());
      list = body.getElementsByTagNameNS("http://org.jboss.ws/testNS", "elementC");
      assertEquals(2, list.getLength());
      list = body.getElementsByTagNameNS("http://org.jboss.ws/testNS", "String_1");
      StringBuilder sb = new StringBuilder();
      for (int i=0; i<list.getLength(); i++)
      {
         Node n = list.item(i);
         sb.append(n.getFirstChild().getNodeValue());
         sb.append(" ");
      }
      assertEquals("Strawberry Apple Banana Orange Raspberry ", sb.toString());
   }
   
   // JBWS-2704
   public void testGetElementByTagNameNS_Wildcard() throws Exception
   {
      InputStream is = getResourceURL("common/soap/jbws2704.xml").openStream();
      MessageFactory messageFactory = MessageFactory.newInstance();
      SOAPMessage soapMessage = messageFactory.createMessage(null, is);
      SOAPEnvelope envelope = soapMessage.getSOAPPart().getEnvelope();
      NodeList nodes = envelope.getElementsByTagNameNS("*", "String_1");
      assertEquals("String_1 nodes", 2, nodes.getLength());
      assertEquals("String_1(0) text content", "Value_1", nodes.item(0).getTextContent());
      assertEquals("String_1(1) text content", "Value_2", nodes.item(1).getTextContent());

      nodes = envelope.getElementsByTagNameNS("*", "String_2");
      assertEquals("String_2 nodes", 2, nodes.getLength());
      assertEquals("String_2(0) text content", "Value_3", nodes.item(0).getTextContent());
      assertEquals("String_2(1) text content", "Value_4", nodes.item(1).getTextContent());

      nodes = envelope.getElementsByTagNameNS("http://org.jboss.ws/testNS1", "*");
      assertEquals("http://org.jboss.ws/testNS1 nodes", 2, nodes.getLength());
      assertEquals("http://org.jboss.ws/testNS1(0) text content", "Value_1", nodes.item(0).getTextContent());
      assertEquals("http://org.jboss.ws/testNS1(1) text content", "Value_3", nodes.item(1).getTextContent());

      nodes = envelope.getElementsByTagNameNS("http://org.jboss.ws/testNS2", "*");
      assertEquals("http://org.jboss.ws/testNS2 nodes", 2, nodes.getLength());
      assertEquals("http://org.jboss.ws/testNS2(0) text content", "Value_2", nodes.item(0).getTextContent());
      assertEquals("http://org.jboss.ws/testNS2(1) text content", "Value_4", nodes.item(1).getTextContent());

      nodes = envelope.getElementsByTagNameNS("http://org.jboss.ws/testNS1", "String_1");
      assertEquals("http://org.jboss.ws/testNS1 String_1 nodes", 1, nodes.getLength());
      assertEquals("http://org.jboss.ws/testNS1 String_1(0) text content", "Value_1", nodes.item(0).getTextContent());

      nodes = envelope.getElementsByTagNameNS("http://org.jboss.ws/testNS2", "String_1");
      assertEquals("http://org.jboss.ws/testNS2 String_1 nodes", 1, nodes.getLength());
      assertEquals("http://org.jboss.ws/testNS2 String_1(0) text content", "Value_2", nodes.item(0).getTextContent());
   }            
   
   // JBWS-2703
   public void testGetElementByTagName() throws Exception
   {
      InputStream is = getResourceURL("common/soap/jbws2703.xml").openStream();
      MessageFactory messageFactory = MessageFactory.newInstance();
      SOAPMessage soapMessage = messageFactory.createMessage(null, is);
      SOAPEnvelope envelope = soapMessage.getSOAPPart().getEnvelope();
      
      NodeList nodeList;
      nodeList = envelope.getElementsByTagName("soapenv:Header");
      assertEquals("soapenv:Header nodes.", 1, nodeList.getLength());
      nodeList = envelope.getElementsByTagName("soapenv:Body");
      assertEquals("soapenv:Body nodes.", 1, nodeList.getLength());
      nodeList = envelope.getElementsByTagName("jbw:echo");
      assertEquals("jbw:echo nodes.", 1, nodeList.getLength());
      nodeList = envelope.getElementsByTagName("arg0");
      assertEquals("arg0 nodes.", 2, nodeList.getLength());
      assertEquals("arg0 text content", "111", nodeList.item(0).getTextContent());
      assertEquals("arg0 text content", "222", nodeList.item(1).getTextContent());
   }
   
   // http://jira.jboss.com/jira/browse/JBWS-773
   public void testGetNamespaceURI() throws Exception
   {
      SOAPElement parent = soapFactory.createElement("bear", "np", "http://northpole.net");
      SOAPElement child = parent.addChildElement("furColor");

      assertEquals("furColor", child.getNodeName());
      assertEquals(soapFactory.createName("furColor"), child.getElementName());
      assertEquals("furColor", child.getLocalName());
      assertNull(child.getNamespaceURI());
   }

   // http://jira.jboss.com/jira/browse/JBWS-774
   public void testGetAllAttributes() throws Exception
   {
      String xml = 
         "<soap:Envelope xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'>" +
         "<soap:Body>" + 
         "<np:bear name='ted' zoo:species='ursus maritimus' xmlns:np='http://northpole.net' xmlns:zoo='http://zoofan.net'/>" + 
         "</soap:Body>" + 
         "</soap:Envelope>";

      SOAPMessage soapMessage = MessageFactory.newInstance().createMessage(null, new ByteArrayInputStream(xml.getBytes()));
      SOAPFactory soapFactory = SOAPFactory.newInstance();
      SOAPElement bearElement = (SOAPElement)soapMessage.getSOAPBody().getChildElements().next();

      assertEquals(2, getIteratorCount(bearElement.getAllAttributes()));

      Iterator it = bearElement.getAllAttributes();
      assertEquals(soapFactory.createName("name"), it.next());
      assertEquals(soapFactory.createName("species", null, "http://zoofan.net"), it.next());
   }
   
   public void testSetTextContent() throws Exception
   {
      InputStream is = getResourceURL("common/soap/jbws2693.xml").openStream();
      MessageFactory messageFactory = MessageFactory.newInstance();
      SOAPMessage soapMessage = messageFactory.createMessage(null, is);
      NodeList nl = soapMessage.getSOAPBody().getChildNodes();
      SOAPElement echo = null;
      for (int i = 0; i < nl.getLength() && echo == null; i++)
      {
         Object current = nl.item(i);
         if (current instanceof SOAPElement)
         {
            echo = (SOAPElement)current;
         }
      }
      nl = echo.getChildNodes();
      SOAPElement arg0 = null;
      for (int i = 0; i < nl.getLength() && arg0 == null; i++)
      {
         Object current = nl.item(i);
         if (current instanceof SOAPElement)
         {
            arg0 = (SOAPElement)current;
         }
      }

      String value = arg0.getTextContent();
      assertEquals("Expected value", "123", value);

      arg0.setTextContent("456");
      value = arg0.getTextContent();
      assertEquals("Expected value", "456", value);
   }

   /** Return the count of iterator elements, rendering the iterator unusable.
    */
   private int getIteratorCount(Iterator i)
   {
      int count = 0;
      while (i.hasNext())
      {
         i.next();
         count++;
      }
      return count;
   }
}
