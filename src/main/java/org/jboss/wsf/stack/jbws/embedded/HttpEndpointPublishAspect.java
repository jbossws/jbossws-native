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

import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.WSFRuntime;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.DeploymentAspect;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.transport.*;

/**
 * @author Heiko.Braun <heiko.braun@jboss.com>
 */
public class HttpEndpointPublishAspect extends DeploymentAspect
{

   private SPIProvider spi;
   private TransportManagerFactory tmf;
   private TransportManager<HttpSpec> http;

   public HttpEndpointPublishAspect()
   {
      spi = SPIProviderResolver.getInstance().getProvider();
      tmf = spi.getSPI(TransportManagerFactory.class);
      http = tmf.createTransportManager(Protocol.HTTP);
   }

   public void start(Deployment dep, WSFRuntime runtime)
   {
      String webcontext = dep.getService().getContextRoot();
      assert webcontext!=null;
      assert dep.getService().getEndpoints().size()>0;

      for(Endpoint endpoint : dep.getService().getEndpoints())
      {
         String urlPattern = endpoint.getURLPattern();

         HttpSpec spec = new HttpSpec(webcontext, urlPattern);
         ListenerRef ref = this.http.createListener(endpoint, spec);

         // Update endpoint address
         endpoint.setAddress(ref.getAddress().toString());
         endpoint.addAttachment(ListenerRef.class, ref);
      }            
   }

   public void stop(Deployment dep, WSFRuntime runtime)
   {
      assert dep.getService().getEndpoints().size()>0;

      for(Endpoint endpoint : dep.getService().getEndpoints())
      {
         ListenerRef ref = endpoint.getAttachment(ListenerRef.class);
         assert ref!=null;
         
         this.http.destroyListener(ref);
      }

   }
}
