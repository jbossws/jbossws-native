/*
* JBoss, Home of Professional Open Source.
* Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws3071;

import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;
import javax.xml.ws.Response;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test case to test JBWS-3071 for the correct 
 * exception mapping for async endpoints.
 * 
 * @author darran.lofthouse@jboss.com
 * @since 12th July 2010
 */
public class JBWS3071TestCase extends JBossWSTest
{

   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-jbws3071/";

   private static TestEndpoint port;

   public static Test suite() throws Exception
   {
      return new JBossWSTestSetup(JBWS3071TestCase.class, "jaxws-jbws3071.war");
   }

   public void setUp() throws Exception
   {
      super.setUp();
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
      QName serviceName = new QName("http://ws.test.jboss.org/jbws3071", "TestEndpointService");

      Service service = Service.create(wsdlURL, serviceName);
      port = service.getPort(TestEndpoint.class);
   }

   public void testEchoSynchronous() throws Exception
   {
      assertEquals("Response", "Message_1", port.echo("Message_1"));
   }

   public void testEchoFailSynchronous() throws Exception
   {
      try
      {
         String response = port.echo("FAIL");
         fail("Expected 'TestException' not thrown.");
      }
      catch (TestException ignored)
      {
      }
   }

   public void testEchoAsyncResponse() throws Exception
   {
      Response<String> echoResponse = port.echoAsync("Message_2");
      String response = echoResponse.get(2, TimeUnit.SECONDS);
      assertEquals("Response", "Message_2", response);
   }

   public void testEchoAsyncFuture() throws Exception
   {
      StringHandler handler = new StringHandler();
      Future future = port.echoAsync("Message_3", handler);
      future.get(2, TimeUnit.SECONDS);
      Response<String> echoResponse = handler.getResponse();
      String response = echoResponse.get(2, TimeUnit.SECONDS);
      assertEquals("Response", "Message_3", response);
   }

   public void testEchoFailAsyncResponse() throws Exception
   {
      Response<String> echoResponse = port.echoAsync("FAIL");
      try
      {
         echoResponse.get(2, TimeUnit.SECONDS);
         fail("Expected 'ExecutionException' not thrown.");
      }
      catch (ExecutionException ee)
      {
         Exception cause = (Exception)ee.getCause();
         assertEquals("Cause Type", TestException.class, cause.getClass());
      }

   }

   public void testEchoFailAsyncFuture() throws Exception
   {
      StringHandler handler = new StringHandler();
      Future future = port.echoAsync("FAIL", handler);
      try
      {
         future.get(2, TimeUnit.SECONDS);
         fail("Expected 'ExecutionException' not thrown.");
      }
      catch (ExecutionException ee)
      {
         Exception cause = (Exception)ee.getCause();
         assertEquals("Cause Type", TestException.class, cause.getClass());
      }
   }

   public void testEchoFailAsyncFuture_isDone() throws Exception
   {
      StringHandler handler = new StringHandler();
      
      Future future = port.echoAsync("FAIL SLEEP", handler);      
      while (future.isDone() == false)
      {
         Thread.sleep(200);
      }
                  
      try
      {
         handler.getResponse().get();
         fail("Expected 'ExecutionException' not thrown.");
      }
      catch (ExecutionException ee)
      {
         Exception cause = (Exception)ee.getCause();
         assertEquals("Cause Type", TestException.class, cause.getClass());
      }
   }
   
}
