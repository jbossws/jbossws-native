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
package org.jboss.test.ws.jaxws.jbws1904;

import java.net.URL;

import junit.framework.Test;

import org.apache.xerces.xs.XSTypeDefinition;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.metadata.wsdl.WSDLUtils;
import org.jboss.ws.metadata.wsdl.xmlschema.JBossXSModel;
import org.jboss.ws.tools.wsdl.WSDLDefinitionsFactory;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-1904] Explicitly set the namespace of a WebFault
 * 
 * http://jira.jboss.org/jira/browse/JBWS-1904
 *
 * @author alessio.soldano@jboss.com
 * @since 13-Dec-2007
 */
public class JBWS1904TestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS1904TestCase.class, "jaxws-jbws1904.jar");
   }

   public void testWSDLSchema() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws1904?wsdl");
      WSDLDefinitionsFactory factory = WSDLDefinitionsFactory.newInstance();
      WSDLDefinitions wsdlDefinitions = factory.parse(wsdlURL);
      JBossXSModel xsModel = WSDLUtils.getSchemaModel(wsdlDefinitions.getWsdlTypes());
      XSTypeDefinition typeDefinition = xsModel.getTypeDefinition("TestException", "http://org.jboss.ws/jbws1904/exceptions");
      assertNotNull(typeDefinition);
      typeDefinition = xsModel.getTypeDefinition("TestException", "http://org.jboss.ws/jbws1904");
      assertNull(typeDefinition);
      typeDefinition = xsModel.getTypeDefinition("TestException", "http://org.jboss.ws/jbws1904/faults");
      assertNull(typeDefinition);
   }
}
