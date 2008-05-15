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
package org.jboss.test.ws.interop.wsse;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * @author Heiko Braun, <heiko@openj.net>
 * @since 07-Mar-2006
 */
public class SignTestCase extends AbstractWSSEBase {
   private ClassLoader origCL;

   public static Test suite()
   {
      return new JBossWSTestSetup(SignTestCase.class, "jbossws-interop-wsse10Sign.war");
   }

   protected void setUp() throws Exception
   {
      origCL = addClientConfToClasspath("jbossws-interop-wsse10Sign-client.jar"); 
   }
   
   protected void tearDown() throws Exception
   {
      Thread.currentThread().setContextClassLoader(origCL);
   }

   String getEndpointURL() {
      return "http://"+getServerHost()+":8080/wsse10Sign/endpoint";
   }

   // Scenario 3.2. X509 Mutual Authentication, Sign Only
   public void testSignOnly() throws Exception{

      //
      Ping request = new Ping();

      request.setScenario("testSignOnly");
      request.setOrigin("JBossWS");
      request.setText("Hello World, signed");
      port.ping(request);

      Echo request2 = new Echo();
      request2.setRequest("Hello World");
      EchoResponse response = port.echo(request2);
      assertNotNull(response);
      assertEquals(response.getEchoResult(), request2.getRequest());
   }
}
