/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.webserviceref;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test @WebServiceRef overrides in jboss-client.xml
 *
 * @author Thomas.Diesler@jboss.com
 * @since 18-Jan-2007
 */
public class ServiceRefOverridesTestCase extends JBossWSTest
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-webserviceref";

   public static Test suite()
   {
      return new JBossWSTestSetup(ServiceRefOverridesTestCase.class, "jaxws-webserviceref.war");
   }

   public void testService1() throws Throwable
   {
      final String reqMsg = getName();
      final String respMsg = executeApplicationClient(reqMsg);
      assertEquals(reqMsg, respMsg);
   }

   public void testService2() throws Throwable
   {
      final String reqMsg = getName();
      final String respMsg = executeApplicationClient(reqMsg);
      assertEquals(reqMsg, respMsg);
   }

   public void testService3() throws Throwable
   {
      final String reqMsg = getName();
      final String respMsg = executeApplicationClient(reqMsg);
      assertEquals(reqMsg + reqMsg, respMsg);
   }

   public void testService4() throws Throwable
   {
      final String reqMsg = getName();
      final String respMsg = executeApplicationClient(reqMsg);
      assertEquals(reqMsg + reqMsg, respMsg);
   }

   public void testPort1() throws Throwable
   {
      final String reqMsg = getName();
      final String respMsg = executeApplicationClient(reqMsg);
      assertEquals(reqMsg, respMsg);
   }

   public void testPort2() throws Throwable
   {
      final String reqMsg = getName();
      final String respMsg = executeApplicationClient(reqMsg);
      assertEquals(reqMsg + reqMsg, respMsg);
   }

   public void testPort3() throws Throwable
   {
      final String reqMsg = getName();
      final String respMsg = executeApplicationClient(reqMsg);
      assertEquals(reqMsg + reqMsg, respMsg);
   }

   private String executeApplicationClient(final String... appclientArgs) throws Throwable
   {
      try
      {
         final OutputStream appclientOS = new ByteArrayOutputStream();
         JBossWSTestHelper.deployAppclient("jaxws-webserviceref-override-appclient.ear#jaxws-webserviceref-override-appclient.jar", appclientOS, appclientArgs);
         // wait till appclient stops
         String appclientLog = appclientOS.toString();
         while (!appclientLog.contains("stopped in")) {
            Thread.sleep(100);
            appclientLog = appclientOS.toString();
         }
         // assert appclient logs
         assertTrue(appclientLog.contains("TEST START"));
         assertTrue(appclientLog.contains("TEST END"));
         assertTrue(appclientLog.contains("RESULT ["));
         assertTrue(appclientLog.contains("] RESULT"));
         int indexOfResultStart = appclientLog.indexOf("RESULT [");
         int indexOfResultStop = appclientLog.indexOf("] RESULT");
         assertTrue(indexOfResultStart > 0);
         assertTrue(indexOfResultStop > 0);
         return appclientLog.substring(indexOfResultStart + 8, indexOfResultStop);
      }
      finally
      {
         JBossWSTestHelper.undeployAppclient("jaxws-webserviceref-override-appclient.ear#jaxws-webserviceref-override-appclient.jar", false);
      }
   }
}
