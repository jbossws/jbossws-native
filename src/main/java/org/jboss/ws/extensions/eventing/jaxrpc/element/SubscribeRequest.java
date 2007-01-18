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
package org.jboss.ws.extensions.eventing.jaxrpc.element;

// $Id$

import java.util.Date;

import javax.xml.soap.SOAPElement;

/**
 * @author Heiko Braun, <heiko@openj.net>
 * @since 24-Nov-2005
 */
public class SubscribeRequest
{
   private EndpointReference endTo;
   private DeliveryType delivery;
   private Date expires;
   private FilterType filter;

   private SOAPElement[] _any;

   public EndpointReference getEndTo()
   {
      return endTo;
   }

   public void setEndTo(EndpointReference endTo)
   {
      this.endTo = endTo;
   }

   public org.jboss.ws.extensions.eventing.jaxrpc.element.DeliveryType getDelivery()
   {
      return delivery;
   }

   public void setDelivery(DeliveryType delivery)
   {
      this.delivery = delivery;
   }

   public Date getExpires()
   {
      return expires;
   }

   public void setExpires(Date expires)
   {
      this.expires = expires;
   }

   public FilterType getFilter()
   {
      return filter;
   }

   public void setFilter(FilterType filter)
   {
      this.filter = filter;
   }

   public SOAPElement[] get_any()
   {
      return _any;
   }

   public void set_any(SOAPElement[] _any)
   {
      this._any = _any;
   }

   public String toString()
   {
      return "SubscribeRequest{" + "delivery=" + getDelivery() + "}";
   }
}
