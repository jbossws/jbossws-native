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
package org.jboss.test.ws.jaxws.samples.oneway;

import java.io.StringReader;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.ws.core.utils.DOMWriter;
import org.jboss.ws.utils.DOMUtils;
import org.w3c.dom.Element;

/**
 * Test the JSR-181 annotation: javax.jws.Oneway
 *
 * @author Thomas.Diesler@jboss.org
 * @since 07-Oct-2005
 */
public class JSR181OneWayTestCase extends JBossWSTest
{
   private static final String targetNS = "http://oneway.samples.jaxws.ws.test.jboss.org/";

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(JSR181OneWayTestCase.class, "jaxws-samples-oneway.war");
   }

   public void testWebService() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-samples-oneway/TestService?wsdl");
      QName serviceName = new QName(targetNS, "PingEndpointService");
      QName portName = new QName(targetNS, "PingEndpointPort");
      Service service = Service.create(wsdlURL, serviceName);
      Dispatch dispatch = service.createDispatch(portName, StreamSource.class, Mode.PAYLOAD);

      String payload = "<ns1:ping xmlns:ns1='http://oneway.samples.jaxws.ws.test.jboss.org/'/>";
      dispatch.invokeOneWay(new StreamSource(new StringReader(payload)));

      payload = "<ns1:feedback xmlns:ns1='http://oneway.samples.jaxws.ws.test.jboss.org/'/>";
      Source retObj = (Source)dispatch.invoke(new StreamSource(new StringReader(payload)));
      
      Element docElement = getElementFromSource(retObj);
      Element retElement = DOMUtils.getFirstChildElement(docElement);
      String retPayload = DOMWriter.printNode(retElement, false);
      assertEquals("<return>ok</return>", retPayload);
   }
}
