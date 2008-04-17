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
package org.jboss.test.ws.jaxrpc.wsse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;

import org.jboss.ws.core.soap.MessageFactoryImpl;
import org.jboss.ws.extensions.security.Constants;
import org.jboss.ws.extensions.security.EncryptionOperation;
import org.jboss.ws.extensions.security.OperationDescription;
import org.jboss.ws.extensions.security.QNameTarget;
import org.jboss.ws.extensions.security.RequireEncryptionOperation;
import org.jboss.ws.extensions.security.RequireSignatureOperation;
import org.jboss.ws.extensions.security.SecurityDecoder;
import org.jboss.ws.extensions.security.SecurityEncoder;
import org.jboss.ws.extensions.security.SecurityStore;
import org.jboss.ws.extensions.security.SendUsernameOperation;
import org.jboss.ws.extensions.security.SignatureOperation;
import org.jboss.ws.extensions.security.Target;
import org.jboss.ws.extensions.security.TimestampOperation;
import org.jboss.ws.extensions.security.Util;
import org.jboss.ws.extensions.security.WsuIdTarget;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.common.DOMWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Simple WS-Security round trip test
 *
 * @author <a href="mailto:jason.greene@jboss.com>Jason T. Greene</a>
 */
public class RoundTripTestCase extends JBossWSTest
{
   /** Test that we can build an envelope from InputStream */
   public void testRoundTrip() throws Exception
   {
      String envStr = "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" + " <env:Header>"
            + "  <tns:someHeader xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'"
            + "    tns:test='hi' xmlns:tns='http://org.jboss.ws/2004'>some header value</tns:someHeader>" + " </env:Header> " + " <env:Body>"
            + "  <tns:echoString2 xmlns:tns='http://org.jboss.ws/2004'>" + "   <string>Hello World!</string>" + "  </tns:echoString2>"
            + "  <tns:echoString xmlns:tns='http://org.jboss.ws/2004'>" + "   <string>Hello World!</string>" + "  </tns:echoString>" + " </env:Body>"
            + "</env:Envelope>";

      ByteArrayInputStream inputStream = new ByteArrayInputStream(envStr.getBytes());

      MessageFactory factory = new MessageFactoryImpl();
      SOAPMessage soapMsg = factory.createMessage(null, inputStream);
      SOAPEnvelope env = soapMsg.getSOAPPart().getEnvelope();
      Document doc = env.getOwnerDocument();

      String inputString = DOMWriter.printNode(soapMsg.getSOAPPart(), true);

      SecurityEncoder encoder = new SecurityEncoder(buildOperations(), new SecurityStore());
      encoder.encode(doc);

      log.debug("Encoded message:" + DOMWriter.printNode(doc, true));

      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      soapMsg.writeTo(stream);

      ByteArrayInputStream instream = new ByteArrayInputStream(stream.toByteArray());
      soapMsg = factory.createMessage(null, instream);
      env = soapMsg.getSOAPPart().getEnvelope();
      doc = env.getOwnerDocument();


      SecurityDecoder decoder = new SecurityDecoder(new SecurityStore(), null);
      decoder.decode(doc);
      decoder.verify(buildRequireOperations());
      decoder.complete();

      cleanupWsuIds(doc.getDocumentElement());

      log.debug("Decoded message:" + DOMWriter.printNode(doc, true));

      assertEquals(inputString, DOMWriter.printNode(doc, true));
   }

   // WS-Security leaves wsu:id attributes arround on elements which are not cleaned
   // up due to performance reasons. This, however, breaks comparisons, so we manually
   // fix this for tests.
   private void cleanupWsuIds(Element element)
   {
      element.removeAttributeNS(Constants.WSU_NS, "Id");
      element.removeAttribute("xmlns:wsu");

      Element child = Util.getFirstChildElement(element);
      while (child != null)
      {
         cleanupWsuIds(child);
         child = Util.getNextSiblingElement(child);
      }
   }

   private LinkedList buildOperations()
   {
      List targets = new ArrayList();
      QName name = new QName("http://org.jboss.ws/2004", "echoString2");
      Target target = new QNameTarget(name);
      targets.add(target);
      name = new QName("http://org.jboss.ws/2004", "someHeader");
      target = new QNameTarget(name);
      targets.add(target);
      targets.add(new WsuIdTarget("timestamp"));

      LinkedList operations = new LinkedList();
      operations.add(new OperationDescription(TimestampOperation.class, null, null, "300", null));
      operations.add(new OperationDescription(SignatureOperation.class, targets, "wsse", null, null));

      name = new QName("http://org.jboss.ws/2004", "someHeader");
      target = new QNameTarget(name);
      targets = new ArrayList();
      targets.add(target);

      name = new QName("http://org.jboss.ws/2004", "echoString2");
      target = new QNameTarget(name, true);
      targets.add(target);

      operations.add(new OperationDescription(EncryptionOperation.class, targets, "wsse", null, null));
      operations.add(new OperationDescription(SendUsernameOperation.class, null, "hi", "there", null));

      return operations;
   }

   private LinkedList buildRequireOperations()
   {
      List targets = new ArrayList();
      QName name = new QName("http://org.jboss.ws/2004", "echoString2");
      Target target = new QNameTarget(name);
      targets.add(target);
      name = new QName("http://org.jboss.ws/2004", "someHeader");
      target = new QNameTarget(name);
      targets.add(target);
      //targets.add(new WsuIdTarget("timestamp"));
      LinkedList operations = new LinkedList();
      operations.add(new OperationDescription(RequireSignatureOperation.class, targets, null, null, null));
      operations.add(new OperationDescription(RequireEncryptionOperation.class, targets, null, null, null));

      return operations;
   }
}
