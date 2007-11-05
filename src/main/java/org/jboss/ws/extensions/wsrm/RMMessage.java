package org.jboss.ws.extensions.wsrm;

public interface RMMessage
{
   byte[] getPayload();
   RMMetadata getMetadata();
}
