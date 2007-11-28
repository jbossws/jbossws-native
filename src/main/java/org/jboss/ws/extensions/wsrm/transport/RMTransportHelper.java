package org.jboss.ws.extensions.wsrm.transport;

import static org.jboss.ws.extensions.wsrm.RMConstant.*;

import java.net.URI;
import java.util.Map;

import org.jboss.ws.extensions.wsrm.RMConstant;
import org.jboss.ws.extensions.wsrm.RMSequenceImpl;

public final class RMTransportHelper
{
   private RMTransportHelper()
   {
      // no instances
   }
   
   public static boolean isRMMessage(Map<String, Object> ctx)
   {
      return (ctx != null) && (ctx.containsKey(RMConstant.REQUEST_CONTEXT)); 
   }
   
   public static String getMessageId(RMMessage rmRequest)
   {
      return (String)getWsrmRequestContext(rmRequest).get(WSA_MESSAGE_ID);
   }
   
   public static URI getBackPortURI(RMMessage rmRequest)
   {
      return getSequence(rmRequest).getBackPort();
   }
   
   private static Map<String, Object> getWsrmRequestContext(RMMessage rmRequest)
   {
      Map<String, Object> invocationCtx = (Map<String, Object>)rmRequest.getMetadata().getContext(INVOCATION_CONTEXT);
      return (Map<String, Object>)invocationCtx.get(REQUEST_CONTEXT);
   }
   
   public static RMSequenceImpl getSequence(RMMessage rmRequest)
   {
      return (RMSequenceImpl)getWsrmRequestContext(rmRequest).get(SEQUENCE_REFERENCE);
   }
   
   public static boolean isOneWayOperation(RMMessage rmRequest)
   {
      return (Boolean)rmRequest.getMetadata().getContext(RMConstant.INVOCATION_CONTEXT).get(ONE_WAY_OPERATION);
   }

}
