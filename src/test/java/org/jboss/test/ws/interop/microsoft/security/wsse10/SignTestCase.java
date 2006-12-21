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
package org.jboss.test.ws.interop.microsoft.security.wsse10;

import junit.framework.Test;
import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.test.ws.interop.microsoft.ClientScenario;
import org.jboss.test.ws.interop.microsoft.InteropConfigFactory;

import javax.naming.InitialContext;
import javax.xml.rpc.Service;
import javax.xml.rpc.Stub;

/**
 * @author Heiko Braun, <heiko@openj.net>
 * @since 07-Mar-2006
 */
public class SignTestCase extends JBossWSTest  {

   IPingServiceSign port;
   PingService10Sign service;

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(SignTestCase.class, "jbossws-interop-wsse10-client.jar");
   }

   protected void setUp() throws Exception
   {
      super.setUp();

      if (port == null)
      {
         InitialContext iniCtx = getInitialContext();
         Service signService = (Service)iniCtx.lookup("java:comp/env/service/interop/IPingServiceSign");
         port = (IPingServiceSign)signService.getPort(IPingServiceSign.class);
         configureClient();
      }

      System.setProperty("org.jboss.ws.wsse.keyStore", "resources/interop/microsoft/security/wsse10Shared/META-INF/alice.jks");
      System.setProperty("org.jboss.ws.wsse.trustStore", "resources/interop/microsoft/security/wsse10Shared/META-INF/wsse10.truststore");
      System.setProperty("org.jboss.ws.wsse.keyStorePassword", "password");
      System.setProperty("org.jboss.ws.wsse.trustStorePassword", "password");
      System.setProperty("org.jboss.ws.wsse.keyStoreType", "jks");
      System.setProperty("org.jboss.ws.wsse.trustStoreType", "jks");
   }

   private void configureClient() {

      InteropConfigFactory factory = InteropConfigFactory.newInstance();
      ClientScenario scenario = factory.createClientScenario(System.getProperty("client.scenario"));
      if(scenario!=null)
      {
         log.info("Using scenario: " + scenario);
         ((Stub)port)._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, scenario.getTargetEndpoint().toString());
      }
      else
      {
         throw new IllegalStateException("Failed to load client scenario");
      }
   }

   // Scenario 3.2. X509 Mutual Authentication, Sign Only
   public void testSignOnly() throws Exception{

      PingResponseBody pingResponse = port.ping(
            new Ping("SignOnly", "JBossWS", "Hello World")
      );
      assertNotNull(pingResponse);

      EchoResponse echoResponse = port.echo( new Echo("JBossWS Sign Only"));
      assertNotNull(echoResponse);
   }
}
