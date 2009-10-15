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

/**
 * Netty Http Server abstraction.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public interface NettyHttpServer
{

   /**
    * Registers new callback.
    *
    * @param callback netty callback
    */
   void registerCallback(NettyCallbackHandler callback);

   /**
    * Registers old callback.
    *
    * @param callback netty callback
    */
   void unregisterCallback(NettyCallbackHandler callback);

   /**
    * Returns registered callback associated with request path.
    *
    * @param requestPath request path to get associated callback for
    * @return callback handler
    */
   NettyCallbackHandler getCallback(String requestPath);

   /**
    * Returns true if server has some callbacks registered, false otherwise.
    *
    * @return true if callbacks are available, false otherwise
    */
   boolean hasMoreCallbacks();

   /**
    * Returns port this server runs on.
    *
    * @return server port
    */
   int getPort();

}
