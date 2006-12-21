/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
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
package org.jboss.ws.integration.tomcat;

// $Id:KernelBootstrap.java 915 2006-09-08 08:40:45Z thomas.diesler@jboss.com $

import java.net.URL;

import org.jboss.kernel.plugins.bootstrap.basic.BasicBootstrap;
import org.jboss.kernel.plugins.deployment.xml.BeanXMLDeployer;
import org.jboss.logging.Logger;
import org.jboss.ws.core.server.KernelLocator;

/**
 * Bootstrap the microkernel in Tomcat
 *
 * @author Thomas.Diesler@jboss.org
 * @since 15-May-2006
 */
public class KernelBootstrap extends BasicBootstrap
{
   // provide logging
   protected final Logger log = Logger.getLogger(KernelBootstrap.class);

   protected BeanXMLDeployer deployer;

   // FIXME: remove ctor that throws exception
   public KernelBootstrap() throws Exception
   {
      super();
   }

   public void bootstrap(URL beansXML)
   {
      // synchronize bootstrap access
      synchronized (KernelBootstrap.class)
      {
         // only bootstrap if the kernel is not there yet
         if (KernelLocator.getKernel() == null)
         {
            try
            {
               super.bootstrap();

               deployer = new BeanXMLDeployer(getKernel());

               Runtime.getRuntime().addShutdownHook(new Shutdown());

               deploy(beansXML);

               // Validate that everything is ok
               deployer.validate();
            }
            catch (RuntimeException rte)
            {
               throw rte;
            }
            catch (Throwable th)
            {
               throw new IllegalStateException("Cannot bootstrap microkernel", th);
            }
         }
      }
   }

   /**
    * Deploy a url
    *
    * @param url the deployment url
    * @throws Throwable for any error  
    */
   protected void deploy(URL url) throws Throwable
   {
      deployer.deploy(url);
   }

   /**
    * Undeploy a url
    * 
    * @param url the deployment url
    */
   protected void undeploy(URL url)
   {
      try
      {
         //deployer.undeploy(url);
      }
      catch (Throwable t)
      {
         log.warn("Error during undeployment: " + url, t);
      }
   }

   protected class Shutdown extends Thread
   {
      public void run()
      {
         log.info("Shutting down");
         //deployer.shutdown();
      }
   }
}
