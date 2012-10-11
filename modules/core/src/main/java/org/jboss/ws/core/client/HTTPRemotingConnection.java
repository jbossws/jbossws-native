/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;

import org.jboss.ws.NativeMessages;
import org.jboss.ws.core.MessageTrace;
import org.jboss.ws.core.StubExt;
import org.jboss.ws.core.client.transport.NettyClient;

/**
 * SOAPConnection implementation.
 * <p/>
 *
 * Per default HTTP 1.1 chunked encoding is used.
 * This may be ovverriden through {@link org.jboss.ws.metadata.config.EndpointProperty#CHUNKED_ENCODING_SIZE}.
 * A chunksize value of zero disables chunked encoding.
 *
 * @author Thomas.Diesler@jboss.org
 * @author <a href="mailto:jason@stacksmash.com">Jason T. Greene</a>
 * @author alessio.soldano@jboss.com
 *
 * @since 02-Feb-2005
 */
public abstract class HTTPRemotingConnection implements RemoteConnection
{
   private boolean closed;
   private Integer chunkSize;

   public HTTPRemotingConnection()
   {
      
   }

   public boolean isClosed()
   {
      return closed;
   }

   public void setClosed(boolean closed)
   {
      this.closed = closed;
   }

   public Integer getChunkSize()
   {
      return chunkSize;
   }

   public void setChunkSize(Integer chunkSize)
   {
      this.chunkSize = chunkSize;
   }

   /** 
    * Sends the given message to the specified endpoint. 
    * 
    * A null reqMessage signifies a HTTP GET request.
    */
   public SOAPMessage invoke(SOAPMessage reqMessage, Object endpoint, boolean oneway) throws IOException
   {
      if (endpoint == null)
         throw NativeMessages.MESSAGES.illegalNullArgument("endpoint");

      if (closed)
         throw NativeMessages.MESSAGES.connectionAlreadyClosed();

      String targetAddress;
      Map<String, Object> callProps = new HashMap<String, Object>();

      if (endpoint instanceof EndpointInfo)
      {
         EndpointInfo epInfo = (EndpointInfo)endpoint;
         targetAddress = epInfo.getTargetAddress();
         callProps = epInfo.getProperties();
      }
      else
      {
         targetAddress = endpoint.toString();
      }
      final String[] transferEncodingValue = reqMessage != null ? reqMessage.getMimeHeaders().getHeader("Transfer-Encoding") : null; 
      if (transferEncodingValue != null && "disabled".equals(transferEncodingValue[0]))
      {
         reqMessage.getMimeHeaders().removeHeader("Transfer-Encoding");
         callProps.put(StubExt.PROPERTY_CHUNKED_ENCODING_SIZE, 0);
      }

      NettyClient client = new NettyClient(getMarshaller(), getUnmarshaller());
      if (chunkSize != null)
      {
         client.setChunkSize(chunkSize);
      }
      
      Map<String, Object> additionalHeaders = new HashMap<String, Object>();
      populateHeaders(reqMessage, additionalHeaders);
      //Trace the outgoing message
      MessageTrace.traceMessage("Outgoing Request Message", reqMessage);
      SOAPMessage resMessage = (SOAPMessage)client.invoke(reqMessage, targetAddress, oneway, additionalHeaders, callProps);
      //Trace the incoming response message
      MessageTrace.traceMessage("Incoming Response Message", resMessage);
      return resMessage;
   }
   
   

   protected void populateHeaders(SOAPMessage reqMessage, Map<String, Object> metadata)
   {
      if (reqMessage != null)
      {
         MimeHeaders mimeHeaders = reqMessage.getMimeHeaders();

         Iterator i = mimeHeaders.getAllHeaders();
         while (i.hasNext())
         {
            MimeHeader header = (MimeHeader)i.next();
            String hName = header.getName();
            Object currentValue = metadata.get(hName);

            /*
             * Coalesce multiple headers into one
             *
             * From HTTP/1.1 RFC 2616:
             *
             * Multiple message-header fields with the same field-name MAY be
             * present in a message if and only if the entire field-value for that
             * header field is defined as a comma-separated list [i.e., #(values)].
             * It MUST be possible to combine the multiple header fields into one
             * "field-name: field-value" pair, without changing the semantics of
             * the message, by appending each subsequent field-value to the first,
             * each separated by a comma.
             */
            if (currentValue != null)
            {
               metadata.put(hName, currentValue + "," + header.getValue());
            }
            else
            {
               metadata.put(hName, header.getValue());
            }
         }
      }
   }
}
