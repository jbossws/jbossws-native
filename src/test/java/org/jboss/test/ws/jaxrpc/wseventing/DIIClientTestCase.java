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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceFactory;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.rpc.handler.HandlerRegistry;
import javax.xml.ws.addressing.AddressingBuilder;
import javax.xml.ws.addressing.AddressingProperties;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.ws.core.jaxrpc.ServiceExt;
import org.jboss.ws.core.jaxrpc.ServiceFactoryImpl;
import org.jboss.ws.core.jaxrpc.StubExt;
import org.jboss.ws.extensions.addressing.AddressingClientUtil;
import org.jboss.ws.extensions.addressing.jaxrpc.WSAddressingClientHandler;
import org.jboss.ws.extensions.eventing.EventSourceEndpoint;
import org.jboss.ws.extensions.eventing.EventingConstants;
import org.jboss.ws.extensions.eventing.SubscriptionManagerEndpoint;
import org.jboss.ws.extensions.eventing.element.DeliveryType;
import org.jboss.ws.extensions.eventing.element.EndpointReference;
import org.jboss.ws.extensions.eventing.element.FilterType;
import org.jboss.ws.extensions.eventing.element.SubscribeRequest;
import org.jboss.ws.extensions.eventing.element.SubscribeResponse;
import org.jboss.ws.extensions.eventing.element.UnsubscribeRequest;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.tools.wsdl.WSDLDefinitionsFactory;

/**
 * @author Heiko Braun, <heiko@openj.net>
 * @since 19-Jan-2006
 */
public class DIIClientTestCase extends JBossWSTest
{
   private String NAMESPACE = "http://schemas.xmlsoap.org/ws/2004/08/eventing";
   private final QName SERVICE_NAME = new QName(NAMESPACE, "EventingService");
   
   private String wsdlLocation;
   private URI eventSourceURI;
   
   private AddressingBuilder addrBuilder = AddressingBuilder.getAddressingBuilder();
   private static Class[] clientHandlerClasses = new Class[] { WSAddressingClientHandler.class };

   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(DIIClientTestCase.class, "jaxrpc-wseventing.war");
   }

   public void setUp() throws Exception
   {
      super.setUp();
      eventSourceURI = new URI("http://schemas.xmlsoap.org/ws/2004/08/eventing/Warnings");
      wsdlLocation = "http://" + getServerHost() + ":8080/jaxrpc-wseventing/subscribe?wsdl";
   }

   public void testSubscription() throws Exception
   {
      assertWSDLAccess();

      // construct the service
      ServiceExt service = buildService();

      // create subscription proxy
      EventSourceEndpoint subscribePort = buildSubscriptionEndpoint(service);

      // append message correlation headers
      AddressingProperties requestProps = AddressingClientUtil.createDefaultProps(EventingConstants.SUBSCRIBE_ACTION, eventSourceURI.toString());
      requestProps.setMessageID(AddressingClientUtil.createMessageID());
      EventingSupport.setRequestProperties((StubExt)subscribePort, requestProps);

      SubscribeRequest request = new SubscribeRequest();
      DeliveryType delivery = getDefaultDelivery();
      request.setDelivery(delivery);
      request.setEndTo(delivery.getNotifyTo());
      request.setFilter(getDefaultFilter());

      SubscribeResponse response = subscribePort.subscribe(request);
      assertNotNull(response);

      // create management proxy
      SubscriptionManagerEndpoint managerPort = buildManagementEndpoint(service);

      requestProps = EventingSupport.buildFollowupProperties(response, EventingConstants.UNSUBSCRIBE_ACTION, eventSourceURI.toString());
      requestProps.setTo(addrBuilder.newURI(eventSourceURI));
      EventingSupport.setRequestProperties((StubExt)managerPort, requestProps);

      UnsubscribeRequest unsubscribeRequest = new UnsubscribeRequest();
      managerPort.unsubscribe(unsubscribeRequest);
   }

   /**
    * Construct a client side service
    */
   private ServiceExt buildService() throws Exception
   {
      File mappingFile = new File("resources/jaxrpc/wseventing/WEB-INF/jaxrpc-mapping.xml");
      assertTrue("jaxrpc-mapping.xml found", mappingFile.exists());
      
      // construct service and setup handlers
      ServiceFactoryImpl serviceFactory = (ServiceFactoryImpl)ServiceFactory.newInstance();
      ServiceExt service = (ServiceExt)serviceFactory.createService(new URL(wsdlLocation), SERVICE_NAME, mappingFile.toURL());
      setupHandlerRegistry(service);

      return service;
   }

   /**
    * Build subscription proxy
    */
   private EventSourceEndpoint buildSubscriptionEndpoint(Service service) throws Exception
   {
      QName portQN = new QName(NAMESPACE, "EventSourcePort");
      return (EventSourceEndpoint)service.getPort(portQN, EventSourceEndpoint.class);
   }

   /**
    * Build management proxy
    */
   private SubscriptionManagerEndpoint buildManagementEndpoint(Service service) throws Exception
   {
      QName portQN = new QName(NAMESPACE, "SubscriptionManagerPort");
      return (SubscriptionManagerEndpoint)service.getPort(portQN, SubscriptionManagerEndpoint.class);
   }

   /**
    * Register the default eventing client side handlers.
    */
   private void setupHandlerRegistry(ServiceExt service)
   {
      HandlerRegistry registry = service.getDynamicHandlerRegistry();
      QName managerPortName = new QName(EventingConstants.NS_EVENTING, "SubscriptionManagerPort");
      QName subscribePortName = new QName(EventingConstants.NS_EVENTING, "EventSourcePort");

      List clientHandlerChain = new ArrayList(clientHandlerClasses.length);

      for (int i=0; i < clientHandlerClasses.length; i++)
      {
         Class cl = clientHandlerClasses[i];
         HandlerInfo handler = new HandlerInfo();
         handler.setHandlerClass(cl);
         clientHandlerChain.add(handler);
      }

      registry.setHandlerChain(subscribePortName, clientHandlerChain);
      registry.setHandlerChain(managerPortName, clientHandlerChain);
   }

   protected DeliveryType getDefaultDelivery()
   {
      try
      {
         DeliveryType delivery = new DeliveryType();
         delivery.setMode(EventingConstants.getDeliveryPush());
         EndpointReference notifyEPR = new EndpointReference();
         notifyEPR.setAddress(new URI("http://" + getServerHost() + ":8080/jaxrpc-eventing/eventSink"));
         delivery.setNotifyTo(notifyEPR);
         return delivery;
      }
      catch (URISyntaxException e)
      {
         throw new IllegalStateException(e.getMessage());
      }
   }

   protected FilterType getDefaultFilter()
   {
      FilterType filter = new FilterType();
      filter.setDialect(EventingConstants.getDialectXPath());
      filter.set_value("/WindReport/State/text()='FL'");
      return filter;
   }

   protected void assertWSDLAccess() throws MalformedURLException
   {
      URL wsdlURL = new URL(wsdlLocation);
      WSDLDefinitionsFactory factory = WSDLDefinitionsFactory.newInstance();
      WSDLDefinitions wsdlDefinitions = factory.parse(wsdlURL);
      assertNotNull(wsdlDefinitions);
   }
}
