package org.jboss.ws.extensions.wsrm.transport;

/**
 * Represents RM source
 *
 * @author richard.opalka@jboss.com
 */
public interface RMMessage
{
   byte[] getPayload();
   RMMetadata getMetadata();
}
