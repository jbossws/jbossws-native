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
package org.jboss.test.ws.jaxws.handlerscope;

// $Id: $

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test SOAP12 binding type
 *
 * @author Thomas.Diesler@jboss.org
 * @since 12-Aug-2006
 */
public class HandlerScopeTestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSTestSetup(HandlerScopeTestCase.class, "jaxws-handlerscope.war");
   }

   public void testClientAccess() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-handlerscope?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/jaxws/handlerscope", "SOAPEndpointService");
      Service service = Service.create(wsdlURL, serviceName);
      SOAPEndpoint port = (SOAPEndpoint)service.getPort(SOAPEndpoint.class);

      String retStr = port.echo("hello");
      
      StringBuffer expStr = new StringBuffer("hello");
      expStr.append(":SOAP12ClientHandler");
      expStr.append(":SOAPClientHandler");
      expStr.append(":ServiceClientHandler");
      expStr.append(":ServiceWildcardClientHandler");
      expStr.append(":PortClientHandler");
      expStr.append(":PortWildcardClientHandler");
      expStr.append(":GeneralClientHandler");
      expStr.append(":GeneralServerHandler");
      expStr.append(":PortWildcardServerHandler");
      expStr.append(":PortServerHandler");
      expStr.append(":ServiceWildcardServerHandler");
      expStr.append(":ServiceServerHandler");
      expStr.append(":SOAPServerHandler");
      expStr.append(":SOAP12ServerHandler");
      expStr.append(":endpoint");
      expStr.append(":SOAP12ServerHandler");
      expStr.append(":SOAPServerHandler");
      expStr.append(":ServiceServerHandler");
      expStr.append(":ServiceWildcardServerHandler");
      expStr.append(":PortServerHandler");
      expStr.append(":PortWildcardServerHandler");
      expStr.append(":GeneralServerHandler");
      expStr.append(":GeneralClientHandler");
      expStr.append(":PortWildcardClientHandler");
      expStr.append(":PortClientHandler");
      expStr.append(":ServiceWildcardClientHandler");
      expStr.append(":ServiceClientHandler");
      expStr.append(":SOAPClientHandler");
      expStr.append(":SOAP12ClientHandler");
      assertEquals(expStr.toString(), retStr);
   }
}
