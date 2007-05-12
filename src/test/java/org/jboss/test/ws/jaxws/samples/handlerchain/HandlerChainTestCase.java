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
package org.jboss.test.ws.jaxws.samples.handlerchain;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;

/**
 * Test the JSR-181 annotation: javax.jws.HandlerChain
 *
 * @author Thomas.Diesler@jboss.org
 * @since 15-Oct-2005
 */
public class HandlerChainTestCase extends JBossWSTest
{
   private static final String targetNS = "http://handlerchain.samples.jaxws.ws.test.jboss.org/";

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(HandlerChainTestCase.class, "jaxws-samples-handlerchain.war");
   }

   public void testDynamicHandlerChain() throws Exception
   {
      QName serviceName = new QName(targetNS, "EndpointImplService");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-samples-handlerchain/TestService?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      Endpoint port = (Endpoint)service.getPort(Endpoint.class);
      
      BindingProvider bindingProvider = (BindingProvider)port;
      List<Handler> handlerChain = new ArrayList<Handler>();
      handlerChain.add(new LogHandler());
      handlerChain.add(new AuthorizationHandler());
      handlerChain.add(new RoutingHandler());
      handlerChain.add(new ClientMimeHandler());
      bindingProvider.getBinding().setHandlerChain(handlerChain);
      
      String resStr = port.echo("Kermit");
      assertEquals("Kermit|LogOut|AuthOut|RoutOut|RoutIn|AuthIn|LogIn|endpoint|LogOut|AuthOut|RoutOut|RoutIn|AuthIn|LogIn", resStr);
   }

   public void testHandlerChainOnServiceEndpointInterface() throws Exception
   {
      QName serviceName = new QName(targetNS, "EndpointImplService");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-samples-handlerchain/TestService?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      EndpointWithHandlerChain port = (EndpointWithHandlerChain)service.getPort(EndpointWithHandlerChain.class);
      
      String resStr = port.echo("Kermit");
      assertEquals("Kermit|LogOut|AuthOut|RoutOut|RoutIn|AuthIn|LogIn|endpoint|LogOut|AuthOut|RoutOut|RoutIn|AuthIn|LogIn", resStr);
   }
}
