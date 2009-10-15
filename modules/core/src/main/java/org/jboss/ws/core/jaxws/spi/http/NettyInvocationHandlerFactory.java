/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ws.core.jaxws.spi.http;

import org.jboss.wsf.common.invocation.InvocationHandlerJAXWS;
import org.jboss.wsf.spi.invocation.InvocationHandler;
import org.jboss.wsf.spi.invocation.InvocationHandlerFactory;
import org.jboss.wsf.spi.invocation.InvocationType;

/**
 * Netty invocation handler factory that supports JAXWS JSE invocation only.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class NettyInvocationHandlerFactory extends InvocationHandlerFactory
{

   /**
    * Constructor.
    */
   public NettyInvocationHandlerFactory()
   {
      super();
   }

   /**
    * Returns invocation handler for specified invocation type.
    *
    * @param type invocation type
    * @return invocation handler
    */
   @Override
   public InvocationHandler newInvocationHandler(final InvocationType type)
   {
      InvocationHandler handler = null;

      switch (type)
      {
         case JAXWS_JSE :
            handler = new InvocationHandlerJAXWS();
            break;
         default :
            throw new IllegalArgumentException("Unable to resolve spi.invocation.InvocationHandler for type: " + type);
      }

      return handler;
   }

}
