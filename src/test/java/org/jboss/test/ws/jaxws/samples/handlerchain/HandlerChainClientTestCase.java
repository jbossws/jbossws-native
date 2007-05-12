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
package org.jboss.test.ws.jaxws.samples.handlerchain;

import junit.framework.Test;

import org.jboss.ejb3.client.ClientLauncher;
import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;

/**
 * Test the JSR-181 annotation: javax.jws.HandlerChain
 *
 * @author Thomas.Diesler@jboss.org
 * @since 15-Oct-2005
 */
public class HandlerChainClientTestCase extends JBossWSTest
{
   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(HandlerChainClientTestCase.class, "jaxws-samples-handlerchain.war, jaxws-samples-handlerchain-client.jar");
   }

   public void testHandlerChainOnWebServiceRef() throws Throwable
   {
      String resStr = invokeTestClient("testService1", "Kermit");
      assertEquals("Kermit|LogOut|AuthOut|RoutOut|RoutIn|AuthIn|LogIn|endpoint|LogOut|AuthOut|RoutOut|RoutIn|AuthIn|LogIn", resStr);
   }

   public void testHandlerChainNegative() throws Throwable
   {
      String resStr = invokeTestClient("testService2", "Kermit");
      assertEquals("Kermit|RoutIn|AuthIn|LogIn|endpoint|LogOut|AuthOut|RoutOut", resStr);
   }

   public void testHandlerChainOverride() throws Throwable
   {
      String resStr = invokeTestClient("testService3", "Kermit");
      assertEquals("Kermit|LogOut|AuthOut|RoutOut|RoutIn|AuthIn|LogIn|endpoint|LogOut|AuthOut|RoutOut|RoutIn|AuthIn|LogIn", resStr);
   }

   private String invokeTestClient(String testName, String reqStr) throws Throwable
   {
      new ClientLauncher().launch(HandlerChainClient.class.getName(), "jbossws-client", new String[] { testName, reqStr });
      String resStr = HandlerChainClient.testResult.get(testName);
      return resStr;
   }
}
