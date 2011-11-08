/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws3380;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.jboss.wsf.test.JBossWSTest;

/**
 * [JBWS-3380] NPE when dealing with default namespace on SOAP Envelope
 * 
 * @author alessio.soldano@jboss.com
 */
public class JBWS3380TestCase extends JBossWSTest
{
   private final String requestMessage = "<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:rep=\"http://jbws3380.jaxws.ws.test.jboss.org/\">"
         + "  <Header/>"
         + "  <Body>"
         + "    <rep:helloWorld>"
         + "       <!--Optional:-->"
         + "       <name>?</name>"
         + "    </rep:helloWorld>"
         + "  </Body>"
         + "</Envelope>";

   public void testParseRequestMessage() throws SOAPException, IOException
   {
      MessageFactory msgFactory = MessageFactory.newInstance();
      SOAPMessage reqMsg = msgFactory.createMessage(null, new ByteArrayInputStream(requestMessage.getBytes()));
      assertNotNull(reqMsg);
   }
}