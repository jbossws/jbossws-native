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

// $Id$

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.servlet.ServletContext;

/**
 * A URLClassLoader that provides access to the webapp base dir 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 16-May-2006
 */
public class CrossContextLoader extends URLClassLoader
{
   public CrossContextLoader(URL[] urls, ClassLoader parent)
   {
      super(urls, parent);
   }

   public static CrossContextLoader newInstance(ServletContext context)
   {
      ClassLoader parent = Thread.currentThread().getContextClassLoader();
      if (parent instanceof CrossContextLoader)
         throw new IllegalStateException("Circular classloader parent association");

      CrossContextLoader loader = new CrossContextLoader(new URL[] {}, parent);

      String path = context.getRealPath("/");
      File contextDir = new File(path);
      if (contextDir.exists() == false || contextDir.isDirectory() == false)
         throw new IllegalStateException("Cannot find expanded dir: " + contextDir);

      // Add URL to context root
      loader.addURL(toURL(contextDir));

      File jbosswsDir = new File(path + "../jbossws");
      if (jbosswsDir.exists() == false || jbosswsDir.isDirectory() == false)
         throw new IllegalStateException("Cannot find expanded dir: " + jbosswsDir);

      // Add URL to jbossws context root
      loader.addURL(toURL(jbosswsDir));

      // Add jars in jbossws/WEB-INF/lib
      File classesDir = new File(path + "../jbossws/WEB-INF/classes");
      if (classesDir.exists() && classesDir.isDirectory())
         loader.addURL(toURL(classesDir));

      // Add jars in jbossws/WEB-INF/lib
      File libDir = new File(path + "../jbossws/WEB-INF/lib");
      if (libDir.exists() && libDir.isDirectory())
      {
         File[] files = libDir.listFiles();
         for (int i = 0; i < files.length; i++)
         {
            File file = files[i];
            loader.addURL(toURL(file));
         }
      }

      return loader;
   }

   private static URL toURL(File file)
   {
      try
      {
         return file.toURL();
      }
      catch (MalformedURLException e)
      {
         // ignore
         return null;
      }
   }
}
