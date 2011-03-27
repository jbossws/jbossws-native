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
package org.jboss.test.ws.core.client;

import java.net.SocketAddress;
import java.util.List;
import java.util.Map;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.ws.core.client.transport.NettyClient;
import org.jboss.ws.core.client.transport.WSResponseHandler;
import org.jboss.ws.core.client.transport.WSResponseHandler.Result;
import org.jboss.wsf.test.JBossWSTest;

/**
 * 
 * @author alessio.soldano@jboss.com
 * @since 03-Dic-2009
 *
 */
public class WSResponseHandlerTestCase extends JBossWSTest
{

   public void testJBWS2849() throws Exception
   {
      WSResponseHandler handler = new WSResponseHandler();
      ChannelHandlerContext ctx = new MyContext();
      MyMessageEvent e = new MyMessageEvent();
      HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
      response.setHeader(NettyClient.RESPONSE_CODE, "123456");
      e.setMessage(response);
      handler.messageReceived(ctx, e);
      Result result = handler.getFutureResult().get();
      Map<String, Object> headers = result.getResponseHeaders();
      assertEquals("123456", ((List<?>)headers.get(NettyClient.RESPONSE_CODE)).iterator().next());
      assertEquals(200, result.getMetadata().get(NettyClient.RESPONSE_CODE));
   }

   private class MyMessageEvent implements MessageEvent
   {

      private HttpResponse message;

      public Object getMessage()
      {
         return message;
      }

      public void setMessage(HttpResponse message)
      {
         this.message = message;
      }

      public SocketAddress getRemoteAddress()
      {
         return null;
      }

      public Channel getChannel()
      {
         return null;
      }

      public ChannelFuture getFuture()
      {
         return null;
      }

   }

   private class MyContext implements ChannelHandlerContext
   {
      public boolean canHandleDownstream()
      {
         return false;
      }

      public boolean canHandleUpstream()
      {
         return false;
      }

      public Object getAttachment()
      {
         return null;
      }

      public Channel getChannel()
      {
         return null;
      }

      public ChannelHandler getHandler()
      {
         return null;
      }

      public String getName()
      {
         return null;
      }

      public ChannelPipeline getPipeline()
      {
         return null;
      }

      public void sendDownstream(ChannelEvent e)
      {
      }

      public void sendUpstream(ChannelEvent e)
      {
      }

      public void setAttachment(Object attachment)
      {
      }

   }
}
