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
package org.jboss.test.ws.jaxws.webfault;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.apache.xerces.xs.XSElementDeclaration;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.metadata.wsdl.WSDLUtils;
import org.jboss.ws.metadata.wsdl.xmlschema.JBossXSModel;
import org.jboss.ws.tools.wsdl.WSDLDefinitionsFactory;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test the JSR-181 annotation: javax.jws.WebFault
 *
 * @author alessio.soldano@jboss.org
 * @since 21-Feb-2008
 */
public class WebFaultTestCase extends JBossWSTest
{
   private String endpointURL = "http://" + getServerHost() + ":8080/jaxws-webfault";
   private static final String TARGET_NS = "http://webfault.jaxws.ws.test.jboss.org/";
   private static final String CUSTOM_FAULT_NS = "org.jboss.test.ws.jaxws.webfault.exceptions";

   public static Test suite()
   {
      return new JBossWSTestSetup(WebFaultTestCase.class, "jaxws-webfault.war");
   }
   
   /**
    * Tests whether the @WebFault annotation correctly sets the fault element's name and namespace
    * (the type doesn't depend on @WebFault, see [JBWS-1904] about this)
    * 
    * @throws Exception
    */
   public void testWebFaultElement() throws Exception
   {
      JBossXSModel xsModel = getSchemaModel();
      XSElementDeclaration myCustomFaultElement = xsModel.getElementDeclaration("myCustomFault", CUSTOM_FAULT_NS);
      assertNotNull(myCustomFaultElement);
      myCustomFaultElement = xsModel.getElementDeclaration("myCustomFault", TARGET_NS);
      assertNull(myCustomFaultElement);
      myCustomFaultElement = xsModel.getElementDeclaration("CustomException", CUSTOM_FAULT_NS);
      assertNull(myCustomFaultElement);
      XSElementDeclaration simpleExceptiontElement = xsModel.getElementDeclaration("SimpleException", TARGET_NS); //default to exception simple class name
      assertNotNull(simpleExceptiontElement);
   }
   
   private JBossXSModel getSchemaModel() throws Exception
   {
      URL wsdlURL = new URL(endpointURL + "?wsdl");
      WSDLDefinitionsFactory factory = WSDLDefinitionsFactory.newInstance();
      WSDLDefinitions wsdlDefinitions = factory.parse(wsdlURL);
      return WSDLUtils.getSchemaModel(wsdlDefinitions.getWsdlTypes());
   }
   
   public void testInvocation() throws Exception
   {
      URL wsdlURL = new URL(endpointURL + "?wsdl");
      QName serviceName = new QName(TARGET_NS, "TestEndpointService");

      Service service = Service.create(wsdlURL, serviceName);
      TestEndpoint port = (TestEndpoint)service.getPort(TestEndpoint.class);

      try
      {
         port.throwCustomException("Hello");
         fail("Exception expected!");
      }
      catch (CustomException e)
      {
         assertEquals(new Integer(5), e.getNumber());
      }
      catch (Exception e)
      {
         fail("Wrong exception catched!");
      }
      try
      {
         port.throwSimpleException("World");
         fail("Exception expected!");
      }
      catch (SimpleException e)
      {
         assertEquals(new Integer(5), e.getNumber());
      }
      catch (Exception e)
      {
         fail("Wrong exception catched!");
      }
   }
}