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

import static org.jboss.netty.channel.Channels.pipeline;

import java.util.Collection;
import java.util.Map;
import java.util.ResourceBundle;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.core.WSTimeoutException;

/**
 * Helper for dealing with Netty channels
 * 
 * @author alessio.soldano@jboss.com
 * @since 08-Sep-2009
 *
 */
public class NettyHelper
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(NettyHelper.class);
   public static final String RESPONSE_HANDLER_NAME = "handler";
   
   public static ChannelPipelineFactory getChannelPipelineFactory()
   {
      return getChannelPipelineFactory(null);
   }
   
   public static ChannelPipelineFactory getChannelPipelineFactory(SslHandler sslHandler)
   {
      BasicPipelineFactory factory = new BasicPipelineFactory();
      if (sslHandler != null)
      {
         factory.setSslHandler(sslHandler);
      }
      return factory;
   }
   
   public static void setResponseHandler(Channel channel, WSResponseHandler responseHandler)
   {
      ChannelPipeline pipeline = channel.getPipeline();
      pipeline.addLast(RESPONSE_HANDLER_NAME, responseHandler);
   }
   
   public static void clearResponseHandler(Channel channel)
   {
      ChannelPipeline pipeline = channel.getPipeline();
      pipeline.remove(RESPONSE_HANDLER_NAME);
   }
   
   /**
    * Utility method for awaiting with or without timeout (timeout == null or <=0 implies not timeout)
    * 
    * @param future
    * @param timeout
    * @throws WSTimeoutException
    */
   public static void awaitUninterruptibly(ChannelFuture future, Long timeout) throws WSTimeoutException
   {
      if (timeout != null && timeout.longValue() > 0)
      {
         boolean bool = future.awaitUninterruptibly(timeout);
         if (!bool)
         {
            throw new WSTimeoutException(BundleUtils.getMessage(bundle, "TIMEOUT"),  timeout);
         }
      }
      else
      {
         future.awaitUninterruptibly();
      }
   }
   
   private static class BasicPipelineFactory implements ChannelPipelineFactory
   {
      private static final int MAX_CONTENT_SIZE = 1073741824;
      private ChannelHandler sslHandler;
      
      public ChannelPipeline getPipeline() throws Exception
      {
         // Create a default pipeline implementation.
         ChannelPipeline pipeline = pipeline();
         
         if (sslHandler != null)
         {
            pipeline.addLast("ssl", sslHandler);
         }
         pipeline.addLast("decoder", new HttpResponseDecoder());
         // Uncomment the following line if you don't want to handle HttpChunks.
         pipeline.addLast("aggregator", new HttpChunkAggregator(MAX_CONTENT_SIZE));
         pipeline.addLast("encoder", new HttpRequestEncoder());
         return pipeline;
      }

      public ChannelHandler getSslHandler()
      {
         return sslHandler;
      }

      public void setSslHandler(ChannelHandler sslHandler)
      {
         this.sslHandler = sslHandler;
      }
   }
   
   public static String getFirstHeaderAsString(Map<String, Object> headers, String name)
   {
      Object obj = headers.get(name);
      if (obj == null) return null;
      if (obj instanceof Collection<?>)
      {
         Object value = ((Collection<?>)obj).iterator().next();
         return (value != null) ? value.toString() : null;
      }
      else
      {
         return obj.toString();
      }
   }
}
