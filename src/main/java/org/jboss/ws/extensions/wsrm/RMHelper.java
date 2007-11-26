package org.jboss.ws.extensions.wsrm;

import static org.jboss.ws.extensions.wsrm.RMConstant.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.jboss.logging.Logger;
import org.jboss.ws.extensions.wsrm.spi.Provider;
import org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequence;
import org.jboss.ws.extensions.wsrm.spi.protocol.Serializable;

public final class RMHelper
{
   private static final Logger log = Logger.getLogger(RMHelper.class);
   
   private RMHelper()
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
      String retVal = (String)wsrmRequestCtx.get(WSA_MESSAGE_ID);
      if (retVal == null)
         throw new RuntimeException();
      return retVal;
   }
   
   public static URI getBackPortURI(RMMessage rmRequest)
   {
      Map<String, Object> invocationCtx = (Map<String, Object>)rmRequest.getMetadata().getContext(INVOCATION_CONTEXT);
      Map<String, Object> wsrmRequestCtx = (Map<String, Object>)invocationCtx.get(REQUEST_CONTEXT);
      List<Serializable> outMsgs = (List<Serializable>)wsrmRequestCtx.get(PROTOCOL_MESSAGES);
      Map<QName, Serializable> msgs = (Map<QName, Serializable>)wsrmRequestCtx.get(PROTOCOL_MESSAGES_MAPPING);
      QName createSequenceQName = Provider.get().getConstants().getCreateSequenceQName();
      URI retVal = null;
      if (outMsgs.contains(createSequenceQName))
      {
         CreateSequence cs = (CreateSequence)msgs.get(createSequenceQName);
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
   
   public static boolean isOneWayOperation(RMMessage rmRequest)
   {
      return (Boolean)rmRequest.getMetadata().getContext(RMConstant.INVOCATION_CONTEXT).get(ONE_WAY_OPERATION);
   }

}
