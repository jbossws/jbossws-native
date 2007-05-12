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
package org.jboss.test.ws.jaxws.samples.context;

// $Id$

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;

/**
 * Test JAXWS WebServiceContext
 *
 * @author Thomas.Diesler@jboss.org
 * @since 29-Apr-2005
 */
public class WebServiceContextJSETestCase extends JBossWSTest
{
   private static TestEndpoint port;
   
   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(WebServiceContextJSETestCase.class, "jaxws-samples-context.war");
   }

   public void setUp() throws Exception
   {
      if (port == null)
      {
         URL wsdlURL = new File("resources/jaxws/samples/context/WEB-INF/wsdl/TestEndpoint.wsdl").toURL();
         QName qname = new QName("http://org.jboss.ws/jaxws/context", "TestEndpointService");
         Service service = Service.create(wsdlURL, qname);
         port =  (TestEndpoint)service.getPort(TestEndpoint.class);
         
         BindingProvider bp = (BindingProvider)port;
         bp.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "kermit");
         bp.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "thefrog");
      }   
   }

   public void testGetWebContext() throws Exception
   {
      String retStr = port.testGetMessageContext();
      assertEquals("pass", retStr);
   }

   public void testGetUserPrincipal() throws Exception
   {
      String retStr = port.testGetUserPrincipal();
      assertEquals("kermit", retStr);
   }

   public void testIsUserInRole() throws Exception
   {
      assertTrue("kermit is my friend", port.testIsUserInRole("friend"));
   }
}
