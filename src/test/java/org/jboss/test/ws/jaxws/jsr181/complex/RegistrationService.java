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
package org.jboss.test.ws.jaxws.jsr181.complex;

import java.util.Calendar;
import java.util.Date;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.jboss.logging.Logger;
import org.jboss.test.ws.jaxws.jsr181.complex.extra.Statistics;

/**
 * A mock registration service that exercises the use of complex types, arrays, inheritence,
 * and exceptions. Note that this test does not yet test polymorphic behavior.
 *
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @version $Revision$
 */
@WebService (endpointInterface = "org.jboss.test.ws.jaxws.jsr181.complex.Registration", serviceName="RegistrationService")
public class RegistrationService implements Registration
{
   // Provide logging
   private static Logger log = Logger.getLogger(RegistrationService.class);

   public long register(Customer customer, Date when) throws ValidationException, AlreadyRegisteredException
   {
      Name name = customer.getName();
      if (name == null)
      {
         long[] ids = new long[1];
         ids[0] = customer.getId();
         throw new ValidationException("No name!", ids);
      }

      if ("al".equalsIgnoreCase(name.getFirstName()) && "capone".equalsIgnoreCase(name.getLastName()))
         throw new AlreadyRegisteredException("Al Capone is already registered", 456);

      for (Customer c : customer.getReferredCustomers())
      {
         log.info("Refered customer: " +  c.getName());
      }

      log.info("registering customer: " + customer);
      return customer.getId();
   }

   public long[] bulkRegister(Customer[] customers, Date when) throws ValidationException, AlreadyRegisteredException
   {
      long[] registered = new long[customers.length];
      long[] failed = new long[customers.length];

      int x = 0;
      int y = 0;

      for (Customer c : customers)
      {
         try
         {
            registered[x++] = register(c, when);
         }
         catch (ValidationException e)
         {
            failed[y++] = e.getFailiedCustomers()[0];
         }
      }

      if (y > 0)
      {
         long[] newFailed = new long[y];
         System.arraycopy(failed, 0, newFailed, 0, y);
         throw new ValidationException("Validation errors on bulk registering customers", newFailed);
      }

      return registered;
   }

   public boolean registerForInvoice(InvoiceCustomer customer) throws ValidationException, AlreadyRegisteredException
   {
      log.info("registerForInvoice: " + customer.getCycleDay());
      return true;
   }

   public Statistics getStatistics(Customer customer)
   {
      Statistics stats = new Statistics();
      stats.hits = 10;
      Calendar cal = Calendar.getInstance();
      cal.clear();
      cal.set(2006, 0, 26, 22, 59, 0);
      stats.activationTime = cal;

      return stats;
   }
}