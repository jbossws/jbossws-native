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
package org.jboss.test.ws.jaxws.jbws860;

// $Id$

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import junit.framework.Test;

import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.tools.wsdl.WSDLDefinitionsFactory;
import org.jboss.wsf.spi.test.JBossWSTest;
import org.jboss.wsf.spi.test.JBossWSTestSetup;

/**
 * Investigate "Is Not A Servlet" error with multiple servlets in the web.xml
 * 
 * http://jira.jboss.com/jira/browse/JBWS-860
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 26-Apr-2006
 */
public class JBWS860TestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS860TestCase.class, "jaxws-jbws860.war");
   }

   public void testAccessInventoryServiceWsdl() throws Exception
   {
      WSDLDefinitionsFactory factory = WSDLDefinitionsFactory.newInstance();
      WSDLDefinitions wsdlDef = factory.parse(new URL("http://" + getServerHost() + ":8080/test/InventoryWebService?wsdl"));
      assertNotNull(wsdlDef);
   }

   public void testServletAccess() throws Exception
   {     
      HttpURLConnection con = (HttpURLConnection)new URL("http://" + getServerHost() + ":8080/test/TestServlet").openConnection();
      BufferedReader isr = new BufferedReader(new InputStreamReader(con.getInputStream()));
      assertEquals("Hello", isr.readLine());
   }
}
