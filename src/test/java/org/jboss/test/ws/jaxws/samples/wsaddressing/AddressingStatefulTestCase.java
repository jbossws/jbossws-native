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
package org.jboss.test.ws.jaxws.samples.wsaddressing;

import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.ws.extensions.addressing.jaxws.WSAddressingClientHandler;

/**
 * Test stateful endpoint using ws-addressing
 *
 * @author Thomas.Diesler@jboss.org
 * @since 24-Nov-2005
 */
public class AddressingStatefulTestCase extends JBossWSTest
{
   private static StatefulEndpoint port1;
   private static StatefulEndpoint port2;

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(AddressingStatefulTestCase.class, "jaxws-samples-wsaddressing.war, jaxws-samples-wsaddressing-client.jar");
   }

   protected void setUp() throws Exception
   {
      super.setUp();

      if (port1 == null || port2 == null)
      {
         URL wsdlURL = new URL(" http://" + getServerHost() + ":8080/jaxws-samples-wsaddressing/TestService?wsdl");
         QName serviceName = new QName("http://org.jboss.ws/samples/wsaddressing", "TestService");

         Service service1 = Service.create(wsdlURL, serviceName);
         port1 = (StatefulEndpoint)service1.getPort(StatefulEndpoint.class);
         BindingProvider bindingProvider = (BindingProvider)port1;
         List<Handler> handlerChain = bindingProvider.getBinding().getHandlerChain();
         handlerChain.add(new ClientHandler());
         handlerChain.add(new WSAddressingClientHandler());

         Service service2 = Service.create(wsdlURL, serviceName);
         port2 = (StatefulEndpoint)service2.getPort(StatefulEndpoint.class);
         bindingProvider = (BindingProvider)port2;
         handlerChain = bindingProvider.getBinding().getHandlerChain();
         handlerChain.add(new ClientHandler());
         handlerChain.add(new WSAddressingClientHandler());
      }
   }

   public void testAddItem() throws Exception
   {
      port1.addItem("Ice Cream");
      port1.addItem("Ferrari");

      port2.addItem("Mars Bar");
      port2.addItem("Porsche");
   }

   public void testGetItems() throws Exception
   {
      String items1 = port1.getItems();
      assertEquals("[Ice Cream, Ferrari]", items1);

      String items2 = port2.getItems();
      assertEquals("[Mars Bar, Porsche]", items2);
   }

   public void testCheckout() throws Exception
   {
      port1.checkout();
      assertEquals("[]", port1.getItems());

      port2.checkout();
      assertEquals("[]", port2.getItems());
   }
}
