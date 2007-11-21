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
package org.jboss.ws.extensions.wsrm.backchannel;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.MBeanServer;

import org.jboss.logging.Logger;
import org.jboss.remoting.InvocationRequest;
import org.jboss.remoting.ServerInvocationHandler;
import org.jboss.remoting.ServerInvoker;
import org.jboss.remoting.callback.InvokerCallbackHandler;
import org.jboss.remoting.transport.coyote.RequestMap;
import org.jboss.remoting.transport.http.HTTPMetadataConstants;
import org.jboss.ws.extensions.wsrm.client_api.RMException;

/**
 * TODO: Add comment
 *
 * @author richard.opalka@jboss.com
 *
 * @since Nov 20, 2007
 */
public final class RMBackPortsInvocationHandler implements ServerInvocationHandler
{
   private static final Logger LOG = Logger.getLogger(RMBackPortsInvocationHandler.class);
   private final List<CallbackHandler> callbacks = new LinkedList<CallbackHandler>();
   private final Lock lock = new ReentrantLock();
   
   public RMBackPortsInvocationHandler()
   {
   }
   
   public CallbackHandler getCallback(String requestPath)
   {
      this.lock.lock();
      try
      {
         for (CallbackHandler handler : this.callbacks)
         {
            if (handler.getHandledPath().equals(requestPath))
               return handler;
         }
      }
      finally
      {
         this.lock.unlock();
      }

      return null;
   }

   public void registerCallback(CallbackHandler callbackHandler)
   {
      this.lock.lock();
      try
      {
         this.callbacks.add(callbackHandler);
      }
      finally
      {
         this.lock.unlock();
      }
   }

   public void unregisterCallback(CallbackHandler callbackHandler)
   {
      this.lock.lock();
      try
      {
         this.callbacks.remove(callbackHandler);
      }
      finally
      {
         this.lock.unlock();
      }
   }

   public Object invoke(InvocationRequest request) throws Throwable
   {
      this.lock.lock();
      try
      {
         RequestMap rm = (RequestMap)request.getRequestPayload();
         String requestPath = (String)rm.get(HTTPMetadataConstants.PATH);
         boolean handlerExists = false;
         for (CallbackHandler handler : this.callbacks)
         {
            if (handler.getHandledPath().equals(requestPath))
            {
               handlerExists = true;
               LOG.debug("Handling request path: " + requestPath);
               handler.handle(request);
               break;
            }
         }
         if (handlerExists == false)
            LOG.warn("No callback handler registered for path: " + requestPath);

         return null;
      }
      finally
      {
         this.lock.unlock();
      }
   }

   public void addListener(InvokerCallbackHandler callbackHandler)
   {
      // do nothing - we're using custom callback handlers
   }

   public void removeListener(InvokerCallbackHandler callbackHandler)
   {
      // do nothing - we're using custom callback handlers
   }
   
   public void setInvoker(ServerInvoker arg0)
   {
      // do nothing
   }

   public void setMBeanServer(MBeanServer arg0)
   {
      // do nothing
   }
   
}
