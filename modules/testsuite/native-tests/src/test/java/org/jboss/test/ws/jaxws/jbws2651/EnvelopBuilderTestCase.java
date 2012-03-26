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
package org.jboss.test.ws.jaxws.jbws2651;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.jboss.ws.core.CommonSOAPFaultException;
import org.jboss.ws.core.soap.EnvelopeBuilderDOM;
import org.jboss.ws.core.soap.MessageFactoryImpl;
import org.jboss.ws.core.soap.SOAPMessageImpl;

public class EnvelopBuilderTestCase extends TestCase {
	
	EnvelopeBuilderDOM domBuilder = new EnvelopeBuilderDOM();
	MessageFactoryImpl factory = new MessageFactoryImpl();

	public void testEmptyInputStream() throws Exception {		
	    InputStream ins = new ByteArrayInputStream("".getBytes());    
		factory.createMessage(null, ins, false);
	}
	
	public void testDomInputStream() throws Exception {
		String soapMsg  =
	         "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
	         "  <env:Header/>" +
	         "  <env:Body>" +
	         "    <ns1:addItemResponse xmlns:ns1='http://org.jboss.ws/addressing/replyto'>" +
	         "      <result>Mars Bar</result>" +
	         "    </ns1:addItemResponse>" +
	         "  </env:Body>" +
	         "</env:Envelope>";
		SOAPMessageImpl soapMessage = (SOAPMessageImpl)factory.createMessage();
	    InputStream ins = new ByteArrayInputStream(soapMsg.getBytes());
		assertNotNull(domBuilder.build(soapMessage, ins, false));
	}
	
	
	public void testDomErroStream(){
		String soapMsg  =
	         "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
	         "  <env:Header/>" +
	         "  <env:Body>" +
	         "    <ns1:addItemResponse xmlns:ns1='http://org.jboss.ws/addressing/replyto'>" +
	         "      <result>Mars Bar</result>" +
	         "    </ns1:addItemResponse>" +
	         "  </env:Body>" +
	         "</env:Envelope";
	    
		try {
			SOAPMessageImpl soapMessage = (SOAPMessageImpl)factory.createMessage();
			InputStream ins = new ByteArrayInputStream(soapMsg.getBytes());
			domBuilder.build(soapMessage, ins, false);
			fail("expected SOAPException");
		} catch (Exception e) {
			assertTrue(e instanceof CommonSOAPFaultException);
		}
	}
	
   //JBWS3159	
   public void testDifferentNSPrefix() throws Exception
   {
      String soapMsg = "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>"
            + "<S:Header xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'/>"
            + "<S:Body xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'>"
            + "<ns1:addItemResponse xmlns:ns1='http://org.jboss.ws/addressing/replyto'>"
            + "<result>Mars Bar</result></ns1:addItemResponse></S:Body></env:Envelope>";
      SOAPMessageImpl soapMessage = (SOAPMessageImpl) factory.createMessage();
      StringReader strReader = new java.io.StringReader(soapMsg);
      StreamSource streamSource2 = new StreamSource(strReader);
      soapMessage.getSOAPPart().setContent(streamSource2);
      
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      soapMessage.writeTo(bout);
      assertTrue(new String(bout.toByteArray()).indexOf("S:Header") > -1);
      assertTrue(new String(bout.toByteArray()).indexOf("S:Body") > -1);
   }
	
	
}
