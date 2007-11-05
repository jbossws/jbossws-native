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
package org.jboss.test.ws.jaxws.jbws1762;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.jboss.test.ws.jaxws.jbws1762.services.EJB3Iface;
import org.jboss.wsf.test.JBossWSTest;

/**
 * [JBWS-1762] web.xml modified to web.xml.org - subsequent runs fail
 *
 * @author richard.opalka@jboss.com
 *
 * @since Oct 17, 2007
 */
public abstract class AbstractEJB3Test extends JBossWSTest
{
   private String ejb3TargetNS = "http://services.jbws1762.jaxws.ws.test.jboss.org/";
   private String ejb3ServiceName = "JBWS1762EJB3Service";
   private static EJB3Iface ejb3Proxy;
   
   @Override
   public void setUp() throws Exception
   {
      super.setUp();

      if (ejb3Proxy == null)
      {
         QName serviceName = new QName(ejb3TargetNS, ejb3ServiceName);
         URL wsdlURL = new URL("http://" + getServerHost() + ":8080/" + getWSDLLocation());
         
         Service service = Service.create(wsdlURL, serviceName);
         ejb3Proxy = service.getPort(EJB3Iface.class);
      }
   }
   
   protected abstract String getWSDLLocation();

   public void testEJB3() throws Exception
   {
      assertEquals(ejb3Proxy.echo("Hello!"), "Hello!");
   }
}
