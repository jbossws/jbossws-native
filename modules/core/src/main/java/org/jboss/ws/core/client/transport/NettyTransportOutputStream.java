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
import java.io.OutputStream;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.handler.codec.http.DefaultHttpChunk;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.ws.NativeMessages;

/**
 * An output stream that sends messages using Netty.
 * This basically adapts the output stream interface
 * to the Netty API, to allow for marshalling and sending
 * soap messages in one step only saving memory
 * (especially when dealing with attachments)
 * 
 * @author alessio.soldano@jboss.com
 * @since 06-Aug-2009
 *
 */
public class NettyTransportOutputStream extends OutputStream
{
   private Channel channel;
   private byte[] buffer;
   private int cur;
   private ChannelFuture future;
   private boolean closed = false;

   /**
    * Constructor
    * 
    * @param channel    The Netty channel to send the message on
    * @param chunkSize  The chunk size (bytes) for chunked encoding (must be > 0)
    */
   public NettyTransportOutputStream(Channel channel, int chunkSize)
   {
      this.channel = channel;
      if (chunkSize <= 0)
      {
         throw NativeMessages.MESSAGES.invalidChunkSize(chunkSize);
      }
      this.cur = 0;
      this.buffer = new byte[chunkSize];
   }

   @Override
   public synchronized void write(int b) throws IOException
   {
      this.internalWrite(b);
   }

   @Override
   public synchronized void write(byte b[], int off, int len) throws IOException
   {
      if (len >= buffer.length)
      {
         /* If the request length exceeds the size of the output buffer,
            flush and then write the data directly using the internalWrite */
         flushBuffer();
         for (int i = 0; i < len; i++)
         {
            internalWrite(b[off + i]);
         }
         return;
      }
      if (len > buffer.length - cur)
      {
         flushBuffer();
      }
      System.arraycopy(b, off, buffer, cur, len);
      cur += len;
   }

   private void internalWrite(int b) throws IOException
   {
      if (cur >= buffer.length)
      {
         flushBuffer();
      }
      buffer[cur++] = (byte)b;
   }

   /**
    * Flush the internal buffer
    */
   private void flushBuffer() throws IOException
   {
      if (cur > 0)
      {
         ChannelBuffer content = ChannelBuffers.copiedBuffer(buffer, 0, cur);
         HttpChunk chunk = new DefaultHttpChunk(content);
         if (future != null)
         {
            future.awaitUninterruptibly();
         }
         future = channel.write(chunk);
         cur = 0;
      }
   }

   @Override
   public synchronized void flush() throws IOException
   {
      //NOOP: we do not flush the buffer as that would mean sending out many messages with size under the chunkSize
   }
   
   /**
    * Close the stream causing the last chunk to be send on the channel (includes flushing)
    */
   @Override
   public synchronized void close() throws IOException
   {
      flushBuffer();
      if (future != null)
      {
         future.awaitUninterruptibly();
      }
      future = channel.write(HttpChunk.LAST_CHUNK);
      closed = true;
   }
   
   /**
    * Get the Netty channel future for the last message sent.
    * 
    * @return     The Netty channel future for the last message sent.
    */
   public synchronized ChannelFuture getChannelFuture()
   {
      if (!closed)
      {
         throw NativeMessages.MESSAGES.cannotGetChannelFuture();
      }
      return future;
   }

}
