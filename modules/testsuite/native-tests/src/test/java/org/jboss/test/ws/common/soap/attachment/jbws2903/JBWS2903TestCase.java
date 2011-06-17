/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.common.soap.attachment.jbws2903;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPMessage;

import junit.framework.Test;

import org.jboss.ws.core.soap.MessageFactoryImpl;
import org.jboss.ws.core.soap.SOAPMessageImpl;
import org.jboss.ws.core.soap.attachment.MimeConstants;
import org.jboss.ws.core.soap.attachment.MultipartRelatedXOPEncoder;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * This tests to make sure the MTOM/XOP root MIME part contains the charset parameter
 * @author bmaxwell
 */
public class JBWS2903TestCase extends JBossWSTest
{   
   public static Test suite() throws Exception
   {
      return new JBossWSTestSetup(JBWS2903TestCase.class, "");
   }
   
   public void testCharEncodingInRootHeader() throws Exception
   {
      MessageFactoryImpl factory = new MessageFactoryImpl();
      SOAPMessage msg1 = factory.createMessage();
      AttachmentPart attachment1 = msg1.createAttachmentPart();
      attachment1.setContent("this is a test", "text/plain; charset=UTF-8");
      attachment1.setContentId("<attachment1@test.ws.jboss.org>");
      msg1.addAttachmentPart(attachment1);

      if (msg1.saveRequired())
         msg1.saveChanges();

      MultipartRelatedXOPEncoder mrxe = new MultipartRelatedXOPEncoder( (SOAPMessageImpl) msg1);
      mrxe.encodeMultipartRelatedMessage();
      
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      mrxe.writeTo(out);

      if ( ! out.toString().contains("Content-Type: application/xop+xml; charset=UTF-8; type=\"text/xml\""))
         fail("Content-Type does not contain charset=UTF-8");                 
   }
}
