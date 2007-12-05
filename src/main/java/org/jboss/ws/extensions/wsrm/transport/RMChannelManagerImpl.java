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
package org.jboss.ws.extensions.wsrm.transport;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.logging.Logger;
import org.jboss.ws.extensions.wsrm.api.RMException;

/**
 * WS-RM channel manager ensures message reliable delivery according to sequence configuration
 *
 * @author richard.opalka@jboss.com
 *
 * @since Dec 5, 2007
 */
public final class RMChannelManagerImpl implements Runnable, RMChannelManager
{
   
   private static final Logger logger = Logger.getLogger(RMChannelManagerImpl.class);
   private static RMChannelManager instance = new RMChannelManagerImpl();
   private static final ExecutorService rmChannelPool = Executors.newFixedThreadPool(5, new RMThreadFactory());
   private static final int countOfAttempts = 10;
   private static final int timeToWait = 2;
   
   private static final class RMThreadFactory implements ThreadFactory
   {
      final ThreadGroup group;
      final AtomicInteger threadNumber = new AtomicInteger(1);
      final String namePrefix = "rm-pool-thread-";
    
      private RMThreadFactory()
      {
         SecurityManager sm = System.getSecurityManager();
         group = (sm != null) ? sm.getThreadGroup() : Thread.currentThread().getThreadGroup();
      }
      
      public Thread newThread(Runnable r)
      {
         Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
         if (t.isDaemon())
            t.setDaemon(false);
         if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
         return t;
      }
   }

   private RMChannelManagerImpl()
   {
      Thread thread = new Thread(this, "RMChannelManager");
      thread.setDaemon(true);
      thread.start();
   }
   
   public static final RMChannelManager getInstance()
   {
      return instance;
   }

   public final void run()
   {
      while (true)
      {
         logger.debug("checking persistent store for undelivered messages");
         try
         {
            Thread.sleep(10);
         }
         catch (InterruptedException ie)
         {
            logger.warn(ie.getMessage(), ie);
         }
      }
      
   }
   
   public final RMMessage send(RMMessage request) throws Throwable
   {
      // submit new task to channel
      // if response has no fault - delete saved request and return response to the client
      // if response has fault - try to detect it's type
      
      RMChannelResponse result = null;
      long startTime = 0L;
      long endTime = 0L;
      
      for (int i = 0; i < countOfAttempts; i++)
      {
         Future<RMChannelResponse> futureResult = rmChannelPool.submit(new RMChannelTask(request));
         try 
         {
            startTime = System.currentTimeMillis();
            result = futureResult.get(timeToWait, TimeUnit.SECONDS);
            endTime = System.currentTimeMillis();
            logger.debug("Response message received in " + (endTime - startTime) + " miliseconds");
            break;
         }
         catch (TimeoutException te)
         {
            endTime = System.currentTimeMillis();
            logger.warn("Response message not received in " + (endTime - startTime) + " miliseconds");
         }
      }

      if (result == null)
         throw new RMException("Unable to deliver message with addressing id: " + RMTransportHelper.getMessageId(request));
      
      Throwable fault = result.getFault();
      if (fault != null)
      {
         throw fault;
      }
      else
      {
         return result.getResponse();
      }
   }
   
}
