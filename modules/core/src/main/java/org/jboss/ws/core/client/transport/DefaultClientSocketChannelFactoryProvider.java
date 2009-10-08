/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @author tags. See the COPYRIGHT.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.ws.core.client.transport;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

/**
 * The default provider of Netty ClientSocketChannelFactory.
 * This basically returns a singleton NioClientSocketChannelFactory using
 * cached thread pools for both boss and worker executors. 
 * 
 * @author alessio.soldano@jboss.com
 * @since 08-Oct-2009
 *
 */
public class DefaultClientSocketChannelFactoryProvider implements ClientSocketChannelFactoryProvider
{
   private static Executor bossExecutor = Executors.newCachedThreadPool();
   private static Executor workerExecutor = Executors.newCachedThreadPool();
   
   private static ClientSocketChannelFactory factory;

   public ClientSocketChannelFactory getClientSocketChannelFactoryInstance()
   {
      return getFactory();
   }
   
   private static synchronized ClientSocketChannelFactory getFactory()
   {
      if (factory == null)
      {
         factory = new NioClientSocketChannelFactory(bossExecutor, workerExecutor);
      }
      return factory;
   }

}
