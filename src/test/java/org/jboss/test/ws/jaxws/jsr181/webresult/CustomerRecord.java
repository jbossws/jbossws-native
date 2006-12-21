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
package org.jboss.test.ws.jaxws.jsr181.webresult;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CustomerRecord", propOrder = { "firstName", "lastName", "address" })
public class CustomerRecord
{
   @XmlElement(required = true, nillable = true)
   private String firstName;
   @XmlElement(required = true, nillable = true)
   private String lastName;
   @XmlElement(required = true, nillable = true)
   private USAddress address;

   public CustomerRecord()
   {
   }

   public CustomerRecord(String firstName, String lastName, USAddress address)
   {
      this.address = address;
      this.firstName = firstName;
      this.lastName = lastName;
   }

   public USAddress getAddress()
   {
      return address;
   }

   public void setAddress(USAddress address)
   {
      this.address = address;
   }

   public String getFirstName()
   {
      return firstName;
   }

   public void setFirstName(String firstName)
   {
      this.firstName = firstName;
   }

   public String getLastName()
   {
      return lastName;
   }

   public void setLastName(String lastName)
   {
      this.lastName = lastName;
   }

   public int hashCode()
   {
      return toString().hashCode();
   }

   public String toString()
   {
      return "[first=" + firstName + ",last=" + lastName + ",addr=" + address + "]";
   }
}
