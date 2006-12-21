package org.jboss.ws.extensions.eventing.deployment;

import java.io.IOException;

import org.jboss.ws.core.utils.DOMUtils;
import org.w3c.dom.Element;

/**
 * Eventsource endpoint deployment info.
 *
 * @author Heiko Braun, <heiko@openj.net>
 * @since 18-Jan-2006
 */
public class EventingEndpointDI {

   /* event source URI */
   private String name;

   /* notification schema */
   private Object schema;

   private Element schemaElement;

   private String portName;

   // event source endpoint address
   private String endpointAddress;

   public EventingEndpointDI(String name, Object schema) {
      this.name = name;
      this.schema = schema;
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

   public Object getSchema() {
      return schema;
   }

   public String getEndpointAddress() {
      return endpointAddress;
   }

   public void setEndpointAddress(String endpointAddress) {
      this.endpointAddress = endpointAddress;
   }

   Element getSchemaElement() {
      try
      {
         if(null == this.schemaElement)
            this.schemaElement = DOMUtils.parse((String)getSchema());
      }
      catch (IOException e)
      {
         throw new IllegalArgumentException("Failed to parse notification schema:" +e.getMessage());
      }

      return this.schemaElement;
   }

}
