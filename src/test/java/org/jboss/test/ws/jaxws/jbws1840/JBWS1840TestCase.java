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
package org.jboss.test.ws.jaxws.jbws1840;

import java.net.URL;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Secure endpoint using @SecurityDomain
 * 
 * http://jira.jboss.org/jira/browse/JBWS-1840
 *
 * @author Thomas.Diesler@jboss.com
 * @since 09-Oct-2007
 */
public class JBWS1840TestCase extends JBossWSTest
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-jbws1840";

   private static SecureEndpoint port;

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS1840TestCase.class, "jaxws-jbws1840.jar");
   }

   protected void setUp() throws Exception
   {
      if (port == null)
      {
         URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
         QName serviceName = new QName("http://org.jboss.ws/jbws1840", "SecureEndpointService");
         port = Service.create(wsdlURL, serviceName).getPort(SecureEndpoint.class);
      }
   }

   public void testNegative()
   {
      try
      {
         port.echo("Hello");
         fail("Expected: Invalid HTTP server response [401] - Unauthorized");
      }
      catch (WebServiceException ex)
      {
         // all good
      }
   }

   public void testPositive()
   {
      Map<String, Object> reqContext = ((BindingProvider)port).getRequestContext();
      reqContext.put(BindingProvider.USERNAME_PROPERTY, "kermit");
      reqContext.put(BindingProvider.PASSWORD_PROPERTY, "thefrog");
      
      String retObj = port.echo("Hello");
      assertEquals("Hello", retObj);

   }
}
