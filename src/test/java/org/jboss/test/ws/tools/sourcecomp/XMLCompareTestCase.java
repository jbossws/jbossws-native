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
package org.jboss.test.ws.tools.sourcecomp;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.jboss.wsf.test.JBossWSTest;

/**
 *  Test case that uses XMLUnit to compare two xml files
 *  @author <mailto:Anil.Saldhana@jboss.org>Anil Saldhana
 *  @since  Apr 6, 2005
 */

public class XMLCompareTestCase extends JBossWSTest
{
   public void testXMLEquivalence() throws Exception
   {
      String str = constructTestString(true);
      String str1 = constructTestString(false);
      XMLUnit.setIgnoreWhitespace(true);
      XMLAssert.assertXMLEqual(str, str);
      XMLAssert.assertXMLNotEqual(str, str1);
   }

   private String constructTestString(boolean addDummy)
   {
      StringBuffer buf = new StringBuffer();
      buf.append("<schema targetNamespace='http://org.jboss.ws/types'");
      buf.append(" xmlns='http://www.w3.org/2001/XMLSchema' ");
      buf.append("  xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' ");
      buf.append(" xmlns:xsd='http://www.w3.org/2001/XMLSchema' ");
      buf.append(" xmlns:tns='http://org.jboss.ws/types'>");
      buf.append("<complexType name='Base'>");
      buf.append("  <sequence>");
      buf.append("   <element name='a' type='xsd:int'/>");
      if (addDummy)
         buf.append("   <element name='b' type='xsd:int'/>");
      buf.append("  </sequence>");
      buf.append(" </complexType>");
      buf.append(" <complexType name='SomeException'>");
      buf.append("   <sequence>");
      buf.append("    <element name='name' type='xsd:string' nillable='true'/>");
      buf.append("   </sequence>");
      buf.append(" </complexType>");
      buf.append(" </schema>");

      return buf.toString();
   }
}