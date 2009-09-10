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
import org.jboss.ws.core.client.UnMarshaller;
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
   private FutureResult future;

   public WSResponseHandler(UnMarshaller unmarshaller)
   {
      super();
      this.unmarshaller = unmarshaller;
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

         Map<String, Object> responseHeaders = result.getResponseHeaders();
         responseHeaders.put(NettyClient.PROTOCOL, response.getProtocolVersion());
         responseHeaders.put(NettyClient.RESPONSE_CODE, response.getStatus().getCode());
         responseHeaders.put(NettyClient.RESPONSE_CODE_MESSAGE, response.getStatus().getReasonPhrase());
         for (String headerName : response.getHeaderNames())
         {
            responseHeaders.put(headerName, response.getHeaders(headerName));
         }

         ChannelBuffer content = response.getContent();
         result.setResponseMessage(unmarshaller.read(content.readable() ? new ChannelBufferInputStream(content) : null, responseHeaders));
      }
      catch (Throwable t)
      {
         result.setError(t);
      }
      finally
      {
         DOMUtils.clearThreadLocals();
         ThreadLocalAssociation.clear();
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
      public Object getResponseMessage();
      
      public Map<String, Object> getResponseHeaders();
      
      public Throwable getError();
   }
   
   private class ResultImpl implements Result
   {
      private Object responseMessage;
      private Map<String, Object> responseHeaders = new HashMap<String, Object>();
      private Throwable error;
      
      public Object getResponseMessage()
      {
         return responseMessage;
      }
      public void setResponseMessage(Object responseMessage)
      {
         this.responseMessage = responseMessage;
      }
      public Map<String, Object> getResponseHeaders()
      {
         return responseHeaders;
      }
      public void setResponseHeaders(Map<String, Object> responseHeaders)
      {
         this.responseHeaders = responseHeaders;
      }
      public Throwable getError()
      {
         return error;
      }
      public void setError(Throwable error)
      {
         this.error = error;
      }
   }
}