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

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.test.ws.jaxws.jbws1762.services.POJOIface;

/**
 * [JBWS-1762] web.xml modified to web.xml.org - subsequent runs fail
 *
 * @author richard.opalka@jboss.com
 *
 * @since Oct 17, 2007
 */
public abstract class AbstractPOJOTest extends JBossWSTest
{
   
   private POJOIface pojoProxy;

   @Override
   protected void setUp() throws Exception
   {
      QName serviceName = new QName("http://services.jbws1762.jaxws.ws.test.jboss.org/", "JBWS1762POJOService");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/" + getWSDLLocation());

      Service service = Service.create(wsdlURL, serviceName);
      this.pojoProxy = (POJOIface)service.getPort(POJOIface.class);
   }
   
   @Override
   protected void tearDown() throws Exception
   {
      this.pojoProxy = null;
   }
   
   public void testPOJO() throws Exception
   {
      assertEquals(pojoProxy.echo("Hello!"), "Hello!");
   }

   protected abstract String getWSDLLocation();

}
