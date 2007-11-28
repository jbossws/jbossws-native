package org.jboss.ws.extensions.wsrm.transport;

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
