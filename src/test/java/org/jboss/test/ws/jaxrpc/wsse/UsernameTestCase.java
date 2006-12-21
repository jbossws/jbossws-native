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
package org.jboss.test.ws.jaxrpc.wsse;

import java.rmi.RemoteException;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.xml.rpc.Service;
import javax.xml.rpc.Stub;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;

/**
 * Test UsernameToken
 *
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @version $Revision$
 */
public class UsernameTestCase extends JBossWSTest
{
   private static JaxRpcTestService endpoint;

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(UsernameTestCase.class, "jaxrpc-wsse-username.jar, jaxrpc-wsse-username-client.jar");
   }

   /*
   protected void setUp() throws Exception
   {
      super.setUp();

      if (endpoint == null)
      {
         InitialContext iniCtx = getInitialContext();
         Service service = (Service)iniCtx.lookup("java:comp/env/service/TestServiceEJB");
         endpoint = (JaxRpcTestService)service.getPort(JaxRpcTestService.class);
         ((Stub)endpoint)._setProperty(Stub.USERNAME_PROPERTY, "kermit");
         ((Stub)endpoint)._setProperty(Stub.PASSWORD_PROPERTY, "thefrog");
      }

      Properties props = System.getProperties();
      props.remove("org.jboss.ws.wsse.keyStore");
      props.remove("org.jboss.ws.wsse.trustStore");
      props.remove("org.jboss.ws.wsse.keyStorePassword");
      props.remove("org.jboss.ws.wsse.trustStorePassword");
      props.remove("org.jboss.ws.wsse.keyStoreType");
      props.remove("org.jboss.ws.wsse.trustStoreType");
   }
   */

   public void testEchoString() throws Exception
   {
      System.out.println("FIXME: [JBWS-1330] Fix jaxrpc wsse tests for jbossws-5.0");
      if (true) return;
      
      String hello = "Hello";
      String world = "world!";
      Object retObj = endpoint.echoString(hello, world);
      assertEquals(hello + world, retObj);
   }

   public void testEchoSimpleUserType() throws Exception
   {
      System.out.println("FIXME: [JBWS-1330] Fix jaxrpc wsse tests for jbossws-5.0");
      if (true) return;
      
      String hello = "Hello";
      SimpleUserType userType = new SimpleUserType(1, 2);
      Object retObj = endpoint.echoSimpleUserType(hello, userType);
      assertEquals(userType, retObj);
   }

   public void testEchoStringNoUsername() throws Exception
   {
      System.out.println("FIXME: [JBWS-1330] Fix jaxrpc wsse tests for jbossws-5.0");
      if (true) return;
      
      String hello = "Hello";
      String world = "world!";

      ((Stub)endpoint)._setProperty(Stub.USERNAME_PROPERTY, null);
      ((Stub)endpoint)._setProperty(Stub.PASSWORD_PROPERTY, null);

      try
      {
         endpoint.echoString(hello, world);
         fail("Expected exception not thrown");
      }
      catch (RemoteException e)
      {
      }
   }
}
