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
package org.jboss.ws.extensions.eventing.mgmt;

// $Id$

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URL;
import java.util.Date;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;

import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;
import org.jboss.logging.Logger;
import org.jboss.ws.core.soap.SOAPConnectionImpl;
import org.jboss.ws.core.utils.DOMWriter;
import org.jboss.ws.extensions.eventing.EventingConstants;
import org.jboss.ws.extensions.eventing.element.EndpointReference;
import org.w3c.dom.Element;

/**
 * Represents a subscription.
 *
 * @author Heiko Braun, <heiko@openj.net>
 * @since 05-Jan-2006
 */
class Subscription
{
   final private static Logger log = Logger.getLogger(Subscription.class);

   final private EndpointReference notifyTo;
   final private EndpointReference endTo;
   private Date expires;
   final private Filter filter;
   final private EndpointReference endpointReference;
   final private URI eventSourceNS;

   public Subscription(URI eventSourceNS, EndpointReference endpointReference, EndpointReference notifyTo, EndpointReference endTo, Date expires, Filter filter)
   {
      this.eventSourceNS = eventSourceNS;
      this.notifyTo = notifyTo;
      this.endTo = endTo; // is optional, can be null
      this.expires = expires;
      this.filter = filter;
      this.endpointReference = endpointReference;
   }

   public void notify(Element event)
   {
      log.debug(getIdentifier() + " dispatching " + event);

      try
      {
         String eventXML = DOMWriter.printNode(event, false);
         MessageFactory msgFactory = MessageFactory.newInstance();

         // notification elements need to declare their namespace locally
         StringBuilder sb = new StringBuilder();
         sb.append("<env:Envelope xmlns:env='http://www.w3.org/2003/05/soap-envelope' ");
         sb.append("xmlns:wse='").append(EventingConstants.NS_EVENTING).append("' ");
         sb.append("xmlns:wsa='").append(EventingConstants.NS_ADDRESSING).append("'>");
         sb.append("<env:Header>");
         sb.append("<wsa:Action>").append(getNotificationAction()).append("</wsa:Action>");
         // todo: add reference parameters when wildcards are supported
         sb.append("<wsa:To>").append(notifyTo.getAddress().toString()).append("</wsa:To>");
         sb.append("</env:Header>");
         sb.append("<env:Body>");
         sb.append(eventXML);
         sb.append("</env:Body>");
         sb.append("</env:Envelope>");

         SOAPMessage reqMsg = msgFactory.createMessage(null, new ByteArrayInputStream(sb.toString().getBytes()));
         URL epURL = notifyTo.getAddress().toURL();
         new SOAPConnectionImpl().callOneWay(reqMsg, epURL);
      }
      catch (Exception e)
      {
         // todo: this should get back to manager
         log.error("Failed to send notification message", e);
      }
   }

   public boolean accepts(Element event)
   {

      boolean b = true;
      if (filter != null)
      {

         try
         {
            XObject o = XPathAPI.eval(event, filter.getExpression());
            b = o.bool();
         }
         catch (TransformerException e)
         {
            log.error("Failed to evalute xpath expression", e);
         }

      }
      return b;
   }

   public void end(String status)
   {
      if (null == endTo) // it's an optional field.
         return;

      log.debug("Ending subscription " + getIdentifier());

      StringBuffer sb = new StringBuffer();
      sb.append("<env:Envelope xmlns:env='http://www.w3.org/2003/05/soap-envelope' ");
      sb.append("xmlns:wse='").append(EventingConstants.NS_EVENTING).append("' ");
      sb.append("xmlns:wsa='").append(EventingConstants.NS_ADDRESSING).append("'>");
      sb.append("<env:Header>");
      sb.append("<wsa:Action>").append(EventingConstants.SUBSCRIPTION_END_ACTION).append("</wsa:Action>");
      sb.append("<wsa:To>").append(endTo.getAddress().toString()).append("</wsa:To>");
      sb.append("</env:Header>");
      sb.append("<env:Body>");

      sb.append("<wse:SubscriptionEnd>");
      sb.append("<wse:SubscriptionManager>");
      sb.append("<wsa:Address>");
      sb.append(endpointReference.getAddress().toString());
      sb.append("</wsa:Address>");
      sb.append("<wsa:ReferenceParameters>");
      sb.append("<wse:Identifier>");
      sb.append(getIdentifier().toString());
      sb.append("</wse:Identifier>");
      sb.append("</wsa:ReferenceParameters>");
      sb.append("</wse:SubscriptionManager>");

      sb.append("<wse:Status>").append(status).append("</wse:Status>");
      sb.append("<wse:Reason/>");
      sb.append("</wse:SubscriptionEnd>");

      sb.append("</env:Body>");
      sb.append("</env:Envelope>");

      try
      {
         MessageFactory msgFactory = MessageFactory.newInstance();
         SOAPMessage reqMsg = msgFactory.createMessage(null, new ByteArrayInputStream(sb.toString().getBytes()));
         URL epURL = endTo.getAddress().toURL();
         new SOAPConnectionImpl().callOneWay(reqMsg, epURL);
      }
      catch (Exception e)
      {
         log.warn("Failed to send subscription end message", e);
      }

   }

   private String getNotificationAction()
   {
      return this.eventSourceNS.toString() + "/Notification";
   }

   public boolean isExpired()
   {
      return System.currentTimeMillis() > expires.getTime();
   }

   public EndpointReference getNotifyTo()
   {
      return notifyTo;
   }

   public EndpointReference getEndTo()
   {
      return endTo;
   }

   public Date getExpires()
   {
      return expires;
   }

   public Filter getFilter()
   {
      return filter;
   }

   public EndpointReference getEndpointReference()
   {
      return endpointReference;
   }

   public URI getIdentifier()
   {
      return endpointReference.getReferenceParams().getIdentifier();
   }

   public void setExpires(Date expires)
   {
      this.expires = expires;
   }
}
