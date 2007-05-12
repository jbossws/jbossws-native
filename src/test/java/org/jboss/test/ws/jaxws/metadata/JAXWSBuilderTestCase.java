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
package org.jboss.test.ws.jaxws.metadata;

// $Id: $

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;

/**
 * Test the JAX-WS metadata builder.
 *
 * @author Heiko.Braun@jboss.org
 * @since 23.01.2007
 */
public class JAXWSBuilderTestCase extends JBossWSTest
{

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(JAXWSBuilderTestCase.class, "jaxws-metadata.war");
   }

   /**
    * If the @WebService.targetNamespace
    * annotation is on a service implementation
    * bean that does NOT reference a service
    * endpoint interface (through the
    * endpointInterface annotation element),
    * the targetNamespace is used for both the
    * wsdl:portType and the wsdl:service (and
    * associated XML elements).
    *
    * If the @WebService.targetNamespace
    * annotation is on a service implementation
    * bean that does reference a service endpoint
    * interface (through the endpointInterface
    * annotation element), the targetNamespace is
    * used for only the wsdl:service (and
    * associated XML elements).
    *
    * @throws Exception
    */
   public void testSEIDerivedNamespaces() throws Exception
   {
      // Create the port
      URL wsdlURL = new File("resources/jaxws/metadata/WEB-INF/wsdl/TestService.wsdl").toURL();
      QName qname = new QName("http://example.org/impl", "TestService");
      Service service = Service.create(wsdlURL, qname);
      EndpointInterface port = service.getPort(EndpointInterface.class);

      String helloWorld = "Hello world!";
      EchoResponse response = port.echo(new Echo(helloWorld));
      assertEquals(helloWorld, response.getMessage());

   }
}
