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
package org.jboss.test.ws.embedded;

import org.jboss.wsf.stack.jbws.embedded.EmbeddedBootstrap;
import org.jboss.wsf.stack.jbws.embedded.EmbeddableWSFRuntime;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.dependency.spi.ControllerContext;

import java.net.URL;

import junit.framework.TestCase;

/**
 * @author Heiko.Braun <heiko.braun@jboss.com>
 */
public class BootstrapTestCase extends TestCase
{
   private URL standaloneConfig;
   
   protected void setUp() throws Exception
   {
      /*ClassLoader cl = EmbeddableWSFRuntime.class.getClassLoader();
      String config = "org/jboss/wsf/stack/jbws/embedded/standalone-config.xml";
      standaloneConfig = cl.getResource(config);
      if(null==standaloneConfig)
         throw new RuntimeException("Unable to read config: "+config);
         */
   }
   
   /**
    * Check if the bootstrap in setup() works correctly
    * @throws Exception
    */
   public void testBootStrap() throws Exception
   {
      EmbeddedBootstrap bootstrap = new EmbeddedBootstrap();     
      bootstrap.run();
      bootstrap.deploy(EmbeddableWSFRuntime.DEFAULT_CONFIG_URL);

      Kernel kernel = bootstrap.getKernel();
      KernelController controller = kernel.getController();

      String beanName = "WSNativeDeploymentAspectInstallerJSE";
      ControllerContext context = controller.getInstalledContext(beanName);
      assertNotNull("Unable to retrieve "+beanName, context);

      System.out.println("WSDeploymentAspectManagerJSE: " + context.getTarget());
   }
}
