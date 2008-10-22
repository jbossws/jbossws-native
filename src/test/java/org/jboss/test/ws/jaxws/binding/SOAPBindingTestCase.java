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
package org.jboss.test.ws.jaxws.binding;

// $Id: $

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;

import junit.framework.Test;

import org.jboss.ws.Constants;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test SOAP12 binding type
 *
 * @author Thomas.Diesler@jboss.org
 * @since 12-Aug-2006
 */
public class SOAPBindingTestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSTestSetup(SOAPBindingTestCase.class, "jaxws-binding.war");
   }

   public void testClientAccess() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-binding?wsdl");
      QName qname = new QName("http://org.jboss.ws/jaxws/binding", "SOAPEndpointBeanService");
      Service service = Service.create(wsdlURL, qname);
      SOAPEndpoint port = (SOAPEndpoint)service.getPort(SOAPEndpoint.class);
      
      BindingProvider provider = (BindingProvider)port;
      List<Handler> handlerChain = new ArrayList<Handler>(); 
      handlerChain.addAll(provider.getBinding().getHandlerChain());
      handlerChain.add(new ClientHandler());
      handlerChain.add(new ClientHandler2());
      provider.getBinding().setHandlerChain(handlerChain);
      
      String nsURI = port.namespace();
      assertEquals(Constants.NS_SOAP12_ENV + ":" + Constants.NS_SOAP12_ENV, nsURI);
   }
   
}
