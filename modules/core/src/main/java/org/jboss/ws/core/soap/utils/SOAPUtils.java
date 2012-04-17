/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ws.core.soap.utils;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPMessage;

import org.jboss.ws.core.soap.MessageFactoryImpl;
import org.jboss.ws.core.soap.SOAPFactoryImpl;

/**
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 *
 */
public final class SOAPUtils {

	private SOAPUtils() {
		// forbidden instantiation
	}
	
	public static MessageFactory newSOAP11MessageFactory() {
		return newMessageFactory(SOAPConstants.SOAP_1_1_PROTOCOL);
	}

	public static MessageFactory newSOAP12MessageFactory() {
		return newMessageFactory(SOAPConstants.SOAP_1_2_PROTOCOL);
	}

	private static MessageFactory newMessageFactory(final String protocol) {
		try {
			return new MessageFactoryImpl(protocol);
		} catch (final SOAPException ignore) {
			return null;
		}
		// TODO: use standard SOAP API to create objects
		//return MessageFactory.newInstance()
	}

	public static SOAPFactory newSOAP11Factory() {
		return newSOAPFactory(SOAPConstants.SOAP_1_1_PROTOCOL);
	}

	public static SOAPFactory newSOAP12Factory() {
		return newSOAPFactory(SOAPConstants.SOAP_1_2_PROTOCOL);
	}

	private static SOAPFactory newSOAPFactory(final String protocol) {
		try {
			return new SOAPFactoryImpl(protocol);
		} catch (final SOAPException ignore) {
			return null;
		}
		// TODO: use standard SOAP API to create objects
		//return MessageFactory.newInstance()
	}

	public static boolean isFaultMessage(final SOAPMessage msg) {
		try {
			return msg.getSOAPBody().getFault() != null;
		} catch (final Exception ignore) {
		}
		return false;
	}

	public static Name newName(final QName faultCode, final SOAPEnvelope soapEnvelope) throws SOAPException {
		return soapEnvelope.createName(faultCode.getLocalPart(), faultCode.getPrefix(), faultCode.getNamespaceURI());
	}

	public static QName toQName(final Name name) {
		return new QName(name.getURI(), name.getLocalName(), name.getPrefix());
	}

}
