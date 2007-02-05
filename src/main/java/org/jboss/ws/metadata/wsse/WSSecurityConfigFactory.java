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
package org.jboss.ws.metadata.wsse;

// $Id: $

import java.io.IOException;
import java.net.URL;

import org.jboss.logging.Logger;
import org.jboss.ws.core.UnifiedVirtualFile;
import org.jboss.ws.metadata.builder.jaxrpc.JAXRPCServerMetaDataBuilder;

/**
 * @author hbraun
 * @author Thomas.Diesler@jboss.com
 */
public class WSSecurityConfigFactory
{
   // provide logging
   final Logger log = Logger.getLogger(JAXRPCServerMetaDataBuilder.class);

   public static WSSecurityConfigFactory newInstance()
   {
      return new WSSecurityConfigFactory();
   }

   public WSSecurityConfiguration createConfiguration(UnifiedVirtualFile vfsRoot, String resourceName) throws IOException
   {
      WSSecurityConfiguration config = null;

      URL location = getResource(vfsRoot, "WEB-INF/" + resourceName);
      if(null == location)
         location = getResource(vfsRoot, "META-INF/" + resourceName);

      if (location != null)
      {
         if(log.isDebugEnabled()) log.debug("createConfiguration from: " + location);
         config = WSSecurityOMFactory.newInstance().parse(location);

         // Get and set deployment path to the keystore file
         if (config.getKeyStoreFile() != null)
         {
            location = getResource(vfsRoot, config.getKeyStoreFile());
            if (location != null)
            {
               if(log.isDebugEnabled()) log.debug("Add keystore: " + location);
               config.setKeyStoreURL(location);
            }
         }

         if (config.getTrustStoreFile() != null)
         {
            location = getResource(vfsRoot, config.getTrustStoreFile());
            if (location != null)
            {
               if(log.isDebugEnabled()) log.debug("Add truststore: " + location);
               config.setTrustStoreURL(location);
            }
         }
      }

      return config;
   }

   private URL getResource(UnifiedVirtualFile vfsRoot, String resource)
   {
      try
      {
         UnifiedVirtualFile child = vfsRoot.findChild(resource);
         return child.toURL();
      }
      catch (Exception e)
      {
         return null;
      }
   }

}
