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
package org.jboss.test.ws.jaxrpc.wseventing;

// $Id$

import java.net.URI;
import java.rmi.RemoteException;
import java.util.Date;

import javax.xml.ws.addressing.AddressingProperties;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.ws.core.jaxrpc.StubExt;
import org.jboss.ws.extensions.addressing.AddressingClientUtil;
import org.jboss.ws.extensions.eventing.EventingConstants;
import org.jboss.ws.extensions.eventing.element.DeliveryType;
import org.jboss.ws.extensions.eventing.element.RenewRequest;
import org.jboss.ws.extensions.eventing.element.RenewResponse;
import org.jboss.ws.extensions.eventing.element.StatusRequest;
import org.jboss.ws.extensions.eventing.element.StatusResponse;
import org.jboss.ws.extensions.eventing.element.SubscribeRequest;
import org.jboss.ws.extensions.eventing.element.SubscribeResponse;
import org.jboss.ws.extensions.eventing.element.UnsubscribeRequest;

/**
 * Test the eventing endpoints.
 *
 * @author heiko@openj.net
 * @since 29-Apr-2005
 */
public class EventingTestCase extends EventingSupport {

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(EventingTestCase.class, "jaxrpc-wseventing.war, jaxrpc-wseventing-client.jar");
   }

   // ----------------------------------------------------------------------------

   public void testExpireDateInPast() throws Exception
   {
      assertWSDLAccess();

      //
      // If the expiration time is either a zero duration or a specific time that occurs in
      // the past according to the event source, then the request MUST fail,
      // and the event source MAY generate a wse:InvalidExpirationTime fault indicating
      // that an invalid expiration time was requested.
      //

      AddressingProperties  requestProps = AddressingClientUtil.createDefaultProps(
        EventingConstants.SUBSCRIBE_ACTION, eventSourceURI.toString()
      );
      requestProps.setMessageID(AddressingClientUtil.createMessageID());
      setRequestProperties((StubExt)eventSourcePort, requestProps);

      SubscribeRequest request = new SubscribeRequest();
      DeliveryType delivery = getDefaultDelivery();
      request.setDelivery(delivery);
      request.setEndTo(delivery.getNotifyTo());
      request.setExpires(new Date(System.currentTimeMillis() - 1000 * 60 * 60)); // invalid lease time
      request.setFilter(getDefaultFilter());

      try
      {
         SubscribeResponse subscribeResponse = eventSourcePort.subscribe(request);
        fail("Subscription should fail due to invalid lease time");
      }
      catch (RemoteException e)
      {
         // ignore expected exception
      }
      catch(Throwable t) {
         fail(t.getMessage()); // can be thrown by AddrConstraintsHandler
      }
   }

   public void testExpireDatePastMax() throws Exception
   {
      assertWSDLAccess();

      //
      // If such a source receives a Subscribe request containing a specific time expiration,
      // then the request MAY fail; if so, the event source MAY generate a
      // wse:UnsupportedExpirationType fault indicating that an unsupported expiration type was requested.
      //

      AddressingProperties  requestProps = AddressingClientUtil.createDefaultProps(
            EventingConstants.SUBSCRIBE_ACTION, eventSourceURI.toString()
      );
      requestProps.setMessageID(AddressingClientUtil.createMessageID());
      setRequestProperties((StubExt)eventSourcePort, requestProps);

      SubscribeRequest request = new SubscribeRequest();
      DeliveryType delivery = getDefaultDelivery();
      request.setDelivery(delivery);
      request.setEndTo(delivery.getNotifyTo());
      request.setExpires(new Date(System.currentTimeMillis() + EventingConstants.MAX_LEASE_TIME + 1000));
      request.setFilter(getDefaultFilter());

      try
      {
         SubscribeResponse subscribeResponse = eventSourcePort.subscribe(request);
         fail("Subscription should fail due to exceedd max lease time");
      }
      catch (RemoteException e)
      {
         // ignore expected exception
      }
      catch(Throwable t) {
         fail(t.getMessage()); // can be thrown by AddrConstraintsHandler
      }
   }

   public void testRenewal() throws Exception {
      assertWSDLAccess();

      //
      // To update the expiration for a subscription,
      // subscription managers MUST support requests to renew subscriptions.
      //
      SubscribeResponse response = doSubscribe();

      AddressingProperties requestProps =
            buildFollowupProperties(response, EventingConstants.RENEW_ACTION, eventSourceURI.toString());
      setRequestProperties((StubExt)managerPort, requestProps);

      Date oldLeaseTime = response.getExpires();
      RenewRequest renewRequest = new RenewRequest();
      RenewResponse renewResponse = managerPort.renew(renewRequest);
      assertNotNull(renewResponse);
      assertNotNull(renewResponse.getExpires());
      AddressingProperties responseProps = getResponseProperties((StubExt)managerPort);
      assertEquals(responseProps.getAction().getURI().toString(), EventingConstants.RENEW_RESPONSE_ACTION);
      assertTrue("Lease time not increased", oldLeaseTime.getTime() < renewResponse.getExpires().getTime());
   }

   public void testSubscriptionStatus() throws Exception
   {
      //
      // If the subscription is valid and has not expired,
      // the subscription manager MUST reply with the actual lease time.
      //
      SubscribeResponse response = doSubscribe();

      AddressingProperties requestProps =
            buildFollowupProperties(response, EventingConstants.GET_STATUS_ACTION, eventSourceURI.toString());
      setRequestProperties((StubExt)managerPort, requestProps);

      Date expectedLeaseTime = response.getExpires();
      StatusRequest statusRequest = new StatusRequest();
      StatusResponse statusResponse = managerPort.getStatus(statusRequest);
      assertNotNull(statusResponse);
      assertNotNull(statusResponse.getExpires());
      AddressingProperties responseProps = getResponseProperties((StubExt)managerPort);
      assertEquals(responseProps.getAction().getURI().toString(), EventingConstants.GET_STATUS_RESPONSE_ACTION);
      assertEquals("Incorrect lease time", expectedLeaseTime, statusResponse.getExpires());
   }

   public void testUnsubscribe() throws Exception
   {
      assertWSDLAccess();

      // Though subscriptions expire eventually, to minimize resources,
      // the subscribing event sink SHOULD explicitly delete a subscription
      // when it no longer wants notifications associated with the subscription.
      SubscribeResponse response = doSubscribe();
      URI identifier = subscriptionIdFromResponse(response);

      AddressingProperties requestProps =
            buildFollowupProperties(response, EventingConstants.UNSUBSCRIBE_ACTION, eventSourceURI.toString());
      setRequestProperties((StubExt)managerPort, requestProps);

      UnsubscribeRequest unsubscribeRequest = new UnsubscribeRequest();
      managerPort.unsubscribe(unsubscribeRequest);

      AddressingProperties responseProps = getResponseProperties((StubExt)managerPort);
      assertEquals(responseProps.getAction().getURI().toString(), EventingConstants.UNSUBSCRIBE_RESPONSE_ACTION);

      try
      {

         AddressingProperties renewProps =
             buildFollowupProperties(response, EventingConstants.RENEW_ACTION, eventSourceURI.toString());
         setRequestProperties((StubExt)managerPort, requestProps);

         RenewRequest renewRequest = new RenewRequest();
         managerPort.renew(renewRequest);  // this should fail 

         fail("Subscription not removed");
      }
      catch (Exception e)
      {
         //
      }
   }

   // Header/wsa:RelatesTo MUST be the value of the wsa:MessageID of the corresponding request.
   public void testMessageCorrelation()
   {
      // this is actually tested within subscribe operations.
      // look for usage of assertRelatesTo()
   }

   //
   // "http://schemas.xmlsoap.org/ws/2004/08/eventing/DeliveryFailure"
   // This value MUST be used if the event source terminated the subscription
   // because of problems delivering notifications.
   //
   public void testSubscriptionEndMessage()
   {
      System.out.println("FIXME: [JBWS-798] Complete EventingTestCase");
   }

   //
   // "http://schemas.xmlsoap.org/ws/2004/08/eventing/SourceShuttingDown"
   // This value MUST be used if the event source terminated the subscription
   // because the source is being shut down in a controlled manner;
   // that is, if the event source is being shut down but has the opportunity
   // to send a SubscriptionEnd message before it exits.
   //
   public void testShutdownMessage()
   {
      System.out.println("FIXME: [JBWS-798] Complete EventingTestCase");
   }

   //
   // "http://schemas.xmlsoap.org/ws/2004/08/eventing/SourceCanceling"
   // This value MUST be used if the event source terminated the subscription
   // for some other reason before it expired.
   //
   public void testCancelMessage()
   {
      System.out.println("FIXME: [JBWS-798] Complete EventingTestCase");
   }
}
