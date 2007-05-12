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
package org.jboss.test.ws.jaxws.samples.jsr181pojo;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;

/**
 * Test the JSR-181 annotation: javax.jws.WebService
 *
 * @author Thomas.Diesler@jboss.org
 * @since 29-Apr-2005
 */
public class JSR181WebServiceJSETestCase extends JBossWSTest
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-samples-jsr181pojo";

   private static EndpointInterface port;

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(JSR181WebServiceJSETestCase.class, "jaxws-samples-jsr181pojo.war");
   }

   public void testWebService() throws Exception
   {
      URL wsdlURL = new File("resources/jaxws/samples/jsr181pojo/META-INF/wsdl/TestService.wsdl").toURL();
      QName qname = new QName("http://org.jboss.ws/samples/jsr181pojo", "TestService");
      Service service = Service.create(wsdlURL, qname);
      port = (EndpointInterface)service.getPort(EndpointInterface.class);

      String helloWorld = "Hello world!";
      Object retObj = port.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }
}
