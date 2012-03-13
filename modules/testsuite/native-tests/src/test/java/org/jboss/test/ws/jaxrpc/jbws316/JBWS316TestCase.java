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
package org.jboss.test.ws.jaxrpc.jbws316;

import javax.naming.InitialContext;
import javax.xml.rpc.Service;

import junit.framework.Test;

import org.jboss.wsf.test.CleanupOperation;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;



/** 
 * field of type byte[] does not serialize correctly in doc/lit web service
 *
 * http://jira.jboss.com/jira/browse/JBWS-316
 *
 * @author Thomas.Diesler@jboss.org
 * @since 02-Dec-2005
 */
public class JBWS316TestCase extends JBossWSTest
{
   private static TestBusinessFacadeBF_WS endpoint;
   private static InitialContext iniCtx;
   
   /** Deploy the test */
   public static Test suite() throws Exception
   {
      return new JBossWSTestSetup(JBWS316TestCase.class, "jaxrpc-jbws316.war, jaxrpc-jbws316-appclient.ear#jaxrpc-jbws316-appclient.jar", new CleanupOperation() {
         @Override
         public void cleanUp() {
            endpoint = null;
         }
      });
   }

   public void setUp() throws Exception
   {
      super.setUp();
      if (endpoint == null)
      {
         iniCtx = getAppclientInitialContext();
         Service service = (Service)iniCtx.lookup("java:service/TestService");
         endpoint = (TestBusinessFacadeBF_WS)service.getPort(TestBusinessFacadeBF_WS.class);
      }
   }
   
   protected void tearDown() throws Exception
   {
      if (iniCtx != null)
      {
         iniCtx.close();
         iniCtx = null;
      }
      super.tearDown();
   }

   /**
    * Test JSE endpoint
    */
   public void testEndpoint() throws Exception
   {
      BinDataDTO inObj = new BinDataDTO();
      inObj.setMethodProp("Hello World".getBytes());
      inObj.fieldProp = "hidden".getBytes();
      BinDataDTO retObj = endpoint.getTestBinData(inObj);
      assertEquals(new String(inObj.getMethodProp()), new String(retObj.getMethodProp()));
      assertNull("Null fieldProp expected", retObj.fieldProp);
   }
}
