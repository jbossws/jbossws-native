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
package org.jboss.test.ws.jaxws.jbws1813;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 *  endpoint using @SecurityDomain
 * 
 * http://jira.jboss.org/jira/browse/JBWS-1813
 *
 * @author Thomas.Diesler@jboss.com
 * @since 09-Oct-2007
 */
public class JBWS1813TestCase extends JBossWSTest
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/test-context";

   private static Endpoint port;

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS1813TestCase.class, "jaxws-jbws1813.ear");
   }

   protected void setUp() throws Exception
   {
      if (port == null)
      {
         URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
         QName serviceName = new QName("http://org.jboss.ws/jbws1813", "EndpointService");
         port = Service.create(wsdlURL, serviceName).getPort(Endpoint.class);
      }
   }

   public void testPositive()
   {
      if (isTargetJBoss42())
      {
         System.out.println("FIXME: [JBWS-1813] context-root in jboss.xml is ignored");
         return;
     }
      
      String retObj = port.echo("Hello");
      assertEquals("Hello", retObj);
   }
}
