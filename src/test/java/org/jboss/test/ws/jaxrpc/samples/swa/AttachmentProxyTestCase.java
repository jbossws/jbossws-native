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
package org.jboss.test.ws.jaxrpc.samples.swa;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import javax.activation.DataHandler;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.Stub;
import javax.xml.transform.stream.StreamSource;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.ws.core.jaxrpc.client.ServiceFactoryImpl;

/**
 * Test SOAP with Attachements (SwA) through the JAXRPC dynamic proxy layer.
 *
 * @author Thomas.Diesler@jboss.org
 * @author <a href="mailto:jason@stacksmash.com">Jason T. Greene</a>
 * @since 16-Nov-2004
 */
public class AttachmentProxyTestCase extends JBossWSTest
{
   private static Attachment port;

   /** Deploy the test ear */
   public static Test suite() throws Exception
   {
      return JBossWSTestSetup.newTestSetup(AttachmentProxyTestCase.class, "jaxrpc-samples-swa.war, jaxrpc-samples-swa-client.jar");
   }

   protected void setUp() throws Exception
   {
      super.setUp();

      if (port == null)
      {
         if (isTargetJBoss())
         {
            InitialContext iniCtx = getInitialContext();
            Service service = (Service)iniCtx.lookup("java:comp/env/service/AttachmentService");
            port = (Attachment)service.getPort(Attachment.class);
         }
         else
         {
            ServiceFactoryImpl factory = new ServiceFactoryImpl();
            URL wsdlURL = new File("resources/jaxrpc/samples/swa/WEB-INF/wsdl/Attachment.wsdl").toURL();
            URL mappingURL = new File("resources/jaxrpc/samples/swa/WEB-INF/jaxrpc-mapping.xml").toURL();
            QName qname = new QName("http://org.jboss.ws/samples/swa", "Attachment");
            Service service = factory.createService(wsdlURL, qname, mappingURL);
            port = (Attachment)service.getPort(Attachment.class);
            ((Stub)port)._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, "http://" + getServerHost() + ":8080/jaxrpc-samples-swa");
         }
      }
   }

   /** Send a multipart message with a text/plain attachment part
    */
   public void testSendMimeImageGIF() throws Exception
   {
      URL url = new File("resources/jaxrpc/samples/swa/attach.gif").toURL();

      // On Linux the X11 server must be installed properly to create images successfully.
      // If the image cannot be created in the test VM, we assume it cannot be done on the
      // server either, so we just skip the test
      Image image = null;
      try
      {
         image = Toolkit.getDefaultToolkit().createImage(url);
      }
      catch (Throwable th)
      {
         //log.warn("Cannot create Image: " + th);
      }

      if (image != null)
      {
         String value = port.sendMimeImageGIF("Some text message", new DataHandler(url));
         assertEquals("[pass]", value);
      }
   }

   /** Send a multipart message with a text/plain attachment part
    */
   public void testSendMimeImageJPEG() throws Exception
   {
      URL url = new File("resources/jaxrpc/samples/swa/attach.jpeg").toURL();

      // On Linux the X11 server must be installed properly to create images successfully.
      // If the image cannot be created in the test VM, we assume it cannot be done on the
      // server either, so we just skip the test
      Image image = null;
      try
      {
         image = Toolkit.getDefaultToolkit().createImage(url);
      }
      catch (Throwable th)
      {
         //log.warn("Cannot create Image: " + th);
      }

      if (image != null)
      {
         String value = port.sendMimeImageJPEG("Some text message", image);
         assertEquals("[pass]", value);
      }
   }

   /** Send a multipart message with a text/plain attachment part
    */
   public void testSendMimeTextPlain() throws Exception
   {
      String value = port.sendMimeTextPlain("Some text message", "This is a plain text attachment.");
      assertEquals("[pass]", value);
   }

   /** Send a multipart message with a text/plain attachment part
    */
   public void testSendMimeMultipart() throws Exception
   {
      URL url = new File("resources/jaxrpc/samples/swa/attach.txt").toURL();
      MimeMultipart multipart = new MimeMultipart("mixed");
      MimeBodyPart bodyPart = new MimeBodyPart();
      bodyPart.setDataHandler(new DataHandler(url));
      String bpct = bodyPart.getContentType();
      bodyPart.setHeader("Content-Type", bpct);
      multipart.addBodyPart(bodyPart);

      String value = port.sendMimeMultipart("Some text message", multipart);
      assertEquals("[pass]", value);
   }

   /** Send a multipart message with a text/plain attachment part
    */
   public void testSendMimeTextXML() throws Exception
   {
      FileInputStream stream = new FileInputStream("resources/jaxrpc/samples/swa/attach.xml");
      StreamSource source = new StreamSource(stream);

      String value = port.sendMimeTextXML("Some text message", new DataHandler(source, "text/xml"));
      assertEquals("[pass]", value);
   }

   /** Send a multipart message with a text/plain attachment part
    */
   public void testSendMimeApplicationXML() throws Exception
   {
      FileInputStream stream = new FileInputStream("resources/jaxrpc/samples/swa/attach.xml");
      StreamSource source = new StreamSource(stream);

      String value = port.sendMimeApplicationXML("Some text message", source);
      assertEquals("[pass]", value);
   }
}
