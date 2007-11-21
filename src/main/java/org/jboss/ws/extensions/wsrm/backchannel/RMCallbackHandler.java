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
package org.jboss.ws.extensions.wsrm.backchannel;

import java.util.HashMap;
import java.util.Map;

import org.jboss.logging.Logger;
import org.jboss.remoting.InvocationRequest;
import org.jboss.ws.core.MessageTrace;
import org.jboss.ws.extensions.wsrm.RMChannelRequest;
import org.jboss.ws.extensions.wsrm.RMMessage;
import org.jboss.ws.extensions.wsrm.RMMessageFactory;
import org.jboss.ws.extensions.wsrm.RMMetadata;

/**
 * TODO: Add comment
 *
 * @author richard.opalka@jboss.com
 *
 * @since Nov 21, 2007
 */
public final class RMCallbackHandler implements CallbackHandler
{
   private static final Logger LOG = Logger.getLogger(RMCallbackHandler.class);
   private final String handledPath;
   private final Object instanceLock = new Object();
   private Map<String, RMMessage> arrivedMessages = new HashMap<String, RMMessage>();
   
   public RMCallbackHandler(String handledPath)
   {
      super();
      this.handledPath = handledPath;
   }
   
   public Throwable getFault(String messageId)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public final String getHandledPath()
   {
      return this.handledPath;
   }

   public final void handle(InvocationRequest request)
   {
      String requestMessage = (String)request.getParameter();
      System.out.println("=================================================");
      System.out.println("4 - " + request.getRequestPayload());
      System.out.println("5 - " + request.getReturnPayload());
      synchronized (instanceLock)
      {
         LOG.debug("Setting response object");
         MessageTrace.traceMessage("Incoming RM Response Message", requestMessage.getBytes());
         String startPattern = "<wsa:RelatesTo>"; // TODO: remove this with XML content inspection
         String endPattern = "</wsa:RelatesTo>";
         int begin = requestMessage.indexOf(startPattern) + startPattern.length();
         int end = requestMessage.indexOf(endPattern); 
         String messageId = requestMessage.substring(begin, end);
         RMMessage message = RMMessageFactory.newMessage(requestMessage.getBytes(), new RMMetadata(new java.util.HashMap<String, Object>())); // TODO create map metadata
         System.out.println("Arrived message id: " + messageId);
         System.out.println("Message content is: " + requestMessage);
         this.arrivedMessages.put(messageId, message); 
         System.out.println("Response object is null? -> " + (message == null));
         System.out.println("=================================================");
      }
   }

   public RMMessage getMessage(String messageId)
   {
      synchronized (instanceLock)
      {
         while (this.arrivedMessages.get(messageId) == null)
         {
            try
            {
               LOG.debug("waiting for response object associated with message id: " + messageId);
               instanceLock.wait(100);
            }
            catch (InterruptedException ignore)
            {
               // TODO: never ignore exceptions - LOG it using logger
            }
         }
         return this.arrivedMessages.get(messageId);
      }
   }
   
}
