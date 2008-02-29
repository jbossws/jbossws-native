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
package org.jboss.ws.core.utils;

// $Id$

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jboss.ws.extensions.validation.ValidationErrorHandler;
import org.jboss.wsf.common.DOMWriter;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;

/**
 * [JBWS-1172] Support schema validation for incoming messages
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 28-Feb-2008
 */
public class SchemaValidationHelper
{
   private URL xsdURL;
   private ErrorHandler errorHandler = new ValidationErrorHandler();
   
   public SchemaValidationHelper(URL xsdURL)
   {
      this.xsdURL = xsdURL;
   }

   public SchemaValidationHelper setErrorHandler(ErrorHandler errorHandler)
   {
      this.errorHandler = errorHandler;
      return this;
   }

   public void validateDocument(String inxml) throws Exception
   {
      ByteArrayInputStream bais = new ByteArrayInputStream(inxml.getBytes());
      validateDocument(bais);
   }

   public void validateDocument(Element inxml) throws Exception
   {
      String xmlStr = DOMWriter.printNode(inxml, false);
      validateDocument(xmlStr);
   }
   
   public void validateDocument(InputStream inxml) throws Exception
   {
      DocumentBuilder builder = getDocumentBuilder();
      builder.parse(inxml);
   }
   
   private DocumentBuilder getDocumentBuilder() throws ParserConfigurationException
   {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(true);
      factory.setNamespaceAware(true);
      factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
      factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", xsdURL.toExternalForm());
      DocumentBuilder builder = factory.newDocumentBuilder();
      builder.setErrorHandler(errorHandler);
      return builder;
   }
}
