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
package org.jboss.test.ws.jaxws.samples.httpbinding;

// $Id$

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.http.HTTPBinding;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.tools.wsdl.WSDLDefinitionsFactory;
import org.jboss.wsintegration.spi.utils.DOMUtils;
import org.w3c.dom.Element;

/**
 * Test a Provider<SOAPMessage>
 *
 * @author Thomas.Diesler@jboss.org
 * @since 29-Jun-2006
 */
public class HttpPayloadTestCase extends JBossWSTest
{
   private String reqString = "<ns1:somePayload xmlns:ns1='http://org.jboss.ws/httpbinding'>Hello</ns1:somePayload>";

   private String resString = "<ns1:somePayload xmlns:ns1='http://org.jboss.ws/httpbinding'>Hello:InboundLogicalHandler:OutboundLogicalHandler</ns1:somePayload>";

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(HttpPayloadTestCase.class, "jaxws-samples-httpbinding-payload.war");
   }

   public void testWSDLAccess() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-samples-httpbinding-payload?wsdl");
      WSDLDefinitionsFactory factory = WSDLDefinitionsFactory.newInstance();
      WSDLDefinitions wsdlDefinitions = factory.parse(wsdlURL);
      assertNotNull(wsdlDefinitions);
   }

   public void testProviderDispatch() throws Exception
   {
      Dispatch<Source> dispatch = createDispatch("ProviderEndpoint");
      Source resPayload = dispatch.invoke(new DOMSource(DOMUtils.parse(reqString)));

      Element docElement = getElementFromSource(resPayload);
      assertEquals(DOMUtils.parse(resString), docElement);
   }

   private Dispatch<Source> createDispatch(String target) throws MalformedURLException, JAXBException
   {
      String targetNS = "http://org.jboss.ws/httpbinding";
      QName serviceName = new QName(targetNS, "ProviderService");
      QName portName = new QName(targetNS, "ProviderPort");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-samples-httpbinding-payload/" + target + "?wsdl");

      Service service = Service.create(serviceName);
      service.addPort(portName, HTTPBinding.HTTP_BINDING, wsdlURL.toExternalForm());

      Dispatch<Source> dispatch = service.createDispatch(portName, Source.class, Mode.PAYLOAD);
      return dispatch;
   }
}
