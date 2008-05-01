/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.wsf.stack.jbws.embedded;

import org.jboss.wsf.spi.invocation.InvocationHandler;
import org.jboss.wsf.spi.invocation.InvocationHandlerFactory;
import org.jboss.wsf.spi.invocation.InvocationType;

/**
 * @author Heiko.Braun <heiko.braun@jboss.com>
 */
public class JSEInvocationHandlerFactory extends InvocationHandlerFactory
{
   public InvocationHandler newInvocationHandler(InvocationType type)
   {
      InvocationHandler handler = null;

      switch(type)
      {
         case JAXWS_JSE:
            handler = new InvocationHandlerJSE();
            break;
      }

      if(null == handler)
         throw new IllegalArgumentException("Unable to resolve spi.invocation.InvocationHandler for type " +type);

      return handler;
   }

}

