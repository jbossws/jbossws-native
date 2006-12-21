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

   private Configuration config;
   private String outputDir = ".";

   /**
    * Entry point for the command line scripts.
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
    *
    * @param args
    * @throws IOException
    */
   public void generate(String[] args) throws IOException
   {
      boolean knownArgument = false;
      for (int i = 0; i < args.length; i++)
      {
         String arg = args[i];

         if ("-config".equals(arg))
         {
            readToolsConfiguration(args[i + 1]);
            knownArgument = true;
            i++;
         }

         else if ("-dest".equals(arg))
         {
            outputDir = args[i + 1];
            knownArgument = true;
            i++;
         }

         else if ("-classpath".equals(arg) || "-cp".equals(arg))
         {
            StringTokenizer st = new StringTokenizer(args[i + 1], File.pathSeparator);

            int tokens = st.countTokens();
            URL[] urls = new URL[tokens];
            for(int j = 0; j < tokens; j++)
            {
               String token = st.nextToken();
               urls[j] = new File(token).toURL();
            }

            ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
            URLClassLoader urlLoader = new URLClassLoader(urls, ctxLoader);
            Thread.currentThread().setContextClassLoader(urlLoader);
            knownArgument = true;
            i++;
         }
      }

      if (! knownArgument)
      {
         System.out.println("Usage: wstools (-classpath|-cp) <classpath> -config <config> [-dest <destination path>]");
         System.exit(1);
      }

      if (config == null)
         throw new IllegalArgumentException("wstools config not found");

      process();
   }

   private void process() throws IOException
   {
      ToolsHelper helper = new ToolsHelper();
      if (config == null)
         throw new WSException("Configuration is null");

      boolean processed = false;
      if (config.getJavaToWSDLConfig(false) != null)
      {
         helper.handleJavaToWSDLGeneration(config, outputDir);
         processed = true;
      }

      if (config.getWSDLToJavaConfig(false) != null)
      {
         helper.handleWSDLToJavaGeneration(config, outputDir);
         processed = true;
      }

      if (!processed)
         throw new WSException("Nothing done, Configuration source must have JavaToWSDL or WSDLToJava specified");
   }

   private void readToolsConfiguration(String filename) throws IOException
   {
      log.debug("Config file name=" + filename);
      ToolsSchemaConfigReader configReader = new ToolsSchemaConfigReader();
      config = configReader.readConfig(filename);
   }
}
