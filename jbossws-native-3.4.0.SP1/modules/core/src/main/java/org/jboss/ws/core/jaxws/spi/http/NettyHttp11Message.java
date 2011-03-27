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

import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.ws.core.client.transport.NettyTransportOutputStream;

/**
 * HTTP 1.1 message.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class NettyHttp11Message extends AbstractNettyMessage
{

   /** Chunks size. */
   private static final int CHUNK_SIZE = 1024;
   /** Flag indicating HTTP headers have been written to the stream. */
   private boolean headersWritten;

   /**
    * Constructor.
    * 
    * @param channel Netty channel
    * @param request original request
    */
   public NettyHttp11Message(final Channel channel, final HttpRequest request)
   {
      super(HttpVersion.HTTP_1_1, channel, request);
   }

   /**
    * @see AbstractNettyMessage#getInputStream()
    * 
    * @return request input stream
    */
   public InputStream getInputStream()
   {
      return new ChannelBufferInputStream(this.getRequest().getContent());
   }

   /**
    * @see AbstractNettyMessage#getOutputStream()
    * 
    * @return response output stream
    */
   public OutputStream getOutputStream()
   {
      return new ChunkingOutputStream(this, new NettyTransportOutputStream(this.getChannel(), NettyHttp11Message.CHUNK_SIZE));
   }

   /**
    * Ensures HTTP message headers are written before message body.
    */
   private void flushHeaders()
   {
      if (this.headersWritten)
      {
         return;
      }

      this.headersWritten = true;
      
      if (this.getStatus() == null)
      {
         this.setStatus(HttpResponseStatus.OK.getCode());
      }
      if (this.getHeader(HttpHeaders.Names.CONTENT_TYPE) == null)
      {
         this.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/xml; charset=UTF-8");
      }
      this.setHeader(HttpHeaders.Names.TRANSFER_ENCODING, "chunked");
      this.setCookies();

      // Write the response headers
      this.getChannel().write(this);
   }
   
   /** 
    * Chunking output stream.
    *
    * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
    */
   private static final class ChunkingOutputStream extends OutputStream
   {

      /** Netty output stream. */
      private final NettyTransportOutputStream delegee;
      /** Netty message. */
      private final NettyHttp11Message message;

      /**
       * Constructor.
       * 
       * @param message wrapped netty message
       * @param os wrapped output stream
       */
      public ChunkingOutputStream(final NettyHttp11Message message, final NettyTransportOutputStream os)
      {
         super();

         this.message = message;
         this.delegee = os;
      }

      /** 
       * @see OutputStream#write(int)
       * 
       * @param b byte
       * @throws IOException if some I/O error occurs
       */
      @Override
      public void write(final int b) throws IOException
      {
         this.message.flushHeaders();
         this.delegee.write(b);
      }

      /** 
       * @see OutputStream#write(byte[], int, int)
       * 
       * @param b byte array
       * @param off offset
       * @param len length
       * @throws IOException if some I/O error occurs
       */
      @Override
      public void write(final byte[] b, final int off, final int len) throws IOException
      {
         this.message.flushHeaders();
         this.delegee.write(b, off, len);
      }

      /** 
       * @see OutputStream#write(byte[])
       * 
       * @param b byte array
       * @throws IOException if some I/O error occurs
       */
      @Override
      public void write(final byte[] b) throws IOException
      {
         this.message.flushHeaders();
         this.delegee.write(b);
      }

      /**
       * @see OutputStream#flush()
       *
       * @throws IOException if some I/O error occurs
       */
      @Override
      public void flush() throws IOException
      {
         this.delegee.flush();
      }

      /**
       * @see OutputStream#close()
       *
       * @throws IOException if some I/O error occurs
       */
      @Override
      public void close() throws IOException
      {
         this.delegee.close();
         this.delegee.getChannelFuture().awaitUninterruptibly();
      }

   }

}
