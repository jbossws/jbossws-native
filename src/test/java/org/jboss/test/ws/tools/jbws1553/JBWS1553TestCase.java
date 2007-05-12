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
package org.jboss.test.ws.tools.jbws1553;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;

import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.metadata.wsdl.WSDLEndpoint;
import org.jboss.ws.metadata.wsdl.WSDLInterface;
import org.jboss.ws.metadata.wsdl.WSDLService;
import org.jboss.ws.tools.wsdl.WSDLDefinitionsFactory;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:alex.guizar@jboss.com">Alejandro Guizar</a>
 * @version $Revision$
 */
public class JBWS1553TestCase extends TestCase
{
   private WSDLDefinitions definitions;
   private static WSDLDefinitionsFactory definitionsFactory = WSDLDefinitionsFactory.newInstance();

   protected void setUp() throws Exception
   {
      URL wsdlLocation = new File("resources/tools/jbws1553/atm-service.wsdl").toURL();
      definitions = definitionsFactory.parse(wsdlLocation);
   }

   public void testPortType()
   {
      WSDLService atmService = definitions.getService("AtmFrontEndService");
      WSDLEndpoint endpoint = atmService.getEndpoint(new QName("urn:samples:atm", "FrontEndPort"));
      WSDLInterface _interface = endpoint.getInterface();
      assertEquals(new QName("urn:samples:frontend", "FrontEnd"), _interface.getName());
   }
}
