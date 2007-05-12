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
package org.jboss.test.ws.jaxws.samples.jsr181ejb;

import java.io.File;
import java.net.URL;
import java.util.Map;

import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;
import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;

/**
 * Test the JSR-181 annotation: javax.jws.WebService
 *
 * @author Thomas.Diesler@jboss.org
 * @since 29-Apr-2005
 */
public class JSR181WebServiceEJB3TestCase extends JBossWSTest
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-samples-jsr181ejb/EJB3Bean01";

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(JSR181WebServiceEJB3TestCase.class, "jaxws-samples-jsr181ejb.jar");
   }

   public void testRemoteAccess() throws Exception
   {
      InitialContext iniCtx = getInitialContext();
      EJB3RemoteInterface ejb3Remote = (EJB3RemoteInterface)iniCtx.lookup("/ejb3/EJB3Bean01");

      SecurityAssociation.setPrincipal(new SimplePrincipal("kermit"));
      SecurityAssociation.setCredential("thefrog");
      
      String helloWorld = "Hello world!";
      Object retObj = ejb3Remote.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   public void testWebService() throws Exception
   {
      URL wsdlURL = new File("resources/jaxws/samples/jsr181ejb/META-INF/wsdl/TestService.wsdl").toURL();
      QName serviceName = new QName("http://org.jboss.ws/samples/jsr181ejb", "TestService");
      Service service = Service.create(wsdlURL, serviceName);
      EndpointInterface port = (EndpointInterface)service.getPort(EndpointInterface.class);

      BindingProvider bindingProvider = (BindingProvider)port;
      Map<String, Object> reqContext = bindingProvider.getRequestContext();
      reqContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, TARGET_ENDPOINT_ADDRESS);
      reqContext.put(BindingProvider.USERNAME_PROPERTY, "kermit");
      reqContext.put(BindingProvider.PASSWORD_PROPERTY, "thefrog");
      
      String helloWorld = "Hello world!";
      Object retObj = port.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }
}
