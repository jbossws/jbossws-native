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
package org.jboss.ws.extensions.wsrm.transport;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.jboss.logging.Logger;
import org.jboss.ws.core.MessageTrace;
import org.jboss.ws.core.client.transport.NettyClient;
import org.jboss.ws.extensions.wsrm.RMClientSequence;
import org.jboss.ws.extensions.wsrm.transport.backchannel.RMCallbackHandler;
import org.jboss.ws.extensions.wsrm.transport.backchannel.RMCallbackHandlerFactory;

/**
 * RM channel task to be executed
 * 
 * @author richard.opalka@jboss.com
 */
final class RMChannelTask implements Callable<RMChannelResponse>
{
   private static final Logger logger = Logger.getLogger(RMChannelTask.class);
   private final RMMessage rmRequest;
   
   RMChannelTask(RMMessage rmRequest)
   {
      super();
      this.rmRequest = rmRequest;
   }
   
   public RMChannelResponse call()
   {
      try
      {
         String targetAddress = (String)rmRequest.getMetadata().getContext(RMChannelConstants.INVOCATION_CONTEXT).get(RMChannelConstants.TARGET_ADDRESS);
         URI backPort = RMTransportHelper.getBackPortURI(rmRequest);
         String messageId = RMTransportHelper.getAddressingMessageId(rmRequest);
         
         logger.debug("[WS-RM] backport URI is: " + backPort);
         RMCallbackHandler callbackHandler = null;

         if (backPort != null)
         {
            callbackHandler = RMCallbackHandlerFactory.getCallbackHandler(backPort);
            RMClientSequence sequence = RMTransportHelper.getSequence(rmRequest);
            if (sequence != null)
            {
               callbackHandler.addUnassignedMessageListener(sequence);
            }
         }
         boolean oneWay = RMTransportHelper.isOneWayOperation(rmRequest);
         
         NettyClient client = new NettyClient(RMMarshaller.getInstance(), RMUnMarshaller.getInstance());
         Map<String, Object> additionalHeaders = rmRequest.getMetadata().getContext(RMChannelConstants.REMOTING_INVOCATION_CONTEXT);
         Map<String, Object> callProps = new HashMap<String, Object>();
         callProps.putAll(rmRequest.getMetadata().getContext(RMChannelConstants.INVOCATION_CONTEXT));
         
         MessageTrace.traceMessage("Outgoing RM Response Message", rmRequest.getPayload());
         RMMessage rmResponse = null;
         if (oneWay && (null == backPort))
         {
            client.invoke(rmRequest.getPayload(), targetAddress, true, additionalHeaders, callProps);
         }
         else
         {
            Object retVal = client.invoke(rmRequest.getPayload(), targetAddress, false, additionalHeaders, callProps);
            if ((null != retVal) && (false == (retVal instanceof RMMessage)))
            {
               String msg = retVal.getClass().getName() + ": '" + retVal + "'";
               logger.warn(msg);
               throw new IOException(msg);
            }
            rmResponse = (RMMessage)retVal;
         }
         rmRequest.getMetadata().getContext(RMChannelConstants.INVOCATION_CONTEXT).putAll(callProps);

         // trace the incomming response message
         if ((rmResponse != null) && (backPort == null))
            MessageTrace.traceMessage("Incoming RM Response Message", rmResponse.getPayload());
         
         if (backPort != null) // TODO: backport support
         {
            if ((null != messageId) && (false == RMTransportHelper.isOneWayOperation(rmRequest)))
            {
               // register callbacks only for outbound messages with messageId
               return new RMChannelResponse(callbackHandler, messageId);
            }
         }

         return new RMChannelResponse(rmResponse);
      }
      catch (Throwable t)
      {
         return new RMChannelResponse(t);
      }
   }
}