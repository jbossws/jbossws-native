package org.jboss.ws.extensions.wsrm;

import java.util.Map;

public final class RMHelper
{
   private RMHelper()
   {
      // no instances
   }
   
   public static boolean isRMMessage(Map<String, Object> ctx)
   {
      return (ctx != null) && (ctx.containsKey(RMConstant.DATA)); 
   }
}
