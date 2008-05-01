package org.jboss.ws.extensions.wsrm.transport;

/**
 * Constructs RM message instances
 *
 * @author richard.opalka@jboss.com
 */
public class RMMessageFactory
{
   
   private RMMessageFactory()
   {
      // forbidden inheritance
   }
   
   public static RMMessage newMessage(byte[] payload, RMMetadata rmMetadata)
   {
      return new RMMessageImpl(payload, rmMetadata);
   }
   
}
