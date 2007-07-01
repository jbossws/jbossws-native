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
package org.jboss.test.ws.jaxrpc.jbws1653;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.naming.InitialContext;
import javax.xml.rpc.Service;

import junit.framework.Test;

import org.jboss.wsf.spi.test.JBossWSTest;
import org.jboss.wsf.spi.test.JBossWSTestSetup;

/**
 * [JBWS-1653] Post-handler-chain not invoked for "Standard Client" configuration
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 26-Jun-2007
 */
public class JBWS1653TestCase extends JBossWSTest
{
   public static Test suite() throws Exception
   {
      return new JBossWSTestSetup(JBWS1653TestCase.class, "jaxrpc-jbws1653.war, jaxrpc-jbws1653-client.jar");
   }

   public void setUp() throws Exception
   {
      ClientHandler.message = null;
   }

   public void testStandardConfig() throws Exception
   {
      InitialContext iniCtx = getInitialContext();
      Service service = (Service)iniCtx.lookup("java:comp/env/service/TestService");
      TestEndpoint port = (TestEndpoint)service.getPort(TestEndpoint.class);

      String retStr = port.echoString("kermit");
      assertEquals("kermit", retStr);
      assertNull(ClientHandler.message);
   }

   public void testCustomConfig() throws Exception
   {
      ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
      URLClassLoader urlLoader = new URLClassLoader(new URL[] {}, ctxLoader)
      {
         public URL getResource(String resName)
         {
            URL resURL = super.getResource(resName);
            try
            {
               if (resName.endsWith("META-INF/standard-jaxrpc-client-config.xml"))
                  resURL = new File("resources/jaxrpc/jbws1653/META-INF/standard-jaxrpc-client-config.xml").toURL();
            }
            catch (MalformedURLException ex)
            {
               // ignore
            }
            return resURL;
         }
      };
      Thread.currentThread().setContextClassLoader(urlLoader);

      InitialContext iniCtx = getInitialContext();
      Service service = (Service)iniCtx.lookup("java:comp/env/service/TestService");
      TestEndpoint port = (TestEndpoint)service.getPort(TestEndpoint.class);

      try
      {
         String retStr = port.echoString("kermit");
         assertEquals("kermit", retStr);
         assertEquals("kermit", ClientHandler.message);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(ctxLoader);
      }
   }
}
