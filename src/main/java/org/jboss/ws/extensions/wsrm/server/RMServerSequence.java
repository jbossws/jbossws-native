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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.jboss.logging.Logger;
import org.jboss.util.NotImplementedException;
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

   private String sequenceId;
   private String inboundId;
   private String outboundId;
   private long creationTime;
   private long duration;
   private Set<Long> acknowledgedOutboundMessages = new TreeSet<Long>();
   private Set<Long> receivedInboundMessages = new TreeSet<Long>();
   private boolean closed;
   private boolean terminated;
   private boolean inboundMessageAckRequested;
   private long messageNumber;
   
   public RMServerSequence(File serializedSequence) throws IOException
   {
      ObjectInputStream ois = null;
      try
      {
         ois = new ObjectInputStream(new FileInputStream(serializedSequence));
         this.sequenceId = serializedSequence.getName();
         this.inboundId = ois.readUTF();
         this.outboundId = ois.readUTF();
         this.closed = ois.readBoolean();
         this.terminated = ois.readBoolean();
         this.creationTime = ois.readLong();
         this.duration = ois.readLong();
         this.messageNumber = ois.readLong();
         int countOfOutboundMessages = ois.readInt();
         for (int i = 0; i < countOfOutboundMessages; i++)
         {
            this.acknowledgedOutboundMessages.add(ois.readLong());
         }
         int countOfInboundMessages = ois.readInt();
         for (int i = 0; i < countOfInboundMessages; i++)
         {
            this.receivedInboundMessages.add(ois.readLong());
         }
      }
      finally
      {
         ois.close();
      }
   }
   
   public RMServerSequence()
   {
      this.sequenceId = "seq-" + System.currentTimeMillis() + "-" + System.identityHashCode(this);
      this.inboundId = AddressingClientUtil.generateMessageID().toString();
      this.outboundId = AddressingClientUtil.generateMessageID().toString();
      this.creationTime = System.currentTimeMillis();
      this.duration = 10 * 60 * 1000L; // 10 minutes duration
   }
   
   public String getId()
   {
      return this.sequenceId;
   }
   
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
   
   public void serialize()
   {
      throw new NotImplementedException();
   }
   
   public void deserialize()
   {
      throw new NotImplementedException();
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
      this.inboundMessageAckRequested = requested;
      logger.debug("Inbound Sequence: " + this.inboundId + ", ack requested. Messages in the queue: " + this.receivedInboundMessages);
   }
   
   public final long newMessageNumber()
   {
      // no need for synchronization
      return ++this.messageNumber;
   }
   
   public final long getLastMessageNumber()
   {
      // no need for synchronization
      return this.messageNumber;
   }
   
   public final boolean isAckRequested()
   {
      return this.inboundMessageAckRequested;
   }

   public Set<Long> getReceivedInboundMessages()
   {
      return this.receivedInboundMessages;
   }

   public void close()
   {
      this.closed = true;
   }
   
   public void terminate()
   {
      this.terminated = true;
   }
   
   public byte[] toByteArray() throws IOException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      oos.writeUTF(this.inboundId);
      oos.writeUTF(this.outboundId);
      oos.writeBoolean(this.closed);
      oos.writeBoolean(this.terminated);
      oos.writeLong(this.creationTime);
      oos.writeLong(this.duration);
      oos.writeLong(this.messageNumber);
      oos.writeInt(this.acknowledgedOutboundMessages.size());
      for (Iterator<Long> i = this.acknowledgedOutboundMessages.iterator(); i.hasNext(); )
      {
         oos.writeLong(i.next());
      }
      oos.writeInt(this.receivedInboundMessages.size());
      for (Iterator<Long> i = this.receivedInboundMessages.iterator(); i.hasNext(); )
      {
         oos.writeLong(i.next());
      }
      oos.close();
      return baos.toByteArray();
   }
   
   public String toString()
   {
      return this.inboundId + " - " + this.outboundId;
   }

}
