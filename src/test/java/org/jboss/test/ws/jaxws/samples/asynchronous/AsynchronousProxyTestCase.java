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
package org.jboss.test.ws.jaxws.samples.asynchronous;

// $Id$

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;

/**
 * Test JAXWS asynchrous proxy
 *
 * @author Thomas.Diesler@jboss.org
 * @since 12-Aug-2006
 */
public class AsynchronousProxyTestCase extends JBossWSTest
{
   private String targetNS = "http://org.jboss.ws/jaxws/asynchronous";
   private Exception handlerException;
   private boolean asyncHandlerCalled;

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(AsynchronousProxyTestCase.class, "jaxws-samples-asynchronous.war");
   }

   public void testInvokeSync() throws Exception
   {
      TestEndpoint port = createProxy();
      String retStr = port.echo("Hello");
      assertEquals("Hello", retStr);
   }

   public void testInvokeAsync() throws Exception
   {
      TestEndpoint port = createProxy();
      Response response = port.echoAsync("Async");

      // access future
      String retStr = (String) response.get();
      assertEquals("Async", retStr);
   }

   public void testInvokeAsyncReentrant() throws Exception
   {
      System.out.println("FIXME: [JBWS-1294] Add support for mapping async methods");
      if (true) return;

      TestEndpoint port = createProxy();
      Response response = port.echoAsync("Async");

      // do something in between
      String retStr = port.echo("Sync");
      assertEquals("Sync", retStr);

      // access future
      retStr = (String) response.get();
      assertEquals("Async", retStr);
   }

   public void testInvokeAsyncHandler() throws Exception
   {
      AsyncHandler<String> handler = new AsyncHandler<String>()
      {
         public void handleResponse(Response response)
         {
            try
            {
               String retStr = (String) response.get(1000, TimeUnit.MILLISECONDS);
               assertEquals("Hello", retStr);
               asyncHandlerCalled = true;
            }
            catch (Exception ex)
            {
               handlerException = ex;
            }
         }
      };

      TestEndpoint port = createProxy();
      Future future = port.echoAsync("Hello", handler);
      future.get(1000, TimeUnit.MILLISECONDS);

      if (handlerException != null)
         throw handlerException;

      assertTrue("Async handler called", asyncHandlerCalled);
   }

   private TestEndpoint createProxy() throws MalformedURLException
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-samples-asynchronous?wsdl");
      QName serviceName = new QName(targetNS, "TestEndpointService");
      Service service = Service.create(wsdlURL, serviceName);
      return (TestEndpoint)service.getPort(TestEndpoint.class);
   }
}
