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
package org.jboss.ws.extensions.wsrm.transport.backchannel;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.logging.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.ws.extensions.wsrm.transport.RMMessage;
import org.jboss.ws.extensions.wsrm.transport.RMUnMarshaller;

/**
 * TODO: Add comment
 *
 * @author richard.opalka@jboss.com
 * @author alessio.soldano@jboss.com
 *
 * @since Nov 20, 2007
 */
@ChannelPipelineCoverage("one")
public final class RMBackPortsInvocationHandler extends SimpleChannelUpstreamHandler
{
   private static final Logger LOG = Logger.getLogger(RMBackPortsInvocationHandler.class);
   private final List<RMCallbackHandler> callbacks = new LinkedList<RMCallbackHandler>();
   private final Lock lock = new ReentrantLock();


   public RMBackPortsInvocationHandler()
   {
   }
   
   @Override
   public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
   {
      // HERE: Add all accepted channels to the group
      //       so that they are closed properly on shutdown
      //       If the added channel is closed before shutdown,
      //       it will be removed from the group automatically.
      RMBackPortsServer.channelGroup.add(ctx.getChannel());
   } 

   @Override
   public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception
   {
      HttpRequest request = (HttpRequest)e.getMessage();
      ChannelBuffer content = request.getContent();

      Map<String, Object> requestHeaders = new HashMap<String, Object>();
      for (String headerName : request.getHeaderNames())
      {
         requestHeaders.put(headerName, request.getHeaders(headerName));
      }
      boolean error = false;
      try
      {
         String requestPath = new URL(request.getUri()).getPath();
         RMMessage message = (RMMessage)RMUnMarshaller.getInstance().read(content.readable() ? new ChannelBufferInputStream(content) : null, requestHeaders);
         handle(requestPath, message);
      }
      catch (Throwable t)
      {
         error = true;
         LOG.error("Error decoding request to the backport", t);
      }
      finally
      {
         writeResponse(e, request, error);
      }
   }
   
   
   private void handle(String requestPath, RMMessage message)
   {
      this.lock.lock();
      try
      {
         boolean handlerExists = false;
         for (RMCallbackHandler handler : this.callbacks)
         {
            if (handler.getHandledPath().equals(requestPath))
            {
               handlerExists = true;
               if (LOG.isDebugEnabled())
                  LOG.debug("Handling request path: " + requestPath);
               handler.handle(message);
               break;
            }
         }
         if (handlerExists == false)
            LOG.warn("No callback handler registered for path: " + requestPath);
      }
      finally
      {
         this.lock.unlock();
      }
   }
   
   private void writeResponse(MessageEvent e, HttpRequest request, boolean error)
   {
      // Build the response object.
      HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, error ? HttpResponseStatus.INTERNAL_SERVER_ERROR : HttpResponseStatus.NO_CONTENT);
      response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");

      String cookieString = request.getHeader(HttpHeaders.Names.COOKIE);
      if (cookieString != null)
      {
         CookieDecoder cookieDecoder = new CookieDecoder();
         Set<Cookie> cookies = cookieDecoder.decode(cookieString);
         if (!cookies.isEmpty())
         {
            // Reset the cookies if necessary.
            CookieEncoder cookieEncoder = new CookieEncoder(true);
            for (Cookie cookie : cookies)
            {
               cookieEncoder.addCookie(cookie);
            }
            response.addHeader(HttpHeaders.Names.SET_COOKIE, cookieEncoder.encode());
         }
      }

      // Write the response.
      ChannelFuture cf = e.getChannel().write(response);
      cf.awaitUninterruptibly();
   }

   @Override
   public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception
   {
      e.getCause().printStackTrace();
      e.getChannel().close();
   }

   public RMCallbackHandler getCallback(String requestPath)
   {
      this.lock.lock();
      try
      {
         for (RMCallbackHandler handler : this.callbacks)
         {
            if (handler.getHandledPath().equals(requestPath))
               return handler;
         }
      }
      finally
      {
         this.lock.unlock();
      }

      return null;
   }

   public void registerCallback(RMCallbackHandler callbackHandler)
   {
      this.lock.lock();
      try
      {
         this.callbacks.add(callbackHandler);
      }
      finally
      {
         this.lock.unlock();
      }
   }

   public void unregisterCallback(RMCallbackHandler callbackHandler)
   {
      this.lock.lock();
      try
      {
         this.callbacks.remove(callbackHandler);
      }
      finally
      {
         this.lock.unlock();
      }
   }
}
