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
package org.jboss.test.ws.jaxws.samples.logicalhandler;

// $Id:LogicalHandlerTestCase.java 888 2006-09-02 00:37:13Z thomas.diesler@jboss.com $

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;

/**
 * Test JAXWS logical handlers
 *
 * @author Thomas.Diesler@jboss.org
 * @since 12-Aug-2006
 */
public class LogicalHandlerSourceTestCase extends JBossWSTest
{
   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(LogicalHandlerSourceTestCase.class, "jaxws-samples-logicalhandler-source.war");
   }

   public void testSourceDoc() throws Exception
   {
      String endpointAddress = "http://" + getServerHost() + ":8080/jaxws-samples-logicalhandler-source/doc";
      QName serviceName = new QName("http://org.jboss.ws/jaxws/samples/logicalhandler", "SOAPEndpointDocService");
      Service service = Service.create(new URL(endpointAddress + "?wsdl"), serviceName);
      SOAPEndpointSourceDoc port = (SOAPEndpointSourceDoc)service.getPort(SOAPEndpointSourceDoc.class);
      
      String retStr = port.echo("hello");
      
      StringBuffer expStr = new StringBuffer("hello");
      expStr.append(":LogicalClientHandler");
      expStr.append(":SOAP11ClientHandler");
      expStr.append(":PortClientHandler");
      expStr.append(":PortServerHandler");
      expStr.append(":SOAP11ServerHandler");
      expStr.append(":LogicalServerHandler");
      expStr.append(":endpoint");
      expStr.append(":LogicalServerHandler");
      expStr.append(":SOAP11ServerHandler");
      expStr.append(":PortServerHandler");
      expStr.append(":PortClientHandler");
      expStr.append(":SOAP11ClientHandler");
      expStr.append(":LogicalClientHandler");
      assertEquals(expStr.toString(), retStr);
   }


   public void testSourceRpc() throws Exception
   {
      String endpointAddress = "http://" + getServerHost() + ":8080/jaxws-samples-logicalhandler-source/rpc";
      QName serviceName = new QName("http://org.jboss.ws/jaxws/samples/logicalhandler", "SOAPEndpointRpcService");
      Service service = Service.create(new URL(endpointAddress + "?wsdl"), serviceName);
      SOAPEndpointSourceRpc port = (SOAPEndpointSourceRpc)service.getPort(SOAPEndpointSourceRpc.class);
      
      String retStr = port.echo("hello");
      
      StringBuffer expStr = new StringBuffer("hello");
      expStr.append(":LogicalClientHandler");
      expStr.append(":SOAP11ClientHandler");
      expStr.append(":PortClientHandler");
      expStr.append(":PortServerHandler");
      expStr.append(":SOAP11ServerHandler");
      expStr.append(":LogicalServerHandler");
      expStr.append(":endpoint");
      expStr.append(":LogicalServerHandler");
      expStr.append(":SOAP11ServerHandler");
      expStr.append(":PortServerHandler");
      expStr.append(":PortClientHandler");
      expStr.append(":SOAP11ClientHandler");
      expStr.append(":LogicalClientHandler");
      assertEquals(expStr.toString(), retStr);
   }
}
