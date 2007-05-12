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
package org.jboss.test.ws.jaxws.wsaddressing.action;

import javax.xml.rpc.Service;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;

/**
 * Test endpoint using ws-addressing.
 * 
 * The wsa:Action should override any other dispatch method
 *
 * @author Thomas.Diesler@jboss.org
 * @since 29-Nov-2005
 */
public class AddressingActionTestCase extends JBossWSTest
{
   private static ActionEndpoint rpcEndpoint;

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(AddressingActionTestCase.class, "jaxws-wsaddressing-action-rpc.war, jaxws-wsaddressing-action-rpc-client.jar");
   }

   protected void setUp() throws Exception
   {
      super.setUp();

      if (rpcEndpoint == null)
      {
         Service rpcService = (Service)getInitialContext("wsarpc-client").lookup("java:comp/env/service/ActionRpcService");
         rpcEndpoint = (ActionEndpoint)rpcService.getPort(ActionEndpoint.class);
      }
   }

   public void testRpcEndpoint() throws Exception
   {
      assertEquals("bar:HelloFoo", rpcEndpoint.foo("HelloFoo"));
      assertEquals("bar:HelloBar", rpcEndpoint.bar("HelloBar"));
   }
}
