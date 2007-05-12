package org.jboss.ws.extensions.eventing.mgmt;

import org.jboss.ws.integration.KernelLocator;
import org.jboss.kernel.spi.registry.KernelRegistry;
import org.jboss.kernel.spi.registry.KernelRegistryEntry;

/**
 * @author Heiko Braun, <heiko@openj.net>
 * @since 22-Mar-2006
 */
public class SubscriptionManagerFactory
{
   private static SubscriptionManagerFactory instance = new SubscriptionManagerFactory();

   // Hide ctor
   protected SubscriptionManagerFactory()
   {
   }

   public static SubscriptionManagerFactory getInstance()
   {
      return instance;
   }

   public SubscriptionManagerMBean getSubscriptionManager()
   {
      KernelRegistry registry = KernelLocator.getKernel().getRegistry();
      KernelRegistryEntry entry = registry.getEntry(SubscriptionManagerMBean.BEAN_NAME);
      return (SubscriptionManagerMBean)entry.getTarget();
   }
}
