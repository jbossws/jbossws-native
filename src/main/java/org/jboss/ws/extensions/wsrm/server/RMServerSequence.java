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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.logging.Logger;
import org.jboss.ws.extensions.addressing.AddressingClientUtil;
import org.jboss.ws.extensions.wsrm.RMSequence;

/**
 * Server side implementation of the RM sequence
 *
 * @author richard.opalka@jboss.com
 *
 * @since Dec 12, 2007
 */
public class RMServerSequence implements RMSequence
{
   private static final Logger logger = Logger.getLogger(RMServerSequence.class);

   private final String inboundId = AddressingClientUtil.generateMessageID().toString();
   private final String outboundId = AddressingClientUtil.generateMessageID().toString();
   private final long duration = 10 * 60 * 1000L; // 10 minutes duration
   private final Set<Long> acknowledgedOutboundMessages = new TreeSet<Long>();
   private final Set<Long> receivedInboundMessages = new TreeSet<Long>();
   private boolean closed;
   private AtomicBoolean inboundMessageAckRequested = new AtomicBoolean();
   private AtomicLong messageNumber = new AtomicLong();
   
   public String getInboundId()
   {
      return this.inboundId;
   }

   public long getDuration()
   {
      return this.duration;
   }
   
   public String getAcksTo()
   {
      return null;
   }

   public String getOutboundId()
   {
      return this.outboundId;
   }

   public final void addReceivedInboundMessage(long messageId)
   {
      this.receivedInboundMessages.add(messageId);
      logger.debug("Inbound Sequence: " + this.inboundId + ", received message no. " + messageId);
   }

   public final void addReceivedOutboundMessage(long messageId)
   {
      this.acknowledgedOutboundMessages.add(messageId);
      logger.debug("Outbound Sequence: " + this.outboundId + ", message no. " + messageId + " acknowledged by server");
   }

   public final void ackRequested(boolean requested)
   {
      this.inboundMessageAckRequested.set(requested);
      logger.debug("Inbound Sequence: " + this.inboundId + ", ack requested. Messages in the queue: " + this.receivedInboundMessages);
   }
   
   public final long newMessageNumber()
   {
      // no need for synchronization
      return this.messageNumber.incrementAndGet();
   }
   
   public final long getLastMessageNumber()
   {
      // no need for synchronization
      return this.messageNumber.get();
   }
   
   public final boolean isAckRequested()
   {
      return this.inboundMessageAckRequested.get();
   }

   public Set<Long> getReceivedInboundMessages()
   {
      return this.receivedInboundMessages;
   }

   public void close()
   {
      this.closed = true;
   }
   
   public String toString()
   {
      return this.inboundId + " - " + this.outboundId;
   }

}
