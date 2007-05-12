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
package org.jboss.test.ws.jaxws.handlerlifecycle;

// $Id$

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;

/**
 * Test JAXWS handler lifecycle
 *
 * @author Thomas.Diesler@jboss.org
 * @since 13-MAr-2007
 */
public class HandlerLifecycleTestCase extends JBossWSTest
{
   private static SOAPEndpoint port;
   private static TrackerEndpoint trackerPort;

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(HandlerLifecycleTestCase.class, "jaxws-handlerlifecycle.war");
   }

   public void setUp() throws Exception
   {
      if (trackerPort == null)
      {
         URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-handlerlifecycle/tracker?wsdl");
         QName serviceName = new QName("http://org.jboss.ws/jaxws/handlerlifecycle", "TrackerEndpointService");
         Service service = Service.create(wsdlURL, serviceName);
         trackerPort = (TrackerEndpoint)service.getPort(TrackerEndpoint.class);
      }

      if (port == null)
      {
         URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-handlerlifecycle/soap?wsdl");
         QName serviceName = new QName("http://org.jboss.ws/jaxws/handlerlifecycle", "SOAPEndpointService");
         Service service = Service.create(wsdlURL, serviceName);
         port = (SOAPEndpoint)service.getPort(SOAPEndpoint.class);
      }
   }

   protected void tearDown() throws Exception
   {
      HandlerTracker.clearListMessages();
      trackerPort.clearListMessages();
      assertEquals("[]", HandlerTracker.getListMessages());
      assertEquals("[]", trackerPort.getListMessages());
   }

   /**
    * All handlers return true 
    */
   public void testHandleMessageTrue() throws Exception
   {
      System.out.println("FIXME: [JBWS-1579] @EndpointConfig ignored on SEI");
      
      String testResponse = port.runTest(getName());

      String trackerMessages = HandlerTracker.getListMessages();
      List<String> expMessages = new ArrayList<String>();
      expMessages.add("ClientHandler1:PostConstruct");
      expMessages.add("ClientHandler2:PostConstruct");
      expMessages.add("ClientHandler3:PostConstruct");
      expMessages.add("ClientHandler1:Message:OutBound");
      expMessages.add("ClientHandler2:Message:OutBound");
      expMessages.add("ClientHandler3:Message:OutBound");
      expMessages.add("ClientHandler3:Message:InBound");
      expMessages.add("ClientHandler2:Message:InBound");
      expMessages.add("ClientHandler1:Message:InBound");
      expMessages.add("ClientHandler3:Close");
      expMessages.add("ClientHandler2:Close");
      expMessages.add("ClientHandler1:Close");
      assertEquals(expMessages.toString(), trackerMessages);

      trackerMessages = trackerPort.getListMessages();
      expMessages = new ArrayList<String>();
      
      // Handler construction
      expMessages.add("PreServerHandler1:PostConstruct");
      expMessages.add("PreServerHandler2:PostConstruct");
      expMessages.add("ServerHandler1:PostConstruct");
      expMessages.add("ServerHandler2:PostConstruct");
      expMessages.add("ServerHandler3:PostConstruct");
      expMessages.add("PostServerHandler1:PostConstruct");
      expMessages.add("PostServerHandler2:PostConstruct");
      
      // Inbound server message
      expMessages.add("PostServerHandler2:Message:InBound");
      expMessages.add("PostServerHandler1:Message:InBound");
      expMessages.add("ServerHandler3:Message:InBound");
      expMessages.add("ServerHandler2:Message:InBound");
      expMessages.add("ServerHandler1:Message:InBound");
      expMessages.add("PreServerHandler2:Message:InBound");
      expMessages.add("PreServerHandler1:Message:InBound");
      
      // Pre/Post handler are defined in the context of outbound
      expMessages.add("PreServerHandler1:Message:OutBound");
      expMessages.add("PreServerHandler2:Message:OutBound");
      expMessages.add("ServerHandler1:Message:OutBound");
      expMessages.add("ServerHandler2:Message:OutBound");
      expMessages.add("ServerHandler3:Message:OutBound");
      expMessages.add("PostServerHandler1:Message:OutBound");
      expMessages.add("PostServerHandler2:Message:OutBound");
      
      // Handler closing
      expMessages.add("PreServerHandler1:Close");
      expMessages.add("PreServerHandler2:Close");
      expMessages.add("ServerHandler1:Close");
      expMessages.add("ServerHandler2:Close");
      expMessages.add("ServerHandler3:Close");
      expMessages.add("PostServerHandler1:Close");
      expMessages.add("PostServerHandler2:Close");
      
      assertEquals(expMessages.toString(), trackerMessages);
      
      assertEquals(getName() + "Response", testResponse);
   }

   /**
    * ClientHandler2 returns false on outbound 
    */
   public void testClientOutboundHandleMessageFalse() throws Exception
   {
      String testResponse = port.runTest(getName());

      String trackerMessages = HandlerTracker.getListMessages();
      List<String> expMessages = new ArrayList<String>();
      expMessages.add("ClientHandler1:Message:OutBound");
      expMessages.add("ClientHandler2:Message:OutBound:false");
      expMessages.add("ClientHandler2:Close");
      expMessages.add("ClientHandler1:Close");
      assertEquals(expMessages.toString(), trackerMessages);

      trackerMessages = trackerPort.getListMessages();
      expMessages = new ArrayList<String>();
      assertEquals(expMessages.toString(), trackerMessages);

      assertNull(testResponse);
   }

   /**
    * ClientHandler2 throws a RuntimeException on outbound 
    */
   public void testClientOutboundHandleMessageThrowsRuntimeException() throws Exception
   {
      String testResponse;
      try
      {
         testResponse = port.runTest(getName());
         fail("RuntimeException expected, but got: " + testResponse);
      }
      catch (WebServiceException ex)
      {
         testResponse = ex.getMessage();
      }

      String trackerMessages = HandlerTracker.getListMessages();
      List<String> expMessages = new ArrayList<String>();
      expMessages.add("ClientHandler1:Message:OutBound");
      expMessages.add("ClientHandler2:Message:OutBound:ErrorInClientHandler2");
      expMessages.add("ClientHandler1:Fault:InBound");
      expMessages.add("ClientHandler2:Close");
      expMessages.add("ClientHandler1:Close");
      assertEquals(expMessages.toString(), trackerMessages);

      trackerMessages = trackerPort.getListMessages();
      expMessages = new ArrayList<String>();
      assertEquals(expMessages.toString(), trackerMessages);

      assertEquals("ErrorInClientHandler2", testResponse);
   }

   /**
    * ServerHandler2 returns false on inbound 
    */
   public void testServerInboundHandleMessageFalse() throws Exception
   {
      String testResponse = port.runTest(getName());

      String trackerMessages = trackerPort.getListMessages();
      List<String> expMessages = new ArrayList<String>();
      expMessages.add("PostServerHandler2:Message:InBound");
      expMessages.add("PostServerHandler1:Message:InBound");
      expMessages.add("ServerHandler3:Message:InBound");
      expMessages.add("ServerHandler2:Message:InBound:false");
      expMessages.add("ServerHandler3:Message:OutBound");
      expMessages.add("PostServerHandler1:Message:OutBound");
      expMessages.add("PostServerHandler2:Message:OutBound");
      expMessages.add("ServerHandler2:Close");
      expMessages.add("ServerHandler3:Close");
      expMessages.add("PostServerHandler1:Close");
      expMessages.add("PostServerHandler2:Close");
      assertEquals(expMessages.toString(), trackerMessages);

      trackerMessages = HandlerTracker.getListMessages();
      expMessages = new ArrayList<String>();
      expMessages.add("ClientHandler1:Message:OutBound");
      expMessages.add("ClientHandler2:Message:OutBound");
      expMessages.add("ClientHandler3:Message:OutBound");
      expMessages.add("ClientHandler3:Message:InBound");
      expMessages.add("ClientHandler2:Message:InBound");
      expMessages.add("ClientHandler1:Message:InBound");
      expMessages.add("ClientHandler3:Close");
      expMessages.add("ClientHandler2:Close");
      expMessages.add("ClientHandler1:Close");
      assertEquals(expMessages.toString(), trackerMessages);
      
      assertEquals("testServerHandler2Response", testResponse);
   }

   /**
    * ServerHandler2 trows a RuntimeException on inbound 
    */
   public void testServerInboundHandleMessageThrowsRuntimeException() throws Exception
   {
      String testResponse;
      try
      {
         testResponse = port.runTest(getName());
         fail("RuntimeException expected, but got: " + testResponse);
      }
      catch (WebServiceException ex)
      {
         testResponse = ex.getMessage();
      }

      String trackerMessages = trackerPort.getListMessages();
      List<String> expMessages = new ArrayList<String>();
      expMessages.add("PostServerHandler2:Message:InBound");
      expMessages.add("PostServerHandler1:Message:InBound");
      expMessages.add("ServerHandler3:Message:InBound");
      expMessages.add("ServerHandler2:Message:InBound:ErrorInServerHandler2");
      expMessages.add("ServerHandler3:Fault:OutBound");
      expMessages.add("PostServerHandler1:Fault:OutBound");
      expMessages.add("PostServerHandler2:Fault:OutBound");
      expMessages.add("ServerHandler2:Close");
      expMessages.add("ServerHandler3:Close");
      expMessages.add("PostServerHandler1:Close");
      expMessages.add("PostServerHandler2:Close");
      assertEquals(expMessages.toString(), trackerMessages);

      trackerMessages = HandlerTracker.getListMessages();
      expMessages = new ArrayList<String>();
      expMessages.add("ClientHandler1:Message:OutBound");
      expMessages.add("ClientHandler2:Message:OutBound");
      expMessages.add("ClientHandler3:Message:OutBound");
      expMessages.add("ClientHandler3:Fault:InBound");
      expMessages.add("ClientHandler2:Fault:InBound");
      expMessages.add("ClientHandler1:Fault:InBound");
      expMessages.add("ClientHandler3:Close");
      expMessages.add("ClientHandler2:Close");
      expMessages.add("ClientHandler1:Close");
      assertEquals(expMessages.toString(), trackerMessages);
      
      assertEquals("ErrorInServerHandler2", testResponse);
   }

   /**
    * ServerHandler2 returns false on outbound 
    */
   public void testServerOutboundHandleMessageFalse() throws Exception
   {
      String testResponse = port.runTest(getName());

      String trackerMessages = trackerPort.getListMessages();
      List<String> expMessages = new ArrayList<String>();
      expMessages.add("PostServerHandler2:Message:InBound");
      expMessages.add("PostServerHandler1:Message:InBound");
      expMessages.add("ServerHandler3:Message:InBound");
      expMessages.add("ServerHandler2:Message:InBound");
      expMessages.add("ServerHandler1:Message:InBound");
      expMessages.add("PreServerHandler2:Message:InBound");
      expMessages.add("PreServerHandler1:Message:InBound");
      expMessages.add("PreServerHandler1:Message:OutBound");
      expMessages.add("PreServerHandler2:Message:OutBound");
      expMessages.add("ServerHandler1:Message:OutBound");
      expMessages.add("ServerHandler2:Message:OutBound:false");
      expMessages.add("PreServerHandler1:Close");
      expMessages.add("PreServerHandler2:Close");
      expMessages.add("ServerHandler1:Close");
      expMessages.add("ServerHandler2:Close");
      expMessages.add("ServerHandler3:Close");
      expMessages.add("PostServerHandler1:Close");
      expMessages.add("PostServerHandler2:Close");
      assertEquals(expMessages.toString(), trackerMessages);

      trackerMessages = HandlerTracker.getListMessages();
      expMessages = new ArrayList<String>();
      expMessages.add("ClientHandler1:Message:OutBound");
      expMessages.add("ClientHandler2:Message:OutBound");
      expMessages.add("ClientHandler3:Message:OutBound");
      expMessages.add("ClientHandler3:Message:InBound");
      expMessages.add("ClientHandler2:Message:InBound");
      expMessages.add("ClientHandler1:Message:InBound");
      expMessages.add("ClientHandler3:Close");
      expMessages.add("ClientHandler2:Close");
      expMessages.add("ClientHandler1:Close");
      assertEquals(expMessages.toString(), trackerMessages);
      
      assertEquals(getName() + "Response", testResponse);
   }

   /**
    * ServerHandler2 trows a RuntimeException on outbound 
    */
   public void testServerOutboundHandleMessageThrowsRuntimeException() throws Exception
   {
      String testResponse;
      try
      {
         testResponse = port.runTest(getName());
         fail("RuntimeException expected, but got: " + testResponse);
      }
      catch (WebServiceException ex)
      {
         testResponse = ex.getMessage();
      }

      String trackerMessages = trackerPort.getListMessages();
      List<String> expMessages = new ArrayList<String>();
      expMessages.add("PostServerHandler2:Message:InBound");
      expMessages.add("PostServerHandler1:Message:InBound");
      expMessages.add("ServerHandler3:Message:InBound");
      expMessages.add("ServerHandler2:Message:InBound");
      expMessages.add("ServerHandler1:Message:InBound");
      expMessages.add("PreServerHandler2:Message:InBound");
      expMessages.add("PreServerHandler1:Message:InBound");
      expMessages.add("PreServerHandler1:Message:OutBound");
      expMessages.add("PreServerHandler2:Message:OutBound");
      expMessages.add("ServerHandler1:Message:OutBound");
      expMessages.add("ServerHandler2:Message:OutBound:ErrorInServerHandler2");
      expMessages.add("ServerHandler3:Fault:OutBound");
      expMessages.add("PostServerHandler1:Fault:OutBound");
      expMessages.add("PostServerHandler2:Fault:OutBound");
      expMessages.add("PreServerHandler1:Close");
      expMessages.add("PreServerHandler2:Close");
      expMessages.add("ServerHandler1:Close");
      expMessages.add("ServerHandler2:Close");
      expMessages.add("ServerHandler3:Close");
      expMessages.add("PostServerHandler1:Close");
      expMessages.add("PostServerHandler2:Close");
      assertEquals(expMessages.toString(), trackerMessages);

      trackerMessages = HandlerTracker.getListMessages();
      expMessages = new ArrayList<String>();
      expMessages.add("ClientHandler1:Message:OutBound");
      expMessages.add("ClientHandler2:Message:OutBound");
      expMessages.add("ClientHandler3:Message:OutBound");
      expMessages.add("ClientHandler3:Fault:InBound");
      expMessages.add("ClientHandler2:Fault:InBound");
      expMessages.add("ClientHandler1:Fault:InBound");
      expMessages.add("ClientHandler3:Close");
      expMessages.add("ClientHandler2:Close");
      expMessages.add("ClientHandler1:Close");
      assertEquals(expMessages.toString(), trackerMessages);
      
      assertEquals("ErrorInServerHandler2", testResponse);
   }

   /**
    * ClientHandler2 returns false on inbound 
    */
   public void testClientInboundHandleMessageFalse() throws Exception
   {
      String testResponse = port.runTest(getName());

      String trackerMessages = HandlerTracker.getListMessages();
      List<String> expMessages = new ArrayList<String>();
      expMessages.add("ClientHandler1:Message:OutBound");
      expMessages.add("ClientHandler2:Message:OutBound");
      expMessages.add("ClientHandler3:Message:OutBound");
      expMessages.add("ClientHandler3:Message:InBound");
      expMessages.add("ClientHandler2:Message:InBound:false");
      expMessages.add("ClientHandler3:Close");
      expMessages.add("ClientHandler2:Close");
      expMessages.add("ClientHandler1:Close");
      assertEquals(expMessages.toString(), trackerMessages);

      trackerMessages = trackerPort.getListMessages();
      expMessages = new ArrayList<String>();
      expMessages.add("PostServerHandler2:Message:InBound");
      expMessages.add("PostServerHandler1:Message:InBound");
      expMessages.add("ServerHandler3:Message:InBound");
      expMessages.add("ServerHandler2:Message:InBound");
      expMessages.add("ServerHandler1:Message:InBound");
      expMessages.add("PreServerHandler2:Message:InBound");
      expMessages.add("PreServerHandler1:Message:InBound");
      expMessages.add("PreServerHandler1:Message:OutBound");
      expMessages.add("PreServerHandler2:Message:OutBound");
      expMessages.add("ServerHandler1:Message:OutBound");
      expMessages.add("ServerHandler2:Message:OutBound");
      expMessages.add("ServerHandler3:Message:OutBound");
      expMessages.add("PostServerHandler1:Message:OutBound");
      expMessages.add("PostServerHandler2:Message:OutBound");
      expMessages.add("PreServerHandler1:Close");
      expMessages.add("PreServerHandler2:Close");
      expMessages.add("ServerHandler1:Close");
      expMessages.add("ServerHandler2:Close");
      expMessages.add("ServerHandler3:Close");
      expMessages.add("PostServerHandler1:Close");
      expMessages.add("PostServerHandler2:Close");
      assertEquals(expMessages.toString(), trackerMessages);

      assertEquals(getName() + "Response", testResponse);
   }

   /**
    * ClientHandler2 throws a RuntimeException on intbound 
    */
   public void testClientInboundHandleMessageThrowsRuntimeException() throws Exception
   {
      String testResponse;
      try
      {
         testResponse = port.runTest(getName());
         fail("RuntimeException expected, but got: " + testResponse);
      }
      catch (WebServiceException ex)
      {
         testResponse = ex.getMessage();
      }

      String trackerMessages = HandlerTracker.getListMessages();
      List<String> expMessages = new ArrayList<String>();
      expMessages.add("ClientHandler1:Message:OutBound");
      expMessages.add("ClientHandler2:Message:OutBound");
      expMessages.add("ClientHandler3:Message:OutBound");
      expMessages.add("ClientHandler3:Message:InBound");
      expMessages.add("ClientHandler2:Message:InBound:ErrorInClientHandler2");
      expMessages.add("ClientHandler1:Fault:InBound");
      expMessages.add("ClientHandler3:Close");
      expMessages.add("ClientHandler2:Close");
      expMessages.add("ClientHandler1:Close");
      assertEquals(expMessages.toString(), trackerMessages);

      trackerMessages = trackerPort.getListMessages();
      expMessages = new ArrayList<String>();
      expMessages.add("PostServerHandler2:Message:InBound");
      expMessages.add("PostServerHandler1:Message:InBound");
      expMessages.add("ServerHandler3:Message:InBound");
      expMessages.add("ServerHandler2:Message:InBound");
      expMessages.add("ServerHandler1:Message:InBound");
      expMessages.add("PreServerHandler2:Message:InBound");
      expMessages.add("PreServerHandler1:Message:InBound");
      expMessages.add("PreServerHandler1:Message:OutBound");
      expMessages.add("PreServerHandler2:Message:OutBound");
      expMessages.add("ServerHandler1:Message:OutBound");
      expMessages.add("ServerHandler2:Message:OutBound");
      expMessages.add("ServerHandler3:Message:OutBound");
      expMessages.add("PostServerHandler1:Message:OutBound");
      expMessages.add("PostServerHandler2:Message:OutBound");
      expMessages.add("PreServerHandler1:Close");
      expMessages.add("PreServerHandler2:Close");
      expMessages.add("ServerHandler1:Close");
      expMessages.add("ServerHandler2:Close");
      expMessages.add("ServerHandler3:Close");
      expMessages.add("PostServerHandler1:Close");
      expMessages.add("PostServerHandler2:Close");
      assertEquals(expMessages.toString(), trackerMessages);

      assertEquals("ErrorInClientHandler2", testResponse);
   }

   public void testPropertyScoping() throws Exception
   {
      Map<String, Object> reqContext = ((BindingProvider)port).getRequestContext();
      Map<String, Object> resContext = ((BindingProvider)port).getResponseContext();
      reqContext.put("client-req-prop", Boolean.TRUE);

      String retStr = port.runTest(getName());
      assertEquals(getName() + "Response", retStr);

      assertNull(resContext.get("client-handler-prop"));
      assertEquals(Boolean.TRUE, resContext.get("client-res-prop"));
   }
}
