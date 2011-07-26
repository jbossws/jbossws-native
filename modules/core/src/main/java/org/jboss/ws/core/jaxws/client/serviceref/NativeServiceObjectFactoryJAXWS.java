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
package org.jboss.ws.core.jaxws.client.serviceref;

import javax.xml.ws.Service;

import org.jboss.ws.core.ConfigProvider;
import org.jboss.ws.common.serviceref.AbstractServiceObjectFactoryJAXWS;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedServiceRefMetaData;

/**
 * {@inheritDoc}
 *
 * @author Thomas.Diesler@jboss.org
 * @author Richard.Opalka@jboss.org
 * @author alessio.soldano@jboss.com
 */
public final class NativeServiceObjectFactoryJAXWS extends AbstractServiceObjectFactoryJAXWS
{
   private static final ThreadLocal<UnifiedServiceRefMetaData> serviceRefAssociation = new ThreadLocal<UnifiedServiceRefMetaData>();

   public static UnifiedServiceRefMetaData getServiceRefAssociation()
   {
      return serviceRefAssociation.get();
   }

   @Override
   protected void init(final UnifiedServiceRefMetaData serviceRefUMDM)
   {
      serviceRefAssociation.set(serviceRefUMDM);
   }

   @Override
   protected void configure(final UnifiedServiceRefMetaData serviceRefUMDM, final Service service)
   {
      final String configFile = serviceRefUMDM.getConfigFile();
      final String configName = serviceRefUMDM.getConfigName();
      if (service instanceof ConfigProvider)
      {
         final ConfigProvider cp = (ConfigProvider) service;
         if (configName != null || configFile != null)
         {
            cp.setConfigName(configName, configFile);
         }
      }
   }

   @Override
   protected void destroy(final UnifiedServiceRefMetaData serviceRefUMDM)
   {
      serviceRefAssociation.set(null);
   }
}
