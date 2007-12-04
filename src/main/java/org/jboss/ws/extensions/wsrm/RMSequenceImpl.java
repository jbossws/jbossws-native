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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.addressing.AddressingBuilder;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.JAXWSAConstants;

import org.jboss.logging.Logger;
import org.jboss.ws.core.jaxws.client.ClientImpl;
import org.jboss.ws.extensions.addressing.AddressingClientUtil;
import org.jboss.ws.extensions.wsrm.config.RMConfig;
import org.jboss.ws.extensions.wsrm.api.RMAddressingType;
import org.jboss.ws.extensions.wsrm.api.RMException;
import org.jboss.ws.extensions.wsrm.api.RMSequence;
import org.jboss.ws.extensions.wsrm.spi.RMConstants;
import org.jboss.ws.extensions.wsrm.spi.RMProvider;
import org.jboss.ws.extensions.wsrm.spi.protocol.RMIncompleteSequenceBehavior;
import org.jboss.ws.extensions.wsrm.transport.RMUnassignedMessageListener;

/**
 * TODO: all termination methods such as terminate, discard, ... etc must unregister the sequence from client
 * Reliable messaging sequence implementation
 *
 * @author richard.opalka@jboss.com
 *
 * @since Oct 25, 2007
 */
@SuppressWarnings("unchecked")
public final class RMSequenceImpl implements RMSequence, RMUnassignedMessageListener
{
   private static final Logger logger = Logger.getLogger(RMSequenceImpl.class);
   private static final RMConstants wsrmConstants = RMProvider.get().getConstants();
   
   private final RMConfig wsrmConfig;
   private final RMAddressingType addrType;
   private final Set<Long> acknowledgedOutboundMessages = new TreeSet<Long>();
   private final Set<Long> receivedInboundMessages = new TreeSet<Long>();
   private RMIncompleteSequenceBehavior behavior = RMIncompleteSequenceBehavior.NO_DISCARD;
   private String incomingSequenceId;
   private String outgoingSequenceId;
   private long duration = -1;
   private long creationTime;
   private URI backPort;
   private ClientImpl client;
   // object states variables
   private boolean terminated;
   private boolean discarded;
   private boolean isFinal;
   private AtomicBoolean inboundMessageAckRequested = new AtomicBoolean();
   private AtomicLong messageNumber = new AtomicLong();
   private final Object lock = new Object();
   private AtomicInteger countOfUnassignedMessagesAvailable = new AtomicInteger();
   
   public void unassignedMessageReceived()
   {
      // we can't use objectLock in the method - possible deadlock
      this.countOfUnassignedMessagesAvailable.addAndGet(1);
      logger.debug("Expected sequence expiration in " + ((System.currentTimeMillis() - this.creationTime) / 1000) + "seconds");
      logger.debug("Unassigned message available in callback handler");
   }

   public RMSequenceImpl(RMAddressingType addrType, RMConfig wsrmConfig)
   {
      super();
      if ((addrType == null) || (wsrmConfig == null))
         throw new IllegalArgumentException();
      
      this.addrType = addrType;
      this.wsrmConfig = wsrmConfig;
      try
      {
         this.backPort = new URI("http://localhost:8888/temporary_listen_address/666"); // TODO: use generator;;
      }
      catch (URISyntaxException use)
      {
         logger.warn(use);
      }
   }
   
   public final Set<Long> getReceivedInboundMessages()
   {
      synchronized (lock)
      {
         return Collections.unmodifiableSet(this.receivedInboundMessages);
      }
   }
   
   public final BindingProvider getBindingProvider()
   {
      synchronized (lock)
      {
         return (BindingProvider)this.client;
      }
   }
   
   public final void setFinal()
   {
      synchronized (lock)
      {
         this.isFinal = true;
         logger.debug("Sequence " + this.outgoingSequenceId + " state changed to final");
      }
   }
   
   public final void ackRequested(boolean requested)
   {
      this.inboundMessageAckRequested.set(requested);
      logger.debug("Inbound Sequence: " + this.incomingSequenceId + ", ack requested. Messages in the queue: " + this.receivedInboundMessages);
   }
   
   public final boolean isAckRequested()
   {
      return this.inboundMessageAckRequested.get();
   }
   
   public final void addReceivedInboundMessage(long messageId)
   {
      synchronized (lock)
      {
         this.receivedInboundMessages.add(messageId);
         logger.debug("Inbound Sequence: " + this.incomingSequenceId + ", received message no. " + messageId);
      }
   }
   
   public final void addReceivedMessage(long messageId)
   {
      synchronized (lock)
      {
         this.acknowledgedOutboundMessages.add(messageId);
         logger.debug("Outbound Sequence: " + this.outgoingSequenceId + ", message no. " + messageId + " acknowledged by server");
      }
   }
   
   public final void setOutboundId(String outboundId)
   {
      synchronized (lock)
      {
         this.outgoingSequenceId = outboundId;
      }
   }
   
   public final void setInboundId(String inboundId)
   {
      synchronized (lock)
      {
         this.incomingSequenceId = inboundId;
      }
   }
   
   public final void setClient(ClientImpl client)
   {
      synchronized (lock)
      {
         this.client = client;
      }
   }
   
   public final void setDuration(long duration)
   {
      synchronized (lock)
      {
         if (duration > 0)
         {
            this.creationTime = System.currentTimeMillis();
            this.duration = duration;
         }
      }
   }
   
   public final long getDuration()
   {
      synchronized (lock)
      {
         return this.duration;
      }
   }
   
   public final URI getBackPort()
   {
      // no need for synchronization
      return (this.addrType == RMAddressingType.ADDRESSABLE) ? this.backPort : null;
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
   
   public final void discard() throws RMException
   {
      synchronized (lock)
      {
         this.client.getWSRMLock().lock();
         try
         {
            this.client.setWSRMSequence(null);
            this.discarded = true;
         }
         finally
         {
            this.client.getWSRMLock().unlock();
         }
      }
   }
   
   public final void close() throws RMException
   {
      synchronized (lock)
      {
         if (this.terminated)
            return; 
         
         this.terminated = true;

         try 
         {
            sendCloseMessage();
            sendTerminateMessage();
         }
         finally
         {
            client.getWSRMLock().lock();
            this.client.setWSRMSequence(null); // TODO: do not remove this
            this.client.getWSRMLock().unlock();
         }
      }
   }

   /**
    * Sets up terminated flag to true.
    */
   private void sendMessage(String action, QName operationQName) throws RMException
   {
      try
      {
         // set up addressing properties
         String address = client.getEndpointMetaData().getEndpointAddress();
         AddressingProperties props = null;
         if (this.client.getWSRMSequence().getBackPort() != null)
         {
            props = AddressingClientUtil.createDefaultProps(action, address);
            props.setReplyTo(AddressingBuilder.getAddressingBuilder().newEndpointReference(this.client.getWSRMSequence().getBackPort()));
         }
         else
         {
            props = AddressingClientUtil.createAnonymousProps(action, address);
         }
         // prepare WS-RM request context
         Map requestContext = client.getBindingProvider().getRequestContext(); 
         Map rmRequestContext = (Map)requestContext.get(RMConstant.REQUEST_CONTEXT);
         if (rmRequestContext == null)
         {
            rmRequestContext = new HashMap(); 
         }
         List outMsgs = new LinkedList();
         outMsgs.add(operationQName);
         rmRequestContext.put(RMConstant.PROTOCOL_MESSAGES, outMsgs);
         rmRequestContext.put(RMConstant.SEQUENCE_REFERENCE, this);
         // set up method invocation context
         requestContext.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND, props);
         requestContext.put(RMConstant.REQUEST_CONTEXT, rmRequestContext);
         // call stub method
         this.client.invoke(operationQName, new Object[] {}, client.getBindingProvider().getResponseContext());
      }
      catch (Exception e)
      {
         throw new RMException("Unable to terminate WSRM sequence", e);
      }
   }
   
   public final void sendCloseMessage()
   {
      synchronized (lock)
      {
         while (this.isAckRequested())
         {
            logger.debug("Waiting till all inbound sequence acknowledgements will be sent");
            sendSequenceAcknowledgementMessage();
         }
      }
      Map<String, Object> wsrmReqCtx = new HashMap<String, Object>();
      wsrmReqCtx.put(RMConstant.ONE_WAY_OPERATION, false);
      this.getBindingProvider().getRequestContext().put(RMConstant.REQUEST_CONTEXT, wsrmReqCtx);
      sendMessage(RMConstant.CLOSE_SEQUENCE_WSA_ACTION, wsrmConstants.getCloseSequenceQName());
   }
   
   public final void sendTerminateMessage()
   {
      sendMessage(RMConstant.TERMINATE_SEQUENCE_WSA_ACTION, wsrmConstants.getTerminateSequenceQName());
   }
   
   public final void sendSequenceAcknowledgementMessage()
   {
      Map<String, Object> wsrmReqCtx = new HashMap<String, Object>();
      wsrmReqCtx.put(RMConstant.ONE_WAY_OPERATION, true);
      this.getBindingProvider().getRequestContext().put(RMConstant.REQUEST_CONTEXT, wsrmReqCtx);
      ackRequested(false);
      sendMessage(RMConstant.SEQUENCE_ACKNOWLEDGEMENT_WSA_ACTION, wsrmConstants.getSequenceAcknowledgementQName());
   }
   
   public final void setBehavior(RMIncompleteSequenceBehavior behavior)
   {
      synchronized (lock)
      {
         if (behavior != null)
         {
            this.behavior = behavior;
         }
      }
   }

   public final boolean isCompleted()
   {
      return true;
   }

   public final boolean isCompleted(int timeAmount, TimeUnit timeUnit)
   {
      return true;
   }

   public final String getOutboundId()
   {
      return outgoingSequenceId;
   }
   
   public final String getInboundId()
   {
      return incomingSequenceId;
   }

   public final boolean isClosed()
   {
      synchronized (lock)
      {
         return this.terminated;
      }
   }

   public final boolean isDiscarded()
   {
      synchronized (lock)
      {
         return this.discarded;
      }
   }
}
