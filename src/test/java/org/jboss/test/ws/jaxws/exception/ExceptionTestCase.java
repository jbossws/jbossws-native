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
package org.jboss.test.ws.jaxws.exception;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPFaultException;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.w3c.dom.Element;

/**
 * Test JAX-WS exception handling
 *
 * @author <a href="jason.greene@jboss.com">Jason T. Greene</a>
 * @version $Revision:1370 $
 */
public class ExceptionTestCase extends JBossWSTest
{
   private String targetNS = "http://exception.jaxws.ws.test.jboss.org/";
   private ExceptionEndpoint proxy;

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(ExceptionTestCase.class, "jaxws-exception.war");
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      QName serviceName = new QName(targetNS, "ExceptionEndpointImplService");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-exception/ExceptionEndpointService?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      proxy = (ExceptionEndpoint)service.getPort(ExceptionEndpoint.class);
   }

   /*
    * 10.2.2.3
    *
    * faultcode (Subcode in SOAP 1.2, Code set to env:Receiver)
    *    1. SOAPFaultException.getFault().getFaultCodeAsQName()
    *    2. env:Server (Subcode omitted for SOAP 1.2).
    * faultstring (Reason/Text)
    *    1. SOAPFaultException.getFault().getFaultString()
    *    2. Exception.getMessage()
    *    3. Exception.toString()
    * faultactor (Role in SOAP 1.2)
    *    1. SOAPFaultException.getFault().getFaultActor()
    *    2. Empty
    * detail (Detail in SOAP 1.2)
    *    1. Serialized service specific exception (see WrapperException.getFaultInfo() in section 2.5)
    *    2. SOAPFaultException.getFault().getDetail()
    */
   public void testRuntimeException() throws Exception
   {
      try
      {
         proxy.throwRuntimeException();
         fail("Expected SOAPFaultException");
      }
      catch (SOAPFaultException e)
      {
         assertEquals("oh no, a runtime exception occured.", e.getMessage());
         assertEquals("oh no, a runtime exception occured.", e.getFault().getFaultString());
      }
   }

   public void testSoapFaultException() throws Exception
   {
      try
      {
         proxy.throwSoapFaultException();
         fail("Expected SOAPFaultException");
      }
      catch (SOAPFaultException e)
      {
         assertEquals("this is a fault string!", e.getMessage());
         assertEquals("this is a fault string!", e.getFault().getFaultString());
         assertEquals("mr.actor", e.getFault().getFaultActor());
         assertEquals("FooCode", e.getFault().getFaultCodeAsName().getLocalName());
         assertEquals("http://foo", e.getFault().getFaultCodeAsName().getURI());
         assertEquals("test", ((Element)e.getFault().getDetail().getChildElements().next()).getLocalName());
      }
   }

   public void testApplicationException() throws Exception
   {
      try
      {
         proxy.throwApplicationException();
         fail("Expected UserException");
      }
      catch (UserException e)
      {
         assertEquals("Some validation error", e.getMessage());
         assertEquals("validation", e.getErrorCategory());
         assertEquals(123, e.getErrorCode());
      }
   }
}