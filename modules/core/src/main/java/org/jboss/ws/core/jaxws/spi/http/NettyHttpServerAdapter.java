/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

import java.util.LinkedList;
import java.util.List;

import javax.xml.ws.Endpoint;

import org.jboss.ws.core.jaxws.spi.EndpointImpl;
import org.jboss.ws.core.server.netty.NettyCallbackHandler;
import org.jboss.ws.core.server.netty.NettyHttpServer;
import org.jboss.ws.core.server.netty.NettyHttpServerFactory;
import org.jboss.ws.core.server.netty.NettyRequestHandlerFactory;
import org.jboss.wsf.common.ResourceLoaderAdapter;
import org.jboss.wsf.framework.deployment.BackwardCompatibleContextRootDeploymentAspect;
import org.jboss.wsf.framework.deployment.DeploymentAspectManagerImpl;
import org.jboss.wsf.framework.deployment.EndpointAddressDeploymentAspect;
import org.jboss.wsf.framework.deployment.EndpointHandlerDeploymentAspect;
import org.jboss.wsf.framework.deployment.EndpointLifecycleDeploymentAspect;
import org.jboss.wsf.framework.deployment.EndpointNameDeploymentAspect;
import org.jboss.wsf.framework.deployment.EndpointRegistryDeploymentAspect;
import org.jboss.wsf.framework.deployment.URLPatternDeploymentAspect;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Deployment.DeploymentType;
import org.jboss.wsf.spi.deployment.DeploymentAspect;
import org.jboss.wsf.spi.deployment.DeploymentModelFactory;
import org.jboss.wsf.spi.http.HttpContext;
import org.jboss.wsf.spi.http.HttpContextFactory;
import org.jboss.wsf.spi.http.HttpServer;
import org.jboss.wsf.stack.jbws.EagerInitializeDeploymentAspect;
import org.jboss.wsf.stack.jbws.PublishContractDeploymentAspect;
import org.jboss.wsf.stack.jbws.ServiceEndpointInvokerDeploymentAspect;
import org.jboss.wsf.stack.jbws.UnifiedMetaDataDeploymentAspect;

/**
 * Netty HTTP server adapter.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
final class NettyHttpServerAdapter implements HttpServer
{

   /** JBossWS SPI provider. */
   private static final SPIProvider SPI_PROVIDER = SPIProviderResolver.getInstance().getProvider();

   /** JBossWS Http Context factory. */
   private static final HttpContextFactory HTTP_CONTEXT_FACTORY = NettyHttpServerAdapter.SPI_PROVIDER
         .getSPI(HttpContextFactory.class);

   /** Deployment model factory. */
   private static final DeploymentModelFactory DEPLOYMENT_FACTORY = NettyHttpServerAdapter.SPI_PROVIDER
         .getSPI(DeploymentModelFactory.class);

   /** Request handler factory. */
   private static final NettyRequestHandlerFactory<NettyRequestHandlerImpl> REQUEST_HANDLER_FACTORY = NettyRequestHandlerFactoryImpl
         .getInstance();

   /**
    * Constructor.
    */
   public NettyHttpServerAdapter()
   {
      super();
   }

   /**
    * @see HttpServer#createContext(String)
    * 
    * @param ctx context root
    * @return http context
    */
   public HttpContext createContext(final String ctx)
   {
      return NettyHttpServerAdapter.HTTP_CONTEXT_FACTORY.newHttpContext(this, ctx);
   }

   /**
    * @see HttpServer#publish(HttpContext, Endpoint)
    * 
    * @param ctx server context 
    * @param endpoint web service endpoint
    */
   public void publish(final HttpContext ctx, final Endpoint endpoint)
   {
      final EndpointImpl epImpl = (EndpointImpl) endpoint;
      final String contextRoot = ctx.getContextRoot();
      final Deployment dep = this.newDeployment(epImpl, contextRoot);

      final DeploymentAspectManagerImpl daManager = new DeploymentAspectManagerImpl();
      daManager.setDeploymentAspects(this.getDeploymentAspects());
      daManager.deploy(dep);
      epImpl.setDeployment(dep);

      final NettyHttpServer server = NettyHttpServerFactory.getNettyHttpServer(epImpl.getPort(), NettyHttpServerAdapter.REQUEST_HANDLER_FACTORY);
      final NettyCallbackHandler callback = new NettyCallbackHandlerImpl(epImpl.getPath(), contextRoot, this
            .getEndpointRegistryPath(epImpl));
      server.registerCallback(callback);
   }

   /**
    * @see HttpServer#destroy(HttpContext, Endpoint)
    * 
    * @param ctx server context 
    * @param endpoint web service endpoint
    */
   public void destroy(final HttpContext ctx, final Endpoint endpoint)
   {
      final EndpointImpl epImpl = (EndpointImpl) endpoint;
      final NettyHttpServer server = NettyHttpServerFactory.getNettyHttpServer(epImpl.getPort(), NettyHttpServerAdapter.REQUEST_HANDLER_FACTORY);
      final NettyCallbackHandler callback = server.getCallback(epImpl.getPath());
      server.unregisterCallback(callback);

      final DeploymentAspectManagerImpl daManager = new DeploymentAspectManagerImpl();
      daManager.setDeploymentAspects(this.getDeploymentAspects());
      daManager.undeploy(epImpl.getDeployment());
   }

   /**
    * Returns endpoint registry path. This path includes also port endpoint is running on.
    * 
    * @param endpoint endpoint
    * @return endpoint registry path
    */
   private String getEndpointRegistryPath(final EndpointImpl endpoint)
   {
      // we need to distinguish ports in endpoints registry in JSE environment
      return endpoint.getPath() + "-port-" + endpoint.getPort();
   }

   /**
    * Creates new deployment.
    * 
    * @param epImpl endpoint implementation
    * @param contextRoot context root
    * @return deployment model
    */
   private Deployment newDeployment(final EndpointImpl epImpl, final String contextRoot)
   {
      final Class<?> endpointClass = this.getEndpointClass(epImpl);
      final ClassLoader loader = endpointClass.getClassLoader();

      final ArchiveDeployment dep = (ArchiveDeployment) NettyHttpServerAdapter.DEPLOYMENT_FACTORY.newDeployment(contextRoot, loader);
      final org.jboss.wsf.spi.deployment.Endpoint endpoint = NettyHttpServerAdapter.DEPLOYMENT_FACTORY.newEndpoint(endpointClass.getName());
      endpoint.setShortName(this.getEndpointRegistryPath(epImpl));
      endpoint.setURLPattern(epImpl.getPathWithoutContext());
      dep.getService().addEndpoint(endpoint);
      dep.setRootFile(new ResourceLoaderAdapter(loader));
      dep.setRuntimeClassLoader(loader);
      dep.setType(DeploymentType.JAXWS_JSE);
      dep.getService().setContextRoot(contextRoot);

      // TODO: remove this properties hack
      dep.getService().setProperty("protocol", "http");
      dep.getService().setProperty("host", "127.0.0.1");
      dep.getService().setProperty("port", epImpl.getPort());

      return dep;
   }

   /**
    * Returns deployment aspects needed to create deployment model.
    * 
    * @return deployment aspects
    */
   private List<DeploymentAspect> getDeploymentAspects()
   {
      final List<DeploymentAspect> retVal = new LinkedList<DeploymentAspect>();

      // TODO: native stack can't use framework classes directly
      retVal.add(new EndpointHandlerDeploymentAspect());
      retVal.add(new BackwardCompatibleContextRootDeploymentAspect());
      retVal.add(new URLPatternDeploymentAspect());
      retVal.add(new EndpointAddressDeploymentAspect());
      retVal.add(new EndpointNameDeploymentAspect());
      retVal.add(new UnifiedMetaDataDeploymentAspect());
      retVal.add(new ServiceEndpointInvokerDeploymentAspect());
      retVal.add(new PublishContractDeploymentAspect());
      retVal.add(new EagerInitializeDeploymentAspect());
      retVal.add(new EndpointRegistryDeploymentAspect());
      retVal.add(new EndpointLifecycleDeploymentAspect());

      return retVal;
   }

   /**
    * Returns implementor class associated with endpoint.
    *
    * @param endpoint to get implementor class from
    * @return implementor class
    */
   private Class<?> getEndpointClass(final Endpoint endpoint)
   {
      final Object implementor = endpoint.getImplementor();
      return implementor instanceof Class<?> ? (Class<?>) implementor : implementor.getClass();
   }

}