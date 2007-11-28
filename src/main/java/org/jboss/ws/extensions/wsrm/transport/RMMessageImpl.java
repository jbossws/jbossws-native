package org.jboss.ws.extensions.wsrm.transport;

/**
 * RM message object 
 * @author richard.opalka@jboss.com
 */
public class RMMessageImpl implements RMMessage
{
   private final byte[] payload;
   private final RMMetadata rmMetadata;
   
   public RMMessageImpl(byte[] payload, RMMetadata rmMetadata)
   {
      super();
      this.payload = payload;
      this.rmMetadata = rmMetadata;
   }
   
   public byte[] getPayload()
   {
      return this.payload;
   }
   
   public RMMetadata getMetadata()
   {
      return this.rmMetadata;
   }
}
