/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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

import static org.jboss.ws.extensions.wsrm.RMConstant.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Callable;

import org.jboss.logging.Logger;
import org.jboss.remoting.Client;
import org.jboss.remoting.InvokerLocator;
import org.jboss.remoting.marshal.MarshalFactory;
import org.jboss.ws.core.MessageTrace;
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
   private static final String JBOSSWS_SUBSYSTEM = "jbossws-wsrm";
   private final RMMessage rmRequest;
   
   RMChannelTask(RMMessage rmRequest)
   {
      super();
      this.rmRequest = rmRequest;
   }
   
   private String addURLParameter(String urlStr, String key, String value) throws MalformedURLException
   {
      URL url = new URL(urlStr);
      urlStr += (url.getQuery() == null ? "?" : "&") + key + "=" + value;
      return urlStr;
   }

   public RMChannelResponse call()
   {
      try
      {
         String targetAddress = (String)rmRequest.getMetadata().getContext(INVOCATION_CONTEXT).get(TARGET_ADDRESS);
         String version = (String)rmRequest.getMetadata().getContext(INVOCATION_CONTEXT).get(REMOTING_VERSION);

         if (version.startsWith("1.4"))
         {
            targetAddress = addURLParameter(targetAddress, InvokerLocator.DATATYPE, "JBossWSMessage");
            MarshalFactory.addMarshaller("JBossWSMessage", RMMarshaller.getInstance(), RMUnMarshaller.getInstance());
         }

         InvokerLocator locator = new InvokerLocator(targetAddress);
         URI backPort = RMTransportHelper.getBackPortURI(rmRequest);
         String messageId = RMTransportHelper.getAddressingMessageId(rmRequest);
         
         logger.debug("[WS-RM] backport URI is: " + backPort);
         RMCallbackHandler callbackHandler = null;
         // TODO: we should remember WSA:MessageId here too

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
         
         Client client = new Client(locator, JBOSSWS_SUBSYSTEM, rmRequest.getMetadata().getContext(REMOTING_CONFIGURATION_CONTEXT));
         client.connect();

         client.setMarshaller(RMMarshaller.getInstance());

         if ((false == oneWay) && (null == backPort))  
            client.setUnMarshaller(RMUnMarshaller.getInstance());
      
         Map<String, Object> remotingInvocationContext = rmRequest.getMetadata().getContext(REMOTING_INVOCATION_CONTEXT);

         // debug the outgoing request message
         MessageTrace.traceMessage("Outgoing RM Request Message", rmRequest.getPayload());
 
         RMMessage rmResponse = null;
         if (oneWay && (null == backPort))
         {
            client.invokeOneway(rmRequest.getPayload(), remotingInvocationContext, false);
         }
         else
         {
            Object retVal = client.invoke(rmRequest.getPayload(), remotingInvocationContext);
            if ((null != retVal) && (false == (retVal instanceof RMMessage)))
            {
               String msg = retVal.getClass().getName() + ": '" + retVal + "'";
               logger.warn(msg);
               throw new IOException(msg);
            }
            rmResponse = (RMMessage)retVal;
         }

         // Disconnect the remoting client
         client.disconnect();

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