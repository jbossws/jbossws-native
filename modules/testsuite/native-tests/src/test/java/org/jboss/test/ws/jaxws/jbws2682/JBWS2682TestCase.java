/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws2682;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * JBWS2682 Test Case
 * 
 * @author darran.lofthouse@jboss.com
 * @since 16th June 2009
 * @see https://jira.jboss.org/jira/browse/JBWS-2682
 */
public class JBWS2682TestCase extends JBossWSTest
{

   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-jbws2682/TestEndpoint";

   private String badMsgString = 
      "<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/' xmlns:jbw='http://ws.jboss.org/jbws2682'>" + 
      "  <soapenv:Header/>" + "  <soapenv:Body>" + 
      "    <jbw:echo>" + 
      "      <arg0>1-1ciao</arg0>" + 
      "    </jbw:echo>" + 
      "  </soapenv:Body>" + 
      "</soapenv:Envelope>";

   private String goodMsgString = 
      "<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/' xmlns:jbw='http://ws.jboss.org/jbws2682'>" + 
      "  <soapenv:Header/>" + 
      "  <soapenv:Body>" + 
      "    <jbw:echo>" + 
      "      <arg0>646</arg0>" + 
      "    </jbw:echo>" + 
      "  </soapenv:Body>" + 
      "</soapenv:Envelope>";

   public static Test suite() throws Exception
   {
      return new JBossWSTestSetup(JBWS2682TestCase.class, "jaxws-jbws2682.war");
   }

   public void testGoodMessage() throws Exception
   {
      SOAPMessage response = sendMessage(this.goodMsgString);
      SOAPEnvelope resEnv = response.getSOAPPart().getEnvelope();
      SOAPBody resBody = resEnv.getBody();

      Iterator it = resBody.getChildElements(new QName("http://ws.jboss.org/jbws2682", "echoResponse"));
      SOAPElement echoResponse = (SOAPElement)it.next();
      it = echoResponse.getChildElements(new QName("return"));
      SOAPElement returnElement = (SOAPElement)it.next();

      String value = returnElement.getValue();
      assertEquals("Expected return value", "646", returnElement.getValue());
   }

   public void testBadMessage() throws Exception
   {
      if (true)
      {
         System.out.println("FIXME [JBWS-2682] Incorrect Parsing of Badly Formed int.");
         return;
      }
      
      SOAPMessage response = sendMessage(this.badMsgString);
      SOAPEnvelope resEnv = response.getSOAPPart().getEnvelope();
      SOAPFault fault = resEnv.getBody().getFault();
      assertNotNull("Expected fault to be raised", fault);
   }

   private SOAPMessage sendMessage(final String message) throws SOAPException, IOException
   {
      MessageFactory msgFactory = MessageFactory.newInstance();
      SOAPMessage reqMsg = msgFactory.createMessage(null, new ByteArrayInputStream(message.getBytes()));

      SOAPConnection con = SOAPConnectionFactory.newInstance().createConnection();
      SOAPMessage resMsg = con.call(reqMsg, TARGET_ENDPOINT_ADDRESS);

      return resMsg;
   }

}
