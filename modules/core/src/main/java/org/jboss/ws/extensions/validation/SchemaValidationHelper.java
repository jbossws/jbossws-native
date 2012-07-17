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
package org.jboss.ws.extensions.validation;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * [JBWS-1172] Support schema validation for incoming messages
 * 
 * @author Thomas.Diesler@jboss.com
 * @author ema@redhat.com
 * @since 28-Feb-2008
 */
public class SchemaValidationHelper {

   private ErrorHandler errorHandler = new StrictlyValidErrorHandler();

   private static SchemaFactory factory = null;

   private Validator validator = null;

   static
   {
      factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
   }

   public SchemaValidationHelper(URL xsdURL) throws SAXException
   {
      Schema schema = factory.newSchema(new File(xsdURL.getFile()));
      validator = schema.newValidator();
      validator.setErrorHandler(errorHandler);
   }

   public SchemaValidationHelper(Map<String, byte[]> xsdStreams) throws SAXException
   {
      SchemaResourceResolver resolver = new SchemaResourceResolver(xsdStreams);
      factory.setResourceResolver(resolver);

      List<Source> schemas = new ArrayList<Source>();
      for (byte[] ins : xsdStreams.values())
      {
         StreamSource source = new StreamSource(new ByteArrayInputStream(ins));
         schemas.add(source);
      }
      Source[] sources = schemas.toArray(new Source[0]);
      
      Schema schema = factory.newSchema(sources);
      validator = schema.newValidator();
      validator.setErrorHandler(errorHandler);

   }

   public SchemaValidationHelper setErrorHandler(ErrorHandler errorHandler)
   {
      validator.setErrorHandler(errorHandler);
      return this;
   }

   public void validateDocument(String inxml) throws Exception
   {
      StreamSource source = new StreamSource(new java.io.ByteArrayInputStream(inxml.getBytes()));
      validateDocument(source);

   }

   public void validateDocument(Source xml) throws Exception
   {
      validator.validate(xml);
   }

   public void validateDocument(Element inxml) throws Exception
   {
      DOMSource domSource = new DOMSource(inxml);
      validator.validate(domSource);
   }

   public void validateDocument(InputStream inxml) throws Exception
   {
      StreamSource source = new StreamSource(inxml);
      validateDocument(source);
   }

   public void validateDocument(InputSource inxml) throws Exception
   {
      StreamSource source = new StreamSource(inxml.getByteStream());
      validateDocument(source);
   }

}
