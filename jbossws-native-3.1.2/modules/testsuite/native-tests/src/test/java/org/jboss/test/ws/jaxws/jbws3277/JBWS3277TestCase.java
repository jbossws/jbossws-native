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
package org.jboss.test.ws.jaxws.jbws3277;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

import junit.framework.Assert;
import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestSetup;
import org.w3c.dom.Document;

public class JBWS3277TestCase extends JBossWSTest
{
   private String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-jbws3277";

   public static Test suite() throws Exception
   {
      return new JBossWSTestSetup(JBWS3277TestCase.class, "jaxws-jbws3277.war");
   }

   public void testMtomSawpFile() throws Exception
   {
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
      QName serviceName = new QName("http://ws.jboss.org/jbws3277", "TestEndpointService");
      Endpoint port = Service.create(wsdlURL, serviceName).getPort(Endpoint.class);
      SOAPBinding binding = (SOAPBinding) ((BindingProvider) port).getBinding();
      binding.setMTOMEnabled(true);
      File file = getResourceFile("jaxws/jbws3277/test-message.xml");

      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
      Document doc = docBuilder.parse(file);
      Source source = new DOMSource(doc);

      MTOMRequest request = new MTOMRequest();
      request.setRequestXML(source);
      request.setId("text/xml mtom request");
      MTOMResponse mtomResponse = port.echo(request);
      Assert.assertEquals("text/xml mtom response", mtomResponse.getResponse());
      source = mtomResponse.getResponseXML();

      Transformer t = javax.xml.transform.TransformerFactory.newInstance().newTransformer();
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      Result result = new StreamResult(os);
      t.transform(source, result);
      assertEquals(375, os.toByteArray().length);
   }

}
