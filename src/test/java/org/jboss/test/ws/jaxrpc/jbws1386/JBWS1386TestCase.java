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
package org.jboss.test.ws.jaxrpc.jbws1386;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceFactory;
import javax.xml.rpc.Stub;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.ws.core.jaxrpc.client.ServiceFactoryImpl;

/**
 * [JBWS-1386] - ComplexType with base64Binary property
 * 
 * http://jira.jboss.org/jira/browse/JBWS-1386
 *
 * @author Thomas.Diesler@jboss.com
 * @since 09-Jan-2007
 */
public class JBWS1386TestCase extends JBossWSTest
{
   private static RequestService port;
   
   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(JBWS1386TestCase.class, "jaxrpc-jbws1386.war");
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      if (port == null)
      {
         ServiceFactoryImpl factory = (ServiceFactoryImpl)ServiceFactory.newInstance();
         URL wsdlURL = new File("resources/jaxrpc/jbws1386/WEB-INF/wsdl/TestService.wsdl").toURL();
         URL mappingURL = new File("resources/jaxrpc/jbws1386/WEB-INF/jaxrpc-mapping.xml").toURL();
         QName serviceName = new QName("http://org.jboss.test.ws/jbws1386", "TestService");
         Service service = factory.createService(wsdlURL, serviceName , mappingURL);
         port = (RequestService)service.getPort(RequestService.class);
         ((Stub)port)._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, "http://" + getServerHost() + ":8080/jaxrpc-jbws1386");
      }
   }

   public final void testEndpointAccess() throws Exception
   {
      Message inObj = new Message("Kermit", "base64".getBytes());
      Message retObj = port.processClaim(inObj);
      assertEquals(inObj, retObj);
   }
}
