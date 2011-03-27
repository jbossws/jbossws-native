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

import java.util.LinkedList;
import java.util.List;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

/**
 * Abstract Netty request handler.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 * @author <a href="mailto:asoldano@redhat.com">Alessio Soldano</a>
 */
@ChannelPipelineCoverage("one")
public abstract class AbstractNettyRequestHandler extends SimpleChannelUpstreamHandler
{

   /** Callbacks registry. */
   private final List<NettyCallbackHandler> callbacks = new LinkedList<NettyCallbackHandler>();

   /**
    * Constructor.
    */
   protected AbstractNettyRequestHandler()
   {
      super();
   }

   /**
    * Template method implementation.
    * 
    * @param ctx channel handler context
    * @param e channel state event
    */
   @Override
   public final void channelOpen(final ChannelHandlerContext ctx, final ChannelStateEvent e)
   {
      // HERE: Add all accepted channels to the group
      //       so that they are closed properly on shutdown
      //       If the added channel is closed before shutdown,
      //       it will be removed from the group automatically.
      NettyHttpServerImpl.NETTY_CHANNEL_GROUP.add(ctx.getChannel());
   }

   /**
    * Template method implementation.
    * 
    * @param ctx channel handler context
    * @param e exception event
    * @throws Exception if some error occurs
    */
   @Override
   public final void exceptionCaught(final ChannelHandlerContext ctx, final ExceptionEvent e) throws Exception
   {
      e.getCause().printStackTrace();
      e.getChannel().close();
   }

   /**
    * Template method for processing incoming requests.
    * 
    * @param ctx channel handler context
    * @param e message event
    * @throws Exception if some error occurs
    */
   @Override
   public abstract void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception;

   /**
    * Returns true if request handler have at least one callback registered, false otherwise.
    *
    * @return true if at least one callback is registered, false otherwise
    */
   public final boolean hasMoreCallbacks()
   {
      return this.callbacks.size() > 0;
   }

   /**
    * Returns callback handler associated with request path.
    * 
    * @param requestPath to get handler for
    * @return callback handler
    */
   public final NettyCallbackHandler getCallback(final String requestPath)
   {
      for (final NettyCallbackHandler handler : this.callbacks)
      {
         if (handler.getPath().equals(requestPath))
         {
            return handler;
         }
      }

      return null;
   }

   /**
    * Registers callback.
    *
    * @param callback netty callback
    */
   public final void registerCallback(final NettyCallbackHandler callback)
   {
      callback.init();
      this.callbacks.add(callback);
   }

   /**
    * Unregisters callback. 
    *
    * @param callback netty callback
    */
   public final void unregisterCallback(final NettyCallbackHandler callback)
   {
      this.callbacks.remove(callback);
      callback.destroy();
   }

}
