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

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLEngine;
import javax.xml.rpc.Stub;
import javax.xml.ws.BindingProvider;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpChunk;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.security.Base64Encoder;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.StubExt;
import org.jboss.ws.core.WSTimeoutException;
import org.jboss.ws.core.client.ssl.SSLContextFactory;
import org.jboss.ws.core.soap.MessageContextAssociation;
import org.jboss.ws.feature.FastInfosetFeature;
import org.jboss.ws.metadata.config.CommonConfig;
import org.jboss.ws.metadata.config.EndpointProperty;
import org.jboss.ws.metadata.umdm.EndpointMetaData;

/**
 * A http client using Netty
 * 
 * @author alessio.soldano@jboss.com
 * @since 29-Jun-2009
 *
 */
public class NettyClient
{
   public static final String RESPONSE_CODE = "ResponseCode";
   public static final String RESPONSE_CODE_MESSAGE = "ResponseCodeMessage";
   
   private Marshaller marshaller;
   private UnMarshaller unmarshaller;
   private Long timeout;
   private static final int DEFAULT_CHUNK_SIZE = 1024;
   //We always use chunked transfer encoding unless explicitely disabled by user 
   private Integer chunkSize = new Integer(DEFAULT_CHUNK_SIZE);
   private Executor bossExecutor;
   private Executor workerExecutor;

   /**
    * Construct a Netty client with the provided marshaller/unmarshaller.
    * 
    * @param marshaller
    * @param unmarshaller
    */
   public NettyClient(Marshaller marshaller, UnMarshaller unmarshaller)
   {
      this.marshaller = marshaller;
      this.unmarshaller = unmarshaller;
      this.bossExecutor = Executors.newCachedThreadPool();
      this.workerExecutor = Executors.newCachedThreadPool();
   }

   /**
    * Construct a Netty client with the provided marshaller/unmarshaller and executors.
    * 
    * @param marshaller
    * @param unmarshaller
    * @param bossExecutor
    * @param workerExecutor
    */
   public NettyClient(Marshaller marshaller, UnMarshaller unmarshaller, Executor bossExecutor, Executor workerExecutor)
   {
      this.marshaller = marshaller;
      this.unmarshaller = unmarshaller;
      this.bossExecutor = bossExecutor;
      this.workerExecutor = workerExecutor;
   }

   /**
    * Performs the invocation; a HTTP GET is performed when the reqMessage is null, otherwise a HTTP POST is performed.
    * 
    * @param reqMessage          The request message
    * @param targetAddress       The target address
    * @param waitForResponse     A boolean saying if the method should wait for the results before returning. Waiting is required for two-ways invocations
    *                            and when maintaining sessions using cookies.
    * @param additionalHeaders   Additional http headers to be added to the request
    * @param callProps
    * @return
    * @throws IOException
    */
   public Object invoke(Object reqMessage, String targetAddress, boolean waitForResponse, Map<String, Object> additionalHeaders, Map<String, Object> callProps)
         throws IOException
   {
      URL target;
      try
      {
         target = new URL(targetAddress);
      }
      catch (MalformedURLException e)
      {
         throw new RuntimeException("Invalid address: " + targetAddress, e);
      }
      
      ChannelFactory factory = new NioClientSocketChannelFactory(bossExecutor, workerExecutor);

      ClientBootstrap bootstrap = new ClientBootstrap(factory);
      WSClientPipelineFactory channelPipelineFactory = new WSClientPipelineFactory();
      WSResponseHandler responseHandler = null;
      if (waitForResponse)
      {
         responseHandler = new WSResponseHandler(unmarshaller);
         channelPipelineFactory.setResponseHandler(responseHandler);
      }
      if ("https".equalsIgnoreCase(target.getProtocol()))
      {
         SSLContextFactory sslContextFactory = new SSLContextFactory(callProps);
         SSLEngine engine = sslContextFactory.getSSLContext().createSSLEngine();
         engine.setUseClientMode(true);
         channelPipelineFactory.setSslHandler(new SslHandler(engine));
      }
      
      bootstrap.setPipelineFactory(channelPipelineFactory);

      Channel channel = null;
      try
      {
         setActualTimeout(callProps);
         //Start the connection attempt
         ChannelFuture future = bootstrap.connect(getSocketAddress(target));

         //Wait until the connection attempt succeeds or fails
         awaitUninterruptibly(future, timeout);
         if (!future.isSuccess())
         {
            IOException io = new IOException("Could not connect to " + target.getHost());
            io.initCause(future.getCause());
            factory.releaseExternalResources();
            throw io;
         }
         channel = future.getChannel();

         //Send the HTTP request
         HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, reqMessage != null ? HttpMethod.POST : HttpMethod.GET, targetAddress);
         request.addHeader(HttpHeaders.Names.HOST, target.getHost());
         request.addHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
         setAdditionalHeaders(request, additionalHeaders);
         setActualChunkedLength(request);
         setAuthorization(request, callProps);

         writeRequest(channel, request, reqMessage);

         if (!waitForResponse)
         {
            //No need to wait for the connection to be closed
            return null;
         }
         //Wait for the server to close the connection
         ChannelFuture closeFuture = channel.getCloseFuture();
         awaitUninterruptibly(closeFuture, timeout);
         if (responseHandler.getError() != null)
         {
            throw responseHandler.getError();
         }
         Object resMessage = null;
         Map<String, Object> resHeaders = null;
         //Get the response
         resMessage = responseHandler.getResponseMessage();
         resHeaders = responseHandler.getResponseHeaders();
         //Update props with response headers (required to maintain session using cookies)
         callProps.clear();
         if (resHeaders != null)
         {
            callProps.putAll(resHeaders);
         }

         return resMessage;
      }
      catch (IOException ioe)
      {
         throw ioe;
      }
      catch (WSTimeoutException toe)
      {
         throw toe;
      }
      catch (Throwable t)
      {
         IOException io = new IOException("Could not transmit message");
         io.initCause(t);
         throw io;
      }
      finally
      {
         if (channel != null)
         {
            channel.close();
         }
         //Shut down executor threads to exit
         factory.releaseExternalResources();
      }
   }

   private InetSocketAddress getSocketAddress(URL target)
   {
      int port = target.getPort();
      if (port < 0)
      {
         //use default port
         String protocol = target.getProtocol();
         if ("http".equalsIgnoreCase(protocol))
         {
            port = 80;
         }
         else if ("https".equalsIgnoreCase(protocol))
         {
            port = 443;
         }
      }
      return new InetSocketAddress(target.getHost(), port);
   }

   private void writeRequest(Channel channel, HttpRequest request, Object reqMessage) throws IOException
   {
      if (reqMessage == null)
      {
         channel.write(request);
      }
      else
      {
         ChannelBuffer content = ChannelBuffers.dynamicBuffer();
         OutputStream os = new ChannelBufferOutputStream(content);
         marshaller.write(reqMessage, os);

         int cs = chunkSize;
         if (cs > 0) //chunked encoding
         {
            os.flush();
            request.addHeader(HttpHeaders.Names.TRANSFER_ENCODING, HttpHeaders.Values.CHUNKED);
            //write headers
            channel.write(request);
            //write content chunks
            int size = content.readableBytes();
            int cur = 0;
            while (cur < size)
            {
               int to = Math.min(cur + cs, size);
               HttpChunk chunk = new DefaultHttpChunk(content.slice(cur, to - cur));
               channel.write(chunk);
               cur = to;
            }
            //write last chunk
            channel.write(HttpChunk.LAST_CHUNK);
         }
         else
         {
            request.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(content.readableBytes()));
            request.setContent(content);
            channel.write(request);
         }
      }
   }

   /**
    * Utility method for awaiting with or without timeout (timeout == null or <=0 implies not timeout)
    * 
    * @param future
    * @param timeout
    * @throws WSTimeoutException
    */
   private static void awaitUninterruptibly(ChannelFuture future, Long timeout) throws WSTimeoutException
   {
      if (timeout != null && timeout.longValue() > 0)
      {
         boolean bool = future.awaitUninterruptibly(timeout);
         if (!bool)
         {
            throw new WSTimeoutException("Timeout after: " + timeout + "ms", timeout);
         }
      }
      else
      {
         future.awaitUninterruptibly();
      }
   }

   /**
    * Set the actual chunk size according to the endpoint config overwrite and/or configured features.
    * 
    * @param message
    */
   protected void setActualChunkedLength(HttpRequest message)
   {
      if (HttpMethod.POST.equals(message.getMethod()))
      {
         CommonMessageContext msgContext = MessageContextAssociation.peekMessageContext();
         //Overwrite, through endpoint config
         if (msgContext != null)
         {
            EndpointMetaData epMetaData = msgContext.getEndpointMetaData();
            CommonConfig config = epMetaData.getConfig();

            String sizeValue = config.getProperty(EndpointProperty.CHUNKED_ENCODING_SIZE);
            if (sizeValue != null)
               chunkSize = Integer.valueOf(sizeValue);
            if (epMetaData.isFeatureEnabled(FastInfosetFeature.class))
               chunkSize = 0;
         }
      }
   }
   
   /**
    * Set the actual timeout according to specified caller properties
    * 
    * @param callProps
    */
   protected void setActualTimeout(Map<String, Object> callProps)
   {
      if (callProps.containsKey(StubExt.PROPERTY_CLIENT_TIMEOUT))
      {
         timeout = new Long(callProps.get(StubExt.PROPERTY_CLIENT_TIMEOUT).toString());
      }
   }

   /**
    * Set the required headers in the Netty's HttpMessage to allow for proper authorization.
    * 
    * @param message
    * @param callProps
    * @throws IOException
    */
   protected void setAuthorization(HttpMessage message, Map<String, Object> callProps) throws IOException
   {
      //Get authentication type, default to BASIC authetication
      String authType = (String)callProps.get(StubExt.PROPERTY_AUTH_TYPE);
      if (authType == null)
         authType = StubExt.PROPERTY_AUTH_TYPE_BASIC;
      String username = (String)callProps.get(Stub.USERNAME_PROPERTY);
      String password = (String)callProps.get(Stub.PASSWORD_PROPERTY);
      if (username == null || password == null)
      {
         username = (String)callProps.get(BindingProvider.USERNAME_PROPERTY);
         password = (String)callProps.get(BindingProvider.PASSWORD_PROPERTY);
      }
      if (username != null && password != null)
      {
         if (authType.equals(StubExt.PROPERTY_AUTH_TYPE_BASIC))
         {
            message.addHeader(HttpHeaders.Names.AUTHORIZATION, getBasicAuthHeader(username, password));
         }
      }
   }

   private static String getBasicAuthHeader(String username, String password) throws IOException
   {
      return "Basic " + Base64Encoder.encode(username + ":" + password);
   }

   /**
    * Copy the provided additional headers to the Netty's HttpMessage.
    * 
    * @param message
    * @param headers
    */
   protected void setAdditionalHeaders(HttpMessage message, Map<String, Object> headers)
   {
      for (String key : headers.keySet())
      {
         try
         {
            String header = (String)headers.get(key);
            message.addHeader(key, header.replaceAll("[\r\n\f]", " "));
         }
         catch (Exception e)
         {
            e.printStackTrace();
            throw new RuntimeException(e);
         }
      }
   }

   /**
    * Set the Netty boss executor
    * 
    * @param bossExecutor
    */
   public void setBossExecutor(Executor bossExecutor)
   {
      this.bossExecutor = bossExecutor;
   }

   /**
    * Set the Netty worker executor
    * 
    * @param workerExecutor
    */
   public void setWorkerExecutor(Executor workerExecutor)
   {
      this.workerExecutor = workerExecutor;
   }

   /**
    * 
    * @return  The current chunk size
    */
   public Integer getChunkSize()
   {
      return chunkSize;
   }

   /**
    * Set the chunk size for chunked transfer encoding.
    * The default chunk size is 1024 bytes.
    * 
    * @param chunkSize
    */
   public void setChunkSize(Integer chunkSize)
   {
      this.chunkSize = chunkSize;
   }
}
