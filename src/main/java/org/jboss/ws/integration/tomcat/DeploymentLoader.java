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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * A classloader to be used during deployment to load servlet
 * classes to allow them to be tested for annotations.
 * 
 * @author darran.lofthouse@jboss.com
 * @since Nov 2, 2006
 */
public class DeploymentLoader extends URLClassLoader
{

   public DeploymentLoader(ClassLoader parent)
   {
      super(new URL[] {}, parent);  
   }
   
  
   public static DeploymentLoader newInstance(URL warUrl)
   {
     DeploymentLoader loader = new DeploymentLoader(Thread.currentThread().getContextClassLoader());
      
      File classesDir = new File(warUrl.getFile() + "/WEB-INF/classes");
      if (classesDir.exists())
      {
         loader.addURL(toURL(classesDir));
      }
      
      File libDir = new File(warUrl.getFile() + "/WEB-INF/lib");
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
