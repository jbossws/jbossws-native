package org.jboss.ws.extensions.eventing.deployment;

import org.jboss.ws.WSException;
import org.jboss.ws.core.server.ServiceEndpoint;
import org.jboss.ws.core.server.ServiceEndpointInfo;
import org.jboss.ws.extensions.eventing.EventingConstants;
import org.jboss.ws.extensions.eventing.metadata.EventingEpMetaExt;
import org.jboss.ws.extensions.eventing.mgmt.SubscriptionManagerFactory;
import org.jboss.ws.extensions.eventing.mgmt.SubscriptionManagerMBean;
import org.jboss.ws.metadata.umdm.ServerEndpointMetaData;

/**
 * EventingEndpointLifecycle is responsible to create event sources
 * and register them with the subscripion manager when a service endpoint is created.
 *
 * @author Heiko Braun, <heiko@openj.net>
 * @since 22-Mar-2006
 */
public class EventingEndpoint extends ServiceEndpoint
{
   // encupsulates access to subscription manager
   private SubscriptionManagerFactory factory = SubscriptionManagerFactory.getInstance();

   public EventingEndpoint(ServiceEndpointInfo seInfo)
   {
      super(seInfo);
   }

   /**
    * Register event source with subscription manager.
    */
   public void create() throws Exception
   {
      super.create();

      ServerEndpointMetaData epMetaData = seInfo.getServerEndpointMetaData();
      EventingEpMetaExt ext = (EventingEpMetaExt)epMetaData.getExtension(EventingConstants.NS_EVENTING);
      if (null == ext)
         throw new WSException("Cannot obtain eventing meta data");

      // Currently several endpoints may belong to an event source deployment.
      // Therefore we have to avoid duplicate registrations
      // Actually there should be a 1:n mapping of event source NS to endpoints.
      // See also http://jira.jboss.org/jira/browse/JBWS-770

      // create pending incomplete event source
      EventingEndpointDI desc = new EventingEndpointDI(ext.getEventSourceNS(), ext.getNotificationSchema(), ext.getNotificationRootElementNS());
      desc.setEndpointAddress(epMetaData.getEndpointAddress());
      desc.setPortName(epMetaData.getPortComponentName());

      SubscriptionManagerMBean manager = factory.getSubscriptionManager();
      manager.registerEventSource(desc);
   }

   /**
    * Unregister event source with subscription manager.    
    */
   public void destroy()
   {
      ServerEndpointMetaData epMetaData = seInfo.getServerEndpointMetaData();
      EventingEpMetaExt ext = (EventingEpMetaExt)epMetaData.getExtension(EventingConstants.NS_EVENTING);
      if (null == ext)
         throw new WSException("Cannot obtain eventing meta data");

      SubscriptionManagerMBean manager = factory.getSubscriptionManager();
      manager.removeEventSource(ext.getEventSourceURI());

      super.destroy();
   }
}
