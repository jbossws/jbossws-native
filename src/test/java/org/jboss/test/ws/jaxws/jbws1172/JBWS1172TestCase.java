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
package org.jboss.test.ws.jaxws.jbws1172;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.Service;
import javax.xml.ws.Service21;

import junit.framework.Test;

import org.jboss.test.ws.jaxws.jbws1172.types.MyTest;
import org.jboss.ws.extensions.validation.SchemaExtractor;
import org.jboss.ws.extensions.validation.ValidationErrorHandler;
import org.jboss.ws.feature.SchemaValidationFeature;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;
import org.xml.sax.SAXException;

/**
 * [JBWS-1172] Support schema validation for incoming messages
 * 
 * http://jira.jboss.org/jira/browse/JBWS-1172
 *
 * @author Thomas.Diesler@jboss.com
 * @since 28-Feb-2008
 */
public class JBWS1172TestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS1172TestCase.class, "jaxws-jbws1172.war");
   }

   public void testSchemaValidationPositive() throws Exception
   {
      URL xsdURL = new File("resources/jaxws/jbws1172/TestService.xsd").toURL();
      String inxml = "<performTest xmlns='http://www.my-company.it/ws/my-test'><Code>1000</Code></performTest>";
      parseDocument(inxml, xsdURL);
   }

   public void testSchemaValidationNegative() throws Exception
   {
      URL xsdURL = new File("resources/jaxws/jbws1172/TestService.xsd").toURL();
      String inxml = "<performTest xmlns='http://www.my-company.it/ws/my-test'><Code>2000</Code></performTest>";
      try
      {
         parseDocument(inxml, xsdURL);
      }
      catch (SAXException ex)
      {
         String msg = ex.getMessage();
         assertTrue("Unexpectd message: " + msg, msg.indexOf("Value '2000' is not facet-valid with respect to maxInclusive '1000'") > 0);
      }
   }

   public void testSchemaExtractor() throws Exception
   {
      URL wsdlURL = new File("resources/jaxws/jbws1172/TestService.wsdl").toURL();
      URL xsdURL = new SchemaExtractor().getSchemaUrl(wsdlURL);
      String inxml = "<performTest xmlns='http://www.my-company.it/ws/my-test'><Code>1000</Code></performTest>";
      parseDocument(inxml, xsdURL);
   }

   public void testEndpointWsdlValidation() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws1172?wsdl");
      URL xsdURL = new SchemaExtractor().getSchemaUrl(wsdlURL);
      String inxml = "<performTest xmlns='http://www.my-company.it/ws/my-test'><Code>1000</Code></performTest>";
      parseDocument(inxml, xsdURL);
   }
   
   public void testNonValidatingClient() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws1172?wsdl");
      QName serviceName = new QName("http://www.my-company.it/ws/my-test", "MyTestService");
      Service service = Service.create(wsdlURL, serviceName);
      MyTest port = service.getPort(MyTest.class);
      port.performTest(new Long(1000));
   }
   
   public void testValidatingClientWithExplicitSchema() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws1172?wsdl");
      URL xsdURL = new SchemaExtractor().getSchemaUrl(wsdlURL);
      QName serviceName = new QName("http://www.my-company.it/ws/my-test", "MyTestService");
      Service21 service = Service21.create(wsdlURL, serviceName);
      MyTest port = service.getPort(MyTest.class, new SchemaValidationFeature(xsdURL.toString()));
      port.performTest(new Long(1000));
   }
   
   private void parseDocument(String inxml, URL xsdURL) throws Exception
   {
      DocumentBuilder builder = getDocumentBuilder(xsdURL);
      ByteArrayInputStream bais = new ByteArrayInputStream(inxml.getBytes());
      builder.parse(bais);
   }

   private DocumentBuilder getDocumentBuilder(URL xsdURL) throws ParserConfigurationException
   {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(true);
      factory.setNamespaceAware(true);
      factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
      factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", xsdURL.toExternalForm());
      DocumentBuilder builder = factory.newDocumentBuilder();
      builder.setErrorHandler(new ValidationErrorHandler());
      return builder;
   }
}
