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
package org.jboss.test.ws.common.wsdl11;

import java.net.URL;

import javax.xml.namespace.QName;

import org.jboss.ws.common.Constants;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.metadata.wsdl.WSDLInterface;
import org.jboss.ws.metadata.wsdl.WSDLInterfaceOperation;
import org.jboss.ws.metadata.wsdl.WSDLInterfaceOperationInput;
import org.jboss.ws.metadata.wsdl.WSDLInterfaceOperationOutput;
import org.jboss.ws.metadata.wsdl.WSDLRPCPart;
import org.jboss.ws.metadata.wsdl.WSDLTypes;
import org.jboss.ws.metadata.wsdl.WSDLUtils;
import org.jboss.ws.tools.wsdl.WSDLDefinitionsFactory;
import org.jboss.wsf.test.JBossWSTest;

/**
 * Test the unmarshalling of wsdl-1.1 into the unified wsdl structure
 *
 * @author Thomas.Diesler@jboss.org
 * @since 02-Jun-2005
 */
public class WSDL11TestCase extends JBossWSTest
{
   private static final String TARGET_NAMESPACE = "http://org.jboss.ws/jaxrpc/types";

   public void testDocLitSimple() throws Exception
   {
      URL wsdlFile = getResourceURL("common/wsdl11/DocLitSimple.wsdl");

      WSDLDefinitionsFactory factory = WSDLDefinitionsFactory.newInstance();
      WSDLDefinitions wsdlDefinitions = factory.parse(wsdlFile);
      WSDLInterface wsdlInterface = wsdlDefinitions.getInterface(new QName(wsdlDefinitions.getTargetNamespace(), "JaxRpcTestService"));

      // check if the schema has been extracted
      WSDLTypes wsdlTypes = wsdlDefinitions.getWsdlTypes();
      assertNotNull(WSDLUtils.getSchemaModel(wsdlTypes));

      // check the echoString operation
      WSDLInterfaceOperation wsdlOperation = wsdlInterface.getOperation("echoString");
      assertEquals(Constants.URI_STYLE_DOCUMENT, wsdlOperation.getStyle());

      WSDLInterfaceOperationInput wsdlInput = wsdlOperation.getInput(new QName(TARGET_NAMESPACE, "echoString"));
      assertEquals(new QName(TARGET_NAMESPACE, "echoString"), wsdlInput.getXMLType());
      WSDLInterfaceOperationOutput wsdlOutput = wsdlOperation.getOutput(new QName(TARGET_NAMESPACE, "echoStringResponse"));
      assertEquals(new QName(TARGET_NAMESPACE, "echoStringResponse"), wsdlOutput.getXMLType());

      // check the echoSimpleUserType operation
      wsdlOperation = wsdlInterface.getOperation("echoSimpleUserType");
      assertEquals(Constants.URI_STYLE_DOCUMENT, wsdlOperation.getStyle());

      wsdlInput = wsdlOperation.getInput(new QName(TARGET_NAMESPACE, "echoSimpleUserType"));
      assertEquals(new QName(TARGET_NAMESPACE, "echoSimpleUserType"), wsdlInput.getXMLType());
      wsdlOutput = wsdlOperation.getOutput(new QName(TARGET_NAMESPACE, "echoSimpleUserTypeResponse"));
      assertEquals(new QName(TARGET_NAMESPACE, "echoSimpleUserTypeResponse"), wsdlOutput.getXMLType());

      QName xmlName = new QName(TARGET_NAMESPACE, "echoString");
      QName xmlType = new QName(TARGET_NAMESPACE, "echoString");
      assertEquals(xmlType, wsdlTypes.getXMLType(xmlName));
      xmlName = new QName(TARGET_NAMESPACE, "echoStringResponse");
      xmlType = new QName(TARGET_NAMESPACE, "echoStringResponse");
      assertEquals(xmlType, wsdlTypes.getXMLType(xmlName));

      xmlName = new QName(TARGET_NAMESPACE, "echoSimpleUserType");
      xmlType = new QName(TARGET_NAMESPACE, "echoSimpleUserType");
      assertEquals(xmlType, wsdlTypes.getXMLType(xmlName));
      xmlName = new QName(TARGET_NAMESPACE, "echoSimpleUserTypeResponse");
      xmlType = new QName(TARGET_NAMESPACE, "echoSimpleUserTypeResponse");
      assertEquals(xmlType, wsdlTypes.getXMLType(xmlName));
   }

   public void testRpcLitSimple() throws Exception
   {
      verifyRPC(getResourceURL("common/wsdl11/RpcLitSimple.wsdl"));
   }

   public void testRpcLitImport() throws Exception
   {
      verifyRPC(getResourceURL("common/wsdl11/RpcLitImport.wsdl"));
   }

   private void verifyRPC(URL wsdlFile) throws Exception
   {
      WSDLDefinitionsFactory factory = WSDLDefinitionsFactory.newInstance();
      WSDLDefinitions wsdlDefinitions = factory.parse(wsdlFile);
      WSDLInterface wsdlInterface = wsdlDefinitions.getInterface(new QName(wsdlDefinitions.getTargetNamespace(), "JaxRpcTestService"));

      // check if the schema has been extracted
      WSDLTypes wsdlTypes = wsdlDefinitions.getWsdlTypes();
      assertNotNull(WSDLUtils.getSchemaModel(wsdlTypes));

      // check the echoString operation
      WSDLInterfaceOperation wsdlOperation = wsdlInterface.getOperation("echoString");
      assertEquals(Constants.URI_STYLE_RPC, wsdlOperation.getStyle());

      WSDLInterfaceOperationInput wsdlInput = wsdlOperation.getInputs()[0];
      WSDLRPCPart childPart = wsdlInput.getChildPart("String_1");
      assertEquals(Constants.TYPE_LITERAL_STRING, childPart.getType());
      childPart = wsdlInput.getChildPart("String_2");
      assertEquals(Constants.TYPE_LITERAL_STRING, childPart.getType());
      WSDLInterfaceOperationOutput wsdlOutput = wsdlOperation.getOutputs()[0];
      childPart = wsdlOutput.getChildPart("result");
      assertEquals(Constants.TYPE_LITERAL_STRING, childPart.getType());

      // check the echoSimpleUserType operation
      wsdlOperation = wsdlInterface.getOperation("echoSimpleUserType");
      assertEquals(Constants.URI_STYLE_RPC, wsdlOperation.getStyle());

      wsdlInput = wsdlOperation.getInputs()[0];
      childPart = wsdlInput.getChildPart("String_1");
      assertEquals(Constants.TYPE_LITERAL_STRING, childPart.getType());
      childPart = wsdlInput.getChildPart("SimpleUserType_2");
      assertEquals(new QName(TARGET_NAMESPACE, "SimpleUserType"), childPart.getType());
      wsdlOutput = wsdlOperation.getOutputs()[0];
      childPart = wsdlOutput.getChildPart("result");
      assertEquals(new QName(TARGET_NAMESPACE, "SimpleUserType"), childPart.getType());
   }

   public void testSwaMessages() throws Exception
   {
      URL wsdlFile = getResourceURL("common/wsdl11/SwaTestService.wsdl");

      WSDLDefinitionsFactory factory = WSDLDefinitionsFactory.newInstance();
      WSDLDefinitions wsdlDefinitions = factory.parse(wsdlFile);
      assertNotNull(wsdlDefinitions); // should throw an Exception when SWA parts are not skipped
   }
   
}
