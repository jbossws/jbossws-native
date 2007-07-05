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
package org.jboss.test.ws.jaxws.jbws771;

// $Id: JBWS771TestCase.java 3729 2007-06-26 19:38:00Z thomas.diesler@jboss.com $

import java.net.URL;
import java.util.List;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Message;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.spi.test.JBossWSTest;
import org.jboss.wsf.spi.test.JBossWSTestSetup;
import org.jboss.wsf.spi.tools.cmd.WSConsume;

/**
 * [JBWS-771] Use part names that are friendly to .NET
 * 
 * @author Thomas.Diesler@jboss.com 
 * @since 04-Jul-2007
 */
public class JBWS771TestCase extends JBossWSTest
{
   private static final String TARGET_NAMESPACE = "http://jbws771.jaxws.ws.test.jboss.org/";
   private static URL wsdlURL;
   private static IWebsvc port;

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS771TestCase.class, "jaxws-jbws771.jar");
   }

   @Override
   protected void setUp() throws Exception
   {
      if (port == null)
      {
         QName serviceName = new QName(TARGET_NAMESPACE, "JBWS771Service");
         wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws771/IWebsvcImpl?wsdl");

         Service service = Service.create(wsdlURL, serviceName);
         port = service.getPort(IWebsvc.class);
      }
   }

   public void testSubmit() throws Exception
   {
      String result = port.submit("foo");
      assertEquals("submit-foo", result);
   }
   
   public void testCancel() throws Exception
   {
      String result = port.cancel("foo", "bar");
      assertEquals("cancel-foobar", result);
   }
   
   public void testMessagePartNames() throws Exception
   {
      Definition wsdl = getWSDLDefinition(wsdlURL.toExternalForm());
      
      Message wsdlReqMessage = wsdl.getMessage(new QName(TARGET_NAMESPACE, "IWebsvc_cancel"));
      assertNotNull("Expected part with name 'parameters' in: " + wsdlReqMessage, wsdlReqMessage.getPart("parameters"));
      assertNotNull("Expected part with name 'bar' in: " + wsdlReqMessage, wsdlReqMessage.getPart("bar"));
      
      Message wsdlResMessage = wsdl.getMessage(new QName(TARGET_NAMESPACE, "IWebsvc_cancelResponse"));
      assertNotNull("Expected part with name 'parameters' in: " + wsdlResMessage, wsdlResMessage.getPart("parameters"));
      
      /*
       <binding name='IWebsvcBinding' type='tns:IWebsvc'>
        <soap:binding style='document' transport='http://schemas.xmlsoap.org/soap/http'/>
        <operation name='cancel'>
         <soap:operation soapAction=''/>
         <input>
          <soap:body parts='parameters' use='literal'/>
          <soap:header message='tns:IWebsvc_cancel' part='bar' use='literal'></soap:header>
         </input>
         <output>
          <soap:body use='literal'/>
         </output>
        </operation>
       */
      Binding wsdlBinding = wsdl.getBinding(new QName(TARGET_NAMESPACE, "IWebsvcBinding"));
      BindingOperation bindingOperation = wsdlBinding.getBindingOperation("cancel", null, null);
      
      boolean foundBody = false;
      boolean foundHeader = false;
      List<ExtensibilityElement> extList = bindingOperation.getBindingInput().getExtensibilityElements();
      for (ExtensibilityElement extElement : extList)
      {
         if (extElement instanceof SOAPBody)
         {
            SOAPBody body = (SOAPBody)extElement;
            assertEquals("parameters", body.getParts().get(0));
            foundBody = true;
         }
         if (extElement instanceof SOAPHeader)
         {
            SOAPHeader header = (SOAPHeader)extElement;
            assertEquals("bar", header.getPart());
            foundHeader = true;
         }
      }
      assertTrue("Found soap:body", foundBody);
      assertTrue("Found soap:header", foundHeader);
   }
   
   public void testWSConsume() throws Exception
   {
      System.out.println("[JBWS-1724] wsconsume cannot use part names that are friendly to .NET");
      //WSConsume.main(new String[]{"--output=tests/wsconsume", wsdlURL.toExternalForm()});
   }
   
   private Definition getWSDLDefinition(String wsdlLocation) throws Exception
   {
      WSDLFactory wsdlFactory = WSDLFactory.newInstance();
      WSDLReader wsdlReader = wsdlFactory.newWSDLReader();

      Definition definition = wsdlReader.readWSDL(null, wsdlLocation);
      return definition;
   }
}
