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
package org.jboss.test.ws.jaxws.jbws1581;

import java.net.URL;

import junit.framework.Test;

import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.tools.wsdl.WSDLDefinitionsFactory;
import org.jboss.wsf.spi.test.JBossWSTest;
import org.jboss.wsf.spi.test.JBossWSTestSetup;

/**
 * EJB vehicle using loader repository not sufficiently isolated
 *
 * http://jira.jboss.org/jira/browse/JBWS-1581
 *
 * @author Thomas.Diesler@jboss.com
 * @since 19-Mar-2007
 */
public class JBWS1581EarTestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS1581EarTestCase.class, "jaxws-jbws1581.ear, jaxws-jbws1581.jar");
   }

   public void testWSDLAccess() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws1581?wsdl");
      WSDLDefinitions wsdl = WSDLDefinitionsFactory.newInstance().parse(wsdlURL);
      assertNotNull("wsdl expected", wsdl);
   }
   
   public void testEJBVehicle() throws Exception
   {
      EJB3Remote remote = (EJB3Remote)getInitialContext().lookup("/ejb3/EJB3Bean");
      String retStr = remote.runTest("Hello World!");
      assertEquals("Hello World!", retStr);
   }
}