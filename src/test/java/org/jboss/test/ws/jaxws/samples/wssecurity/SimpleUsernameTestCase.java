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
package org.jboss.test.ws.jaxws.samples.wssecurity;

// $Id$

import java.io.File;
import java.net.URL;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.ws.core.StubExt;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test WS-Security for Username Token
 *
 * @author <a href="mailto:mageshbk@jboss.com">Magesh Kumar B</a>
 * @since 15-Aug-2007
 * @version $Revision$
 */
public class SimpleUsernameTestCase extends JBossWSTest
{
   /** Deploy the test */
   public static Test suite() throws Exception
   {
      return new JBossWSTestSetup(SimpleUsernameTestCase.class, "jaxws-samples-wssecurity-username.war");
   }

   /**
    * Test SOAP Envelope for Username Token
    */
   public void testUsernameToken() throws Exception
   {
      UsernameEndpoint username = getPort();
      String retObj = username.getUsernameToken();
      
      System.out.println("FIXME [JBWS-1766]: UsernameToken ignores BindingProvider.USERNAME_PROPERTY");
      //assertTrue(retObj.indexOf("UsernameToken") > 0);
   }

   private UsernameEndpoint getPort() throws Exception
   {
      URL wsdlURL = new File("resources/jaxws/samples/wssecurity/simple-username/META-INF/wsdl/UsernameService.wsdl").toURL();
      URL securityURL = new File("resources/jaxws/samples/wssecurity/simple-username/META-INF/jboss-wsse-client.xml").toURL();
      QName serviceName = new QName("http://org.jboss.ws/samples/wssecurity", "UsernameService");

      Service service = Service.create(wsdlURL, serviceName);
      
      UsernameEndpoint port = (UsernameEndpoint)service.getPort(UsernameEndpoint.class);
      ((StubExt)port).setSecurityConfig(securityURL.toExternalForm());
      ((StubExt)port).setConfigName("Standard WSSecurity Client");

      Map<String, Object> reqContext = ((BindingProvider)port).getRequestContext();
	   reqContext.put(BindingProvider.USERNAME_PROPERTY, "kermit");
      reqContext.put(BindingProvider.PASSWORD_PROPERTY, "thefrog");
      // If these below parameters are set it appears in the log, but the test fails as another
      // request for the getHeader() returns empty <env:Header></env:Header>
      //reqContext.put("javax.xml.rpc.security.auth.username", "kermit");
      //reqContext.put("javax.xml.rpc.security.auth.password", "thefrog");
      reqContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "http://" + getServerHost() + ":8080/jaxws-samples-wssecurity-username");

      return port;
   }
}