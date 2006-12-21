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
package org.jboss.test.ws.tools.config.globalconfig;

// $Id$

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.jboss.test.ws.tools.WSToolsTest;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.metadata.wsdl.WSDLUtils;
import org.jboss.ws.metadata.wsdl.xmlschema.JBossXSModel;
import org.jboss.ws.tools.WSTools;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.JavaSource;

/**
 * Tests Global Config usage in Tools Configuration
 *
 * @author <mailto:Anil.Saldhana@jboss.org>Anil Saldhana
 * @author Thomas.Diesler@jboss.org
 * @since Aug 29, 2005
 */
public class GlobalConfigTestCase extends WSToolsTest
{

   public void testJavaToWSDL() throws IOException
   {
      String[] args = new String[] { "-dest", "tools/globalconfig", "-config", "resources/tools/config/java2wsdlglobal.xml" };
      WSTools tools = new WSTools();
      tools.generate(args);

      WSDLDefinitions wsdl = getWSDLDefinitions(new File("tools/globalconfig/wsdl/MarshallService.wsdl"));
      JBossXSModel xsmodel = WSDLUtils.getSchemaModel(wsdl.getWsdlTypes());
      assertNotNull(xsmodel.getNamespaceItem("http://jboss.org/types"));
   }

   public void testWSDLToJava() throws IOException, ClassNotFoundException
   {
      String[] args = new String[] { "-dest", "tools/globalconfig", "-config", "resources/tools/config/wsdl2javaglobal.xml" };
      WSTools tools = new WSTools();
      tools.generate(args);
      File file = new File("tools/globalconfig/org/jboss/test/ws/StandardJavaTypes.java");
      assertTrue(file.exists());
      checkGeneratedClass(file);
   }

   private void checkGeneratedClass(File file) throws FileNotFoundException
   {
      JavaSource source1 = new JavaDocBuilder().addSource(new FileReader(file));
      assertEquals("org.jboss.test.ws", source1.getPackage());
   }
}
