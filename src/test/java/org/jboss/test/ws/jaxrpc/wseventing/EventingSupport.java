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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.Stub;
import javax.xml.ws.addressing.AddressingBuilder;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.JAXWSAConstants;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.ws.core.jaxrpc.StubExt;
import org.jboss.ws.extensions.addressing.AddressingClientUtil;
import org.jboss.ws.extensions.eventing.EventSourceEndpoint;
import org.jboss.ws.extensions.eventing.EventingConstants;
import org.jboss.ws.extensions.eventing.SubscriptionManagerEndpoint;
import org.jboss.ws.extensions.eventing.element.DeliveryType;
import org.jboss.ws.extensions.eventing.element.EndpointReference;
import org.jboss.ws.extensions.eventing.element.FilterType;
import org.jboss.ws.extensions.eventing.element.SubscribeRequest;
import org.jboss.ws.extensions.eventing.element.SubscribeResponse;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.tools.wsdl.WSDLDefinitionsFactory;

/**
 * Eventing test case support.
 *
 * @author Heiko Braun, <heiko@openj.net>
 * @since 17-Jan-2006
 */
public class EventingSupport extends JBossWSTest
{

   EventSourceEndpoint eventSourcePort;

   SubscriptionManagerEndpoint managerPort;

   protected URI eventSourceURI = null;

   private static int msgId = 0;

   protected String eventString = 
      "<WindReport type='critical'>\n" + 
      "    <Date>030701</Date>\n" + 
      "    <Time>0041</Time>\n" + 
      "    <Speed>65</Speed>\n" + 
      "    <Location>BRADENTON BEACH</Location>\n" + 
      "    <County>MANATEE</County>\n" + 
      "    <State>FL</State>\n" + 
      "    <Lat>2746</Lat>\n" + 
      "    <Long>8270</Long>\n" + 
      "    <Comments xml:lang='en-US' >\n" + 
      "        WINDS 55 WITH GUSTS TO 65. ROOF TORN OFF BOAT HOUSE. REPORTED\n" + 
      "        BY STORM SPOTTER. (TBW)\n" + 
      "    </Comments>\n" + 
      "</WindReport>";

   protected static AddressingBuilder addrBuilder = AddressingBuilder.getAddressingBuilder();

   protected void setUp() throws Exception
   {
      super.setUp();

      if (eventSourcePort == null)
      {
         InitialContext iniCtx = getInitialContext();
         Service service = (Service)iniCtx.lookup("java:comp/env/service/EventingService");
         eventSourcePort = (EventSourceEndpoint)service.getPort(EventSourceEndpoint.class);
         managerPort = (SubscriptionManagerEndpoint)service.getPort(SubscriptionManagerEndpoint.class);
      }

      eventSourceURI = new URI("http://schemas.xmlsoap.org/ws/2004/08/eventing/Warnings");

      // register event source
      /*ObjectName oname = EventingConstants.getSubscriptionManagerName();
       getServer().invoke(oname, "registerEventSource", new Object[] { new URI(eventSourceURI) }, new String[] { "java.net.URI" });*/
   }

   protected void tearDown() throws Exception
   {

      super.tearDown();

      // remove event source
      /*ObjectName oname = EventingConstants.getSubscriptionManagerName();
       getServer().invoke(oname, "removeEventSource", new Object[] { new URI(eventSourceURI) }, new String[] { "java.net.URI" });
       */
   }

   // ----------------------------------------------------------------------------
   protected SubscribeResponse doSubscribe()
   {

      try
      {
         // append message correlation headers
         AddressingProperties  requestProps = AddressingClientUtil.createDefaultProps(
               EventingConstants.SUBSCRIBE_ACTION, eventSourceURI.toString()
         );
         requestProps.setMessageID(AddressingClientUtil.createMessageID());
         setRequestProperties((StubExt)eventSourcePort, requestProps);

         // subscription
         SubscribeRequest request = new SubscribeRequest();
         DeliveryType delivery = getDefaultDelivery();
         request.setDelivery(delivery);
         request.setEndTo(delivery.getNotifyTo());
         request.setFilter(getDefaultFilter());
         SubscribeResponse subscribeResponse = eventSourcePort.subscribe(request);
         assertNotNull(subscribeResponse);

         AddressingProperties responseProps = getResponseProperties((StubExt)eventSourcePort);
         assertEquals(responseProps.getAction().getURI().toString(), EventingConstants.SUBSCRIBE_RESPONSE_ACTION);

         return subscribeResponse;
      }
      catch (Exception e)
      {
         fail("Failed to create subscription: " + e.getMessage());
         return null;
      }

   }

   protected DeliveryType getDefaultDelivery()
   {
      try
      {
         DeliveryType delivery = new DeliveryType();
         delivery.setMode(EventingConstants.getDeliveryPush());
         EndpointReference notifyEPR = new EndpointReference();
         notifyEPR.setAddress(new URI("http://" + getServerHost() + ":8080/jaxrpc-wseventing/eventSink"));
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

   public static URI subscriptionIdFromResponse(SubscribeResponse response)
   {
      return response.getSubscriptionManager().getReferenceParams().getIdentifier();
   }

   public static void setSubscriptionId(Stub stub, URI identifier)
   {
      stub._setProperty("subscriptionId", identifier);
   }

   public static void setTo(Stub stub, URI wsaTo)
   {
      //stub._setProperty(SOAPClientHandler.CLIENT_ADDRESSING_REQUEST_TO, wsaTo);
   }

   public static void setAction(Stub stub, URI wsaAction)
   {
      //stub._setProperty(SOAPClientHandler.CLIENT_ADDRESSING_REQUEST_ACTION, wsaAction);
   }

   public static URI setMessageId(Stub stub) throws Exception
   {
      URI messageId = new URI("http://www.example.org/eventSink/message#" + (msgId++));
      //stub._setProperty(SOAPClientHandler.CLIENT_ADDRESSING_REQUEST_MESSAGE_ID, messageId);
      return messageId;
   }

   protected void assertReplyAction(Stub stub, URI expectedAction) throws Exception
   {
      // see AddrConstraintsHandler
      stub._setProperty("wsa:expectedReplyAction", expectedAction);
   }

   protected void assertRelatesTo(Stub stub, URI expectedRelatesTo) throws Exception
   {
      // see AddrConstraintsHandler
      stub._setProperty("wsa:expectedRelatesTo", expectedRelatesTo);
   }

   protected void assertWSDLAccess() throws MalformedURLException
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxrpc-wseventing/subscribe?wsdl");
      WSDLDefinitionsFactory factory = WSDLDefinitionsFactory.newInstance();
      WSDLDefinitions wsdlDefinitions = factory.parse(wsdlURL);
      assertNotNull(wsdlDefinitions);
   }

   public static void setRequestProperties(Stub stub, AddressingProperties props) {
      stub._setProperty(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND, props);
   }

   protected AddressingProperties getResponseProperties(Stub stub) {
      return (AddressingProperties)stub._getProperty(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_INBOUND);
   }

   protected static String buildIdentifierElement(URI subscriptionId)
   {
      final QName IDQN = new QName("http://schemas.xmlsoap.org/ws/2004/08/eventing", "Identifier", "ns1");
      String qualname = IDQN.getPrefix() + ":" + IDQN.getLocalPart();
      StringBuffer buffer = new StringBuffer("<" + qualname);
      buffer.append(" xmlns:" + IDQN.getPrefix() + "='" + IDQN.getNamespaceURI() + "'");
      buffer.append(">" + subscriptionId + "</" + qualname + ">");
      return buffer.toString();
   }

   public static AddressingProperties buildFollowupProperties(SubscribeResponse response, String wsaAction, String wsaTo) {
      try
      {
         AddressingProperties props = addrBuilder.newAddressingProperties();
         props.initializeAsDestination(
               response.getSubscriptionManager().toWsaEndpointReference()
         );
         props.setTo(addrBuilder.newURI(wsaTo));
         props.setAction(addrBuilder.newURI(wsaAction));
         props.setMessageID(AddressingClientUtil.createMessageID());
         return props;
      }
      catch (URISyntaxException e)
      {
         throw new IllegalArgumentException(e.getMessage());
      }
   }
}
