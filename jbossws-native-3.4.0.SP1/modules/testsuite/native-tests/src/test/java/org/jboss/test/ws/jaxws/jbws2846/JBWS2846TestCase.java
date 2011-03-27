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
package org.jboss.test.ws.jaxws.jbws2846;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import org.jboss.wsf.test.JBossWSTest;

/**
 * [JBWS-2846] Service.create(java.net.URL, javax.xml.namespace.QName) should
 * throw javax.xml.ws.WebServiceException not
 * org.jboss.ws.metadata.wsdl.WSDLException
 * 
 * @author darran.lofthouse@jboss.com
 * @since 14th January 2010
 * @see https://jira.jboss.org/jira/browse/JBWS-2846
 */
public class JBWS2846TestCase extends JBossWSTest {

	// This endpoint is not deployed so the attempt to retrieve the WSDL will fail.
	public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-jbws2846/";

	public void testCreate() throws Exception {
		URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
		QName serviceName = new QName("http://ws.jboss.org/jbws2846",
				"EndpointImplService");		
		
		try {
			Service service = Service.create(wsdlURL, serviceName);
			fail("Expected WebServiceException not thrown.");
		} catch (WebServiceException expected) {
			// Expected so ignore.
		}

	}

}
