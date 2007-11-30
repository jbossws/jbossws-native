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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.logging.Logger;

/**
 * TODO: Add comment
 *
 * @author richard.opalka@jboss.com
 *
 * @since Nov 26, 2007
 */
public final class RMSequenceManager implements Runnable
{
   private static final Logger logger = Logger.getLogger(RMSequenceManager.class);
   
   private static RMSequenceManager instance;
   private static final Lock classLock = new ReentrantLock();
   private final Object instanceLock = new Object();
   private final List<RMSequenceImpl> sequences = new LinkedList<RMSequenceImpl>();
   private boolean destroyed;
   
   private RMSequenceManager()
   {
      // hidden constructor
   }
   
   public static RMSequenceManager getInstance()
   {
      classLock.lock();
      try
      {
         if (instance == null)
         {
            instance = new RMSequenceManager();
            new Thread(instance, "RMSequenceManager").start();
            logger.debug("started");
         }
         return instance;
      }
      finally
      {
         classLock.unlock();
      }
   }

   public void register(RMSequenceImpl sequence)
   {
      synchronized (instanceLock)
      {
         this.sequences.add(sequence);
      }
   }
   
   public void unregister(RMSequenceImpl sequence)
   {
      synchronized (instanceLock)
      {
         this.sequences.remove(sequence);
         if (this.sequences.size() == 0)
            this.shutdown();
      }
   }
   
   public void run()
   {
      synchronized (instanceLock)
      {
         while (this.destroyed == false)
         {
            for (RMSequenceImpl sequence : sequences)
            {
               logger.debug("Processing outbound sequence " + sequence.getOutboundId());
               if (sequence.isAckRequested())
               {
                  /*
                  logger.debug("Sending ack for inbound sequence " + sequence.getInboundId());
                  Map<String, Object> wsrmReqCtx = new HashMap<String, Object>();
                  wsrmReqCtx.put(RMConstant.ONE_WAY_OPERATION, true);
                  sequence.getBindingProvider().getRequestContext().put(RMConstant.REQUEST_CONTEXT, wsrmReqCtx);
                  sequence.sendSequenceAcknowledgementMessage();
                  sequence.ackRequested(false);
                  */
               }
            }

            try
            {
               logger.debug("sleeping for 10 miliseconds");
               instanceLock.wait(10);
            }
            catch (InterruptedException ie)
            {
               logger.warn(ie);
            }
         }
      }
   }
   
   public void shutdown()
   {
      classLock.lock();
      try
      {
         instance = null;
      }
      finally
      {
         classLock.unlock();
      }
      synchronized (this.instanceLock)
      {
         this.destroyed = true;
         logger.debug("destroyed");
      }
   }
}
