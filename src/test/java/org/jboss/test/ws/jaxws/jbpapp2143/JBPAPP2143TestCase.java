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
package org.jboss.test.ws.jaxws.jbpapp2143;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.ws.core.StubExt;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBPAPP-2143] Setting a new config at runtime the properties are not 
 * available to RemotingConnectionImpl
 * 
 * @author darran.lofthouse@jboss.com
 * @since 3rd December 2010
 * @see https://jira.jboss.org/browse/JBPAPP-2143
 */
public class JBPAPP2143TestCase extends JBossWSTest
{

   public static Test suite() throws Exception
   {
      return new JBossWSTestSetup(JBPAPP2143TestCase.class, "jaxws-jbpapp2143.war");
   }

   // Default config, chunked so no content length.
   public void testNoConfig_Success() throws Exception
   {
      Service service = getService();
      Endpoint port = service.getPort(Endpoint.class);

      String message = "Hello 1";
      String response = port.verifyNoContentLength(message);
      assertEquals("Response Message", message, response);
   }

   // Default config, chunked so no content length.
   public void testNoConfig_Failure() throws Exception
   {
      Service service = getService();
      Endpoint port = service.getPort(Endpoint.class);

      String message = "Hello 2";
      try
      {
         port.verifyHasContentLength(message);
         fail("Expected exception not thrown.");
      }
      catch (Exception ignored)
      {
      }

   }

   // Custom config, chunking disables so has content length.
   public void testWithConfig_Success() throws Exception
   {
      Service service = getService();
      Endpoint port = service.getPort(Endpoint.class);
      setConfigName(port);

      String message = "Hello 3";
      String response = port.verifyHasContentLength(message);
      assertEquals("Response Message", message, response);
   }

   // Custom config, chunking disables so has content length.
   public void testWithConfig_Failure() throws Exception
   {
      Service service = getService();
      Endpoint port = service.getPort(Endpoint.class);
      setConfigName(port);

      String message = "Hello 4";
      try
      {
         port.verifyNoContentLength(message);
         fail("Expected exception not thrown.");
      }
      catch (Exception ignored)
      {
      }

   }

   void setConfigName(Endpoint port)
   {
      File config = new File("resources/jaxws/jbpapp2143/META-INF/jbpapp2143-client-config.xml");      
      ((StubExt)port).setConfigName("Test Config", config.getAbsolutePath());
   }

   Service getService() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbpapp2143?wsdl");
      QName serviceName = new QName("http://ws.jboss.org/jbpapp2143", "EndpointImplService");

      Service service = Service.create(wsdlURL, serviceName);

      return service;
   }

}
