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
package org.jboss.test.ws.jaxrpc.samples.dynamichandler;

import java.util.Iterator;
import java.util.List;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.rpc.handler.HandlerRegistry;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.ws.core.jaxrpc.client.ServiceExt;
import org.jboss.ws.metadata.umdm.HandlerMetaData;
import org.jboss.wsf.spi.utils.ObjectNameFactory;

/**
 * Test dynamic handlers
 *
 * @author Thomas.Diesler@jboss.org
 * @since 20-Jul-2005
 */
public class DynamicHandlerTestCase extends JBossWSTest
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxrpc-samples-dynamichandler";
   private static final String TARGET_NAMESPACE = "http://org.jboss.ws/samples/dynamichandler";

   private static ServiceExt service;
   private static HandlerTestService endpoint;

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(DynamicHandlerTestCase.class, "jaxrpc-samples-dynamichandler.war, jaxrpc-samples-dynamichandler-client.jar");
   }

   protected void setUp() throws Exception
   {
      super.setUp();

      if (endpoint == null)
      {
         InitialContext iniCtx = getInitialContext();
         service = (ServiceExt)iniCtx.lookup("java:comp/env/service/TestService");
         endpoint = (HandlerTestService)service.getPort(HandlerTestService.class);
      }
   }

   public void testStaticHandlers() throws Exception
   {
      String res = endpoint.testHandlers("InitalMessage");
      assertEquals("InitalMessage|ClientRequest|ServerRequest|ServerResponse|ClientResponse", res);
   }
   
   public void testRemoveClientHandlers() throws Exception
   {
      HandlerRegistry registry = service.getDynamicHandlerRegistry();
      QName portName = new QName(TARGET_NAMESPACE, "HandlerTestServicePort");
      
      List infos = registry.getHandlerChain(portName);
      Iterator it = infos.iterator();
      while (it.hasNext())
      {
         HandlerInfo info = (HandlerInfo)it.next();
         if (info.getHandlerClass() == ClientSideHandler.class)
            it.remove();
      }
      registry.setHandlerChain(portName, infos);
      
      String res = endpoint.testHandlers("InitalMessage");
      assertEquals("InitalMessage|ServerRequest|ServerResponse", res);
   }
   
   public void testRemoveServerHandlers() throws Exception
   {
      if (true)
      {
         System.out.println("FIXME: [JBWS-1626] Resurect dynamic server handlers");
         return;
      }
      
      MBeanServerConnection server = getServer();
      ObjectName oname = ObjectNameFactory.create("jboss.ws:service=ServiceEndpointManager");
      ObjectName serviceID = new ObjectName("jboss.ws:context=jaxrpc-samples-dynamichandler,endpoint=TestService");

      List handlers = (List)server.invoke(oname, "getHandlerMetaData", new Object[]{serviceID}, new String[]{"javax.management.ObjectName"});
      Iterator it = handlers.iterator();
      while (it.hasNext())
      {
         HandlerMetaData handlerMetaData = (HandlerMetaData)it.next();
         if (handlerMetaData.getHandlerClassName().equals(ServerSideHandler.class.getName()))
            it.remove();
      }
      server.invoke(oname, "stopServiceEndpoint", new Object[]{serviceID}, new String[]{"javax.management.ObjectName"});
      server.invoke(oname, "setHandlerMetaData", new Object[]{serviceID, handlers}, new String[]{"javax.management.ObjectName", "java.util.List"});
      server.invoke(oname, "startServiceEndpoint", new Object[]{serviceID}, new String[]{"javax.management.ObjectName"});
      
      String res = endpoint.testHandlers("InitalMessage");
      assertEquals("InitalMessage", res);
   }
}
