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
package org.jboss.test.ws.tools.jaxws;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.jaxws.samples.soapbinding.DocWrappedServiceImpl;
import org.jboss.test.ws.jaxws.samples.soapbinding.PurchaseOrder;
import org.jboss.test.ws.jaxws.samples.soapbinding.PurchaseOrderAck;
import org.jboss.ws.tools.jaxws.api.WSContractProvider;
import org.jboss.ws.tools.wsdl.WSDLDefinitionsFactory;
import org.jboss.wsintegration.spi.utils.JavaUtils;

/**
 * Tests the WSContractProvider API.
 * 
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @version $Revision$
 */
public class WSContractProviderTestCase extends JBossWSTest
{
   public void testBasic() throws Exception
   {
      WSContractProvider gen = getGenerator();
      File outputDir = new File("tools/wsprovide/basic/out");
      gen.setOutputDirectory(outputDir);
      gen.provide(DocWrappedServiceImpl.class);
      
      checkWrapperClasses(outputDir);
   
      // There should be no source code
      checkWrapperSource(outputDir, false);
   }

   private WSContractProvider getGenerator()
   {
      return WSContractProvider.newInstance();
   }

   private void checkWrapperSource(File outputDir, boolean shouldExist)
   {
      File file1 = new File(outputDir, "org/jboss/test/ws/jaxws/samples/soapbinding/jaxws/SubmitPO.java");
      File file2 = new File(outputDir, "org/jboss/test/ws/jaxws/samples/soapbinding/jaxws/SubmitPOResponse.java");
      assertEquals(shouldExist, file1.exists());
      assertEquals(shouldExist, file2.exists());
   }

   private void checkWrapperClasses(File outputDir) throws MalformedURLException, ClassNotFoundException, NoSuchMethodException
   {
      // Use a different loader each time to make sure the files exist
      URLClassLoader classLoader = new URLClassLoader(new URL[]{outputDir.toURL()}, Thread.currentThread().getContextClassLoader());
      
      // Check request wrapper
      Class wrapper = JavaUtils.loadJavaType("org.jboss.test.ws.jaxws.samples.soapbinding.jaxws.SubmitPO", classLoader);      
      wrapper.getMethod("setPurchaseOrder", PurchaseOrder.class);
      assertEquals(PurchaseOrder.class.getName(), wrapper.getMethod("getPurchaseOrder").getReturnType().getName());
      
      // Check response wrapper
      wrapper = JavaUtils.loadJavaType("org.jboss.test.ws.jaxws.samples.soapbinding.jaxws.SubmitPOResponse", classLoader);
      wrapper.getMethod("setPurchaseOrderAck", PurchaseOrderAck.class);
      assertEquals(PurchaseOrderAck.class.getName(), wrapper.getMethod("getPurchaseOrderAck").getReturnType().getName());
   }
   
   public void testSource() throws Exception
   {
      WSContractProvider gen = getGenerator();
      File outputDir = new File("tools/wsprovide/source/out");
      gen.setOutputDirectory(outputDir);
      gen.setGenerateSource(true);
      gen.provide(DocWrappedServiceImpl.class);
      
      checkWrapperClasses(outputDir);
      checkWrapperSource(outputDir, true);
   }
   
   public void testSourceDir() throws Exception
   {
      WSContractProvider gen = getGenerator();
      File outputDir = new File("tools/wsprovide/sourcedir/out");
      File sourceDir = new File("tools/wsprovide/sourcedir/source");
      
      gen.setOutputDirectory(outputDir);
      gen.setSourceDirectory(sourceDir);
      gen.setGenerateSource(true);
      gen.provide(DocWrappedServiceImpl.class);
      
      checkWrapperClasses(outputDir);
      checkWrapperSource(outputDir, false);
      checkWrapperSource(sourceDir, true);
   }
   
   public void testWsdl() throws Exception
   {
      WSContractProvider gen = getGenerator();
      File outputDir = new File("tools/wsprovide/wsdl/out");
      gen.setOutputDirectory(outputDir);
      gen.setGenerateWsdl(true);
      gen.provide(DocWrappedServiceImpl.class);
      
      checkWrapperClasses(outputDir);
   
      // There should be no source code
      checkWrapperSource(outputDir, false);
      
      File wsdlFile = new File(outputDir, "DocWrappedService.wsdl");
      WSDLDefinitionsFactory wsdlFactory = WSDLDefinitionsFactory.newInstance();
      wsdlFactory.parse(wsdlFile.toURL());
   }
   
   public void testResourceDir() throws Exception
   {
      WSContractProvider gen = getGenerator();
      File outputDir = new File("tools/wsprovide/resourcedir/out");
      File wsdlDir = new File("tools/wsprovide/resourcedir/wsdl");
      gen.setOutputDirectory(outputDir);
      gen.setResourceDirectory(wsdlDir);
      gen.setGenerateWsdl(true);
      gen.provide(DocWrappedServiceImpl.class);
      
      checkWrapperClasses(outputDir);
   
      // There should be no source code
      checkWrapperSource(outputDir, false);
      
      String wsdlName = "DocWrappedService.wsdl";
      File wsdlFile = new File(outputDir, wsdlName);
      assertFalse(wsdlFile.exists());
      
      wsdlFile = new File(wsdlDir, wsdlName);
      WSDLDefinitionsFactory wsdlFactory = WSDLDefinitionsFactory.newInstance();
      wsdlFactory.parse(wsdlFile.toURL());
   }
}