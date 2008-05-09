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
package org.jboss.test.ws.jaxws.jbws1809;

import junit.framework.Test;
import org.jboss.wsf.common.DOMUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;
import org.jboss.ws.tools.wsdl.WSDLDefinitionsFactory;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.metadata.wsdl.WSDLTypes;
import org.jboss.ws.metadata.wsdl.DOMTypes;
import org.jboss.ws.metadata.wsdl.XSModelTypes;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Iterator;

/**
 * Test the JAXBIntroduction features.
 *
 * Check if the WSDL is generated correctly.
 * The introduction should turn a property into a xsd:attribute declaration.
 *
 * @author heiko.braun@jboss.com
 */
public class JBWS1809TestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS1809TestCase.class, "jaxws-jbws1809.jar");
   }

   public void testWSDLAccess() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws1809/EndpointImpl?wsdl");
      WSDLDefinitionsFactory factory = WSDLDefinitionsFactory.newInstance();
      WSDLDefinitions wsdlDefinitions = factory.parse(wsdlURL);
      assertNotNull("Unable to read WSDL definitions", wsdlDefinitions);

      XSModelTypes wsdlTypes = (XSModelTypes)wsdlDefinitions.getWsdlTypes();

      // convert XSModelTypes to dom representation
      // it's easier to consume...

      Element types = DOMUtils.parse(wsdlTypes.getSchemaModel().serialize());            
      Iterator it = DOMUtils.getChildElements(types, "complexType");

      boolean foundAttributeDeclaration = false;
      while(it.hasNext())
      {
         Element next = (Element)it.next();
         if(DOMUtils.getAttributeValue(next, "name").equals("docRequest"))
         {
            Iterator it2 = DOMUtils.getChildElements(next, "attribute");

            while(it2.hasNext())
            {
               Element next2 = (Element)it2.next();
               if(DOMUtils.getAttributeValue(next2, "name").equals("value"))
               {
                  foundAttributeDeclaration = true;
               }
            }
         }
      }

      assertTrue("JAXBIntros should turn the 'docRequest.name' property into a XML attribute", foundAttributeDeclaration);

   }
}
