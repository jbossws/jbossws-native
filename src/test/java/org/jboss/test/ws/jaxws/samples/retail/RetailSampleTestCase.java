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
package org.jboss.test.ws.jaxws.samples.retail;

import junit.framework.Test;
import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.tools.wsdl.WSDLDefinitionsFactory;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Heiko Braun <heiko.braun@jboss.com>
 * @version $Id$
 * @since Nov 8, 2006
 */
public class RetailSampleTestCase extends JBossWSTest {

   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-samples-retail/OrderMgmtBean";

   private static Order ORDER;
   private static Customer CUSTOMER;

   static
   {
      CUSTOMER = new Customer();
      CUSTOMER.setFirstName("Chuck");
      CUSTOMER.setLastName("Norris");
      CUSTOMER.setCreditCardDetails("1000-4567-3456-XXXX");

      ORDER = new Order(CUSTOMER);
      ORDER.setOrderNum(12345);
      ORDER.getItems().add( new OrderItem("Introduction to Web Services", 39.99) );
   }

   private OrderMgmt orderMgmtWS;

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(RetailSampleTestCase.class, "jaxws-samples-retail.jar, jaxws-samples-retail-client.jar");
   }

   protected void setUp() throws Exception
   {

      QName serviceName = new QName("http://org.jboss.ws/samples/retail", "OrderMgmtService");
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS+"?wsdl");

      javax.xml.ws.Service service = javax.xml.ws.Service.create(wsdlURL, serviceName);
      orderMgmtWS = (OrderMgmt)service.getPort(OrderMgmt.class);

   }

   public void testWebService() throws Exception
   {
      assertWSDLAccess();

      OrderStatus result = orderMgmtWS.prepareOrder(ORDER);
      assertEquals("Prepared", result.getStatus());
   }

   private void assertWSDLAccess() throws MalformedURLException
   {
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
      WSDLDefinitionsFactory factory = WSDLDefinitionsFactory.newInstance();
      WSDLDefinitions wsdlDefinitions = factory.parse(wsdlURL);
      assertNotNull(wsdlDefinitions);
   }
}
