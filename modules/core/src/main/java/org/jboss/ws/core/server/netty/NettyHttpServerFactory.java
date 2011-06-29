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
package org.jboss.ws.core.server.netty;

import java.util.HashMap;
import java.util.ResourceBundle;
import org.jboss.ws.api.util.BundleUtils;
import java.util.Map;

/**
 * Netty based http server factory.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class NettyHttpServerFactory
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(NettyHttpServerFactory.class);

   /**
    * Servers registry.
    */
   static final Map<Integer, NettyHttpServer> SERVERS = new HashMap<Integer, NettyHttpServer>();

   /**
    * Constructor.
    */
   private NettyHttpServerFactory()
   {
      super();
   }

   /**
    * Starts Netty based http server on the background if method is called for the first time,
    * otherwise returns already existing and running server instance.
    * 
    * @param port server port
    * @param requestHandlerFactory request handle factory
    * @param <T> factory type
    * @return running Netty based http server
    */
   public static <T extends NettyRequestHandlerFactory<?>> NettyHttpServer getNettyHttpServer(final int port,
         final T requestHandlerFactory)
   {
      if (port <= 0)
      {
         throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "NOT_POSITIVE_PORT_VALUE"));
      }
      if (requestHandlerFactory == null)
      {
         throw new NullPointerException(BundleUtils.getMessage(bundle, "FACTORY_CANNOT_BE_NULL"));
      }

      synchronized (NettyHttpServerFactory.SERVERS)
      {
         NettyHttpServer server = NettyHttpServerFactory.SERVERS.get(port);
         if (server == null)
         {
            server = new NettyHttpServerImpl(port, requestHandlerFactory);
            NettyHttpServerFactory.SERVERS.put(port, server);
         }
         return server;
      }
   }

}
