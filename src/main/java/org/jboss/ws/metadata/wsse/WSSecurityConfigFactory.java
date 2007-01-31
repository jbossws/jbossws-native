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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.jboss.ws.core.UnifiedVirtualFile;

/**
 * Created by IntelliJ IDEA.
 * User: hbraun
 * Date: 14.12.2006
 * Time: 16:17:02
 * To change this template use File | Settings | File Templates.
 */
public class WSSecurityConfigFactory
{

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
         config = WSSecurityOMFactory.newInstance().parse(location);

         // Get and set deployment path to the keystore file
         if (config.getKeyStoreFile() != null)
         {
            location = getResource(vfsRoot, config.getKeyStoreFile());
            if (location != null)
               config.setKeyStoreURL(location);
         }

         if (config.getTrustStoreFile() != null)
         {
            location = getResource(vfsRoot, config.getTrustStoreFile());
            if (location != null)
               config.setTrustStoreURL(location);
         }
      }

      return config;
   }

   /**
    *
    * @param vfsRoot
    * @param resource
    * @return null, when the resource cannot be found
    */
   private URL getResource(UnifiedVirtualFile vfsRoot, String resource)
   {
      try
      {
         UnifiedVirtualFile child = vfsRoot.findChild(resource);
         URL url = child.toURL();
         if (url != null)
         {
            InputStream inputStream = url.openStream();
            inputStream.close();
         }
         
         return url;
      }
      catch (Exception e)
      {
         return null;
      }
   }

}
