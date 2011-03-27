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

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import javax.xml.ws.WebServiceException;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.ws.core.client.transport.WSServerPipelineFactory;

/**
 * Netty http server implementation.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 * @author <a href="mailto:asoldano@redhat.com">Alessio Soldano</a>
 */
final class NettyHttpServerImpl implements NettyHttpServer, Runnable
{

   /** Wait period. */
   private static final long WAIT_PERIOD = 100;

   /** Netty channel group. */
   static final ChannelGroup NETTY_CHANNEL_GROUP = new DefaultChannelGroup("NettyHttpServer");

   /** Server port. */
   private final int port;

   /** Indicates server is stopped. */
   private boolean stopped;

   /** Channel factory. */
   private ChannelFactory channelFactory;

   /** Netty request handler. */
   private AbstractNettyRequestHandler nettyRequestHandler;

   /**
    * Constructor. This starts new http server in the background and registers shutdown hook for it.
    *
    * @param port server port
    * @param nettyRequestHandlerFactory request handler factory
    */
   NettyHttpServerImpl(final int port, final NettyRequestHandlerFactory<?> nettyRequestHandlerFactory)
   {
      super();
      this.port = port;
      try
      {
         this.channelFactory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors
               .newCachedThreadPool());

         final ServerBootstrap bootstrap = new ServerBootstrap(this.channelFactory);
         this.nettyRequestHandler = nettyRequestHandlerFactory.newNettyRequestHandler();
         final WSServerPipelineFactory channelPipelineFactory = new WSServerPipelineFactory();
         channelPipelineFactory.setRequestHandler(this.nettyRequestHandler);
         bootstrap.setPipelineFactory(channelPipelineFactory);
         bootstrap.setOption("child.tcpNoDelay", true);
         bootstrap.setOption("child.keepAlive", true);
         // Bind and start to accept incoming connections.
         final Channel c = bootstrap.bind(new InetSocketAddress(this.port));
         NettyHttpServerImpl.NETTY_CHANNEL_GROUP.add(c);
         // forking Netty server
         final Thread t = new Thread(this, "NettyHttpServer listening on port " + port);
         t.setDaemon(true);
         t.start();
         // registering shutdown hook
         final Runnable shutdownHook = new NettyHttpServerShutdownHook(this);
         Runtime.getRuntime().addShutdownHook(
               new Thread(shutdownHook, "NettyHttpServerShutdownHook(port=" + port + ")"));
      }
      catch (Exception e)
      {
         throw new WebServiceException(e.getMessage(), e);
      }
   }

   /**
    * @see NettyHttpServer#registerCallback(NettyCallbackHandler)
    * 
    * @param callback new callback
    */
   public synchronized void registerCallback(final NettyCallbackHandler callback)
   {
      if (callback != null)
      {
         this.ensureUpAndRunning();
         this.nettyRequestHandler.registerCallback(callback);
      }
   }

   /**
    * @see NettyHttpServer#unregisterCallback(NettyCallbackHandler)
    *
    * @param callback old callback
    */
   public synchronized void unregisterCallback(final NettyCallbackHandler callback)
   {
      if (callback != null)
      {
         this.ensureUpAndRunning();
         try
         {
            this.nettyRequestHandler.unregisterCallback(callback);
         }
         finally
         {
            if (!this.hasMoreCallbacks())
            {
               this.terminate();
            }
         }
      }
   }

   /**
    * @see NettyHttpServer#getCallback(String)
    *
    * @param requestPath request path
    * @return callback handler associated with path
    */
   public synchronized NettyCallbackHandler getCallback(final String requestPath)
   {
      if (requestPath == null)
      {
         throw new IllegalArgumentException("Null request path");
      }

      this.ensureUpAndRunning();
      return this.nettyRequestHandler.getCallback(requestPath);
   }
   
   /**
    * @see NettyHttpServer#hasMoreCallbacks()
    * 
    * @return true if at least one callback handler is registered, false otherwise
    */
   public synchronized boolean hasMoreCallbacks()
   {
      this.ensureUpAndRunning();
      return this.nettyRequestHandler.hasMoreCallbacks();
   }

   /**
    * @see NettyHttpServer#getPort()
    * 
    * @return server port
    */
   public synchronized int getPort()
   {
      this.ensureUpAndRunning();
      return this.port;
   }

   /**
    * Handles incomming connections.
    */
   public synchronized void run()
   {
      while (!this.stopped)
      {
         try
         {
            this.wait(NettyHttpServerImpl.WAIT_PERIOD);
         }
         catch (InterruptedException ie)
         {
            ie.printStackTrace();
         }
      }
   }

   /**
    * Terminates server instance.
    */
   public synchronized void terminate()
   {
      synchronized (NettyHttpServerFactory.SERVERS)
      {
         if (this.stopped)
         {
            return;
         }

         this.stopped = true;

         try
         {
            //Close all connections and server sockets.
            NettyHttpServerImpl.NETTY_CHANNEL_GROUP.close().awaitUninterruptibly();
            //Shutdown the selector loop (boss and worker).
            if (this.channelFactory != null)
            {
               this.channelFactory.releaseExternalResources();
            }
         }
         finally
         {
            NettyHttpServerFactory.SERVERS.remove(this.port);
         }
      }
   }

   /**
    * Ensures server is up and running.
    */
   private void ensureUpAndRunning()
   {
      if (this.stopped)
      {
         throw new IllegalStateException("Server is down");
      }
   }

}
