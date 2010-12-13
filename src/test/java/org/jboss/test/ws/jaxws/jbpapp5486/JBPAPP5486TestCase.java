/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.test.ws.jaxws.jbpapp5486;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import junit.framework.Test;

import org.jboss.ws.core.StubExt;
import org.jboss.ws.core.WSTimeoutException;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBPAPP-5486] Timeout value gets inserted into URLs
 * 
 * @author darran.lofthouse@jboss.com
 * @since 13th December 2010
 * @see https://jira.jboss.org/browse/JBPAPP-5486
 */
public class JBPAPP5486TestCase extends JBossWSTest
{

   public static Test suite() throws Exception
   {
      return new JBossWSTestSetup(JBPAPP5486TestCase.class, "jaxws-jbpapp5486.war");
   }

   public void testNoTimeout() throws Exception
   {
      Service service = getService();
      Endpoint port = service.getPort(Endpoint.class);

      String message = "Hello 1";
      String response = port.verifyNoTimeoutParameter(message);
      assertEquals("Response Message", message, response);
   }

   public void testTimeout() throws Exception
   {
      Service service = getService();
      Endpoint port = service.getPort(Endpoint.class);

      ((BindingProvider)port).getRequestContext().put(StubExt.PROPERTY_CLIENT_TIMEOUT, "10000");

      String message = "Hello 2";
      String response = port.verifyNoTimeoutParameter(message);
      assertEquals("Response Message", message, response);
   }

   public void testTimeoutWorks() throws Exception
   {
      Service service = getService();
      Endpoint port = service.getPort(Endpoint.class);

      ((BindingProvider)port).getRequestContext().put(StubExt.PROPERTY_CLIENT_TIMEOUT, "500");

      String message = "Hello 3";
      try
      {
         port.doSleep(message, 1000);
         fail("Expected WS exception not thrown.");
      }
      catch (WebServiceException ignored)
      {
         assertEquals("Expected Cause", WSTimeoutException.class.getName(), ignored.getCause().getClass().getName());
      }

   }

   Service getService() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbpapp5486?wsdl");
      QName serviceName = new QName("http://ws.jboss.org/jbpapp5486", "EndpointImplService");

      Service service = Service.create(wsdlURL, serviceName);

      return service;
   }

}
