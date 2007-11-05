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

/**
 * Represents response that goes from the RM channel
 * @see org.jboss.ws.extensions.wsrm.RMChannel
 * @author richard.opalka@jboss.com
 */
final class RMChannelResponse
{
   private final Throwable fault;
   private final RMMessage result;
   
   public RMChannelResponse(Throwable fault)
   {
      this(null, fault);
   }
   
   public RMChannelResponse(RMMessage result)
   {
      this(result, null);
   }
   
   private RMChannelResponse(RMMessage result, Throwable fault)
   {
      super();
      this.result = result;
      this.fault = fault;
   }
   
   public Throwable getFault()
   {
      return this.fault;
   }
   
   public RMMessage getResponse()
   {
      return this.result;
   }
}