/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.test.ws.jaxrpc.samples.secureejb;

import java.net.URL;
import java.rmi.RemoteException;

import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.Stub;

import junit.framework.Test;

import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.tools.wsdl.WSDLDefinitionsFactory;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * A web service client that connects to a secured SLSB endpoint using.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 26-Apr-2004
 */
public class SecureEJBTestCase extends JBossWSTest
{
   public static final String USERNAME = "kermit";
   public static final String PASSWORD = "thefrog";
   private InitialContext appclientCtx;

   public static Test suite() throws Exception
   {
      return new JBossWSTestSetup(SecureEJBTestCase.class, "jaxrpc-samples-secureejb.jar, jaxrpc-samples-secureejb-appclient.ear#jaxrpc-samples-secureejb-appclient.jar", true);
   }
   
   public void setUp() throws Exception
   {
      super.setUp();
      appclientCtx = getAppclientInitialContext();
   }

   public void tearDown() throws Exception
   {
      if (appclientCtx != null)
      {
         appclientCtx.close();
         appclientCtx = null;
      }
      super.tearDown();
   }

   public void testRoleSecuredWSDLAccess() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxrpc-samples-ejb/RoleSecured?wsdl");
      WSDLDefinitionsFactory factory = WSDLDefinitionsFactory.newInstance();
      WSDLDefinitions wsdl = factory.parse(wsdlURL);
      assertNotNull("Expect unsecured wsdl access by default for jaxrpc", wsdl);
   }

   public void testRoleSecuredServiceAccess() throws Exception
   {
      Service service = (Service)appclientCtx.lookup("java:service/RoleSecured");
      QName portName = new QName("http://org.jboss.ws/samples/secureejb", "RoleSecuredPort");
      OrganizationService port = (OrganizationService)service.getPort(portName, OrganizationService.class);

      try
      {
         port.getContactInfo("mafia");
         fail("Security exception expected");
      }
      catch (RemoteException ignore)
      {
         // ignore expected exception
      }

      Stub stub = (Stub)port;
      stub._setProperty(Stub.USERNAME_PROPERTY, USERNAME);
      stub._setProperty(Stub.PASSWORD_PROPERTY, PASSWORD);

      String info = port.getContactInfo("mafia");
      assertEquals("The 'mafia' boss is currently out of office, please call again.", info);
   }

   public void testBasicSecuredServiceAccess() throws Exception
   {
      Service service = (Service)appclientCtx.lookup("java:service/BasicSecured");
      QName portName = new QName("http://org.jboss.ws/samples/secureejb", "BasicSecuredPort");
      OrganizationService port = (OrganizationService)service.getPort(portName, OrganizationService.class);

      try
      {
         port.getContactInfo("mafia");
         fail("Security exception expected");
      }
      catch (RemoteException ignore)
      {
         // ignore expected exception
      }

      Stub stub = (Stub)port;
      stub._setProperty(Stub.USERNAME_PROPERTY, USERNAME);
      stub._setProperty(Stub.PASSWORD_PROPERTY, PASSWORD);

      String info = port.getContactInfo("mafia");
      assertEquals("The 'mafia' boss is currently out of office, please call again.", info);
   }

   public void testConfidentialSecuredWSDLAccess() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxrpc-samples-ejb/ConfidentialSecured?wsdl");
      WSDLDefinitionsFactory factory = WSDLDefinitionsFactory.newInstance();
      WSDLDefinitions wsdl = factory.parse(wsdlURL);
      assertNotNull("Expect unsecured wsdl access", wsdl);
   }

   public void testConfidentialServiceAccess() throws Exception
   {
      Service service = (Service)appclientCtx.lookup("java:service/ConfidentialSecured");
      QName portName = new QName("http://org.jboss.ws/samples/secureejb", "ConfidentialPort");
      OrganizationService port = (OrganizationService)service.getPort(portName, OrganizationService.class);

      Stub stub = (Stub)port;
      String address = (String)stub._getProperty(Stub.ENDPOINT_ADDRESS_PROPERTY);
      assertEquals("https://" + getServerHost() + ":8443/jaxrpc-samples-ejb/ConfidentialSecured", address);

      // test non-confidential access
      try
      {
         stub._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, "http://" + getServerHost() + ":8080/jaxrpc-samples-ejb/ConfidentialSecured");
         port.getContactInfo("mafia");

         fail("Security exception expected");
      }
      catch (RemoteException ignore)
      {
         // ignore expected exception
      }
   }
}
