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
package org.jboss.test.ws.jaxrpc.jbws1303;

// $Id$

import javax.naming.InitialContext;
import javax.xml.rpc.Service;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;

/**
 * Unmarshall issue with xsi:type specified bean property
 *
 * http://jira.jboss.org/jira/browse/JBWS-1303
 *
 * @author Thomas.Diesler@jboss.org
 * @since 16-Oct-2006
 */
public class JBWS1303TestCase extends JBossWSTest
{
   private static LastMod_PortType port;

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(JBWS1303TestCase.class, "jaxrpc-jbws1303.war, jaxrpc-jbws1303-client.jar");
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      if (port == null)
      {
         InitialContext iniCtx = getInitialContext();
         Service service = (Service)iniCtx.lookup("java:comp/env/service/TestService");
         port = (LastMod_PortType)service.getPort(LastMod_PortType.class);
      }
   }

   public void testEndpoint() throws Exception
   {
      Lastmod lastmod = new Lastmod("yesterday");
      LastmodResponse lastmodRes = port.lastmod(lastmod);
      assertEquals("yesterday", lastmodRes.getTimeChanged());
   }
}
