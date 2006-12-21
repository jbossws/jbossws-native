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
package org.jboss.test.ws.jaxws.jsr181.complex.client;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;

import javax.xml.rpc.Service;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.ws.core.jaxrpc.ServiceFactoryImpl;

/**
 *
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @version $Revision$
 */
public class JSR181ComplexTestCase extends JBossWSTest
{
   private RegistrationService port;

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(JSR181ComplexTestCase.class, /* "jaxws-jsr181-complex.war" */ "");
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
//      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jsr181-complex/RegistrationService?wsdl");
//      File mappingFile = new File("resources/jaxws/jsr181/complex/jaxrpc-mapping.xml");
//      ServiceFactoryImpl factory = new ServiceFactoryImpl();
      //Service service = factory.createService(wsdlURL, null, mappingFile.toURL());
      //port = (RegistrationService) service.getPort(RegistrationService.class);
   }


   public void testRegistration() throws Exception
   {
      System.out.println("FIXME: [JBWS-1297] Implement JAXB Fault Marshalling");
      if (true) return;

      Customer customer = getFredJackson();
      customer.setReferredCustomers(new Customer[] {getJohnDoe(), getAlCapone()});

      port.register(customer, Calendar.getInstance());

      customer = getAlCapone();
      boolean pass = false;
      try
      {
         port.register(customer, Calendar.getInstance());
      }
      catch (AlreadyRegisteredException e)
      {
         if (e.getExistingId() == 456)
            pass = true;
      }

      assertTrue(pass);
   }

   public void testInvoiceRegistration() throws Exception
   {
      System.out.println("FIXME: [JBWS-1297] Implement JAXB Fault Marshalling");
      if (true) return;

      InvoiceCustomer customer = getInvoiceFredJackson();
      customer.setReferredCustomers(new Customer[] {getJohnDoe(), getAlCapone()});

      assertTrue(port.registerForInvoice(customer));
   }

   public void testOtherPackage() throws Exception
   {
      System.out.println("FIXME: [JBWS-1297] Implement JAXB Fault Marshalling");
      if (true) return;

      Statistics stats = port.getStatistics(getFredJackson());
      Calendar cal = Calendar.getInstance();
      cal.clear();
      cal.set(2006, 0, 26, 22, 59, 0);
      assertEquals(cal.getTime(), stats.activationTime.getTime());
      assertEquals(10, stats.hits);
   }

   public void testBulkRegistration() throws Exception
   {
      System.out.println("FIXME: [JBWS-1297] Implement JAXB Fault Marshalling");
      if (true) return;

      Customer[] customers = new Customer[] {getFredJackson(), getJohnDoe()};

      long[] ids = port.bulkRegister(customers, Calendar.getInstance());

      assertTrue(Arrays.equals(ids, new long[] {123, 124}));

      customers = new Customer[] {getFredJackson(), getInvalid(754), getInvalid(753), getJohnDoe(), getInvalid(752)};

      boolean pass = false;
      try
      {
         port.bulkRegister(customers, Calendar.getInstance());
      }
      catch (ValidationException e)
      {
         pass = Arrays.equals(e.getFailiedCustomers(), new long[] {754, 753, 752});
      }

      assertTrue(pass);
   }

   private Customer getFredJackson()
   {
      Name name = new Name();
      name.setFirstName("Fred");
      name.setMiddleName("Jones");
      name.setLastName("Jackson");

      Address address = new Address();
      address.setCity("Atlanta");
      address.setState("Georgia");
      address.setZip("53717");
      address.setStreet("Yet Another Peach Tree St.");

      PhoneNumber number1 = new PhoneNumber();
      number1.setAreaCode("123");
      number1.setExchange("456");
      number1.setLine("7890");

      PhoneNumber number2 = new PhoneNumber();
      number1.setAreaCode("333");
      number1.setExchange("222");
      number1.setLine("1234");

      Customer customer = new Customer();
      customer.setId(123);
      customer.setName(name);
      customer.setAddress(address);
      customer.setContactNumbers(new PhoneNumber[] {number1, number2});
      return customer;
   }

   private InvoiceCustomer getInvoiceFredJackson()
   {
      Name name = new Name();
      name.setFirstName("Fred");
      name.setMiddleName("Jones");
      name.setLastName("Jackson");

      Address address = new Address();
      address.setCity("Atlanta");
      address.setState("Georgia");
      address.setZip("53717");
      address.setStreet("Yet Another Peach Tree St.");

      PhoneNumber number1 = new PhoneNumber();
      number1.setAreaCode("123");
      number1.setExchange("456");
      number1.setLine("7890");

      PhoneNumber number2 = new PhoneNumber();
      number1.setAreaCode("333");
      number1.setExchange("222");
      number1.setLine("1234");

      InvoiceCustomer customer = new InvoiceCustomer();
      customer.setId(123);
      customer.setName(name);
      customer.setAddress(address);
      customer.setContactNumbers(new PhoneNumber[] {number1, number2});
      customer.setCycleDay(10);
      return customer;
   }

   private Customer getJohnDoe()
   {
      Name name = new Name();
      name.setFirstName("John");
      name.setLastName("Doe");

      Address address = new Address();
      address.setCity("New York");
      address.setState("New York");
      address.setZip("10010");
      address.setStreet("Park Street");

      PhoneNumber number1 = new PhoneNumber();
      number1.setAreaCode("555");
      number1.setExchange("867");
      number1.setLine("5309");

      Customer customer = new Customer();
      customer.setName(name);
      customer.setAddress(address);
      customer.setContactNumbers(new PhoneNumber[] {number1});
      customer.setId(124);
      return customer;
   }

   private Customer getInvalid(long id)
   {
      Address address = new Address();
      address.setCity("New York");
      address.setState("New York");
      address.setZip("10010");
      address.setStreet("Park Street");

      PhoneNumber number1 = new PhoneNumber();
      number1.setAreaCode("555");
      number1.setExchange("867");
      number1.setLine("5309");

      Customer customer = new Customer();
      customer.setAddress(address);
      customer.setContactNumbers(new PhoneNumber[] {number1});
      customer.setId(id);
      return customer;
   }


   private Customer getAlCapone()
   {
      Name name = new Name();
      name.setFirstName("Al");
      name.setLastName("Capone");

      Address address = new Address();
      address.setCity("Chicago");
      address.setState("Illinois");
      address.setZip("60619");
      address.setStreet("7244 South Prairie Avenue.");

      PhoneNumber number1 = new PhoneNumber();
      number1.setAreaCode("888");
      number1.setExchange("722");
      number1.setLine("7322");

      Customer customer = new Customer();
      customer.setName(name);
      customer.setAddress(address);
      customer.setContactNumbers(new PhoneNumber[] {number1});
      customer.setId(125);
      return customer;
   }
}