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

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.Date;

import javax.xml.namespace.QName;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.JAXWSAConstants;
import javax.xml.ws.addressing.ReferenceParameters;

import org.jboss.ws.Constants;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.soap.MessageContextAssociation;
import org.jboss.ws.core.utils.DOMUtils;
import org.jboss.ws.extensions.eventing.jaxrpc.element.RenewRequest;
import org.jboss.ws.extensions.eventing.jaxrpc.element.RenewResponse;
import org.jboss.ws.extensions.eventing.jaxrpc.element.StatusRequest;
import org.jboss.ws.extensions.eventing.jaxrpc.element.StatusResponse;
import org.jboss.ws.extensions.eventing.jaxrpc.element.UnsubscribeRequest;
import org.jboss.ws.extensions.eventing.common.EventingEndpointBase;
import org.jboss.ws.extensions.eventing.jaxrpc.SubscriptionManagerEndpoint;
import org.jboss.ws.extensions.eventing.mgmt.SubscriptionError;
import org.jboss.ws.extensions.eventing.EventingConstants;
import org.w3c.dom.Element;

/**
 * SubscriptionManagerEndpoint endpoint implementation.<br>
 * Delegates to {@link org.jboss.ws.extensions.eventing.mgmt.SubscriptionManager}
 *
 * @author Heiko Braun, <heiko@openj.net>
 * @since 16-Dec-2005
 */
public class SubscriptionManagerEndpointImpl extends EventingEndpointBase
      implements SubscriptionManagerEndpoint
{

   public static final QName IDQN = new QName("http://schemas.xmlsoap.org/ws/2004/08/eventing", "Identifier", "ns1");

   /**
    * Get the status of a subscription.
    *
    * @param request
    * @return response including the lease time.
    * @throws RemoteException
    */
   public StatusResponse getStatus(StatusRequest request) throws RemoteException
   {
      URI identifier = retrieveSubscriptionId();

      try
      {
         Date leaseTime = getSubscriptionManager().getStatus(identifier);
         StatusResponse response = new StatusResponse();
         response.setExpires(leaseTime);

         return response;
      }
      catch (SubscriptionError e)
      {
         throw new SOAPFaultException(buildFaultQName(e.getSubcode()), e.getReason(), null, null);
      }
   }

   /**
    * Update the expiration for a subscription.
    *
    * @param request
    * @return response inclduing the new lease time.
    * @throws RemoteException
    */
   public RenewResponse renew(RenewRequest request) throws RemoteException
   {
      URI identifier = retrieveSubscriptionId();

      try
      {
         Date newLeaseTime = getSubscriptionManager().renew(identifier, request.getExpires());
         RenewResponse response = new RenewResponse();
         response.setExpires(newLeaseTime);

         return response;
      }
      catch (SubscriptionError e)
      {
         throw new SOAPFaultException(buildFaultQName(e.getSubcode()), e.getReason(), null, null);
      }

   }

   /**
    * Explicitly delete a subscription.
    *
    * @param request
    * @throws RemoteException
    */
   public void unsubscribe(UnsubscribeRequest request) throws RemoteException
   {
      URI identifier = retrieveSubscriptionId();

      try
      {
         getSubscriptionManager().unsubscribe(identifier);         
      }
      catch (SubscriptionError e)
      {
         throw new SOAPFaultException(buildFaultQName(e.getSubcode()), e.getReason(), null, null);
      }

   }

   private URI retrieveSubscriptionId()
   {
      URI subscriptionId = null;
      CommonMessageContext msgContext = MessageContextAssociation.peekMessageContext();
      AddressingProperties addrProps = (AddressingProperties)msgContext.getProperty(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);

      if (null == addrProps)
      {
         throw new SOAPFaultException(
               Constants.SOAP11_FAULT_CODE_CLIENT,
               "The message is not valid and cannot be processed:" +
                     "Cannot obtain addressing properties.",
               null, null
         );
      }

      ReferenceParameters refParams = addrProps.getReferenceParameters();
      if (refParams != null)
      {
         for (Object obj : refParams.getElements())
         {
            if (obj instanceof Element)
            {
               Element el = (Element)obj;
               QName qname = DOMUtils.getElementQName(el);
               if (qname.equals(IDQN))
               {
                  try
                  {
                     subscriptionId = new URI(DOMUtils.getTextContent(el));
                     break;
                  }
                  catch (URISyntaxException e)
                  {
                     throw new SOAPFaultException(
                           Constants.SOAP11_FAULT_CODE_CLIENT,
                           "The message is not valid and cannot be processed:" +
                                 "Invalid subscription id.",
                           null, null
                     );
                  }
               }
            }
         }
      }

      if (null == subscriptionId)
      {
         throw new SOAPFaultException(
               buildFaultQName(EventingConstants.CODE_INVALID_MESSAGE),
               "The message is not valid and cannot be processed."
                     + "Cannot obtain subscription id.",
               null, null
         );
      }

      return subscriptionId;
   }
}
