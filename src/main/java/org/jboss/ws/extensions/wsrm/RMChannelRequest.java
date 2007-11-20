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
package org.jboss.ws.extensions.wsrm;

import static org.jboss.ws.extensions.wsrm.RMConstant.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.jboss.logging.Logger;
import org.jboss.remoting.Client;
import org.jboss.remoting.InvokerLocator;
import org.jboss.ws.core.MessageTrace;
import org.jboss.ws.extensions.wsrm.backchannel.RMBackPortsServer;
import org.jboss.ws.extensions.wsrm.spi.Provider;
import org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequence;
import org.jboss.ws.extensions.wsrm.spi.protocol.Serializable;

/**
 * Represents request that goes to the RM channel
 * @see org.jboss.ws.extensions.wsrm.RMChannel
 * @author richard.opalka@jboss.com
 */
final class RMChannelRequest implements Callable<RMChannelResponse>
{
   private static final Logger log = Logger.getLogger(RMChannelRequest.class);
   private static final String JBOSSWS_SUBSYSTEM = "jbossws";
   private final RMMessage rmRequest;
   
   RMChannelRequest(RMMessage rmRequest)
   {
      super();
      this.rmRequest = rmRequest;
   }
   
   public RMChannelResponse call()
   {
      InvokerLocator locator = null;
      try
      {
         locator = new InvokerLocator((String)rmRequest.getMetadata().getContext(INVOCATION_CONTEXT).get(TARGET_ADDRESS));
      }
      catch (MalformedURLException e)
      {
         return new RMChannelResponse(new IllegalArgumentException("Malformed endpoint address", e));
      }

      try
      {
         Client client = new Client(locator, JBOSSWS_SUBSYSTEM, rmRequest.getMetadata().getContext(REMOTING_CONFIGURATION_CONTEXT));
         client.connect();

         client.setMarshaller(RMMarshaller.getInstance());

         URI backPort = RMHelper.getBackPortURI(rmRequest);
         System.out.println("[WS-RM] backport URI is: " + backPort);

         if (backPort != null)
         {
            RMBackPortsServer backPortsServer = RMBackPortsServer.getInstance(backPort.getScheme(), backPort.getHost(), backPort.getPort());
            //backPortsServer.registerCallback(backPort.getPath(), this);
         }
         boolean oneWay = RMHelper.isOneWayOperation(rmRequest);
         if (!oneWay)  
            client.setUnMarshaller(RMUnMarshaller.getInstance());
      
         Map<String, Object> remotingInvocationContext = rmRequest.getMetadata().getContext(REMOTING_INVOCATION_CONTEXT);
         if (log.isDebugEnabled())
            log.debug("Remoting metadata: " + remotingInvocationContext);

         // debug the outgoing request message
         MessageTrace.traceMessage("Outgoing RM Request Message", rmRequest.getPayload());
 
         RMMessage rmResponse = null;
         if (oneWay)
         {
            client.invokeOneway(rmRequest.getPayload(), remotingInvocationContext, false);
         }
         else
         {
            rmResponse = (RMMessage)client.invoke(rmRequest.getPayload(), remotingInvocationContext);
         }

         // Disconnect the remoting client
         client.disconnect();

         // trace the incomming response message
         if (rmResponse != null)
            MessageTrace.traceMessage("Incoming RM Response Message", rmResponse.getPayload());

         return new RMChannelResponse(rmResponse);
      }
      catch (Throwable t)
      {
         return new RMChannelResponse(t);
      }
   }
}