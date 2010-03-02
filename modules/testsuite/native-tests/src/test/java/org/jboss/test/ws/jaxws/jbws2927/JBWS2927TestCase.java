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
package org.jboss.test.ws.jaxws.jbws2927;

import java.net.URL;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.addressing.JAXWSAConstants;

import junit.framework.Test;

import org.jboss.ws.core.jaxws.handler.SOAPMessageContextJAXWS;
import org.jboss.ws.core.jaxws.spi.EndpointImpl;
import org.jboss.ws.core.soap.NodeImpl;
import org.jboss.ws.extensions.addressing.jaxws.WSAddressingClientHandler;
import org.jboss.ws.extensions.addressing.soap.SOAPAddressingPropertiesImpl;
import org.jboss.ws.metadata.umdm.ServerEndpointMetaData;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A JBWS2927TestCase.
 * 
 * @author <a href="ema@redhat.com">Jim Ma</a>
 */
public class JBWS2927TestCase extends JBossWSTest
{
   
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-jbws2927";

   private static WSAEndpoint port;

   public static Test suite() throws Exception
   {
      return new JBossWSTestSetup(JBWS2927TestCase.class, "jaxws-jbws2927.war");
   }

   public void setUp() throws Exception
   {
      super.setUp();
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
      QName serviceName = new QName("http://ws.jboss.org/jbws2927", "WSAEndpointService");

      Service service = Service.create(wsdlURL, serviceName);
      port = service.getPort(WSAEndpoint.class);
   }

   public void testCall() throws Exception
   {     
      String response = port.echo("testJBWS2927");
      assertEquals("testJBWS2927", response);
   }
  
   public void testSOAPRequestWithoutWsaHeader() throws Exception
   {
      SOAPMessage reqMsg = getRequestMessage();
      URL epURL = new URL(TARGET_ENDPOINT_ADDRESS);
      SOAPConnection con = SOAPConnectionFactory.newInstance().createConnection();
      SOAPMessage resMsg = con.call(reqMsg, epURL);
      SOAPEnvelope resEnv = resMsg.getSOAPPart().getEnvelope();

      String response = "";

      SOAPBody body = resEnv.getBody();
      Iterator it = body.getChildElements(new QName("http://ws.jboss.org/jbws2927", "echoResponse"));
      Node node = (Node)it.next();
      NodeList nodes = node.getChildNodes();
      for (int i = 0; i < nodes.getLength(); i++)
      {
         Node current = nodes.item(i);
         if (current.getNodeName().equals("return"))
         {
            response = ((NodeImpl)current).getValue();
         }
      }

      // The logical handler should have replaced the incoming String.
      assertEquals("testJBWS2927", response);
   }

   private SOAPMessage getRequestMessage() throws Exception
   {
      URL reqMessage = getResourceFile("jaxws/jbws2927/request-message.xml").toURL();
      MessageFactory msgFactory = MessageFactory.newInstance();

      SOAPMessage reqMsg = msgFactory.createMessage(null, reqMessage.openStream());
      return reqMsg;
   }

}