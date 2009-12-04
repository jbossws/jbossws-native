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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpVersion;

/**
 * A Netty channel upstream handler that receives MessageEvent
 * and sends data back to the NettyClient.
 * 
 * @author alessio.soldano@jboss.com
 * @since 24-Jun-2009
 *
 */
@ChannelPipelineCoverage("one")
public class WSResponseHandler extends SimpleChannelUpstreamHandler
{
   private FutureResult future;

   public WSResponseHandler()
   {
      super();
      this.future = new FutureResult();
   }

   @Override
   public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception
   {
      if (future.isCancelled())
      {
         return;
      }
      future.start();
      ResultImpl result = new ResultImpl();
      future.setResult(result);
      try
      {
         HttpResponse response = (HttpResponse)e.getMessage();

         Map<String, Object> metadata = result.getMetadata();
         metadata.put(NettyClient.PROTOCOL, response.getProtocolVersion());
         metadata.put(NettyClient.RESPONSE_CODE, response.getStatus().getCode());
         metadata.put(NettyClient.RESPONSE_CODE_MESSAGE, response.getStatus().getReasonPhrase());
         Map<String, Object> responseHeaders = result.getResponseHeaders();
         for (String headerName : response.getHeaderNames())
         {
            responseHeaders.put(headerName, response.getHeaders(headerName));
         }

         ChannelBuffer content = response.getContent();
         result.setResponse(new ChannelBufferInputStream(content));
      }
      catch (Throwable t)
      {
         future.setException(t);
      }
      finally
      {
         future.done();
      }
   }
   
   @Override
   public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception
   {
      if (future.isCancelled())
      {
         return;
      }
      future.start();
      future.setException(e.getCause());
      future.done();
   }
   
   public Future<Result> getFutureResult()
   {
      return future;
   }
   
   private class FutureResult implements Future<Result>
   {
      protected Result result;
      protected Throwable exception;
      protected boolean done = false;
      protected boolean cancelled = false;
      protected boolean started = false;

      public FutureResult()
      {
      }

      public void setResult(Result result)
      {
         this.result = result;
      }
      
      public void setException(Throwable exception)
      {
         this.exception = exception;
      }
      
      public void start()
      {
         started = true;
      }

      public void done()
      {
         done = true;
         synchronized (this)
         {
            notifyAll();
         }
      }

      public boolean cancel(boolean mayInterruptIfRunning)
      {
         if (!started)
         {
            cancelled = true;
            synchronized (this)
            {
               notifyAll();
            }
            return true;
         }
         return false;
      }

      public Result get() throws InterruptedException, ExecutionException
      {
         synchronized (this)
         {
            if (!done)
            {
               wait();
            }
         }
         if (cancelled)
         {
            throw new InterruptedException("Operation Cancelled");
         }
         if (exception != null)
         {
            throw new ExecutionException(exception);
         }
         return result;
      }

      public Result get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
      {
         synchronized (this)
         {
            if (!done)
            {
               unit.timedWait(this, timeout);
            }
         }
         if (cancelled)
         {
            throw new InterruptedException("Operation Cancelled");
         }
         if (!done)
         {
            throw new TimeoutException("Timeout Exceeded");
         }
         if (exception != null)
         {
            throw new ExecutionException(exception);
         }
         return result;
      }

      public boolean isCancelled()
      {
         return cancelled;
      }

      public boolean isDone()
      {
         return done;
      }
   }
   
   public interface Result
   {
      public InputStream getResponse();
      
      public Map<String, Object> getResponseHeaders();
      
      public Map<String, Object> getMetadata();
   }
   
   private class ResultImpl implements Result
   {
      private InputStream is;
      private Map<String, Object> responseHeaders = new HashMap<String, Object>();
      private Map<String, Object> metadata = new HashMap<String, Object>();
      
      public InputStream getResponse()
      {
         return is;
      }
      public void setResponse(InputStream is)
      {
         this.is = is;
      }
      public Map<String, Object> getResponseHeaders()
      {
         return responseHeaders;
      }
      public void setResponseHeaders(Map<String, Object> responseHeaders)
      {
         this.responseHeaders = responseHeaders;
      }
      public Map<String, Object> getMetadata()
      {
         return metadata;
      }
   }
}