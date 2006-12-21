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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.logging.Logger;
import org.jboss.util.file.JarUtils;

/**
 * Publish a standard portable J2EE web service endpoint 
 * to standalone Tomcat
 *
 * @author Thomas.Diesler@jboss.org
 * @since 15-May-2006
 */
public class wspublish
{
   // provide logging
   protected final Logger log = Logger.getLogger(wspublish.class);

   public static final String DEFAULT_TOMCAT_SERVICE_ENDPOINT_SERVLET = "org.jboss.ws.integration.tomcat.TomcatServiceEndpointServlet";

   public URL process(URL warURL, File destDir, String servletName) throws IOException
   {
      if (warURL == null)
         throw new IllegalArgumentException("Invalid war URL: " + warURL);

      if (destDir == null || destDir.isDirectory() == false)
         throw new IllegalArgumentException("Invalid destintion dir: " + destDir);

      if (servletName == null)
         servletName = DEFAULT_TOMCAT_SERVICE_ENDPOINT_SERVLET;

      InputStream in = warURL.openStream();
      String warName = new File(warURL.getFile()).getName();
      File tmpDir = new File("./wspublish/" + warName);
      tmpDir.mkdirs();

      log.debug("Extracting war to: " + tmpDir);
      JarUtils.unjar(in, tmpDir);
      in.close();

      TomcatServiceEndpointPublisher publisher = new TomcatServiceEndpointPublisher();
      publisher.setServiceEndpointServlet(servletName);
      publisher.rewriteWebXml(tmpDir.toURL());

      File outFile = new File(destDir.getCanonicalPath() + "/" + warName);
      outFile.getParentFile().mkdirs();

      log.info("Writing war to: " + outFile.toURL());
      FileOutputStream fos = new FileOutputStream(outFile);
      JarUtils.jar(fos, tmpDir.listFiles());
      fos.close();

      return outFile.toURL();
   }

   public static void main(String[] args) throws Exception
   {
      URL warURL = null;
      File destDir = null;
      String servletName = null;

      for (int i = 0; i < args.length; i++)
      {
         String arg = args[i];
         if ("-url".equals(arg))
         {
            warURL = getURL(args[i + 1]);
            i++;
         }
         else if ("-dest".equals(arg))
         {
            destDir = new File(args[i + 1]);
            i++;
         }
         else if ("-servlet".equals(arg))
         {
            servletName = args[i + 1];
            i++;
         }
      }

      if (warURL == null || destDir == null)
      {
         System.out.println("Usage: wspublish -url warURL -dest webappsDir [-servlet servletName]");
         System.exit(1);
      }

      // process the args
      new wspublish().process(warURL, destDir, servletName);
   }

   private static URL getURL(String urlStr)
   {
      URL warURL = null;
      try
      {
         warURL = new URL(urlStr);
      }
      catch (MalformedURLException ex)
      {
         // ignore
      }
      try
      {
         warURL = new File(urlStr).toURL();
      }
      catch (MalformedURLException ex)
      {
         // ignore
      }
      return warURL;
   }
}
