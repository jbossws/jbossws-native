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
package org.jboss.test.ws.jaxws.jbws981;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;

/**
 * [JBWS-981] Virtual host configuration for EJB endpoints
 *
 * http://jira.jboss.org/jira/browse/JBWS-981
 *
 * @author darran.lofthouse@jboss.com
 * @since Nov 2, 2006
 */
public class JBWS981TestCase extends JBossWSTest
{

   private static EndpointInterface port;

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(JBWS981TestCase.class, "jaxws-jbws981.jar");
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      if (true) return;
      if (port == null)
      {
         URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws981/EJB3Bean?wsdl");
         QName serviceName = new QName("http://www.jboss.org/test/ws/jaxws/jbws981", "TestService");
         Service.create(wsdlURL, serviceName);
         Service service = Service.create(wsdlURL, serviceName);
         port = (EndpointInterface)service.getPort(EndpointInterface.class);
      }
   }

   public void testCall() throws Exception
   {
      System.out.println("FIXME: [JBWS-981] Virtual host configuration for EJB endpoints (Enable once JBossAS supports virtual-host element)");
      if (true) return;

      String message = "hello";
      assertEquals("Web service mapped to virtual host.", port.hello(message));
   }
}