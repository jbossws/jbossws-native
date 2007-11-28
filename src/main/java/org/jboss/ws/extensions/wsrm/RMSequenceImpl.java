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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.namespace.QName;
import javax.xml.ws.addressing.AddressingBuilder;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.JAXWSAConstants;

import org.jboss.logging.Logger;
import org.jboss.ws.core.jaxws.client.ClientImpl;
import org.jboss.ws.extensions.addressing.AddressingClientUtil;
import org.jboss.ws.extensions.wsrm.api.RMException;
import org.jboss.ws.extensions.wsrm.api.RMSequence;
import org.jboss.ws.extensions.wsrm.spi.RMProvider;
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
   private static final Logger LOGGER = Logger.getLogger(RMSequenceImpl.class);
   private final String incomingSequenceId;
   private final String outgoingSequenceId;
   private final URI backPort;
   private final ClientImpl client;
   // object states variables
   private boolean terminated = false;
   private boolean discarded = false;
   private AtomicLong messageNumber = new AtomicLong();
   private final Lock objectLock = new ReentrantLock();
   private AtomicInteger countOfUnassignedMessagesAvailable = new AtomicInteger();
   
   public RMSequenceImpl(ClientImpl client, String outId, URI backPort)
   {
      this(client, outId, null, backPort);
   }
   
   public void unassignedMessageReceived()
   {
      // we can't use objectLock in the method - possible deadlock
      this.countOfUnassignedMessagesAvailable.addAndGet(1);
      LOGGER.debug("Unassigned message available in callback handler");
   }

   public RMSequenceImpl(ClientImpl client, String outId, String inId, URI backPort)
   {
      super();
      this.client = client;
      this.incomingSequenceId = inId;
      this.outgoingSequenceId = outId;
      this.backPort = backPort;
      RMSequenceManager.getInstance().register(this);
   }
   
   public final URI getBackPort()
   {
      return this.backPort;
   }

   public final long newMessageNumber()
   {
      return this.messageNumber.incrementAndGet();
   }
   
   public final long getLastMessageNumber()
   {
      return this.messageNumber.get();
   }
   
   public final void discard() throws RMException
   {
      this.objectLock.lock();
      try
      {
         this.client.getWSRMLock().lock();
         try
         {
            this.client.setWSRMSequence(null);
            this.discarded = true;
            RMSequenceManager.getInstance().unregister(this);
         }
         finally
         {
            this.client.getWSRMLock().unlock();
         }
      }
      finally
      {
         this.objectLock.unlock();
      }
   }

   /**
    * Sets up terminated flag to true.
    */
   public final void terminate() throws RMException
   {
      this.objectLock.lock();
      try
      {
         if (this.terminated)
            return; 
         
         this.terminated = true;

         client.getWSRMLock().lock();
         try 
         {
            try
            {
               // set up addressing properties
               String address = client.getEndpointMetaData().getEndpointAddress();
               String action = RMConstant.TERMINATE_SEQUENCE_WSA_ACTION;
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
               QName terminateSequenceQN = RMProvider.get().getConstants().getTerminateSequenceQName();
               Map rmRequestContext = new HashMap();
               List outMsgs = new LinkedList();
               outMsgs.add(terminateSequenceQN);
               rmRequestContext.put(RMConstant.PROTOCOL_MESSAGES, outMsgs);
               rmRequestContext.put(RMConstant.SEQUENCE_REFERENCE, client.getWSRMSequence());
               // set up method invocation context
               Map requestContext = client.getBindingProvider().getRequestContext(); 
               requestContext.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND, props);
               requestContext.put(RMConstant.REQUEST_CONTEXT, rmRequestContext);
               // call stub method
               this.client.invoke(terminateSequenceQN, new Object[] {}, client.getBindingProvider().getResponseContext());
               RMSequenceManager.getInstance().unregister(this);
            }
            catch (Exception e)
            {
               throw new RMException("Unable to terminate WSRM sequence", e);
            }
            finally
            {
               this.client.setWSRMSequence(null); // TODO: do not remove this
            }
         }
         finally
         {
            this.client.getWSRMLock().unlock();
         }
      } 
      finally
      {
         this.objectLock.unlock();
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

   public final boolean isTerminated()
   {
      this.objectLock.lock();
      try
      {
         return this.terminated;
      }
      finally
      {
         this.objectLock.unlock();
      }
   }

   public final boolean isDiscarded()
   {
      this.objectLock.lock();
      try
      {
         return this.discarded;
      }
      finally
      {
         this.objectLock.unlock();
      }
   }
}
