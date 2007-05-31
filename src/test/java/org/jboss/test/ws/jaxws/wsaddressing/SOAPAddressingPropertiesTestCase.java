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
package org.jboss.test.ws.jaxws.wsaddressing;

// $Id: SOAPAddressingPropertiesTestCase.java,v 1.2 2005/11/24 21:40:55 tdiesler
// Exp $

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.addressing.AddressingBuilder;
import javax.xml.ws.addressing.soap.SOAPAddressingBuilder;
import javax.xml.ws.addressing.soap.SOAPAddressingProperties;

import org.jboss.util.xml.DOMUtils;
import org.jboss.wsf.spi.test.JBossWSTest;

/**
 * Test the SOAPAddressingProperties
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 13-Nov-2005
 */
public class SOAPAddressingPropertiesTestCase extends JBossWSTest 
{
   private SOAPAddressingProperties addrProps;
   
	private String reqEnvStr = "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>"
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

	private String resEnvStr = "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
         "  <env:Header xmlns:wsa='http://www.w3.org/2005/08/addressing'>" +
         "    <wsa:To>http://business456.example/client1</wsa:To>" +
         "    <wsa:Action>http://fabrikam123.example/ReplyPO</wsa:Action>" +
         "    <wsa:RelatesTo>uuid:6B29FC40-CA47-1067-B31D-00DD010662DA</wsa:RelatesTo>" +
         "    <ns1:sessionid wsa:IsReferenceParameter='true' xmlns:ns1='http://somens'>someuniqueid</ns1:sessionid>" +
         "  </env:Header>" +
         "  <env:Body/>" +
         "</env:Envelope>";

   public void setUp() throws Exception
   {
      super.setUp();
      
      MessageFactory factory = MessageFactory.newInstance();
      SOAPMessage soapMessage = factory.createMessage(null, new ByteArrayInputStream(reqEnvStr.getBytes()));

      AddressingBuilder addrBuilder = SOAPAddressingBuilder.getAddressingBuilder();
      addrProps = (SOAPAddressingProperties)addrBuilder.newAddressingProperties();
      addrProps.readHeaders(soapMessage);
   }
   
	public void testReadHeaders() throws Exception
   {
      assertEquals(new URI("uuid:6B29FC40-CA47-1067-B31D-00DD010662DA"), addrProps.getMessageID().getURI());
      assertEquals(new URI("http://business456.example/client1"), addrProps.getReplyTo().getAddress().getURI());
      assertEquals(new URI("http://fabrikam123.example/Purchasing"), addrProps.getTo().getURI());
      assertEquals(new URI("http://fabrikam123.example/SubmitPO"), addrProps.getAction().getURI());
   }

   public void testWriteHeaders() throws Exception
   {
      MessageFactory factory = MessageFactory.newInstance();
      SOAPMessage wasMsg = factory.createMessage();
      addrProps.writeHeaders(wasMsg);
      
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      wasMsg.writeTo(baos);
      String wasEnv = new String(baos.toByteArray());

      SOAPMessage expMsg = factory.createMessage(null, new ByteArrayInputStream(reqEnvStr.getBytes()));
      baos = new ByteArrayOutputStream();
      expMsg.writeTo(baos);
      String expEnv = new String(baos.toByteArray());
      
      assertEquals(DOMUtils.parse(expEnv), DOMUtils.parse(wasEnv));
   }

   public void testReplyToHeaders() throws Exception
   {
      AddressingBuilder addrBuilder = SOAPAddressingBuilder.getAddressingBuilder();
      SOAPAddressingProperties replyProps = (SOAPAddressingProperties)addrBuilder.newAddressingProperties();
      replyProps.initializeAsReply(addrProps, false);
      replyProps.setAction(addrBuilder.newURI("http://fabrikam123.example/ReplyPO"));
      
      MessageFactory factory = MessageFactory.newInstance();
      SOAPMessage wasMsg = factory.createMessage();
      replyProps.writeHeaders(wasMsg);
      
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      wasMsg.writeTo(baos);
      String wasEnv = new String(baos.toByteArray());

      SOAPMessage expMsg = factory.createMessage(null, new ByteArrayInputStream(resEnvStr.getBytes()));
      baos = new ByteArrayOutputStream();
      expMsg.writeTo(baos);
      String expEnv = new String(baos.toByteArray());
      
      assertEquals(DOMUtils.parse(expEnv), DOMUtils.parse(wasEnv));
   }
}
