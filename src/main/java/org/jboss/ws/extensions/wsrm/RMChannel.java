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

import static org.jboss.ws.extensions.wsrm.RMConstant.*;

import org.jboss.remoting.marshal.Marshaller;
import org.jboss.remoting.marshal.UnMarshaller;
import org.jboss.ws.core.MessageAbstraction;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RM Channel
 * @author richard.opalka@jboss.com
 */
public class RMChannel
{
   private static final RMChannel INSTANCE = new RMChannel();
   
   private RMChannel()
   {
      // forbidden inheritance
   }

   public static RMChannel getInstance()
   {
      return INSTANCE;
   }

   // Holds the list of tasks that will be send to the remoting transport channel
   private static final ExecutorService rmChannelPool = Executors.newFixedThreadPool(5, new RMThreadFactory());
   
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

   private RMMessage createRMMessage(MessageAbstraction request, RMMetadata rmMetadata) throws Throwable
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      Marshaller marshaller = (Marshaller)rmMetadata.getContext(SERIALIZATION_CONTEXT).get(MARSHALLER);
      // we have to serialize message before putting it to the rm pool
      //  * contextClassloader not serializable issue
      //  * DOMUtil threadlocal issue (if message is de/serialized in separate thread)
      marshaller.write(request, baos);
      RMMessage rmMessage = RMMessageFactory.newMessage(baos.toByteArray(), rmMetadata);
      return rmMessage;
   }
   
   private MessageAbstraction createResponse(RMMessage rmResponse, RMMetadata rmMetadata) throws Throwable
   {
      Map<String, Object> invocationContext = rmMetadata.getContext(INVOCATION_CONTEXT);
      boolean oneWay = (Boolean)rmMetadata.getContext(INVOCATION_CONTEXT).get(ONE_WAY_OPERATION);
      MessageAbstraction response = null;
      //if (!oneWay)
      {
         byte[] payload = rmResponse.getPayload();
         InputStream is = payload == null ? null : new ByteArrayInputStream(rmResponse.getPayload()); 
         // we have to deserialize message after pick up from the rm pool
         //  * contextClassloader not serializable issue
         //  * DOMUtil threadlocal issue (if message is de/serialized in separate thread)
         UnMarshaller unmarshaller = (UnMarshaller)rmMetadata.getContext(SERIALIZATION_CONTEXT).get(UNMARSHALLER);
         response = (MessageAbstraction)unmarshaller.read(is, rmResponse.getMetadata().getContext(REMOTING_INVOCATION_CONTEXT));
      }
      invocationContext.clear();
      invocationContext.putAll(rmMetadata.getContext(REMOTING_INVOCATION_CONTEXT));
      return response;
   }
   
   public MessageAbstraction send(MessageAbstraction request, RMMetadata rmMetadata) throws Throwable
   {
      RMMessage rmRequest = createRMMessage(request, rmMetadata);
      RMMessage rmResponse = sendToChannel(rmRequest);
      return createResponse(rmResponse, rmMetadata);
   }
   
   private RMMessage sendToChannel(RMMessage request) throws Throwable
   {
      RMChannelResponse result = rmChannelPool.submit(new RMChannelRequest(request)).get();

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
