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
package org.jboss.ws.extensions.eventing.element;

// $Id$

import java.net.URI;

import org.jboss.ws.extensions.addressing.EndpointReferenceImpl;

/**
 * @author Heiko Braun, <heiko@openj.net>
 * @since 25-Nov-2005
 */
public class SubscriptionEndRequest
{
   EndpointReferenceImpl subscriptionManager;
   URI code;
   String reason;

   public EndpointReferenceImpl getSubscriptionManager()
   {
      return subscriptionManager;
   }

   /**
    * todo: remove, since this is one-way op
    *
    * @param subscriptionManager
    */
   public void setSubscriptionManager(EndpointReferenceImpl subscriptionManager)
   {
      this.subscriptionManager = subscriptionManager;
   }

   public URI getCode()
   {
      return code;
   }

   public void setCode(URI code)
   {
      this.code = code;
   }

   public String getReason()
   {
      return reason;
   }

   public void setReason(String reason)
   {
      this.reason = reason;
   }
}
