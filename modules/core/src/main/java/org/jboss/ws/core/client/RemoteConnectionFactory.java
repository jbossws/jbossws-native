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
package org.jboss.ws.core.client;

import java.util.ResourceBundle;

import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.api.util.ServiceLoader;
import org.jboss.ws.feature.FastInfosetFeature;
import org.jboss.ws.feature.JsonEncodingFeature;

/**
 * A factory for remote connections 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 10-Jan-2008
 */
public class RemoteConnectionFactory
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(RemoteConnectionFactory.class);
   public RemoteConnection getRemoteConnection(EndpointInfo epInfo)
   {
      String targetAddress = epInfo.getTargetAddress();
      if (targetAddress == null)
         throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "CANNOT_OBTAIN_TARGET_ADDRESS",  epInfo));
      
      String key = null;
      targetAddress = targetAddress.toLowerCase();
      if (targetAddress.startsWith("http"))
         key = RemoteConnection.class.getName() + ".http";
      
      if (key == null)
         throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "CANNOT_OBTAIN_REMOTE_CONNETION",  targetAddress));
      
      if (epInfo.isFeatureEnabled(FastInfosetFeature.class))
      {
         key += ".fastinfoset";
      }
      else if (epInfo.isFeatureEnabled(JsonEncodingFeature.class))
      {
         key += ".json";
      }
      
      RemoteConnection con = (RemoteConnection)ServiceLoader.loadService(key, null, this.getClass().getClassLoader());
      if (con == null)
         throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "CANNOT_OBTAIN_REMOTE_CONNETION",  key));
      
      return con;
   }
}
