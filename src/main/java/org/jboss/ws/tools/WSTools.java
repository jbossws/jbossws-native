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
package org.jboss.ws.tools;

// $Id$

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.StringTokenizer;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.tools.config.ToolsSchemaConfigReader;
import org.jboss.ws.tools.helpers.ToolsHelper;

/**
 *  Main Class for WSTools
 *
 *  @author Anil.Saldhana@jboss.org
 *  @author Thomas.Diesler@jboss.org
 *  @since  19-Aug-2005
 */
public class WSTools
{
   private static Logger log = Logger.getLogger(WSTools.class);

   /**
    * Entry point for the cmd line scripts.
    * Just passes the arguments to
    * @see generate(String)
    * @param args
    * @throws IOException
    */
   public static void main(String[] args) throws IOException
   {
      WSTools tools = new WSTools();
      tools.generate(args);
   }

   /**
    * Entry point for the programmatic use
    */
   public boolean generate(String configLocation, String outputDir) throws IOException
   {
      ToolsSchemaConfigReader configReader = new ToolsSchemaConfigReader();
      Configuration config = configReader.readConfig(configLocation);

      return process(config, outputDir);
   }

   /**
    * Entry point for the programmatic use
    */
   public boolean generate(String[] args) throws IOException
   {
      String configLocation = null;
      String outputDir = null;
      for (int i = 0; i < args.length; i++)
      {
         String arg = args[i];

         if ("-config".equals(arg))
         {
            configLocation = args[i + 1];
            i++;
         }

         else if ("-dest".equals(arg))
         {
            outputDir = args[i + 1];
            i++;
         }

         else if ("-classpath".equals(arg) || "-cp".equals(arg))
         {
            StringTokenizer st = new StringTokenizer(args[i + 1], File.pathSeparator);

            int tokens = st.countTokens();
            URL[] urls = new URL[tokens];
            for (int j = 0; j < tokens; j++)
            {
               String token = st.nextToken();
               urls[j] = new File(token).toURL();
            }

            ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
            URLClassLoader urlLoader = new URLClassLoader(urls, ctxLoader);
            Thread.currentThread().setContextClassLoader(urlLoader);
            i++;
         }
         else
         {
            System.out.println("Usage: wstools (-classpath|-cp) <classpath> -config <config> [-dest <destination path>]");
            System.exit(1);
         }
      }

      return generate(configLocation, outputDir);
   }

   private boolean process(Configuration config, String outputDir) throws IOException
   {
      if (config == null)
         throw new IllegalArgumentException("Configuration is null");
      
      if (outputDir == null)
         outputDir = ".";
      
      ToolsHelper helper = new ToolsHelper();
      if (config.getJavaToWSDLConfig(false) != null)
      {
         helper.handleJavaToWSDLGeneration(config, outputDir);
      }
      else if (config.getWSDLToJavaConfig(false) != null)
      {
         helper.handleWSDLToJavaGeneration(config, outputDir);
      }
      else
      {
         throw new WSException("Nothing done, Configuration source must have JavaToWSDL or WSDLToJava specified");
      }
      return true;
   }
}
