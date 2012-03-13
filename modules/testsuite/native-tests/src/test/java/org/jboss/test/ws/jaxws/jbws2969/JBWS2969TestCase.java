/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws2969;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;



/**
 * A JBWS2969TestCase.
 * 
 * @author <a href="ema@redhat.com">Jim Ma</a>
 */
public class JBWS2969TestCase extends JBossWSTest
{
   
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-jbws2969/";

   public static Test suite() throws Exception
   {
      return new JBossWSTestSetup(JBWS2969TestCase.class, "jaxws-jbws2969.war");
   }

   public void testRpcLitPassNull() throws Exception
   {
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
      QName serviceName = new QName("http://ws.jboss.org", "EndpointService");

      Service service = Service.create(wsdlURL, serviceName);
      Endpoint port = service.getPort(Endpoint.class);
      
      // this should be successful
      assertEquals("helloworld",port.testPassNull("hello"));
      //Throw WebServiceException 
      try
      {
         port.testPassNull(null);
         fail("WebServiceException is expected when the rpc lit pass in prameter is null");
      }
      catch (WebServiceException e)
      { 
         //do nothing
      }
      
      try
      {
         port.testReturnNull("hello");
         fail("WebServiceException is expected when the rpc lit return value is null");
      }
      catch (WebServiceException e)
      { 
         //do nothing
      }
      
      //should be successful
      Holder<String> arg1 = new Holder<String>();
      arg1.value = "arg1";
      port.testInHolderNull("arg0", arg1, new Holder<String>(), new Holder<String>());
      
      
      //Throw WebServiceException, inout holder pass in parameter is null 
      try
      {
         port.testInHolderNull("arg0", new Holder<String>() , new Holder<String>(), new Holder<String>());
         fail("WebServiceException is expected when the rpc lit pass in holder value is null");
      }
      catch (WebServiceException e)
      { 
         //do nothing
      }
      
      
      //Throw WebServiceException, inout holder response parameter is null 
      try
      {
         Holder<String> arg = new Holder<String>();
         arg.value = "arg";
         port.testInOutHolderNull("arg0", arg , new Holder<String>(), new Holder<String>());
         fail("WebServiceException is expected when the rpc lit inout holder return value is null");
      }
      catch (WebServiceException e)
      { 
         //do nothing
      }
      
      
      //Throw WebServiceException, inout holder response parameter is null 
      try
      {
         Holder<String> tmpArg = new Holder<String>();
         tmpArg.value = "arg";
         port.testOutHolderNull("arg0", tmpArg , new Holder<String>(), new Holder<String>());
         fail("WebServiceException is expected when the rpc lit out holder return value is null");
      }
      catch (WebServiceException e)
      { 
         //do nothing
      }      
   }
}
