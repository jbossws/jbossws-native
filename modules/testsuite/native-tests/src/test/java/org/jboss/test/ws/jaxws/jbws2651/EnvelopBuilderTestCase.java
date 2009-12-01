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
import java.io.IOException;
import java.io.InputStream;

import javax.xml.soap.SOAPException;


import org.jboss.ws.core.CommonSOAPFaultException;
import org.jboss.ws.core.soap.EnvelopeBuilderDOM;
import org.jboss.ws.core.soap.EnvelopeBuilderStax;
import org.jboss.ws.core.soap.MessageFactoryImpl;
import org.jboss.ws.core.soap.SOAPMessageImpl;

import junit.framework.TestCase;

public class EnvelopBuilderTestCase extends TestCase {
	
	EnvelopeBuilderDOM domBuilder = new EnvelopeBuilderDOM();
	EnvelopeBuilderStax staxBuilder = new EnvelopeBuilderStax();
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
	
	
	public void testStaxInputStream() throws Exception {
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
		assertNotNull(staxBuilder.build(soapMessage, ins, false));
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
	
	
	public void testStaxErroStream(){
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
			staxBuilder.build(soapMessage, ins, false);
			fail("expected IOException");
		} catch (Exception e) {
			assertTrue(e instanceof IOException);
		}
	}

}
