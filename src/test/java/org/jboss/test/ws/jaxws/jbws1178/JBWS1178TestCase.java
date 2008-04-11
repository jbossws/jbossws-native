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
package org.jboss.test.ws.jaxws.jbws1178;

import java.net.InetAddress;
import java.net.URL;
import java.util.Map;

import javax.management.Attribute;
import javax.management.ObjectName;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.jboss.wsf.common.ObjectNameFactory;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-1178] Multiple virtual host and soap:address problem
 * [JBWS-864] soap:address in wsdl ignores <url-pattern>
 *
 * @author Thomas.Diesler@jboss.com
 * @since 05-Oct-2006
 */
public class JBWS1178TestCase extends JBossWSTest
{

   public static Test suite()
   {
      TestSetup testSetup = new JBossWSTestSetup(JBWS1178TestCase.class, "jaxws-jbws1178.war")
      {
         private final ObjectName objectName = ObjectNameFactory.create("jboss.ws:service=ServerConfig");
         private String webServiceHost;

         public void setUp() throws Exception
         {
            // Setting the WebServiceHost to an empty string, causes the request host to be used.  
            // This must be done before deploy time.
            webServiceHost = (String)getServer().getAttribute(objectName, "WebServiceHost");
            getServer().setAttribute(objectName, new Attribute("WebServiceHost", ""));
            super.setUp();
         }

         public void tearDown() throws Exception
         {
            super.tearDown();
            getServer().setAttribute(objectName, new Attribute("WebServiceHost", webServiceHost));
         }
      };
      return testSetup;
   }


   public void testHostAddress() throws Exception
   {
      InetAddress inetAddr = InetAddress.getByName(getServerHost());
      URL wsdlURL = new URL("http://" + inetAddr.getHostAddress() + ":8080/jaxws-jbws1178/testpattern?wsdl");

      QName serviceName = new QName("http://org.jboss.ws/jbws1178", "TestEndpointService");
      Service service = Service.create(wsdlURL, serviceName);
      TestEndpoint port = service.getPort(TestEndpoint.class);
      Map<String, Object> reqCtx = ((BindingProvider)port).getRequestContext(); 
      URL epURL = new URL((String)reqCtx.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY));

      assertEquals(wsdlURL.getHost(), epURL.getHost());
   }

   public void testHostName() throws Exception
   {
      InetAddress inetAddr = InetAddress.getByName(getServerHost());
      URL wsdlURL = new URL("http://" + inetAddr.getHostName() + ":8080/jaxws-jbws1178/testpattern?wsdl");

      QName serviceName = new QName("http://org.jboss.ws/jbws1178", "TestEndpointService");
      Service service = Service.create(wsdlURL, serviceName);
      TestEndpoint port = service.getPort(TestEndpoint.class);
      Map<String, Object> reqCtx = ((BindingProvider)port).getRequestContext(); 
      URL epURL = new URL((String)reqCtx.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY));

      assertEquals(wsdlURL.getHost(), epURL.getHost());
   }
}
