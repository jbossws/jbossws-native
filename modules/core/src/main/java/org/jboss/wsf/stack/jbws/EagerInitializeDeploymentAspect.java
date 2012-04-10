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
package org.jboss.wsf.stack.jbws;

import java.util.ResourceBundle;

import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.common.integration.AbstractDeploymentAspect;
import org.jboss.ws.common.utils.DelegateClassLoader;
import org.jboss.ws.metadata.umdm.UnifiedMetaData;
import org.jboss.wsf.spi.deployment.Deployment;

/**
 * A deployer that initializes the UMDM 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 25-Apr-2007
 */
public class EagerInitializeDeploymentAspect extends AbstractDeploymentAspect
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(EagerInitializeDeploymentAspect.class);
   @Override
   public void start(Deployment dep)
   {
      UnifiedMetaData umd = dep.getAttachment(UnifiedMetaData.class);
      if (umd == null)
         throw new IllegalStateException(BundleUtils.getMessage(bundle, "CANNOT_OBTAIN_UNIFIEDMD"));

      ClassLoader runtimeClassLoader = dep.getRuntimeClassLoader();
      if(null == runtimeClassLoader)
         throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "NULL_RUNTIME_CLASSLOADER"));
      
      umd.setClassLoader(new DelegateClassLoader(runtimeClassLoader, SecurityActions.getContextClassLoader()));
      umd.eagerInitialize();
   }
}
