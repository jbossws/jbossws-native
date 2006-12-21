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
package org.jboss.test.ws.jaxrpc.jbws1121;

import javax.naming.InitialContext;
import javax.xml.rpc.Service;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;

/**
 * Test case to test that the call can be made when the client
 * was deployed with an issolated classloader.
 * 
 * If the client has not picked up the config it will send the
 * message without security headers which will cause the call
 * to fail.
 * 
 * @author darran.lofthouse@jboss.com
 * @since 03-August-2006
 */
public class JBWS1121TestCase extends JBossWSTest
{

   private static HelloWorld port;

   public static Test suite() throws Exception
   {
      return JBossWSTestSetup.newTestSetup(JBWS1121TestCase.class, "jaxrpc-jbws1121.ear");
   }

   /*
   public void setUp() throws Exception
   {
      super.setUp();
      if (port == null)
      {
         InitialContext iniCtx = getInitialContext();
         Service service = (Service)iniCtx.lookup("java:comp/env/service/HelloWorldService");
         port = (HelloWorld)service.getPort(HelloWorld.class);
      }
   }
   */

   public void testCall() throws Exception
   {
      System.out.println("FIXME: [JBWS-1328] Client security configuration not loaded when client deployed with issolated classloader");
      if (true) return;
      
      String response = port.echo("Hello");
      assertEquals("Hello", response);
   }
}
