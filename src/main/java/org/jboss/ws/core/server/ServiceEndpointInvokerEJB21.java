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
package org.jboss.ws.core.server;

// $Id$

import org.jboss.ws.core.EndpointInvocation;
import org.jboss.ws.metadata.umdm.ServerEndpointMetaData;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.invocation.Invocation;
import org.jboss.wsf.spi.invocation.HandlerCallback;
import org.jboss.wsf.spi.invocation.InvocationContext;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerMetaData.HandlerType;

/**
 * Handles invocations on EJB21 endpoints.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 25-Apr-2007
 */
public class ServiceEndpointInvokerEJB21 extends ServiceEndpointInvoker
{
   @Override
   protected Invocation setupInvocation(Endpoint ep, EndpointInvocation epInv, InvocationContext invContext) throws Exception
   {
      Invocation inv = super.setupInvocation(ep, epInv, invContext);

      // Attach the handler callback
      ServerEndpointMetaData sepMetaData = endpoint.getAttachment(ServerEndpointMetaData.class);
      invContext.addAttachment(HandlerCallback.class, new HandlerCallbackImpl(sepMetaData));

      return inv;
   }

   /** Handlers are beeing called through the HandlerCallback from the EJB interceptor */
   @Override
   public boolean callRequestHandlerChain(ServerEndpointMetaData sepMetaData, HandlerType type)
   {
      if (type == HandlerType.PRE)
         return delegate.callRequestHandlerChain(sepMetaData, type);
      else return true;
   }

   /** Handlers are beeing called through the HandlerCallback from the EJB interceptor */
   @Override
   public boolean callResponseHandlerChain(ServerEndpointMetaData sepMetaData, HandlerType type)
   {
      if (type == HandlerType.PRE)
         return delegate.callResponseHandlerChain(sepMetaData, type);
      else return true;
   }

   /** Handlers are beeing called through the HandlerCallback from the EJB interceptor */
   @Override
   public boolean callFaultHandlerChain(ServerEndpointMetaData sepMetaData, HandlerType type, Exception ex)
   {
      if (type == HandlerType.PRE)
         return delegate.callFaultHandlerChain(sepMetaData, type, ex);
      else return true;
   }

   // The ServiceEndpointInterceptor calls the methods in this callback
   public class HandlerCallbackImpl implements HandlerCallback
   {
      private ServerEndpointMetaData sepMetaData;

      public HandlerCallbackImpl(ServerEndpointMetaData sepMetaData)
      {
         this.sepMetaData = sepMetaData;
      }

      /** Handlers are beeing called through the HandlerCallback from the EJB interceptor */
      public boolean callRequestHandlerChain(HandlerType type)
      {
         if (type == HandlerType.PRE)
            return true;
         else return delegate.callRequestHandlerChain(sepMetaData, type);
      }

      /** Handlers are beeing called through the HandlerCallback from the EJB interceptor */
      public boolean callResponseHandlerChain(HandlerType type)
      {
         if (type == HandlerType.PRE)
            return true;
         else return delegate.callResponseHandlerChain(sepMetaData, type);
      }

      /** Handlers are beeing called through the HandlerCallback from the EJB interceptor */
      public boolean callFaultHandlerChain(HandlerType type, Exception ex)
      {
         if (type == HandlerType.PRE)
            return true;
         else return delegate.callFaultHandlerChain(sepMetaData, type, ex);
      }
   }
}
