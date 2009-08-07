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
package org.jboss.ws.core.client;

import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.ws.core.utils.ThreadLocalAssociation;
import org.jboss.wsf.common.DOMUtils;

/**
 * A Netty channel upstream handler that receives MessageEvent
 * and extract the JBossWS message using the provided unmarshaller.
 * 
 * @author alessio.soldano@jboss.com
 * @since 24-Jun-2009
 *
 */
@ChannelPipelineCoverage("one")
public class WSResponseHandler extends SimpleChannelUpstreamHandler
{
   private UnMarshaller unmarshaller;
   private Object responseMessage;
   private Map<String, Object> responseHeaders;
   private Throwable error;

   public WSResponseHandler(UnMarshaller unmarshaller)
   {
      super();
      this.unmarshaller = unmarshaller;
   }

   @Override
   public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception
   {
      try
      {
         reset();
         HttpResponse response = (HttpResponse)e.getMessage();

         responseHeaders.put(NettyClient.RESPONSE_CODE, response.getStatus().getCode());
         responseHeaders.put(NettyClient.RESPONSE_CODE_MESSAGE, response.getStatus().getReasonPhrase());
         for (String headerName : response.getHeaderNames())
         {
            responseHeaders.put(headerName, response.getHeaders(headerName));
         }

         ChannelBuffer content = response.getContent();
         this.responseMessage = unmarshaller.read(content.readable() ? new ChannelBufferInputStream(content) : null, responseHeaders);
      }
      catch (Throwable t)
      {
         this.error = t;
      }
      finally
      {
         DOMUtils.clearThreadLocals();
         ThreadLocalAssociation.clear();
         e.getChannel().close();
      }
   }
   
   @Override
   public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception
   {
      if (this.error != null)
      {
         this.error = e.getCause();
      }
   }
   
   private void reset()
   {
      this.error = null;
      this.responseMessage = null;
      this.responseHeaders = new HashMap<String, Object>();
   }

   public Object getResponseMessage()
   {
      return this.responseMessage;
   }

   public Map<String, Object> getResponseHeaders()
   {
      return responseHeaders;
   }

   public Throwable getError()
   {
      return error;
   }
}