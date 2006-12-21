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
package org.jboss.test.ws.jaxws.eardeployment;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;
import org.jboss.ws.core.utils.DOMUtils;
import org.jboss.ws.core.utils.ResourceURL;
import org.w3c.dom.Element;

/**
 * Test ear deployment
 *
 * @author Thomas.Diesler@jboss.org
 * @since 08-Dec-2006
 */
public class EarTestCase extends JBossWSTest
{
   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(EarTestCase.class, "jaxws-eardeployment.ear");
   }
   
   public void testResourceURL() throws Exception
   {
      File earFile = new File("libs/jaxws-eardeployment.ear");
      assertTrue(earFile.exists());
      
      URL warURL = new URL("jar:" + earFile.toURL() + "!/jaxws-eardeployment.war!/");
      URL wsdlURL = new URL(warURL, "WEB-INF/wsdl/TestEndpoint.wsdl");
      
      Element root = DOMUtils.parse(new ResourceURL(wsdlURL).openStream());
      assertNotNull(root);
   }

   public void testVirtualFile() throws Exception
   {
      File earFile = new File("libs/jaxws-eardeployment.ear");
      assertTrue(earFile.exists());
      
      URL earURL = earFile.toURL();
      VFS fsEar = VFS.getVFS(earURL);
      VirtualFile vfWar = fsEar.findChild("jaxws-eardeployment.war");
      assertNotNull(earURL + "!/jaxws-eardeployment.war", vfWar);
      
      VirtualFile vfWsdl = vfWar.findChild("WEB-INF/wsdl/TestEndpoint.wsdl");
      assertNotNull(earURL + "!/jaxws-eardeployment.war!/WEB-INF/wsdl/TestEndpoint.wsdl", vfWsdl);
      
      Element root = DOMUtils.parse(vfWsdl.openStream());
      assertNotNull(root);
   }

   public void testEJB3Endpoint() throws Exception
   {
      URL wsdlURL = new URL ("http://" + getServerHost() + ":8080/earejb3/EJB3Bean?wsdl");
      QName serviceName = new QName("http://eardeployment.jaxws.ws.test.jboss.org/", "TestEndpointService");
      TestEndpointService service = new TestEndpointService(wsdlURL, serviceName);
      TestEndpoint port = service.getTestEndpointPort();
      
      String helloWorld = "Hello world!";
      String retObj = port.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   public void testJSEEndpoint() throws Exception
   {
      URL wsdlURL = new URL ("http://" + getServerHost() + ":8080/earjse/JSEBean?wsdl");
      QName serviceName = new QName("http://eardeployment.jaxws.ws.test.jboss.org/", "TestEndpointService");
      TestEndpointService service = new TestEndpointService(wsdlURL, serviceName);
      TestEndpoint port = service.getTestEndpointPort();
      
      String helloWorld = "Hello world!";
      String retObj = port.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }
}
