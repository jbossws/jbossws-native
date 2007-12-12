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
package org.jboss.ws.extensions.wsrm.server;

import java.util.Set;
import java.util.TreeSet;

import org.jboss.ws.extensions.addressing.AddressingClientUtil;
import org.jboss.ws.extensions.wsrm.RMSequenceIface;

/**
 * Server side implementation of the RM sequence
 *
 * @author richard.opalka@jboss.com
 *
 * @since Dec 12, 2007
 */
public class RMServerSequence implements RMSequenceIface
{

   private final String inboundId = AddressingClientUtil.generateMessageID().toString();
   private final String outboundId = AddressingClientUtil.generateMessageID().toString();
   private final long duration = 10 * 60 * 1000L; // 10 minutes duration
   private final Set receivedInboundMessages = new TreeSet<Long>();
   private boolean closed;
   
   public String getInboundId()
   {
      return this.inboundId;
   }

   public long getLastMessageNumber()
   {
      // TODO Auto-generated method stub
      return 0;
   }
   
   public long getDuration()
   {
      return this.duration;
   }

   public String getOutboundId()
   {
      return this.outboundId;
   }

   public Set<Long> getReceivedInboundMessages()
   {
      return this.receivedInboundMessages;
   }

   public long newMessageNumber()
   {
      // TODO Auto-generated method stub
      return 0;
   }

   public void close()
   {
      this.closed = true;
   }

}
