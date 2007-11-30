package org.jboss.ws.extensions.wsrm.transport;

import static org.jboss.ws.extensions.wsrm.RMConstant.*;

import java.net.URI;
import java.util.Map;

import org.jboss.logging.Logger;
import org.jboss.ws.extensions.wsrm.RMConstant;
import org.jboss.ws.extensions.wsrm.RMSequenceImpl;

public final class RMTransportHelper
{

   private static Logger logger = Logger.getLogger(RMTransportHelper.class);
   
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
      RMMetadata meta = rmRequest.getMetadata();
      if (meta == null) throw new RuntimeException("Unable to obtain wsrm metadata");
      Map<String, Object> invCtx = meta.getContext(RMConstant.INVOCATION_CONTEXT);
      if (invCtx == null) throw new RuntimeException("Unable to obtain invocation context");
      Map<String, Object> wsrmReqCtx = (Map<String, Object>)invCtx.get(RMConstant.REQUEST_CONTEXT);
      Boolean isOneWay = (Boolean)wsrmReqCtx.get(ONE_WAY_OPERATION); 
      logger.debug("oneWayMessage == " + (isOneWay == null ? false : isOneWay.booleanValue()));
      logger.debug("messages == " + wsrmReqCtx.get(PROTOCOL_MESSAGES));
      return isOneWay == null ? false : isOneWay.booleanValue();
   }

}
