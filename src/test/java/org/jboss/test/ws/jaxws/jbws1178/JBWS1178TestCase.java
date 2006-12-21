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

import javax.management.Attribute;
import javax.management.ObjectName;
import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceFactory;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.ws.core.jaxrpc.CallImpl;

/**
 * [JBWS-1178] Multiple virtual host and soap:address problem
 * [JBWS-864] soap:address in wsdl ignores <url-pattern>
 *
 * @author Thomas.Diesler@jboss.com
 * @since 05-Oct-2006
 */
public class JBWS1178TestCase extends JBossWSTest
{
   String webServiceHost;
   
   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(JBWS1178TestCase.class, "jaxws-jbws1178.war");
   }

   public void setUp() throws Exception
   {
      ObjectName objectName = new ObjectName("jboss.ws:service=ServiceEndpointManager");
      webServiceHost = (String)getServer().getAttribute(objectName, "WebServiceHost");
      // Setting the WebServiceHost to an empty string, causes the request host to be used  
      getServer().setAttribute(objectName, new Attribute("WebServiceHost", ""));
   }

   public void tearDown() throws Exception
   {
      ObjectName objectName = new ObjectName("jboss.ws:service=ServiceEndpointManager");
      getServer().setAttribute(objectName, new Attribute("WebServiceHost", webServiceHost));
   }
   
   public void testHostAddress() throws Exception
   {
      InetAddress inetAddr = InetAddress.getByName(getServerHost());
      URL wsdlURL = new URL("http://" + inetAddr.getHostAddress() + ":8080/jaxws-jbws1178/testpattern?wsdl");
      
      ServiceFactory factory = ServiceFactory.newInstance();
      QName serviceName = new QName("http://org.jboss.ws/jbws1178", "TestEndpointService");
      QName portName = new QName("http://org.jboss.ws/jbws1178", "TestEndpointPort");
      Service service = factory.createService(wsdlURL, serviceName);
      CallImpl call = (CallImpl)service.createCall(portName);
      URL epURL = new URL(call.getEndpointMetaData().getEndpointAddress());
      
      assertEquals(wsdlURL.getHost(), epURL.getHost());
   }

   public void testHostName() throws Exception
   {
      InetAddress inetAddr = InetAddress.getByName(getServerHost());
      URL wsdlURL = new URL("http://" + inetAddr.getHostName() + ":8080/jaxws-jbws1178/testpattern?wsdl");
      
      ServiceFactory factory = ServiceFactory.newInstance();
      QName serviceName = new QName("http://org.jboss.ws/jbws1178", "TestEndpointService");
      QName portName = new QName("http://org.jboss.ws/jbws1178", "TestEndpointPort");
      Service service = factory.createService(wsdlURL, serviceName);
      CallImpl call = (CallImpl)service.createCall(portName);
      URL epURL = new URL(call.getEndpointMetaData().getEndpointAddress());
      
      assertEquals(wsdlURL.getHost(), epURL.getHost());
   }
}
