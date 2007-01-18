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
package org.jboss.ws.extensions.eventing.jaxrpc;

// $Id$

import java.rmi.RemoteException;

import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.AttributedURI;

import org.jboss.logging.Logger;
import org.jboss.ws.extensions.eventing.jaxrpc.element.EndpointReference;
import org.jboss.ws.extensions.eventing.jaxrpc.element.SubscribeRequest;
import org.jboss.ws.extensions.eventing.jaxrpc.element.SubscribeResponse;
import org.jboss.ws.extensions.eventing.jaxrpc.element.SubscriptionEndRequest;
import org.jboss.ws.extensions.eventing.common.EventingEndpointBase;
import org.jboss.ws.extensions.eventing.jaxrpc.EventSourceEndpoint;
import org.jboss.ws.extensions.eventing.mgmt.*;
import org.jboss.ws.extensions.eventing.EventingConstants;

/**
 * Event source endpoint implementation.<br>
 * Delegates to {@link org.jboss.ws.extensions.eventing.mgmt.SubscriptionManager}
 *
 * @author Heiko Braun, <heiko@openj.net>
 * @since 25-Nov-2005
 */
public class EventSourceEndpointImpl extends EventingEndpointBase
      implements EventSourceEndpoint
{
   private static Logger log = Logger.getLogger(EventSourceEndpointImpl.class);

   public SubscribeResponse subscribe(SubscribeRequest request) throws RemoteException
   {
      /*try
      {
         // retrieve addressing headers
         AddressingProperties inProps = getAddrProperties();
         AttributedURI eventSourceURI = inProps.getTo();
         log.debug("Subscribe request for event source: " + eventSourceURI.getURI());

         assertSubscriberEndpoints(request);
         EndpointReference notifyTo = request.getDelivery().getNotifyTo();
         EndpointReference endTo = request.getEndTo();

         // adapt filter elements
         Filter filter = null;
         if (request.getFilter() != null)
         {
            filter = new Filter(request.getFilter().getDialect(), request.getFilter().get_value());
         }

         // invoke subscription manager
         SubscriptionManagerMBean subscriptionManager = getSubscriptionManager();
         SubscriptionTicket ticket = subscriptionManager.subscribe(eventSourceURI.getURI(), notifyTo, endTo, request.getExpires(), filter);

         // create the response element
         SubscribeResponse res = new SubscribeResponse();
         res.setExpires(ticket.getExpires());
         res.setSubscriptionManager(ticket.getSubscriptionManager());

         return res;

      }
      catch (SubscriptionError e)
      {
         throw new SOAPFaultException(buildFaultQName(e.getSubcode()), e.getReason(), null, null);
      } */

      return null;
   }

   public SubscriptionEndRequest subscriptionEnd() throws RemoteException
   {
      return new SubscriptionEndRequest();
   }

   /**
    * Ensure that the subscriber endpoint information is supplied in request.
    * Namely NotifyTo and EndTo need to be set.
    * @param request
    */
   private void assertSubscriberEndpoints(SubscribeRequest request) {
      if(null == request.getDelivery().getNotifyTo() ||  null == request.getEndTo() )
         throw new SOAPFaultException( buildFaultQName(EventingConstants.CODE_INVALID_MESSAGE) ,
               "Subcriber endpoint information missing from request",
               null, null
         );
   }
}
