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
package org.jboss.ws.extensions.wsrm;

import org.jboss.ws.extensions.wsrm.backchannel.CallbackHandler;

/**
 * Represents response that goes from the RM channel
 * @see org.jboss.ws.extensions.wsrm.RMChannel
 * @author richard.opalka@jboss.com
 */
final class RMChannelResponse
{
   private final Throwable fault;
   private final RMMessage result;
   private final CallbackHandler callback;
   private final String messageId; // WS-Addressing: MessageID
   
   public RMChannelResponse(CallbackHandler callback, String messageId)
   {
      this(null, null, callback, messageId);
   }
   
   public RMChannelResponse(Throwable fault)
   {
      this(null, fault, null, null);
   }
   
   public RMChannelResponse(RMMessage result)
   {
      this(result, null, null, null);
   }
   
   private RMChannelResponse(RMMessage result, Throwable fault, CallbackHandler callback, String messageId)
   {
      super();
      this.result = result;
      this.fault = fault;
      this.callback = callback;
      this.messageId = messageId;
   }
   
   public Throwable getFault()
   {
      return (this.callback != null) ? this.callback.getFault(this.messageId) : this.fault;
   }
   
   public RMMessage getResponse()
   {
      return (this.callback != null) ? this.callback.getMessage(this.messageId) : this.result;
   }
}