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
package org.jboss.test.ws.jaxrpc.xop.rpclit;

import javax.naming.InitialContext;
import javax.xml.rpc.Service;

import junit.framework.Test;

import org.jboss.ws.core.StubExt;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 *
 * @author Heiko.Braun@jboss.org
 * @since 11-Nov-2005
 */
public class XOPTestCase extends XOPBase {

   private static XOPPing port;
   private static InitialContext iniCtx;

   public static Test suite()
   {
      return new JBossWSTestSetup(XOPTestCase.class, "jaxrpc-xop-rpclit.war, jaxrpc-xop-rpclit-appclient.ear#jaxrpc-xop-rpclit-appclient.jar");
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      if (port == null)
      {
         iniCtx = getAppclientInitialContext();
         Service service = (Service)iniCtx.lookup("java:service/XOPTestCaseRPC");
         port = (XOPPing)service.getPort(XOPPing.class);
         ((StubExt)port).setConfigName("Standard MTOM client");
      }
   }

   protected void tearDown() throws Exception
   {
      if (iniCtx != null)
      {
         iniCtx.close();
         iniCtx = null;
      }
   }

   protected XOPPing getPort() {
      return port;
   }

}
