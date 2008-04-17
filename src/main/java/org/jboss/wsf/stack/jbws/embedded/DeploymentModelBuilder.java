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

import org.jboss.wsf.spi.deployment.*;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.common.ResourceLoaderAdapter;

import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

/**
 * A builder that helps creating {@link org.jboss.wsf.spi.deployment.Deployment}'s
 * for embedded use.
 *
 * @author Heiko.Braun <heiko.braun@jboss.com>
 */
public class DeploymentModelBuilder
{
   private Deployment deployment;   
   private DeploymentModelFactory modelFactory;

   public DeploymentModelBuilder()
   {
      SPIProvider spi = SPIProviderResolver.getInstance().getProvider();
      modelFactory = spi.getSPI(DeploymentModelFactory.class);

      // Deployment
      ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
      this.deployment = modelFactory.newDeployment(UUID.randomUUID().toString(), contextClassLoader);
      this.deployment.setRuntimeClassLoader(contextClassLoader);

      // TODO: Hack, should this become another DeploymentAspect?
      ((ArchiveDeployment)this.deployment).setRootFile(new ResourceLoaderAdapter());
      
      this.deployment.setType(Deployment.DeploymentType.JAXWS_JSE);

      // Service
      this.deployment.setService(modelFactory.newService());

   }

   public DeploymentModelBuilder setContextRoot(String contextRoot)
   {
      assert contextRoot!=null;
      this.deployment.getService().setContextRoot(contextRoot);
      return this;
   }

   public DeploymentModelBuilder addEndpoint(String className, String urlPattern)
   {
      assert className!=null;
      assert urlPattern!=null;

      // Endpoint
      Endpoint endpoint = modelFactory.newEndpoint("org.jboss.test.ws.embedded.HelloWorldEndpoint");
      endpoint.setShortName(className + "-Endpoint");
      endpoint.setURLPattern(urlPattern);
      this.deployment.getService().addEndpoint(endpoint);
      return this;
   }

   public Deployment build()
   {
      if(null == this.deployment.getService().getContextRoot())
         throw new IllegalArgumentException("No context root");

      List<String> usedPatterns = new ArrayList<String>();
      for(Endpoint ep : this.deployment.getService().getEndpoints())
      {
         String urlPattern = ep.getURLPattern();
         if(null == urlPattern)
            throw new IllegalArgumentException("No urlPattern");

         for(String usedPattern : usedPatterns)
         {
            if(urlPattern.equals(usedPattern))
               throw new IllegalArgumentException("URL pattern already used: " + urlPattern);
         }

         usedPatterns.add(urlPattern);
      }
      return this.deployment;
   }
}
