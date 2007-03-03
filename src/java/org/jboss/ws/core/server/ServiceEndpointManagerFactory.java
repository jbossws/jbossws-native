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
package org.jboss.ws.core.server;

import org.jboss.kernel.spi.registry.KernelRegistry;
import org.jboss.kernel.spi.registry.KernelRegistryEntry;

// $Id$

/**
 * Factory to the singleton instance of the ServiceEndpointManager 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 08-May-2006
 */
public class ServiceEndpointManagerFactory
{
   private static ServiceEndpointManagerFactory instance = new ServiceEndpointManagerFactory();

   // Hide ctor
   private ServiceEndpointManagerFactory()
   {
   }

   public static ServiceEndpointManagerFactory getInstance()
   {
      return instance;
   }

   public ServiceEndpointManager getServiceEndpointManager()
   {
      KernelRegistry registry = KernelLocator.getKernel().getRegistry();
      KernelRegistryEntry entry = registry.getEntry(ServiceEndpointManager.BEAN_NAME);
      return (ServiceEndpointManager)entry.getTarget();
   }
}
