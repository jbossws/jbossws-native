package org.jboss.ws.extensions.eventing.mgmt;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

/**
 * Creates event dispatcher delegates.
 *
 * @see DispatcherDelegate
 * 
 * @author Heiko Braun, <heiko@openj.net>
 * @since 11-Jan-2006
 */
public class DispatcherFactory implements ObjectFactory  {
   public Object getObjectInstance(Object object, Name name, Context context, Hashtable<?, ?> hashtable) throws Exception {

      Reference ref = (Reference)object;
      String hostname = (String)ref.get(DispatcherDelegate.MANAGER_HOSTNAME).getContent();

      Class cls = Thread.currentThread().getContextClassLoader().loadClass(ref.getClassName());
      DispatcherDelegate delegate = (DispatcherDelegate)cls.newInstance();
      delegate.setHostname(hostname);
      return delegate;
   }
}
