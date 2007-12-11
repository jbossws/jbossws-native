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

import org.jboss.logging.Logger;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.invocation.Invocation;
import org.jboss.wsf.spi.invocation.InvocationHandler;

/**
 * RM Invocation Handler 
 *
 * @author richard.opalka@jboss.com
 *
 * @since Dec 11, 2007
 */
public final class RMInvocationHandler extends InvocationHandler
{

   private static final Logger logger = Logger.getLogger(RMInvocationHandler.class);
   
   private final InvocationHandler delegate;
   
   RMInvocationHandler(InvocationHandler delegate)
   {
      this.delegate = delegate;
   }
   
   @Override
   public final Invocation createInvocation()
   {
      return this.delegate.createInvocation();
   }

   @Override
   public final void handleInvocationException(Throwable th) throws Exception
   {
      // TODO is it necessary to handle it specially in the case of WS-RM ?
      super.handleInvocationException(th);
   }

   @Override
   public final void init(Endpoint ep)
   {
      this.delegate.init(ep);
   }

   @Override
   public final void invoke(Endpoint ep, Invocation inv) throws Exception
   {
      // TODO: do WS-RM magic here (such as create, close or terminate sequence
      if (inv.getJavaMethod() != null)
      {
         logger.debug("Invoking method: " + inv.getJavaMethod().getName());
         this.delegate.invoke(ep, inv);
      }
      else
      {
         logger.debug("RM protocol method detected");
      }
   }
   
   public final InvocationHandler getDelegate()
   {
      return this.delegate;
   }

}
