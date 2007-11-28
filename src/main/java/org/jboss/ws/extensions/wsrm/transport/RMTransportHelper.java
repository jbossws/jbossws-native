package org.jboss.ws.extensions.wsrm.transport;

import static org.jboss.ws.extensions.wsrm.RMConstant.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.jboss.logging.Logger;
import org.jboss.ws.extensions.wsrm.RMConstant;
import org.jboss.ws.extensions.wsrm.RMSequenceImpl;
import org.jboss.ws.extensions.wsrm.spi.RMProvider;
import org.jboss.ws.extensions.wsrm.spi.protocol.RMCreateSequence;
import org.jboss.ws.extensions.wsrm.spi.protocol.RMSerializable;

public final class RMTransportHelper
{
   private static final Logger log = Logger.getLogger(RMTransportHelper.class);
   
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
      Map<String, Object> invocationCtx = (Map<String, Object>)rmRequest.getMetadata().getContext(INVOCATION_CONTEXT);
      Map<String, Object> wsrmRequestCtx = (Map<String, Object>)invocationCtx.get(REQUEST_CONTEXT);
      return (String)wsrmRequestCtx.get(WSA_MESSAGE_ID);
   }
   
   public static URI getBackPortURI(RMMessage rmRequest)
   {
      Map<String, Object> invocationCtx = (Map<String, Object>)rmRequest.getMetadata().getContext(INVOCATION_CONTEXT);
      Map<String, Object> wsrmRequestCtx = (Map<String, Object>)invocationCtx.get(REQUEST_CONTEXT);
      List<RMSerializable> outMsgs = (List<RMSerializable>)wsrmRequestCtx.get(PROTOCOL_MESSAGES);
      Map<QName, RMSerializable> msgs = (Map<QName, RMSerializable>)wsrmRequestCtx.get(PROTOCOL_MESSAGES_MAPPING);
      QName createSequenceQName = RMProvider.get().getConstants().getCreateSequenceQName();
      URI retVal = null;
      if (outMsgs.contains(createSequenceQName))
      {
         RMCreateSequence cs = (RMCreateSequence)msgs.get(createSequenceQName);
         try
         {
            retVal = RMConstant.WSA_ANONYMOUS_URI.equals(cs.getAcksTo()) ? null : new URI(cs.getAcksTo());;
         }
         catch (Exception e)
         {
            log.warn(e.getMessage(), e);
         }
      }
      else
      {
         retVal = ((RMSequenceImpl)wsrmRequestCtx.get(SEQUENCE_REFERENCE)).getBackPort();
      }
      
      return retVal;
   }
   
   public static RMSequenceImpl getSequence(RMMessage rmRequest)
   {
      Map<String, Object> invocationCtx = (Map<String, Object>)rmRequest.getMetadata().getContext(INVOCATION_CONTEXT);
      Map<String, Object> wsrmRequestCtx = (Map<String, Object>)invocationCtx.get(REQUEST_CONTEXT);
      return (RMSequenceImpl)wsrmRequestCtx.get(SEQUENCE_REFERENCE);
   }
   
   public static boolean isOneWayOperation(RMMessage rmRequest)
   {
      return (Boolean)rmRequest.getMetadata().getContext(RMConstant.INVOCATION_CONTEXT).get(ONE_WAY_OPERATION);
   }

}
