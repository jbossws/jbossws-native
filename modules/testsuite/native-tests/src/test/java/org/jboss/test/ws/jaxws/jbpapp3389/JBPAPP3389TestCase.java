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
package org.jboss.test.ws.jaxws.jbpapp3389;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBPAPP-3389] Error marshaling elements with attributes.
 * 
 * https://jira.jboss.org/jira/browse/JBPAPP-3389
 * 
 * @author darran.lofthouse@jboss.com
 * @since 12th January 2010
 */
public class JBPAPP3389TestCase extends JBossWSTest {

	private String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-jbpapp3389";

	public static Test suite() {
		return new JBossWSTestSetup(JBPAPP3389TestCase.class,
				"jaxws-jbpapp3389.war");
	}

	public void testCall() throws Exception {
		URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
		QName serviceName = new QName("http://ws.jboss.org/jbpapp3389",
				"EndpointImplService");
		Endpoint port = Service.create(wsdlURL, serviceName).getPort(
				Endpoint.class);

		Result result = port.echo("Hello");
	}
}
