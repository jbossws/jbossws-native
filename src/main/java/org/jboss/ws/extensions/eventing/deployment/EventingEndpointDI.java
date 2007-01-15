package org.jboss.ws.extensions.eventing.deployment;

/**
 * Eventsource endpoint deployment info.
 *
 * @author Heiko Braun, <heiko@openj.net>
 * @since 18-Jan-2006
 */
public class EventingEndpointDI {

   /* event source URI */
   private String name;

   private String portName;

   // event source endpoint address
   private String endpointAddress;

   /* notification schema */
   private String[] schema;

   private String notificationRootElementNS;

   public EventingEndpointDI(String name, String[] schema, String notificationRootElementNS) {
      this.name = name;
      this.schema = schema;
      this.notificationRootElementNS = notificationRootElementNS;
   }

   public String getPortName() {
      return portName;
   }

   public void setPortName(String portName) {
      this.portName = portName;
   }

   public String getName() {
      return name;
   }

   public String[] getSchema() {
      return schema;
   }

   public String getEndpointAddress() {
      return endpointAddress;
   }

   public void setEndpointAddress(String endpointAddress) {
      this.endpointAddress = endpointAddress;
   }

   public String getNotificationRootElementNS() {
      return notificationRootElementNS;
   }

   public void setNotificationRootElementNS(String notificationRootElementNS) {
      this.notificationRootElementNS = notificationRootElementNS;
   }

}
