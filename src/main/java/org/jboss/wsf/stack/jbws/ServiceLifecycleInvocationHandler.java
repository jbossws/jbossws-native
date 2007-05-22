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
package org.jboss.wsf.stack.jbws;

// $Id$

import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;

import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.invocation.Invocation;
import org.jboss.wsf.spi.invocation.InvocationContext;
import org.jboss.wsf.spi.invocation.InvocationHandlerJSE;

/**
 * Handles invocations on JSE endpoints.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 25-Apr-2007
 */
public class ServiceLifecycleInvocationHandler extends InvocationHandlerJSE
{
   public void invoke(Endpoint ep, Object beanInstance, Invocation epInv) throws Exception
   {
      try
      {
         if (beanInstance == null)
            beanInstance = super.getBeanInstance(ep);

         InvocationContext invContext = epInv.getInvocationContext();
         if (beanInstance instanceof ServiceLifecycle)
         {
            ServletEndpointContext sepContext = invContext.getAttachment(ServletEndpointContext.class);
            if (sepContext != null)
               ((ServiceLifecycle)beanInstance).init(sepContext);
         }

         try
         {
            super.invoke(ep, beanInstance, epInv);
         }
         finally
         {
            if (beanInstance instanceof ServiceLifecycle)
            {
               ((ServiceLifecycle)beanInstance).destroy();
            }
         }
      }
      catch (Exception e)
      {
         handleInvocationException(e);
      }
   }
}
