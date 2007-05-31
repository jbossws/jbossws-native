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
package org.jboss.test.ws.common.wsdl11;

import java.io.File;

import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.metadata.wsdl.WSDLTypes;
import org.jboss.ws.metadata.wsdl.WSDLUtils;
import org.jboss.ws.tools.wsdl.WSDLDefinitionsFactory;
import org.jboss.wsf.spi.test.JBossWSTest;

/**
 * Test the XSModel, and how it handles multiple schemas
 *
 * @author jason.greene@jboss.org
 * @since 10-aug-2005
 */
public class MultiSchemaTestCase extends JBossWSTest
{
   public void testMultipleSchemas() throws Exception
   {
      File wsdlFile = new File("resources/jaxrpc/marshall-rpclit/WEB-INF/wsdl/MarshallService.wsdl");
      assertTrue(wsdlFile.exists());

      WSDLDefinitionsFactory factory = WSDLDefinitionsFactory.newInstance();
      WSDLDefinitions wsdlDefinitions = factory.parse(wsdlFile.toURL());

      // check if all schemas have been extracted
      WSDLTypes wsdlTypes = wsdlDefinitions.getWsdlTypes();
      Object o1 = WSDLUtils.getSchemaModel(wsdlTypes).getNamespaceItem("http://math.java/jaws");
      Object o2 = WSDLUtils.getSchemaModel(wsdlTypes).getNamespaceItem("http://org.jboss.ws/marshall/rpclit/types");

      assertNotNull("NS item for 'http://math.java/jaws' not found", o1);
      assertNotNull("NS item for 'http://org.jboss.ws/marshall/rpclit/types' not found", o2);
   }
}
