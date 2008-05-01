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
package org.jboss.test.ws.jaxrpc.jbws1068;

import java.io.File;

import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.tools.wsdl.WSDLDefinitionsFactory;
import org.jboss.wsf.test.JBossWSTest;

/**
 * Cannot handle xsd:import of xml schema
 * 
 * http://jira.jboss.org/jira/browse/JBWS-1068
 *
 * @author Thomas.Diesler@jboss.org
 * @since 14-Jul-2006
 */
public class JBWS1068TestCase extends JBossWSTest
{
   public void testWSDLParse() throws Exception
   {
      File wsdlFile = getResourceFile("jaxrpc/jbws1068/wsrp_services.wsdl");
      assertTrue(wsdlFile.exists());

      WSDLDefinitionsFactory factory = WSDLDefinitionsFactory.newInstance();
      WSDLDefinitions wsdlDefinitions = factory.parse(wsdlFile.toURL());
   }
}
