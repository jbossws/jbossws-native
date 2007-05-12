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

import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Response;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.ws.utils.DOMUtils;
import org.xml.sax.InputSource;

/**
 * Test JAXWS asynchrous dispatch
 *
 * @author Thomas.Diesler@jboss.org
 * @since 12-Aug-2006
 */
public class AsynchronousDispatchTestCase extends JBossWSTest
{
   private String targetNS = "http://org.jboss.ws/jaxws/asynchronous";
   private String reqPayload = "<ns1:echo xmlns:ns1='" + targetNS + "'><String_1>Hello</String_1></ns1:echo>";
   private String expPayload = "<ns1:echoResponse xmlns:ns1='" + targetNS + "'><result>Hello</result></ns1:echoResponse>";
   private Exception handlerException;
   private boolean asyncHandlerCalled;

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(AsynchronousDispatchTestCase.class, "jaxws-samples-asynchronous.war");
   }

   public void testInvokeAsynch() throws Exception
   {
      System.out.println("FIXME: [JBWS-1294] Add support for mapping async methods");
      if (true) return;
      
      StreamSource reqObj = new StreamSource(new StringReader(reqPayload));
      Response response = createDispatch().invokeAsync(reqObj);
      StreamSource result = (StreamSource)response.get(1000, TimeUnit.MILLISECONDS);
      InputSource inputSource = new InputSource(result.getReader());
      assertEquals(DOMUtils.parse(expPayload), DOMUtils.parse(inputSource));
   }

   public void testInvokeAsynchHandler() throws Exception
   {
      System.out.println("FIXME: [JBWS-1294] Add support for mapping async methods");
      if (true) return;
      
      AsyncHandler handler = new AsyncHandler()
      {
         public void handleResponse(Response response)
         {
            try
            {
               StreamSource result = (StreamSource)response.get();
               InputSource inputSource = new InputSource(result.getReader());
               assertEquals(DOMUtils.parse(expPayload), DOMUtils.parse(inputSource));
               asyncHandlerCalled = true;
            }
            catch (Exception ex)
            {
               handlerException = ex;
            }
         }
      };
      StreamSource reqObj = new StreamSource(new StringReader(reqPayload));
      Future future = createDispatch().invokeAsync(reqObj, handler);
      future.get(1000, TimeUnit.MILLISECONDS);

      if (handlerException != null)
         throw handlerException;

      assertTrue("Async handler called", asyncHandlerCalled);
   }

   private Dispatch createDispatch() throws MalformedURLException
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-samples-asynchronous?wsdl");
      QName serviceName = new QName(targetNS, "TestEndpointService");
      QName portName = new QName(targetNS, "TestEndpointPort");
      Service service = Service.create(wsdlURL, serviceName);
      Dispatch dispatch = service.createDispatch(portName, StreamSource.class, Mode.PAYLOAD);
      return dispatch;
   }
}
