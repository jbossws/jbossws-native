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
package org.jboss.test.ws.jaxrpc.samples.wseventing;

// $Id$

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;

import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.rpc.handler.HandlerRegistry;
import javax.xml.ws.addressing.AddressingProperties;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.ws.core.jaxrpc.ServiceFactoryImpl;
import org.jboss.ws.core.jaxrpc.ServiceImpl;
import org.jboss.ws.core.jaxrpc.StubExt;
import org.jboss.ws.extensions.addressing.AddressingClientUtil;
import org.jboss.ws.extensions.addressing.jaxrpc.WSAddressingClientHandler;
import org.jboss.ws.extensions.eventing.EventSourceEndpoint;
import org.jboss.ws.extensions.eventing.EventingConstants;
import org.jboss.ws.extensions.eventing.SubscriptionManagerEndpoint;
import org.jboss.ws.extensions.eventing.element.DeliveryType;
import org.jboss.ws.extensions.eventing.element.EndpointReference;
import org.jboss.ws.extensions.eventing.element.FilterType;
import org.jboss.ws.extensions.eventing.element.StatusRequest;
import org.jboss.ws.extensions.eventing.element.SubscribeRequest;
import org.jboss.ws.extensions.eventing.element.SubscribeResponse;
import org.jboss.ws.extensions.eventing.element.UnsubscribeRequest;

/**
 * Test the eventing example service.
 *
 * @author heiko@openj.net
 * @since 29-Apr-2005
 */
public class SysmonTestCase extends JBossWSTest
{

   // event source endpoint
   private EventSourceEndpoint subscriptionPort = null;

   // subscription manager endpoint
   private SubscriptionManagerEndpoint managementPort = null;

   // the logical event source name
   private final static String eventSourceURI = "http://www.jboss.org/sysmon/SystemInfo";

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(SysmonTestCase.class, "jaxrpc-samples-wseventing.war, jaxrpc-samples-wseventing-client.jar");
   }

   protected void setUp() throws Exception
   {
      super.setUp();

      if (subscriptionPort == null || managementPort == null)
      {
         if (isTargetServerJBoss())
         {
            InitialContext iniCtx = getInitialContext();
            Service service = (Service)iniCtx.lookup("java:comp/env/service/SysmonService");
            subscriptionPort = (EventSourceEndpoint)service.getPort(EventSourceEndpoint.class);
            managementPort = (SubscriptionManagerEndpoint)service.getPort(SubscriptionManagerEndpoint.class);
         }
         else
         {
            ServiceFactoryImpl factory = new ServiceFactoryImpl();
            URL wsdlURL = new File("resources/jaxrpc/samples/wseventing/WEB-INF/wsdl/sysmon.wsdl").toURL();
            URL mappingURL = new File("resources/jaxrpc/samples/wseventing/WEB-INF/jaxrpc-mapping.xml").toURL();
            
            QName serviceName = new QName("http://schemas.xmlsoap.org/ws/2004/08/eventing", "EventingService");
            QName portName1 = new QName("http://schemas.xmlsoap.org/ws/2004/08/eventing", "EventSourcePort");
            QName portName2 = new QName("http://schemas.xmlsoap.org/ws/2004/08/eventing", "SubscriptionManagerPort");
            
            ServiceImpl service = (ServiceImpl)factory.createService(wsdlURL, serviceName, mappingURL);
            HandlerRegistry registry = service.getDynamicHandlerRegistry();
            List infos1 = registry.getHandlerChain(portName1);
            infos1.add(new HandlerInfo(WSAddressingClientHandler.class, new HashMap(), new QName[]{}));
            registry.setHandlerChain(portName1, infos1);
            
            List infos2 = registry.getHandlerChain(portName2);
            infos2.add(new HandlerInfo(WSAddressingClientHandler.class, new HashMap(), new QName[]{}));
            registry.setHandlerChain(portName2, infos2);
            
            subscriptionPort = (EventSourceEndpoint)service.getPort(EventSourceEndpoint.class);
            managementPort = (SubscriptionManagerEndpoint)service.getPort(SubscriptionManagerEndpoint.class);
         }
      }
   }

   /**
    * Subscribe using a custom notification filter.
    */
   public void testSubscribe() throws Exception
   {
      SubscribeResponse subscribeResponse = doSubscribe("/SystemStatus/HostName/text()='localhost'");
      SysmonUtil.printSubscriptionDetails(subscribeResponse);
   }

   /**
    * Cancel subscription and check if it was really removed.
    */
   public void testUnsubscribe() throws Exception
   {

      SubscribeResponse subscribeResponse = doSubscribe(null);
      SysmonUtil.printSubscriptionDetails(subscribeResponse);

      EndpointReference managerEPR = subscribeResponse.getSubscriptionManager();
      URI subscriptionID = managerEPR.getReferenceParams().getIdentifier();

      // addressing correlation
      AddressingProperties unsubscribeProps = SysmonUtil.buildFollowupProperties(subscribeResponse, EventingConstants.UNSUBSCRIBE_ACTION, eventSourceURI);
      SysmonUtil.setRequestProperties((StubExt)managementPort, unsubscribeProps);
      managementPort.unsubscribe(new UnsubscribeRequest(subscriptionID));

      try
      {
         AddressingProperties getStatusProps = SysmonUtil.buildFollowupProperties(subscribeResponse, EventingConstants.GET_STATUS_ACTION, eventSourceURI);
         SysmonUtil.setRequestProperties((StubExt)managementPort, getStatusProps);

         managementPort.getStatus(new StatusRequest());

         fail("Unsubscribe error! The subscription was not removed");
      }
      catch (RemoteException e)
      {
         // this should fail
      }
   }

   private SubscribeResponse doSubscribe(String filterString) throws URISyntaxException, RemoteException
   {
      AddressingProperties reqProps = AddressingClientUtil.createDefaultProps(EventingConstants.SUBSCRIBE_ACTION, eventSourceURI);
      reqProps.setMessageID(AddressingClientUtil.createMessageID());
      SysmonUtil.setRequestProperties((StubExt)subscriptionPort, reqProps);

      // build subscription payload
      SubscribeRequest request = new SubscribeRequest();

      // default delivery type with notification EPR
      DeliveryType delivery = new DeliveryType();
      delivery.setMode(EventingConstants.getDeliveryPush());
      EndpointReference notifyEPR = new EndpointReference();
      notifyEPR.setAddress(new URI("http://jboss.org")); // bogus address, replace with a real notification endpoint
      delivery.setNotifyTo(notifyEPR);
      request.setDelivery(delivery);

      // receive endTo messages at the same port
      request.setEndTo(delivery.getNotifyTo());

      if (filterString != null)
      {
         // custom filter that applies to a certain hostname only
         FilterType filter = new FilterType();
         filter.setDialect(EventingConstants.getDialectXPath());
         filter.set_value(filterString);
         request.setFilter(filter);
      }

      // invoke subscription request
      SubscribeResponse subscriptionTicket = subscriptionPort.subscribe(request);

      // check message constraints
      AddressingProperties resProps = SysmonUtil.getResponseProperties((StubExt)subscriptionPort);
      assertEquals(reqProps.getMessageID().getURI(), resProps.getRelatesTo()[0].getID());

      return subscriptionTicket;
   }
}
