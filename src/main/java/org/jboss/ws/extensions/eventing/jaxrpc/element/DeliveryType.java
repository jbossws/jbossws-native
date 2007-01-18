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

import java.net.URI;

import javax.xml.soap.SOAPElement;

/**
 * A delivery element states where and how to send notifications.
 * It can contain an EPR where the notifications should be delivered to:
 * <p/>
 * <pre>
 * &lt;wse:Delivery&gt;
 *   &lt;wse:NotifyTo&gt;
 *       &lt;wsa:Address&gt;
 *           http://www.other.example.com/OnStormWarning
 *       &lt;/wsa:Address&gt;
 *       &lt;wsa:ReferenceParameters&gt;
 *           &lt;ew:MySubscription&gt;2597&lt;/ew:MySubscription&gt;
 *       &lt;/wsa:ReferenceParameters&gt;
 *   &lt;/wse:NotifyTo&gt;
 * &lt;/wse:Delivery&gt;
 * </pre>
 * <p/>
 * NOTE: In case the NotifyTo is missing, the subscribers EPR should be used
 * (denoted by soap:Header/wsa:ReplyTo).<p>
 * <p/>
 * The specification defines single delivery mode,
 * Push Mode, which is simple asynchronous messaging.
 *
 * @author Heiko Braun, <heiko@openj.net>
 * @since 24-Nov-2005
 */
public class DeliveryType
{
   private URI mode;
   private SOAPElement[] _any;
   public String _value;
   private EndpointReference notifyTo;

   public URI getMode()
   {
      return mode;
   }

   public void setMode(URI mode)
   {
      this.mode = mode;
   }

   public SOAPElement[] get_any()
   {
      return _any;
   }

   public void set_any(SOAPElement[] _any)
   {
      this._any = _any;
   }

   public EndpointReference getNotifyTo()
   {
      return notifyTo;
   }

   public void setNotifyTo(EndpointReference notifyTo)
   {
      this.notifyTo = notifyTo;
   }
   public String get_value()
   {
      return _value;
   }

   public void set_value(String _value)
   {
      this._value = _value;
   }
}
