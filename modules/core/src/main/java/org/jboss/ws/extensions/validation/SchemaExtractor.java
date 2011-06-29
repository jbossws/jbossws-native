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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.jboss.logging.Logger;
import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.common.DOMUtils;
import org.jboss.ws.common.DOMWriter;
import org.w3c.dom.Element;

/**
 * Extracts the schema from a given WSDL
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 29-Feb-2008
 */
public class SchemaExtractor
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(SchemaExtractor.class);
   // provide logging
   private static Logger log = Logger.getLogger(SchemaExtractor.class);

   private File xsdFile;
   private String path;
   
   public InputStream[] getSchemas(URL wsdlURL) throws IOException
   {
      //Get the path to the WSDL
      Pattern p = Pattern.compile("[a-zA-Z]+\\.[a-zA-Z]+$");
      Matcher m = p.matcher(wsdlURL.getFile());
      path = m.replaceFirst("");

      // parse the wsdl
      Element root = DOMUtils.parse(wsdlURL.openStream());

      // get the types element
      QName typesQName = new QName(root.getNamespaceURI(), "types");
      Element typesEl = DOMUtils.getFirstChildElement(root, typesQName);
      if (typesEl == null)
      {
         log.warn(BundleUtils.getMessage(bundle, "CANNOT_FIND_ELEMENT",  typesQName));
         return null;
      }

      // get the schema element
      QName schemaQName = new QName("http://www.w3.org/2001/XMLSchema", "schema");
      List<Element> schemaElements = DOMUtils.getChildElementsAsList(typesEl, schemaQName);
      if (schemaElements.size() == 0)
      {
         log.warn(BundleUtils.getMessage(bundle, "CANNOT_FIND_ELEMENT",  schemaQName));
         return null;
      }
      if (schemaElements.size() > 1)
      {
         log.warn(BundleUtils.getMessage(bundle, "MULTIPLE_SCHEMA_ELEMENTS_NOT_SUPPORTED"));
      }
      Element schemaElement = schemaElements.get(0);

      List<InputStream> streams = new ArrayList<InputStream>();

      pullImportedSchemas(schemaElement, streams);

      //Add the WSDL schema to the schema array
      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      OutputStreamWriter outwr = new OutputStreamWriter( outStream );
      DOMWriter domWriter = new DOMWriter(outwr);
      domWriter.setPrettyprint(true);
      domWriter.print(schemaElement);

      streams.add(new ByteArrayInputStream(outStream.toByteArray()));

      return streams.toArray(new InputStream[streams.size()]);
   }

   private void pullImportedSchemas(Element schemaElement, List<InputStream> streams)
   {
      QName importQName = new QName( "http://www.w3.org/2001/XMLSchema", "import" );
      List<Element> importElements = DOMUtils.getChildElementsAsList( schemaElement, importQName );

      ArrayList<String> schemaLocations = new ArrayList<String>();
      for( Element importElement : importElements )
      {
         String schemaLocation = importElement.getAttribute( "schemaLocation" );
         schemaLocations.add( schemaLocation );
      }

      ByteArrayOutputStream outStream = null;

      for( int i=0; i < schemaLocations.size(); i++ )
      {
         String schemaLocation = schemaLocations.get( i );

         try
         {
            FileInputStream in = new FileInputStream( path + schemaLocation );
            outStream = new ByteArrayOutputStream();
 
            int bt = 0;
            while(( bt = in.read() ) != -1 )
            {
               outStream.write( (byte)bt );
            }
 
            InputStream inputStream = new ByteArrayInputStream(outStream.toByteArray());
            inputStream.mark(0);
 
            Element root = DOMUtils.parse(inputStream);
            pullImportedSchemas(root, streams);
 
            inputStream.reset();
            streams.add(inputStream);
         }
         catch(IOException ioe)
         {
            log.warn(BundleUtils.getMessage(bundle, "ERROR_OBTAINING_SCHEMA",  path ));
         }
      }
   }
}
