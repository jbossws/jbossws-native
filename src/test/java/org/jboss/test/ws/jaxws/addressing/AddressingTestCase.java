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
package org.jboss.test.ws.jaxws.addressing;

import junit.framework.TestCase;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.addressing.AddressingException;
import java.io.ByteArrayInputStream;

import org.jboss.ws.extensions.addressing.soap.SOAPAddressingPropertiesImpl;

/**
 * Verify the ws-addressing parsing logic
 * 
 * @author Heiko.Braun@jboss.com
 * @version $Revision$
 */
public class AddressingTestCase extends TestCase
{

	private String ERRORNOUS_XML = "<soap:Envelope xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'>\n" +
			" <soap:Header xmlns:wsa='http://www.w3.org/2005/08/addressing'>" +			
			"<wsa:Action></wsa:Action>\n" +
			"<wsa:MessageID>urn:uuid:1fa5a31f-bbe7-4ad5-8b92-d765f4a32dc9</wsa:MessageID>\n" +
			"<wsa:ReplyTo>\n" +
			"<wsa:Address>http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous</wsa:Address>\n" +
			"<wsa:ReferenceParameters>\n" +
			"<ns1:clientid xmlns:ns1=\"http://somens\">clientid-1</ns1:clientid>\n" +
			"</wsa:ReferenceParameters>\n" +
			"</wsa:ReplyTo>\n" +
			"<wsa:To>http://mycomp:8080/testws</wsa:To>\n" +			
			"</soap:Header>" +
			" <soap:Body/>\n" +
			"</soap:Envelope>";
	

	public void testReplyToWithoutAction() throws Exception
	{
		MessageFactory mf = MessageFactory.newInstance();
		SOAPMessage message = mf.createMessage(null, new ByteArrayInputStream(ERRORNOUS_XML.getBytes()));

		SOAPAddressingPropertiesImpl props = new SOAPAddressingPropertiesImpl();
		try
		{
			props.readHeaders(message);
			fail("ERRORNOUS_XML should cause a parsing exception due to missing wsa:Action value");
		}
		catch (AddressingException e)
		{
			// expected an exception
		}

	}
}
