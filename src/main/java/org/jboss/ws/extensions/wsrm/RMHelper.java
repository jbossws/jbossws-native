package org.jboss.ws.extensions.wsrm;

import org.jboss.ws.core.MessageAbstraction;

public final class RMHelper
{
   private RMHelper()
   {
      // no instances
   }
   
   public static boolean isRMMessage(MessageAbstraction requestMessage)
   {
      // TODO: here is the most suitable place to start RM resender
      return (new java.io.File("/home/ropalka/rm.enabled").exists()) && (requestMessage != null);
   }
}
