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
package org.jboss.ws.core.client.transport;

import static org.jboss.netty.channel.Channels.pipeline;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

/**
 * 
 * @author alessio.soldano@jboss.com
 * @since 01-Jul-2009
 *
 */
public class WSServerPipelineFactory implements ChannelPipelineFactory
{
   private static final int MAX_CONTENT_SIZE = 1073741824;
   private ChannelHandler requestHandler;
   private ChannelHandler sshHandler;

   public ChannelPipeline getPipeline() throws Exception
   {
      // Create a default pipeline implementation.
      ChannelPipeline pipeline = pipeline();
      // Uncomment the following line if you want HTTPS
      //SSLEngine engine = SecureChatSslContextFactory.getServerContext().createSSLEngine();
      //engine.setUseClientMode(false);
      //pipeline.addLast("ssl", new SslHandler(engine));

      pipeline.addLast("decoder", new HttpRequestDecoder());
      // Uncomment the following line if you don't want to handle HttpChunks.
      pipeline.addLast("aggregator", new HttpChunkAggregator(MAX_CONTENT_SIZE));
      pipeline.addLast("encoder", new HttpResponseEncoder());
      pipeline.addLast("handler", requestHandler);
      return pipeline;
   }

   public ChannelHandler getRequestHandler()
   {
      return requestHandler;
   }

   public void setRequestHandler(ChannelHandler requestHandler)
   {
      this.requestHandler = requestHandler;
   }

   public ChannelHandler getSshHandler()
   {
      return sshHandler;
   }

   public void setSshHandler(ChannelHandler sshHandler)
   {
      this.sshHandler = sshHandler;
   }

}
