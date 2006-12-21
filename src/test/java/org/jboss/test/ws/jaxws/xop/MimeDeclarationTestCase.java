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
package org.jboss.test.ws.jaxws.xop;

import junit.framework.TestCase;

import javax.xml.bind.annotation.XmlMimeType;
import java.awt.*;
import java.lang.reflect.Method;

import org.jboss.ws.extensions.xop.jaxws.ReflectiveXOPScanner;

/**
 * Test the ReflectiveXOPScanner.
 * 
 * @author Heiko Braun <heiko.braun@jboss.com>
 * @version $Id$
 * @since 04.12.2006
 */
public class MimeDeclarationTestCase extends TestCase {

   static ReflectiveXOPScanner SCANNER = new ReflectiveXOPScanner();


   protected void setUp() throws Exception
   {
      SCANNER.reset();
   }

   public void testFieldAnnotation() throws Exception
   {      
      String mimeType = SCANNER.scan(FieldAnnotation.class);
      assertNotNull("Unable to find xop declaration", mimeType);
      assertEquals("text/xml", mimeType);
   }

   public void testMethodAnnotation() throws Exception
   {
      String mimeType = SCANNER.scan(MethodAnnotation.class);
      assertNotNull("Unable to find xop declaration", mimeType);
   }

   public void testAnnotationMissing() throws Exception
   {
      String mimeType = SCANNER.scan(NoAnnotation.class);
      assertNull("There should be no mimeType available", mimeType);
   }

   public void testAnnotatedParameter() throws Exception
   {

      if(true)
      {
         System.out.println("FIXME: XmlMimeType on SEI parameter declarations");
         return;
      }
      
      Method m = AnnotatedSEI.class.getMethod("foo", new Class[] {byte[].class});
      assertNotNull(m);

      System.out.println(m.getParameterAnnotations().length);
      
      String mimeType = SCANNER.scan( m.getParameterTypes()[0]);
      assertNotNull("Unable to find xop declaration", mimeType);
      assertEquals("text/xml", mimeType);
   }

   public void testSimpleRecursion() throws Exception
   {
      String mimeType = SCANNER.scan(SimpleRecursion.class);
      assertNull(mimeType);
   }

   public void testComplexRecursion() throws Exception
   {
      String mimeType = SCANNER.scan(ComplexRecursion.class);
      assertNotNull("Unable to find xop declaration", mimeType);
      assertEquals("text/plain", mimeType);
   }

   class FieldAnnotation
   {
      @XmlMimeType("text/xml")
      public byte[] data;


      @XmlMimeType("text/plain") // check field level precedence
      public byte[] getData() {
         return data;
      }
   }

   class MethodAnnotation
   {
      private Image data;

      @XmlMimeType("image/jpeg")
      public Image getData() {
         return data;
      }
   }

   class NoAnnotation
   {
      private Image data;
      
      public Image getData() {
         return data;
      }
   }

   interface AnnotatedSEI {
      void foo(@XmlMimeType("text/xml")byte[] bar);
   }

   class SimpleRecursion {
      private SimpleRecursion data;
   }

   class ComplexRecursion
   {
      String data;
      Nested nested;
   }

   class Nested
   {
      @XmlMimeType("text/plain")
      String data;
   }
}
