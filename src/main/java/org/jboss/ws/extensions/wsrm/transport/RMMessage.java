package org.jboss.ws.extensions.wsrm.transport;

public interface RMMessage
{
   byte[] getPayload();
   RMMetadata getMetadata();
}
