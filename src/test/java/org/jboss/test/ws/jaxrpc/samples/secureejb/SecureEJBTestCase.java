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
package org.jboss.test.ws.jaxrpc.samples.secureejb;

import java.rmi.RemoteException;

import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.Stub;

import junit.framework.Test;

import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;
import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;


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

   /*
   public static Test suite() throws Exception
   {
      return JBossWSTestSetup.newTestSetup(SecureEJBTestCase.class, "jaxrpc-samples-secureejb.jar, jaxrpc-samples-secureejb-client.jar");
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      SecurityAssociation.setPrincipal(null);
      SecurityAssociation.setCredential(null);
   }
   */

   /** Test required principal/credential for this bean
    */
   public void testRoleSecuredSLSB() throws Exception
   {
      
      System.out.println("FIXME: [JBAS-3817] Fix EJB2.1 deployments");
      if (true) return;
      
      InitialContext iniCtx = getInitialContext();
      OrganizationHome home = (OrganizationHome)iniCtx.lookup("ejb/RoleSecuredSLSB");

      OrganizationRemote bean = null;
      try
      {
         bean = home.create();
         fail("Security exception expected");
      }
      catch (Exception e)
      {
         // all cool, now try again with valid credentials
         SecurityAssociation.setPrincipal(new SimplePrincipal(USERNAME));
         SecurityAssociation.setCredential(PASSWORD);
         bean = home.create();
      }

      String info = bean.getContactInfo("mafia");
      assertEquals("The 'mafia' boss is currently out of office, please call again.", info);
   }

   /** Test that the remote access to this bean is unchecked
    */
   public void testBasicSecuredSLSB() throws Exception
   {
      
      System.out.println("FIXME: [JBAS-3817] Fix EJB2.1 deployments");
      if (true) return;
      
      InitialContext iniCtx = getInitialContext();
      OrganizationHome home = (OrganizationHome)iniCtx.lookup("ejb/BasicSecuredSLSB");

      OrganizationRemote bean = home.create();
      String info = bean.getContactInfo("mafia");
      assertEquals("The 'mafia' boss is currently out of office, please call again.", info);
   }

   public void testBasicSecuredServiceAccess() throws Exception
   {
      
      System.out.println("FIXME: [JBAS-3817] Fix EJB2.1 deployments");
      if (true) return;
      
      InitialContext iniCtx = getInitialContext();
      Service service = (Service)iniCtx.lookup("java:comp/env/service/BasicSecured");
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

   public void testRoleSecuredServiceAccess() throws Exception
   {
      
      System.out.println("FIXME: [JBAS-3817] Fix EJB2.1 deployments");
      if (true) return;
      
      InitialContext iniCtx = getInitialContext();
      Service service = (Service)iniCtx.lookup("java:comp/env/service/RoleSecured");
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

   public void testConfidentialServiceAccess() throws Exception
   {
      
      System.out.println("FIXME: [JBAS-3817] Fix EJB2.1 deployments");
      if (true) return;
      
      InitialContext iniCtx = getInitialContext();
      Service service = (Service)iniCtx.lookup("java:comp/env/service/ConfidentialSecured");
      QName portName = new QName("http://org.jboss.ws/samples/secureejb", "ConfidentialPort");
      OrganizationService port = (OrganizationService)service.getPort(portName, OrganizationService.class);
      
      Stub stub = (Stub)port;
      String address = (String)stub._getProperty(Stub.ENDPOINT_ADDRESS_PROPERTY);
      assertEquals("https://" + getServerHost() + ":8443/ws4ee-samples-ejb/ConfidentialSecured", address);

      // test non-confidential access
      try
      {
         stub._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, "http://" + getServerHost() + ":8080/ws4ee-samples-ejb/ConfidentialSecured");
         port.getContactInfo("mafia");
         System.out.println("FIXME: JBAS-3595");
         //fail("Security exception expected");
      }
      catch (RemoteException ignore)
      {
         // ignore expected exception
      }
      
      // test confidential access
      //stub._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, "https://" + getServerHost() + ":8443/ws4ee-samples-ejb/ConfidentialSecured");
      //String info = port.getContactInfo("mafia");
      //assertEquals("The 'mafia' boss is currently out of office, please call again.", info);
   }
}
