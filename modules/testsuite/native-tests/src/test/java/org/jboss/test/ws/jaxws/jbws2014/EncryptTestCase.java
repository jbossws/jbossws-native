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
package org.jboss.test.ws.jaxws.jbws2014;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

import junit.framework.Test;

import org.jboss.ws.core.StubExt;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test JBWS-2014 (WS-Security with MTOM) - signature + encryption
 *
 * @author alessio.soldano@jboss.com
 * @since 01-May-2008
 */
public class EncryptTestCase extends JBossWSTest
{
   public static Test suite() throws Exception
   {
      return new JBossWSTestSetup(EncryptTestCase.class, "jaxws-jbws2014-encrypt.jar");
   }
   
   @SuppressWarnings("unchecked")
   public void testEndpoint() throws Exception
   {
      TestDto in0 = new TestDto();
      in0.setId("myId");
      String text = "This is the attachment for id: myId";
      in0.setContent(new DataHandler(text, "text/plain"));
      TestEndpoint port = getPort();
      TestDto retObj = port.echo(in0);
      
      Map<String, Object> ctx = ((BindingProvider)port).getResponseContext();
      Object resContentType = ((HashMap<String,Object>)ctx.get("javax.xml.ws.http.response.headers")).get("Content-Type");
      
      //Inline data when using encryption
      assertTrue(resContentType.toString().contains("text/xml"));
      assertTrue(!resContentType.toString().contains("multipart"));
      
      
      assertEquals("myId", retObj.getId());
      Object result = retObj.getContent().getContent();
      if (result instanceof InputStream)
      {
         InputStream in = (InputStream)result;
         StringBuilder out = new StringBuilder();
         byte[] b = new byte[4096];
         for (int n; (n = in.read(b)) != -1;) {
             out.append(new String(b, 0, n));
         }
         result = out.toString();
      }
      assertEquals(text, result);
   }

   private TestEndpoint getPort() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws2014?wsdl");
      URL securityURL = getResourceURL("jaxws/jbws2014/encrypt/META-INF/jboss-wsse-client.xml");
      QName serviceName = new QName("http://org.jboss.ws/jbws2014", "TestService");

      Service service = Service.create(wsdlURL, serviceName);
      
      TestEndpoint port = (TestEndpoint)service.getPort(TestEndpoint.class);
      ((StubExt)port).setSecurityConfig(securityURL.toExternalForm());
      ((StubExt)port).setConfigName("Standard WSSecurity Client");
      
      SOAPBinding binding = (SOAPBinding)((BindingProvider)port).getBinding();
      binding.setMTOMEnabled(true);
      return port;
   }
}
