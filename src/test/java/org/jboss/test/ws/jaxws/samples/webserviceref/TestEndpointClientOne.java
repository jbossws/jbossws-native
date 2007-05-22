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
package org.jboss.test.ws.jaxws.samples.webserviceref;

import java.util.ArrayList;

import javax.naming.InitialContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceRef;
import javax.xml.ws.WebServiceRefs;

import org.jboss.logging.Logger;

// Test on type with wsdlLocation
@WebServiceRef(name = "service1", value = TestEndpointService.class, wsdlLocation = "META-INF/wsdl/TestEndpoint.wsdl")

// Test multiple on type
@WebServiceRefs( { 
   @WebServiceRef(name = "service2", value = TestEndpointService.class),
   @WebServiceRef(name = "port1", value = TestEndpointService.class, type = TestEndpoint.class) })
public class TestEndpointClientOne
{
   // Provide logging
   private static Logger log = Logger.getLogger(TestEndpointClientOne.class);

   // Test on field with name
   @WebServiceRef(name = "TestEndpointService3")
   static TestEndpointService service3;

   // Test on field without name
   @WebServiceRef
   static TestEndpointService service4;

   // Test on method with name
   @WebServiceRef(name = "TestEndpointService5")
   static void setService5(TestEndpointService service)
   {
      TestEndpointClientOne.service5 = service;
   }
   private static TestEndpointService service5;

   // Test on method without name
   @WebServiceRef
   static void setService6(TestEndpointService service)
   {
      TestEndpointClientOne.service6 = service;
   }
   private static TestEndpointService service6;

   // Test on field with name and value
   @WebServiceRef(name = "Port2", value = TestEndpointService.class)
   static TestEndpoint port2;

   // Test on field with value
   @WebServiceRef(value = TestEndpointService.class)
   static TestEndpoint port3;

   // Test on field
   @WebServiceRef
   static TestEndpoint port4;

   // Test on field with name
   @WebServiceRef (name = "Port5")
   static TestEndpoint port5;

   static InitialContext iniCtx;
   static String retStr;

   public static void main(String[] args)
   {
      String inStr = args[0];
      log.info("echo: " + inStr);

      ArrayList<TestEndpoint> ports = new ArrayList<TestEndpoint>();
      try
      {
         ports.add(((TestEndpointService)iniCtx.lookup("java:comp/env/service1")).getTestEndpointPort());
         ports.add(((TestEndpointService)iniCtx.lookup("java:comp/env/service2")).getTestEndpointPort());
         ports.add((TestEndpoint)service3.getPort(TestEndpoint.class));
         ports.add(((TestEndpointService)iniCtx.lookup("java:comp/env/TestEndpointService3")).getTestEndpointPort());
         ports.add((TestEndpoint)service4.getPort(TestEndpoint.class));
         ports.add(((TestEndpointService)iniCtx.lookup("java:comp/env/" + TestEndpointClientOne.class.getName() + "/service4")).getTestEndpointPort());
         ports.add((TestEndpoint)service5.getPort(TestEndpoint.class));
         ports.add(((TestEndpointService)iniCtx.lookup("java:comp/env/TestEndpointService5")).getTestEndpointPort());
         ports.add((TestEndpoint)service6.getPort(TestEndpoint.class));
         ports.add(((TestEndpointService)iniCtx.lookup("java:comp/env/" + TestEndpointClientOne.class.getName() + "/service6")).getTestEndpointPort());
         ports.add((TestEndpoint)iniCtx.lookup("java:comp/env/port1"));
         ports.add(port2);
         ports.add((TestEndpoint)iniCtx.lookup("java:comp/env/Port2"));
         ports.add(port3);
         ports.add((TestEndpoint)iniCtx.lookup("java:comp/env/" + TestEndpointClientOne.class.getName() + "/port3"));
         ports.add(port4);
         ports.add(port5);
      }
      catch (Exception ex)
      {
         log.error("Cannot add port", ex);
         throw new WebServiceException(ex);
      }

      for (TestEndpoint port : ports)
      {
         String outStr = port.echo(inStr);
         if (inStr.equals(outStr) == false)
            throw new WebServiceException("Invalid echo return: " + inStr);
      }

      retStr = inStr;
   }
}
