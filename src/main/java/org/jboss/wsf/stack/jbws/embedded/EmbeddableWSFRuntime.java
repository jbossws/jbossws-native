/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.jbws.embedded;

import org.jboss.kernel.Kernel;
import org.jboss.logging.Logger;
import org.jboss.wsf.spi.ComposableRuntime;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.WSFRuntime;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.DeploymentAspectManager;
import org.jboss.wsf.spi.deployment.DeploymentAspectManagerFactory;
import org.jboss.wsf.spi.invocation.InvocationHandlerFactory;
import org.jboss.wsf.spi.invocation.RequestHandlerFactory;
import org.jboss.wsf.spi.management.EndpointRegistry;
import org.jboss.wsf.spi.management.EndpointRegistryFactory;
import org.jboss.wsf.spi.transport.TransportManagerFactory;

import java.net.URL;

/**
 * A WSF runtime that bootstraps through the {@link org.jboss.wsf.spi.SPIProvider}.<br>
 * It support JSE endpoints only.
 *
 * @author Heiko.Braun <heiko.braun@jboss.com>
 */
public class EmbeddableWSFRuntime implements WSFRuntime, ComposableRuntime
{
   private static final Logger log = Logger.getLogger(EmbeddableWSFRuntime.class);
   
   private SPIProvider spi;

   private Kernel kernel;
   
   private DeploymentAspectManager deploymentManager;

   private EndpointRegistry endpointRegistry;

   private RequestHandlerFactory rqhFactory;

   private InvocationHandlerFactory ivhFactory;
   
   private TransportManagerFactory tmFactory;

   public static URL DEFAULT_CONFIG_URL;

   static
   {
      ClassLoader cl = EmbeddableWSFRuntime.class.getClassLoader();
      String config = "org/jboss/wsf/stack/jbws/embedded/standalone-config.xml";
      DEFAULT_CONFIG_URL = cl.getResource(config);
      if(null== DEFAULT_CONFIG_URL) throw new RuntimeException("Unable to read config: "+config);
   }
   
   private EmbeddableWSFRuntime(Kernel kernel)
   {
      this.kernel = kernel;
      this.spi = SPIProviderResolver.getInstance().getProvider();
   }

   public static EmbeddableWSFRuntime bootstrap(URL jbosswsBeansXml) throws Exception
   {

      log.debug("Bootstrap runtime from microcontainer config");

      EmbeddedBootstrap bootstrap = new EmbeddedBootstrap();
      bootstrap.run();
      bootstrap.deploy(jbosswsBeansXml);

      Kernel kernel = bootstrap.getKernel();
      EmbeddableWSFRuntime container = new EmbeddableWSFRuntime(kernel);
      container.assemble();
      return container;
   }

   private void assemble()
   {

      log.info("Assembling runtime");

      // DeploymentAspectManager
      setDeploymentAspectManager(
        spi.getSPI(DeploymentAspectManagerFactory.class).getDeploymentAspectManager("WSDeploymentAspectManagerJSE")
      );

      // EndpointRegistry
      setEndpointRegistry(
        spi.getSPI(EndpointRegistryFactory.class).getEndpointRegistry()
      );

      // Transport
      setTransportManagerFactory( spi.getSPI(TransportManagerFactory.class) );

      // Requesthandling
      setRequestHandlerFactory( spi.getSPI(RequestHandlerFactory.class));

      // InvocationHandling
      setInvocationHandlerFactory( spi.getSPI(InvocationHandlerFactory.class) );
   }

   // ---------------------------------------------------------------------------------


   public void create(Deployment deployment)
   {
      deploymentManager.create(deployment, this);
   }

   public void start(Deployment deployment)
   {
      deploymentManager.start(deployment, this);
   }

   public void stop(Deployment deployment)
   {
      deploymentManager.stop(deployment, this);
   }

   public void destroy(Deployment deployment)
   {
      deploymentManager.destroy(deployment, this);
   }

   // ---------------------------------------------------------------------------------


   public void setTransportManagerFactory(TransportManagerFactory factory)
   {
      assert factory!=null;
      log.info("Using TransportManagerFactory: " + factory);
      this.tmFactory = factory;
   }

   public TransportManagerFactory getTransportManagerFactory()
   {
      return this.tmFactory;
   }

   public void setEndpointRegistry(EndpointRegistry endpointRegistry)
   {
      assert endpointRegistry!=null;
      log.info("Using EndpointRegistry: " + endpointRegistry);
      this.endpointRegistry = endpointRegistry;
   }

   public EndpointRegistry getEndpointRegistry()
   {
      return this.endpointRegistry;
   }

   public void setDeploymentAspectManager(DeploymentAspectManager deploymentManager)
   {
      assert deploymentManager!=null;
      log.info("Using DeploymentAspectManager: " + deploymentManager);
      this.deploymentManager = deploymentManager;
   }

   public DeploymentAspectManager getDeploymentAspectManager()
   {
      return this.deploymentManager;
   }

   public void setRequestHandlerFactory(RequestHandlerFactory factory)
   {
      assert factory!=null;
      log.info("Using RequestHandlerFactory: "+ factory);
      this.rqhFactory = factory;
   }


   public RequestHandlerFactory getRequestHandlerFactory()
   {
      return this.rqhFactory;
   }

   public void setInvocationHandlerFactory(InvocationHandlerFactory factory)
   {
      assert factory!=null;
      log.info("Using InvocationHandlerFactory: "+ factory);
      this.ivhFactory = factory;
   }

   public InvocationHandlerFactory getInvocationHandlerFactory()
   {
      return this.ivhFactory;
   }
}