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
package org.jboss.test.ws.jaxrpc.jbws718;

// $Id$

import javax.naming.InitialContext;
import javax.xml.rpc.Service;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;

/**
 * DOM Serialization in ServiceReferencable leads to stack overflow
 *
 * http://jira.jboss.org/jira/browse/JBWS-718
 *
 * @author Thomas.Diesler@jboss.org
 * @since 11-Nov-2005
 */
public class JBWS718TestCase extends JBossWSTest
{
   private static TestService_PortType port;

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(JBWS718TestCase.class, "jaxrpc-jbws718.war, jaxrpc-jbws718-client.jar");
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      if (port == null)
      {
         InitialContext iniCtx = getInitialContext();
         Service service = (Service)iniCtx.lookup("java:comp/env/service/TestService");
         port = (TestService_PortType)service.getPort(TestService_PortType.class);
      }
   }

   public void testPingMsg() throws Exception {
       PingMsgResponse response = port.ping(new PingMsg("Hello World"));
       assertNotNull(response);
   }
}
