package org.jboss.ws.extensions.wsrm;

import java.util.Map;
import java.util.HashMap;
import org.jboss.remoting.marshal.Marshaller;
import org.jboss.remoting.marshal.UnMarshaller;

public final class RMMetadata
{
   private Map<String, Map<String, Object>> contexts = new HashMap<String, Map<String, Object>>();
   
   public RMMetadata(
         String targetAddress,
         boolean oneWay,
         Marshaller marshaller,
         UnMarshaller unmarshaller,
         Map<String, Object> invocationContext,
         Map<String, Object> remotingInvocationContext,
         Map<String, Object> remotingConfigurationContext)
   {
      if (targetAddress == null)
         throw new IllegalArgumentException("Target address cannot be null");
      
      invocationContext.put(RMConstant.TARGET_ADDRESS, targetAddress);
      invocationContext.put(RMConstant.ONE_WAY_OPERATION, oneWay);
      setContext(RMConstant.INVOCATION_CONTEXT, invocationContext);
      
      if (marshaller == null || unmarshaller == null)
         throw new IllegalArgumentException("Unable to create de/serialization context");
      
      Map<String, Object> serializationContext = new HashMap<String, Object>();
      serializationContext.put(RMConstant.MARSHALLER, marshaller);
      serializationContext.put(RMConstant.UNMARSHALLER, unmarshaller);
      setContext(RMConstant.SERIALIZATION_CONTEXT, serializationContext);
         
      if (remotingInvocationContext == null)
         throw new IllegalArgumentException("Remoting invocation context cannot be null");
      
      setContext(RMConstant.REMOTING_INVOCATION_CONTEXT, remotingInvocationContext);

      if (remotingConfigurationContext == null)
         throw new IllegalArgumentException("Remoting configuraton context cannot be null");

      setContext(RMConstant.REMOTING_CONFIGURATION_CONTEXT, remotingConfigurationContext);
   }
   
   public RMMetadata(Map<String, Object> remotingInvocationContext)
   {
      if (remotingInvocationContext == null)
         throw new IllegalArgumentException("Remoting invocation context cannot be null");
      
      setContext(RMConstant.REMOTING_INVOCATION_CONTEXT, remotingInvocationContext);
   }
   
   void setContext(String key, Map<String, Object> ctx)
   {
      this.contexts.put(key, ctx);
   }
   
   Map<String, Object> getContext(String key)
   {
      return this.contexts.get(key);
   }
}
