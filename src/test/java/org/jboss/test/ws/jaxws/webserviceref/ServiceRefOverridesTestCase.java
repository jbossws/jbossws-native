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
package org.jboss.test.ws.jaxws.webserviceref;

import junit.framework.Test;

import org.jboss.ejb3.client.ClientLauncher;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test @WebServiceRef overrides in jboss-client.xml
 *
 * @author Thomas.Diesler@jboss.com
 * @since 18-Jan-2007
 */
public class ServiceRefOverridesTestCase extends JBossWSTest
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-samples-webserviceref";

   public static Test suite()
   {
      return new JBossWSTestSetup(ServiceRefOverridesTestCase.class, "jaxws-samples-webserviceref.war, jaxws-samples-webserviceref-override-client.jar");
   }

   protected void setUp() throws Exception
   {
      super.setUp();

      if (TestEndpointClientTwo.iniCtx == null)
         TestEndpointClientTwo.iniCtx = getInitialContext();
   }

   public void testService1() throws Throwable
   {
      String resStr = invokeTest(getName());
      assertEquals(getName(), resStr);
   }

   public void testService2() throws Throwable
   {
      String resStr = invokeTest(getName());
      assertEquals(getName(), resStr);
   }

   public void testService3() throws Throwable
   {
      String resStr = invokeTest(getName());
      assertEquals(getName() + getName(), resStr);
   }

   public void testService4() throws Throwable
   {
      String resStr = invokeTest(getName());
      assertEquals(getName() + getName(), resStr);
   }

   public void testPort1() throws Throwable
   {
      String resStr = invokeTest(getName());
      assertEquals(getName(), resStr);
   }

   public void testPort2() throws Throwable
   {
      String resStr = invokeTest(getName());
      assertEquals(getName() + getName(), resStr);
   }

   public void testPort3() throws Throwable
   {
      String resStr = invokeTest(getName());
      assertEquals(getName() + getName(), resStr);
   }

   private String invokeTest(String reqStr) throws Throwable
   {
      new ClientLauncher().launch(TestEndpointClientTwo.class.getName(), "jbossws-client", new String[] { reqStr });
      return TestEndpointClientTwo.testResult.get(reqStr);
   }
}
