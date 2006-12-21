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

/**
 * Represents a customer. Part of the JSR-181 Complex Test Case.
 *
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @version $Revision$
 */
public class Customer
{
   private long id;
   private Address address;
   private Name name;
   private PhoneNumber[] contactNumbers;

   // since there is no way to differentiate between a null array
   // and an array with 1 element that is null
   private Customer[] referredCustomers = new Customer[0];

   public Address getAddress()
   {
      return address;
   }


   public void setAddress(Address address)
   {
      this.address = address;
   }


   public PhoneNumber[] getContactNumbers()
   {
      return contactNumbers;
   }


   public void setContactNumbers(PhoneNumber[] contactNumbers)
   {
      this.contactNumbers = contactNumbers;
   }


   public long getId()
   {
      return id;
   }


   public void setId(long id)
   {
      this.id = id;
   }


   public Name getName()
   {
      return name;
   }


   public void setName(Name name)
   {
      this.name = name;
   }


   public Customer[] getReferredCustomers()
   {
      return referredCustomers;
   }


   public void setReferredCustomers(Customer[] referredCustomers)
   {
      this.referredCustomers = referredCustomers;
   }
}
