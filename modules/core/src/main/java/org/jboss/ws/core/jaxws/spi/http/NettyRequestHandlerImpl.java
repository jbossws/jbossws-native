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
package org.jboss.ws.core.jaxws.spi.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.logging.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.ws.Constants;
import org.jboss.ws.core.server.netty.AbstractNettyRequestHandler;
import org.jboss.wsf.spi.invocation.InvocationContext;

/**
 * Netty request handler for endpoint publish API.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
final class NettyRequestHandlerImpl extends AbstractNettyRequestHandler
{
   /** Logger. */
   private static final Logger LOG = Logger.getLogger(NettyRequestHandlerImpl.class);

   /**
    * Constructor.
    */
   public NettyRequestHandlerImpl()
   {
      super();
   }

   /**
    * Callback method called by Netty HTTP server.
    * 
    * @param ctx channel handler context
    * @param event message event
    * @throws Exception
    */
   @Override
   public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent event)
   {
      // TODO: create custom HttpNettyRequest and don't use Netty provided default impl.
      final HttpRequest request = (HttpRequest) event.getMessage();
      if (HttpVersion.HTTP_1_1.equals(request.getProtocolVersion()))
      {
         final AbstractNettyMessage message = new NettyHttp11Message(ctx.getChannel(), request);

         final InvocationContext invCtx = new InvocationContext();
         invCtx.setProperty(Constants.NETTY_MESSAGE, message);
         try
         {
            final String requestPath = this.getRequestPath(request.getUri());
            final String httpMethod = request.getMethod().getName();
            this.handle(requestPath, httpMethod, message.getInputStream(), message.getOutputStream(), invCtx);
         }
         catch (Exception e)
         {
            NettyRequestHandlerImpl.LOG.error(e);
            this.sendError(event, HttpResponseStatus.INTERNAL_SERVER_ERROR);
         }
      }
      else
      {
         NettyRequestHandlerImpl.LOG.fatal("HTTP 1.0 not supported");
         this.sendError(event, HttpResponseStatus.NOT_IMPLEMENTED);
      }
   }

   /**
    * Calls Netty callback handler associated with request path.
    * 
    * @param requestPath to handle
    * @param httpMethod http method (GET or POST)
    * @param is input stream 
    * @param os output stream
    * @param invCtx invocation context
    * @throws IOException if some I/O error occurs
    */
   private void handle(final String requestPath, final String httpMethod, final InputStream is,
         final OutputStream os, final InvocationContext invCtx) throws IOException
   {
      final NettyCallbackHandlerImpl handler = (NettyCallbackHandlerImpl) this.getCallback(requestPath);
      if (handler != null)
      {
         handler.handle(httpMethod, is, os, invCtx);
      }
      else
      {
         final String errorMessage = "No callback handler registered for path: " + requestPath;
         NettyRequestHandlerImpl.LOG.warn(errorMessage);
         throw new IllegalArgumentException(errorMessage);
      }
   }

   /**
    * Sends error to HTTP client.
    * 
    * @param event message event
    * @param statusCode HTTP status code
    */
   private void sendError(final MessageEvent event, final HttpResponseStatus statusCode)
   {
      // Build the response object.
      final HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, statusCode);
      response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");

      // Write the response.
      event.getChannel().write(response).awaitUninterruptibly();
   }

   /**
    * Returns request path without query string.
    * 
    * @param requestPath to parse
    * @return request path without query string
    */
   private String getRequestPath(final String requestPath)
   {
      String retVal = requestPath;
      
      // remove query string if available
      final int paramIndex = retVal.indexOf('?');
      if (paramIndex != -1)
      {
         retVal = retVal.substring(0, paramIndex);
      }

      // remove protocol, host and port if available
      if (retVal.startsWith("http"))
      {
         try
         {
            retVal = new URL(retVal).getPath();
         }
         catch (MalformedURLException mue)
         {
            NettyRequestHandlerImpl.LOG.error(mue.getMessage(), mue);
         }
      }
      
      // remove '/' characters at the end
      while (retVal.endsWith("/"))
      {
         retVal = retVal.substring(0, retVal.length() - 1);
      }

      return retVal;
   }

}
