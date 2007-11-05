package org.jboss.ws.extensions.wsrm;

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
