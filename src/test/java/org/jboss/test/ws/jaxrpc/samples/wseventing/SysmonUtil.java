package org.jboss.test.ws.jaxrpc.samples.wseventing;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import javax.xml.rpc.Stub;
import javax.xml.ws.addressing.AddressingBuilder;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.JAXWSAConstants;

import org.jboss.logging.Logger;
import org.jboss.ws.extensions.addressing.AddressingClientUtil;
import org.jboss.ws.extensions.eventing.element.EndpointReference;
import org.jboss.ws.extensions.eventing.element.SubscribeResponse;

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
      EndpointReference managerEPR = subscribeResponse.getSubscriptionManager();
      URI subscriptionID = managerEPR.getReferenceParams().getIdentifier();
      Date expiryTime = subscribeResponse.getExpires();

      log.info("SubscriptionManager " + managerEPR.getAddress());
      log.info("SubscriptionID " + subscriptionID);
      log.info("ExpiryTime " + expiryTime);
   }

   /**
    * Bind request properties.
    */
   public static void setRequestProperties(Stub stub, AddressingProperties props)
   {
      stub._setProperty(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND, props);
   }

   /**
    * Access response properties.
    */
   public static AddressingProperties getResponseProperties(Stub stub)
   {
      return (AddressingProperties)stub._getProperty(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_INBOUND);
   }

   /**
    * Followup addressing properties basically use the
    * subscription id as wsa:ReferenceParameter
    */
   public static AddressingProperties buildFollowupProperties(SubscribeResponse response, String wsaAction, String wsaTo)
   {
      try
      {
         AddressingBuilder addrBuilder = AddressingBuilder.getAddressingBuilder();
         AddressingProperties props = addrBuilder.newAddressingProperties();
         props.initializeAsDestination(response.getSubscriptionManager().toWsaEndpointReference());
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
