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
package org.jboss.test.ws.jaxws.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.codehaus.jettison.badgerfish.BadgerFishXMLInputFactory;
import org.codehaus.jettison.badgerfish.BadgerFishXMLOutputFactory;
import org.codehaus.jettison.json.JSONTokener;
import org.jboss.wsf.test.JBossWSTest;

/**
 * Test Json functionality
 *
 * @author Thomas.Diesler@jboss.com
 * @since 12-Mar-2008
 */
public class JsonAPITestCase extends JBossWSTest
{
   public void testSimple() throws Exception
   {
      String xmlStr = "<root>hello world</root>";
      String expStr = "{\"root\":{\"$\":\"hello world\"}}";
      String resStr = toJSON(xmlStr);
      assertEquals("Unexpected result: " + resStr, expStr, resStr);
      
      String resXML = toXML(resStr);
      assertEquals("Unexpected result: " + resXML, xmlStr, resXML);
   }

   public void testSimpleAttribute() throws Exception
   {
      String xmlStr = "<root myat=\"value\">hello world</root>";
      String expStr = "{\"root\":{\"@myat\":\"value\",\"$\":\"hello world\"}}";
      String resStr = toJSON(xmlStr);
      assertEquals("Unexpected result: " + resStr, expStr, resStr);
      
      String resXML = toXML(resStr);
      assertEquals("Unexpected result: " + resXML, xmlStr, resXML);
   }

   public void _testDefaultNamespace() throws Exception
   {
      String xmlStr = "<root xmlns=\"http://somns\">hello world</root>";
      String expStr = "{\"root\":{\"$\":\"hello world\"}}";
      String resStr = toJSON(xmlStr);
      assertEquals("Unexpected result: " + resStr, expStr, resStr);
      
      String resXML = toXML(resStr);
      assertEquals("Unexpected result: " + resXML, xmlStr, resXML);
   }

   private String toJSON(String srcXML) throws FactoryConfigurationError, XMLStreamException
   {
      ByteArrayInputStream bais = new ByteArrayInputStream(srcXML.getBytes());

      XMLInputFactory readerFactory = XMLInputFactory.newInstance();
      XMLStreamReader streamReader = readerFactory.createXMLStreamReader(bais);
      XMLEventReader eventReader = readerFactory.createXMLEventReader(streamReader);

      StringWriter strWriter = new StringWriter();
      BadgerFishXMLOutputFactory writerFactory = new BadgerFishXMLOutputFactory();
      XMLStreamWriter streamWriter = writerFactory.createXMLStreamWriter(strWriter);

      // UnsupportedOperationException in jettison-1.0-RC2
      //XMLEventWriter eventWriter = writerFactory.createXMLEventWriter(strWriter);

      XMLEventWriter eventWriter = new BadgerFishXMLEventWriter(streamWriter);
      eventWriter.add(eventReader);
      eventWriter.close();

      String jsonStr = strWriter.toString();
      return jsonStr;
   }

   private String toXML(String jsonStr) throws XMLStreamException, FactoryConfigurationError
   {
      BadgerFishXMLInputFactory inputFactory = new BadgerFishXMLInputFactory();
      XMLStreamReader streamReader = inputFactory.createXMLStreamReader(new JSONTokener(jsonStr));
      XMLInputFactory readerFactory = XMLInputFactory.newInstance();
      XMLEventReader eventReader = readerFactory.createXMLEventReader(streamReader);
      
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
      XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(baos);
      eventWriter.add(eventReader);
      eventWriter.close();
      
      String resXML = new String(baos.toByteArray());
      if (resXML.startsWith("<?xml "))
         resXML = resXML.substring(resXML.indexOf("?>") + 2);
      
      return resXML;
   }
}
