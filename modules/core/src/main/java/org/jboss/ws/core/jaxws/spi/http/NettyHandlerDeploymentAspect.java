/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.jboss.ws.core.jaxws.spi.http;

import org.jboss.wsf.common.integration.AbstractDeploymentAspect;
import org.jboss.wsf.common.invocation.InvocationHandlerJAXWS;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.LifecycleHandler;
import org.jboss.wsf.spi.deployment.LifecycleHandlerFactory;
import org.jboss.wsf.spi.invocation.InvocationHandler;
import org.jboss.wsf.spi.invocation.RequestHandler;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.stack.jbws.RequestHandlerImpl;

/**
 * Netty endpoint handler deployment aspect.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class NettyHandlerDeploymentAspect extends AbstractDeploymentAspect
{
   private SPIProvider spiProvider;

   public NettyHandlerDeploymentAspect()
   {
      super();
      spiProvider = SPIProviderResolver.getInstance().getProvider();
   }

   @Override
   public void start(final Deployment dep)
   {
      for (final Endpoint ep : dep.getService().getEndpoints())
      {
         ep.setRequestHandler(getRequestHandler(dep));
         ep.setLifecycleHandler(getLifecycleHandler(dep));
         ep.setInvocationHandler(getInvocationHandler(ep));
      }
   }

   private LifecycleHandler getLifecycleHandler(Deployment dep)
   {
      return spiProvider.getSPI(LifecycleHandlerFactory.class).newLifecycleHandler();
   }

   private InvocationHandler getInvocationHandler(final Endpoint ep)
   {
      return new InvocationHandlerJAXWS();
   }
   
   private RequestHandler getRequestHandler(final Deployment dep)
   {
      dep.addAttachment(ServerConfig.class, NettyHttpServerConfig.SINGLETON);
      return new RequestHandlerImpl(NettyHttpServerConfig.SINGLETON);
   }
}
