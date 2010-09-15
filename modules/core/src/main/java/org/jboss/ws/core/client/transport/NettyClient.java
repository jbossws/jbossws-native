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
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.ClosedChannelException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import javax.net.ssl.SSLEngine;
import javax.xml.rpc.Stub;
import javax.xml.ws.BindingProvider;

import org.jboss.logging.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
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
import org.jboss.ws.core.client.Marshaller;
import org.jboss.ws.core.client.UnMarshaller;
import org.jboss.ws.core.client.ssl.SSLContextFactory;
import org.jboss.ws.core.client.transport.WSResponseHandler.Result;
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
   public static final String RESPONSE_CODE = "org.jboss.ws.core.client.transport.NettyClient#ResponseCode";
   public static final String RESPONSE_CODE_MESSAGE = "org.jboss.ws.core.client.transport.NettyClient#ResponseCodeMessage";
   public static final String PROTOCOL = "org.jboss.ws.core.client.transport.NettyClient#Protocol";
   public static final String RESPONSE_HEADERS = "org.jboss.ws.core.client.transport.NettyClient#ResponseHeaders";
   private static Logger log = Logger.getLogger(NettyClient.class);
   
   private Marshaller marshaller;
   private UnMarshaller unmarshaller;
   private Long timeout;
   private Long connectionTimeout;
   private Long receiveTimeout;
   private static final int DEFAULT_CHUNK_SIZE = 1024;
   //We always use chunked transfer encoding unless explicitly disabled by user 
   private Integer chunkSize = new Integer(DEFAULT_CHUNK_SIZE);
   
   private static final Pattern headerCleanerPattern = Pattern.compile("[\r\n\f]");
   
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
   }

   /**
    * Performs the invocation; a HTTP GET is performed when the reqMessage is null, otherwise a HTTP POST is performed.
    * 
    * @param reqMessage          The request message
    * @param targetAddress       The target address
    * @param oneway              True for one-way message exchanges
    *                            and when maintaining sessions using cookies.
    * @param additionalHeaders   Additional http headers to be added to the request
    * @param callProps
    * @return
    * @throws IOException
    */
   public Object invoke(Object reqMessage, String targetAddress, boolean oneway, Map<String, Object> additionalHeaders, Map<String, Object> callProps) throws IOException
   {
	   try
	   {
		   return invokeInternal(reqMessage, targetAddress, oneway, additionalHeaders, callProps);
	   }
	   catch (ClosedChannelException cce)
	   {
		   if (NettyTransportHandler.getHttpKeepAliveSet())
		   {
			   log.info("Retrying with a new connection..."); //because using keep-alive connections it's possible to try re-using closed connections before they've been evicted
			   return invokeInternal(reqMessage, targetAddress, oneway, additionalHeaders, callProps);
		   }
		   else
		   {
			   throw cce;
		   }
	   }
   }
   
   private Object invokeInternal(Object reqMessage, String targetAddress, boolean oneway, Map<String, Object> additionalHeaders, Map<String, Object> callProps) throws IOException
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
      
      NettyTransportHandler transport = NettyTransportHandler.getInstance(target, NettyHelper.getChannelPipelineFactory(getSSLHandler(target, callProps)));
      Channel channel = null;
      Map<String, Object> resHeaders = null;
      Map<String, Object> resMetadata = null;
      try
      {
         setActualTimeout(callProps);
         //JBWS-3114:Provide the new connection timeout configuration
         if (callProps.containsKey(StubExt.PROPERTY_CONNECTION_TIMEOUT))
         {
            connectionTimeout = new Long(callProps.get(StubExt.PROPERTY_CONNECTION_TIMEOUT).toString());
         }
         else
         {
            if (timeout != null)
            {
               connectionTimeout = timeout;
            }
         }
       
         channel = transport.getChannel(connectionTimeout);
         
         WSResponseHandler responseHandler = new WSResponseHandler();
         NettyHelper.setResponseHandler(channel, responseHandler);
         
         //Send the HTTP request
         HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, reqMessage != null ? HttpMethod.POST : HttpMethod.GET, targetAddress);
         request.addHeader(HttpHeaders.Names.HOST, target.getHost());
         request.addHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
         setAdditionalHeaders(request, additionalHeaders);
         setActualChunkedLength(request, callProps);
         setAuthorization(request, callProps);

         ChannelFuture writeFuture = null;
         
         writeFuture = writeRequest(channel, request, reqMessage);

         if (writeFuture != null)
         {
            writeFuture.awaitUninterruptibly();
         }
         
         //Get the response
         Future<Result> futureResult = responseHandler.getFutureResult();
         Result result = null;
         if (callProps.containsKey(StubExt.PROPERTY_RECEIVE_TIMEOUT))
         {
            receiveTimeout = new Long(callProps.get(StubExt.PROPERTY_RECEIVE_TIMEOUT).toString());
         }
         else
         {
            if (timeout != null)
            {
               receiveTimeout = timeout;
            }
         }
         try
         {
            result = receiveTimeout == null ? futureResult.get() : futureResult.get(receiveTimeout,
                  TimeUnit.MILLISECONDS);
         }
         catch (ExecutionException ee)
         {
        	 //unwrap ExecutionException
        	 Throwable t = ee.getCause();
        	 throw t != null ? t : ee;
         }
	 catch (TimeoutException te) 
	 {
	    throw new WSTimeoutException("Receive timeout", receiveTimeout == null ? -1 : receiveTimeout);
	 }
         resHeaders = result.getResponseHeaders();
         resMetadata = result.getMetadata();
         Object resMessage = oneway ? null : unmarshaller.read(result.getResponse(), resMetadata, resHeaders);
         
         //Update props with response headers (required to maintain session using cookies)
         callProps.clear();
         callProps.put(RESPONSE_HEADERS, resHeaders != null ? resHeaders : new HashMap<String, Object>());
         if (resMetadata != null) {
            callProps.putAll(resMetadata);
         }

         return resMessage;
      }
      catch (ClosedChannelException cce)
      {
         log.error("Channel closed by remote peer while sending message");
         transport.end();
         throw cce;
      }
      catch (ConnectException ce)
      {
         transport.end();
         throw ce;
      }
      catch (TimeoutException te) 
      {
	 throw new WSTimeoutException("Connection timeout", connectionTimeout == null ? -1 : connectionTimeout);
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
         transport.end();
         throw io;
      }
      finally
      {
         if (channel != null)
         {
            NettyHelper.clearResponseHandler(channel);
         }
         transport.finished(resMetadata, resHeaders); //provide both headers and metadata to the transport to allow for proper keepAlive checks
      }
   }
      
   private static SslHandler getSSLHandler(URL target, Map<String, Object> callProps) throws IOException
   {
      SslHandler handler = null;
      if ("https".equalsIgnoreCase(target.getProtocol()))
      {
         SSLContextFactory sslContextFactory = new SSLContextFactory(callProps);
         SSLEngine engine = sslContextFactory.getSSLContext().createSSLEngine();
         engine.setUseClientMode(true);
         handler = new SslHandler(engine);
      }
      return handler;
   }

   private ChannelFuture writeRequest(Channel channel, HttpRequest request, Object reqMessage) throws IOException
   {
      if (reqMessage == null)
      {
         return channel.write(request);
      }
      else
      {
         int cs = chunkSize;
         if (cs > 0) //chunked encoding
         {
            request.addHeader(HttpHeaders.Names.TRANSFER_ENCODING, HttpHeaders.Values.CHUNKED);
            //write headers
            channel.write(request);
            //write content chunks
            NettyTransportOutputStream os = new NettyTransportOutputStream(channel, cs);
            marshaller.write(reqMessage, os);
            os.flush();
            os.close();
            return os.getChannelFuture();
         }
         else
         {
            ChannelBuffer content = ChannelBuffers.dynamicBuffer();
            OutputStream os = new ChannelBufferOutputStream(content);
            marshaller.write(reqMessage, os);
            os.flush();
            request.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(content.readableBytes()));
            request.setContent(content);
            return channel.write(request);
         }
      }
   }

   /**
    * Set the actual chunk size according to the endpoint config overwrite and/or configured features.
    * 
    * @param message
    */
   protected void setActualChunkedLength(HttpRequest message, Map<String, Object> callProps)
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

            //override using call props
            try
            {
               Object obj = callProps.get(StubExt.PROPERTY_CHUNKED_ENCODING_SIZE);
               if (obj != null) //explicit 0 value is required to disable chunked mode
                  chunkSize = (Integer)obj;
            }
            catch (Exception e)
            {
               log.warn("Can't set chunk size from call properties, illegal value provided!");
            }
            
            //fastinfoset always disable chunking
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
         String header = (String)headers.get(key);
         message.addHeader(key, headerCleanerPattern.matcher(header).replaceAll(" "));
      }
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
