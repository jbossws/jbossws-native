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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.ws.addressing.EndpointReference;

import org.jboss.logging.Logger;
import org.jboss.ws.core.MessageAbstraction;
import org.jboss.ws.core.MessageTrace;
import org.jboss.ws.core.client.transport.NettyClient;
import org.jboss.ws.extensions.wsrm.transport.RMChannel;
import org.jboss.ws.extensions.wsrm.transport.RMMetadata;
import org.jboss.ws.extensions.wsrm.transport.RMTransportHelper;

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
   // provide logging
   private static Logger log = Logger.getLogger(HTTPRemotingConnection.class);
   
   private boolean closed;
   private Integer chunkSize;

   private static final RMChannel RM_CHANNEL = RMChannel.getInstance();

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
   public MessageAbstraction invoke(MessageAbstraction reqMessage, Object endpoint, boolean oneway) throws IOException
   {
      if (endpoint == null)
         throw new IllegalArgumentException("Given endpoint cannot be null");

      if (closed)
         throw new IOException("Connection is already closed");

      String targetAddress;
      Map<String, Object> callProps = new HashMap<String, Object>();

      if (endpoint instanceof EndpointInfo)
      {
         EndpointInfo epInfo = (EndpointInfo)endpoint;
         targetAddress = epInfo.getTargetAddress();
         callProps = epInfo.getProperties();
      }
      else if (endpoint instanceof EndpointReference)
      {
         EndpointReference epr = (EndpointReference)endpoint;
         targetAddress = epr.getAddress().toString();
      }
      else
      {
         targetAddress = endpoint.toString();
      }

      if (RMTransportHelper.isRMMessage(callProps))
      {
         try
         {
            Map<String, Object> additionalHeaders = new HashMap<String, Object>();
            populateHeaders(reqMessage, additionalHeaders);
            RMMetadata rmMetadata = new RMMetadata(targetAddress, getMarshaller(), getUnmarshaller(), callProps, additionalHeaders);
            return RM_CHANNEL.send(reqMessage, rmMetadata);
         }
         catch (Throwable t)
         {
            IOException io = new IOException();
            io.initCause(t);
            throw io;
         }
      }
      else
      {
         NettyClient client = new NettyClient(getMarshaller(), getUnmarshaller());
         if (chunkSize != null)
         {
            client.setChunkSize(chunkSize);
         }
         
         Map<String, Object> additionalHeaders = new HashMap<String, Object>();
         populateHeaders(reqMessage, additionalHeaders);
         //Trace the outgoing message
         MessageTrace.traceMessage("Outgoing Request Message", reqMessage);
         MessageAbstraction resMessage = (MessageAbstraction)client.invoke(reqMessage, targetAddress, oneway, additionalHeaders, callProps);
         //Trace the incoming response message
         MessageTrace.traceMessage("Incoming Response Message", resMessage);
         return resMessage;
      }
   }
   
   

   protected void populateHeaders(MessageAbstraction reqMessage, Map<String, Object> metadata)
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
