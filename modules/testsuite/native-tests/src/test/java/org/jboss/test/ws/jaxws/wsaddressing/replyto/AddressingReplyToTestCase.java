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
package org.jboss.test.ws.jaxws.wsaddressing.replyto;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test endpoint using ws-addressing
 *
 * NOTE: This test uses a JAX-RPC client against a JAX-WS endpoint.
 *  
 * @author Thomas.Diesler@jboss.org
 * @since 24-Nov-2005
 */
public class AddressingReplyToTestCase extends JBossWSTest
{

   public static Test suite()
   {
      return new JBossWSTestSetup(AddressingReplyToTestCase.class,
            "jaxws-wsaddressing-initial.war," +
            "jaxws-wsaddressing-replyto.war," +
            "jaxws-wsaddressing-faultto.war");
   }

   public void testApplicationClient() throws Exception
   {
      try
      {
         final String appclientArg = getServerHost();
         final OutputStream appclientOS = new ByteArrayOutputStream();
         JBossWSTestHelper.deployAppclient("jaxws-wsaddressing-appclient.ear#jaxws-wsaddressing-appclient.jar", appclientOS, appclientArg);
         // wait till appclient stops
         String appclientLog = appclientOS.toString();
         while (!appclientLog.contains("stopped in")) {
            Thread.sleep(100);
            appclientLog = appclientOS.toString();
         }
         // assert appclient logs
         assertTrue(!appclientLog.contains("APPCLIENT TEST FAILURE"));
         assertTrue(appclientLog.contains("TEST START"));
         assertTrue(appclientLog.contains("TEST END"));
      }
      finally
      {
         JBossWSTestHelper.undeployAppclient("jaxws-wsaddressing-appclient.ear#jaxws-wsaddressing-appclient.jar", false);
      }
   }
   
}
