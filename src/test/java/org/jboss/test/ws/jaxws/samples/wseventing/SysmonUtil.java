package org.jboss.test.ws.jaxws.samples.wseventing;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.addressing.AddressingBuilder;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.EndpointReference;
import javax.xml.ws.addressing.JAXWSAConstants;

import org.jboss.logging.Logger;
import org.jboss.ws.core.soap.SOAPElementImpl;
import org.jboss.ws.extensions.addressing.AddressingClientUtil;
import org.jboss.ws.extensions.eventing.EventingConstants;
import org.jboss.ws.extensions.eventing.jaxws.AttributedURIType;
import org.jboss.ws.extensions.eventing.jaxws.DeliveryType;
import org.jboss.ws.extensions.eventing.jaxws.EndpointReferenceType;
import org.jboss.ws.extensions.eventing.jaxws.FilterType;
import org.jboss.ws.extensions.eventing.jaxws.SubscribeResponse;

/**
 * Util methods that drive the SysmoneTestCase.
 *
 * @author Heiko Braun, <heiko@openj.net>
 * @since 28-Mar-2006
 */
public class SysmonUtil
{
   // provide logging
   private static final Logger log = Logger.getLogger(SysmonUtil.class);

   public static void printSubscriptionDetails(SubscribeResponse subscribeResponse)
   {
      EndpointReferenceType managerEPR = subscribeResponse.getSubscriptionManager();
      String subscriptionID = subscribeResponse.getSubscriptionId();
      Date expiryTime = subscribeResponse.getExpires();

      log.info("SubscriptionManager " + managerEPR.getAddress());
      log.info("SubscriptionID " + subscriptionID);
      log.info("ExpiryTime " + expiryTime);
   }

   /**
    * Bind request properties.
    */
   public static void setRequestProperties(BindingProvider binding, AddressingProperties props)
   {
      binding.getRequestContext().put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND, props);
   }

   /**
    * Access response properties.
    */
   public static AddressingProperties getResponseProperties(BindingProvider binding)
   {
      return (AddressingProperties)binding.getResponseContext().get(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_INBOUND);
   }

   /**
    * Followup addressing properties basically use the
    * subscription id as wsa:ReferenceParameter
    */
   public static AddressingProperties buildFollowupProperties(SubscribeResponse response, String wsaAction, String wsaTo) {
      try
      {
         AddressingBuilder addrBuilder = AddressingBuilder.getAddressingBuilder();
         AddressingProperties followupProps = addrBuilder.newAddressingProperties();

         //followupProps.setTo(addrBuilder.newURI(wsaTo));
         followupProps.setAction(addrBuilder.newURI(wsaAction));
         followupProps.setMessageID(AddressingClientUtil.createMessageID());

         // necessary in order to parse ref params
         EndpointReference epr = addrBuilder.newEndpointReference(new URI(wsaTo));

         String subscriptionID = response.getSubscriptionId();
         SOAPElementImpl element = new SOAPElementImpl(
            "Identifier",
            "wse", "http://schemas.xmlsoap.org/ws/2004/08/eventing"
         );
         element.setValue(subscriptionID);
         epr.getReferenceParameters().addElement(element);

         followupProps.initializeAsDestination(epr);

         return followupProps;
      }
      catch (URISyntaxException e)
      {
         throw new IllegalArgumentException(e.getMessage());
      }
   }

   public static DeliveryType getDefaultDelivery()
   {
      try
      {
         DeliveryType delivery = new DeliveryType();
         delivery.setMode(EventingConstants.getDeliveryPush().toString());
         EndpointReferenceType notifyEPR = new EndpointReferenceType();
         AttributedURIType attURI = new AttributedURIType();
         attURI.setValue("http://jboss.org");
         notifyEPR.setAddress(attURI);
         delivery.setNotifyTo(notifyEPR);
         return delivery;
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Failed to create delivery payload", e);
      }
   }

   public static FilterType wrapFilterString(String s)
   {
      FilterType filter = new FilterType();
      filter.setDialect(EventingConstants.getDialectXPath().toString());
      filter.getContent().add(s);
      return filter;
   }
}
