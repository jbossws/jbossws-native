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
package org.jboss.test.ws.jaxws.jbws2526;

import java.net.URL;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPMessage;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-2526] org.jboss.ws.core.soap.TextImpl does not implement
 * org.w3c.dom.Comment.
 *
 * @author <a href="mailto:gturner@unzane.com">Gerald Turner</a>
 */
public class JBWS2526TestCase extends JBossWSTest
{

   public static Test suite() throws Exception
   {
      return new JBossWSTestSetup(JBWS2526TestCase.class, "jaxws-jbws2526.war");
   }

   public void testWithoutComment() throws Exception
   {
      test("jaxws/jbws2526/request-message-without-comment.xml");
   }

   public void testWithComment() throws Exception
   {
      test("jaxws/jbws2526/request-message-with-comment.xml");
   }

   private void test(String requestFile) throws Exception
   {
      URL requestURL = getResourceFile(requestFile).toURL();
      MessageFactory messageFactory = MessageFactory.newInstance();
      SOAPMessage requestMessage = messageFactory.createMessage(null, requestURL.openStream());
      URL endpointURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws2526/");
      SOAPConnection connection = SOAPConnectionFactory.newInstance().createConnection();
      SOAPMessage responseMessage = connection.call(requestMessage, endpointURL);
      SOAPBody responseBody = responseMessage.getSOAPPart().getEnvelope().getBody();

      assertFalse(responseBody.hasFault());

      // Assume request-message-*.xml has Value1 = 9
      // LogicalHandler will replace Value2 = 2
      assertEquals("18", responseBody.getFirstChild().getFirstChild().getFirstChild().getNodeValue());
   }

}
