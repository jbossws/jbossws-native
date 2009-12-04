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
package org.jboss.ws.core.client.transport;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.security.AccessController;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.jboss.logging.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.ws.Constants;
import org.jboss.wsf.spi.util.ServiceLoader;

/**
 * This handles the Netty channels, allowing for a
 * keep-alive system.
 * 
 * @author alessio.soldano@jboss.com
 * @since 08-Sep-2009
 *
 */
public class NettyTransportHandler
{
   private static Logger log = Logger.getLogger(NettyTransportHandler.class);
   private static final int DEFAULT_KEEP_ALIVE_CONS = 5;
   
   private URL url;
   private ChannelFuture connectFuture;
   private Channel channel;
   private static ClientSocketChannelFactoryProvider factoryProvider;
   //the idle cache
   private static KeepAliveCache cache = new KeepAliveCache();
   private static boolean keepAliveProp = true;
   
   //whether this is currently in the idle cache
   private boolean inCache;
   //whether this is a keep-alive-connection
   private volatile boolean keepingAlive = true;
   //the number of keep-alive connections left
   private int keepAliveConnections = DEFAULT_KEEP_ALIVE_CONS;
   //the keep alive timeout in seconds
   private int keepAliveTimeout;
   
   static
   {
      String keepAlive = AccessController.doPrivileged(new java.security.PrivilegedAction<String>() {
         public String run()
         {
            return System.getProperty(Constants.HTTP_KEEP_ALIVE);
         }
      });
      if (keepAlive != null)
      {
         keepAliveProp = Boolean.valueOf(keepAlive).booleanValue();
      }
      else
      {
         keepAliveProp = true;
      }
      factoryProvider = (ClientSocketChannelFactoryProvider)ServiceLoader.loadService(ClientSocketChannelFactoryProvider.class.getName(),
            DefaultClientSocketChannelFactoryProvider.class.getName());
   }

   private NettyTransportHandler(URL url, ChannelPipelineFactory pipelineFactory)
   {
      this.url = url;
      ChannelFactory factory = factoryProvider.getClientSocketChannelFactoryInstance();
      ClientBootstrap bootstrap = new ClientBootstrap(factory);
      bootstrap.setPipelineFactory(pipelineFactory);
      
      //Start the connection attempt
      bootstrap.setOption("tcpNoDelay", true);
      connectFuture = bootstrap.connect(getSocketAddress(url));
   }
   
   public static NettyTransportHandler getInstance(URL url, ChannelPipelineFactory pipelineFactory)
   {
      return getInstance(url, true, pipelineFactory);
   }
   
   public static NettyTransportHandler getInstance(URL url, boolean useCache, ChannelPipelineFactory pipelineFactory)
   {
      NettyTransportHandler ret = null;
      if (useCache && keepAliveProp)
      {
         ret = cache.get(url);
         if (ret != null)
         {
            synchronized (ret)
            {
               ret.inCache = false;
            }
         }
      }
      if (ret == null)
      {
         ret = new NettyTransportHandler(url, pipelineFactory);
      }
      else
      {
         SecurityManager security = System.getSecurityManager();
         if (security != null)
         {
            security.checkConnect(url.getHost(), url.getPort());
         }
         ret.url = url;
      }
      return ret;
   }
   
   /**
    * Get the channel for using the http connection
    * 
    * @return A Netty channel
    * @throws IOException
    */
   public Channel getChannel() throws ConnectException
   {
      return getChannel(null);
   }
   
   /**
    * Get the channel for using the http connection
    * 
    * @param timeout
    * @return A Netty channel
    * @throws IOException
    */
   public Channel getChannel(Long timeout) throws ConnectException
   {
      if (channel == null && connectFuture != null) //first call after connection attempt
      {
         NettyHelper.awaitUninterruptibly(connectFuture, timeout);
         if (!connectFuture.isSuccess())
         {
            ConnectException ce = new ConnectException("Could not connect to " + url.getHost());
            ce.initCause(connectFuture.getCause());
            throw ce;
         }
         channel = connectFuture.getChannel();
      }
      return channel;
   }
   
   /**
    * Signal an invocation has been done using the current connection,
    * but we're going on using it.
    * 
    * @param headers The received message's headers
    */
   public void goOn(Map<String, Object> metadata, Map<String, Object> headers)
   {
      checkKeepAliveHeaders(metadata, headers);
   }
   
   
   /**
    * Signal end of processing for the current connection. The transport
    * handler either goes to the idle cache or is closed depending on
    * the number of keep alive connections left (and this being a
    * keep-alive connection or not, of course)
    *
    * @param headers The received message's headers
    * 
    */
   public void finished(Map<String, Object> metadata, Map<String, Object> headers)
   {
      checkKeepAliveHeaders(metadata, headers);
      keepAliveConnections--;
      if (keepAliveConnections > 0 && isKeepingAlive())
      {
         /* This connection is keepingAlive && still valid.
          * Return it to the cache.
          */
         putInKeepAliveCache();
      }
      else
      {
         end();
      }
   }
   
   /**
    * Update the keep-alive status according to the received message's headers.
    * 
    * @param headers The received message's headers
    */
   protected void checkKeepAliveHeaders(Map<String, Object> metadata, Map<String, Object> headers)
   {
      if (headers == null || metadata == null) return;
      keepAliveConnections = -1;
      keepAliveTimeout = 0;
      try
      {
         String connectionHeader = NettyHelper.getFirstHeaderAsString(headers, HttpHeaders.Names.CONNECTION);
         if (connectionHeader != null && connectionHeader.equalsIgnoreCase(HttpHeaders.Values.KEEP_ALIVE))
         {
            //support for old servers (out of spec but quite used)
            String keepAliveHeader = NettyHelper.getFirstHeaderAsString(headers, "Keep-Alive");
            if (keepAliveHeader != null)
            {
               StringTokenizer st = new StringTokenizer(keepAliveHeader, ", ", false);
               while (st.hasMoreTokens())
               {
                  keepAliveTimeout = 5;
                  keepAliveConnections = DEFAULT_KEEP_ALIVE_CONS;
                  String s = st.nextToken();
                  if (s.startsWith("timeout="))
                  {
                     keepAliveTimeout = Integer.parseInt(s.substring(8));
                  }
                  if (s.startsWith("max="))
                  {
                     keepAliveConnections = Integer.parseInt(s.substring(4));
                  }
               }
            }
         }
         else if (HttpVersion.HTTP_1_1.toString().equals(NettyHelper.getFirstHeaderAsString(metadata, NettyClient.PROTOCOL)))
         {
            //Consider the only valid value for Connection in responses is "close" 
            keepAliveConnections = (connectionHeader == null) ? DEFAULT_KEEP_ALIVE_CONS : 1;
         }
         
         if (keepAliveConnections <=1)
            keepingAlive = false;
      }
      catch (Exception ex)
      {
         log.error("Error while parsing headers for configuring keep-alive, closing connection. ", ex);
         keepAliveConnections = -1;
         keepingAlive = false;
      }
   }

   protected synchronized void putInKeepAliveCache()
   {
      if (inCache)
      {
         return;
      }
      inCache = true;
      cache.put(url, this);
   }
   
   private InetSocketAddress getSocketAddress(URL target)
   {
      int port = target.getPort();
      if (port < 0)
      {
         //use default port
         String protocol = target.getProtocol();
         if ("http".equalsIgnoreCase(protocol))
         {
            port = 80;
         }
         else if ("https".equalsIgnoreCase(protocol))
         {
            port = 443;
         }
      }
      return new InetSocketAddress(target.getHost(), port);
   }
   
   /**
    * End the transport handler, closing the underlying connection.
    * 
    */
   public void end()
   {
      keepingAlive = false;
      if (channel == null)
      {
         channel = connectFuture.getChannel();
         connectFuture.cancel();
      }
      if (channel != null)
      {
         channel.close();
      }
   }
   
   public static boolean getHttpKeepAliveSet()
   {
      return keepAliveProp;
   }

   protected boolean isKeepingAlive()
   {
      return getHttpKeepAliveSet() && keepingAlive;
   }

   public int getKeepAliveTimeout()
   {
      return keepAliveTimeout;
   }
   
   public URL getUrl()
   {
      return url;
   }

   public void setKeepAliveConnections(int keepAliveConnections)
   {
      this.keepAliveConnections = keepAliveConnections;
   }
   
}
