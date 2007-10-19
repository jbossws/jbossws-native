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
package org.jboss.test.ws.jaxrpc.jbws1762;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceFactory;

import org.jboss.test.ws.jaxrpc.jbws1762.services.POJOIface;
import org.jboss.ws.core.jaxrpc.client.ServiceFactoryImpl;
import org.jboss.wsf.test.JBossWSTest;

/**
 * [JBWS-1762] web.xml modified to web.xml.org - subsequent runs fail
 *
 * @author richard.opalka@jboss.com
 *
 * @since Oct 18, 2007
 */
public abstract class AbstractPOJOTest extends JBossWSTest
{
   private String pojoTargetNS = "http://org.jboss.test.ws/jbws1762";
   private String pojoServiceName = "POJOBean";
   private POJOIface pojoProxy;

   public void setUp() throws Exception
   {
      super.setUp();
      
      if (pojoProxy == null)
      {
         ServiceFactoryImpl factory = (ServiceFactoryImpl)ServiceFactory.newInstance();
         URL wsdlURL = new URL("http://" + getServerHost() + ":8080/" + getWSDLLocation());
         URL mappingURL = new File("resources/jaxrpc/jbws1762/WEB-INF/jaxrpc-mapping.xml").toURL();
         QName serviceName = new QName(pojoTargetNS, pojoServiceName);
         
         Service service = factory.createService(wsdlURL, serviceName, mappingURL);
         pojoProxy = (POJOIface)service.getPort(POJOIface.class);
      }
   }
   
   protected abstract String getWSDLLocation();

   public void testPOJO() throws Exception
   {
      assertEquals(pojoProxy.echo("Hello!"), "Hello!");
   }
}
