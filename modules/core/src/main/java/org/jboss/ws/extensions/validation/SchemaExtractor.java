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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jboss.logging.Logger;
import org.jboss.wsf.common.DOMUtils;
import org.jboss.wsf.common.DOMWriter;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Extracts the schema from a given WSDL
 * 
 * @author Thomas.Diesler@jboss.com
 * @author ema@redhat.com
 * @since 29-Feb-2008
 */
public class SchemaExtractor
{
   // provide logging
   private static Logger log = Logger.getLogger(SchemaExtractor.class);
   private static Transformer transformer = null;
   private String path; 
   static {
      try
      {
         transformer = TransformerFactory.newInstance().newTransformer();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }

   }
   public Map<String, byte[]> getSchemas(URL wsdlURL) throws IOException
   {
      Map<String, byte[]> streams = new HashMap<String, byte[]>();
      //Get the path to the WSDL
      String wsdlFile = wsdlURL.getFile();
      int lastSlash = wsdlFile.lastIndexOf(File.separator);
      path = wsdlFile.substring(0, lastSlash+1);

      // parse the wsdl
      Element root = DOMUtils.parse(wsdlURL.openStream());

      List<Attr> nsAttrs = getNamespaceAttrs(root);

      // get the types element
      QName typesQName = new QName(root.getNamespaceURI(), "types");
      Element typesEl = DOMUtils.getFirstChildElement(root, typesQName);
      if (typesEl == null)
      {
         log.warn("Cannot find element: " + typesQName);
         return null;
      }

      // get the schema element
      QName schemaQName = new QName("http://www.w3.org/2001/XMLSchema", "schema");
      List<Element> schemaElements = DOMUtils.getChildElementsAsList(typesEl, schemaQName);
      if (schemaElements.size() == 0)
      {
         log.warn("Cannot find element: " + schemaQName);
         return null;
      }

      for (Element schemaElement : schemaElements)
      {

         DOMSource domSource = new DOMSource(schemaElement);
         ByteArrayOutputStream bout = new ByteArrayOutputStream();
         StreamResult result = new StreamResult(bout);
         Element newSchemeElement = null;
         try
         {
            transformer.transform(domSource, result);
            String content = new String(bout.toByteArray());
            newSchemeElement = DOMUtils.parse(content);
         }
         catch (Exception e)
         {
            log.error("Failed to parse schema with schemaElement with targetNamepace : "
                  + schemaElement.getAttribute("targetNamespace"));
         }


         
         for (Attr nsAttr : nsAttrs)
         {  
            
            Attr newAttr = newSchemeElement.getOwnerDocument().createAttribute(nsAttr.getName());
            newAttr.setNodeValue(nsAttr.getValue());
            if (newSchemeElement.getAttribute(nsAttr.getName()).equals("")) {
               newSchemeElement.setAttributeNodeNS(newAttr);
            }
         }

         pullImportedSchemas(newSchemeElement, streams);

         // Add the WSDL schema to the schema array
         ByteArrayOutputStream outStream = new ByteArrayOutputStream();
         OutputStreamWriter outwr = new OutputStreamWriter(outStream);
         DOMWriter domWriter = new DOMWriter(outwr);
         domWriter.setPrettyprint(true);
         domWriter.print(newSchemeElement);
         String tns = newSchemeElement.getAttribute("targetNamespace");
         streams.put(tns, outStream.toByteArray());
      }

      return streams;
   }

   private List<Attr> getNamespaceAttrs(Element element)
   {
      List<Attr> nsAttrs = new ArrayList<Attr>();

      NamedNodeMap nodes = element.getAttributes();

      for(int i=0; i < nodes.getLength(); i++)
      {
         Node node = nodes.item(i);
         Attr attr = (Attr)node;
         if(attr.getName().startsWith("xmlns:"))
            nsAttrs.add((Attr)attr.cloneNode(true));
      }

      return nsAttrs;
   }

   private void pullImportedSchemas(Element schemaElement, Map<String, byte[]> streams)
   {
      QName importQName = new QName( "http://www.w3.org/2001/XMLSchema", "import" );
      List<Element> importElements = DOMUtils.getChildElementsAsList( schemaElement, importQName );

      ArrayList<String> schemaLocations = new ArrayList<String>();
      for( Element importElement : importElements )
      {
         String schemaLocation = importElement.getAttribute( "schemaLocation" );
         if (!"".equals(schemaLocation))
         {
            schemaLocations.add(schemaLocation);
         }
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
            String tns = root.getAttribute("targetNamespace"); 
            streams.put(tns , outStream.toByteArray());
         }
         catch(IOException ioe)
         {
            log.warn("Error obtaining schema: " + path + schemaLocation);
         }
      }
   }
}
