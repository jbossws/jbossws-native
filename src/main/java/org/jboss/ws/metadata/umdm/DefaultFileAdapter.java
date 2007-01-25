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
package org.jboss.ws.metadata.umdm;

import org.jboss.ws.core.server.UnifiedVirtualFile;

import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * The default file adapter loads resources through an associated classloader.
 * If no classload is set, the the thread context classloader will be used.
 *
 * @author Heiko.Braun@jboss.org
 * @version $Id$
 * @since 25.01.2007
 */
public class DefaultFileAdapter implements UnifiedVirtualFile {

   private URL location;
   private ClassLoader loader;

   public DefaultFileAdapter(URL location) {
      this.location = location;
   }

   public DefaultFileAdapter() {
   }

   public UnifiedVirtualFile findChild(String child) throws IOException
   {

      URL loc = null;
      if (child != null)
      {
         // Try the child as URL
         try
         {
            loc = new URL(child);
         }
         catch (MalformedURLException ex)
         {
            // ignore
         }

         // Try the filename as File
         if (loc == null)
         {
            try
            {
               File file = new File(child);
               if (file.exists())
                  loc = file.toURL();
            }
            catch (MalformedURLException e)
            {
               // ignore
            }
         }

         // Try the filename as Resource
         if (loc == null)
         {
            try
            {
               loc = getLoader().getResource(child);
            }
            catch (Exception ex)
            {
               // ignore
            }
         }

      }

      if (loc == null)
         throw new IllegalArgumentException("Cannot get URL for: " + child);

      return new DefaultFileAdapter(loc);
   }

   public URL toURL() {
      if(null == this.location)
         throw new IllegalStateException("UnifiedVirtualFile not initialized");
      return location;
   }

   public void setLoader(ClassLoader loader) {
      this.loader = loader;
   }

   public ClassLoader getLoader() {
      if(null == this.loader)
         loader = Thread.currentThread().getContextClassLoader();
      return loader;
   }
}
