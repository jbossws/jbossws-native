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
package org.jboss.ws.tools.jaxws.impl;

import com.sun.tools.ws.wscompile.WsimportTool;
import org.jboss.wsf.spi.tools.WSContractConsumer;

import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * WSContractConsumer that delegates to the Sun CompileTool.
 *
 * @author <a href="jason.greene@jboss.com">Jason T. Greene</a>
 * @author <a href="heiko.braun@jboss.com">Heiko Braun</a>
 * @version $Revision$
 */
public class SunRIConsumerImpl extends WSContractConsumer
{
   private List<File> bindingFiles = null;
   private File catalog = null;
   private boolean generateSource = false;
   private File outputDir = new File("output");
   private File sourceDir = null;
   private String targetPackage = null;
   private PrintStream messageStream = null;
   private String wsdlLocation = null;
   private List<String> additionalCompilerClassPath = null;

   @Override
   public void setBindingFiles(List<File> bindingFiles) {
      this.bindingFiles = bindingFiles;
   }

   @Override
   public void setCatalog(File catalog) {
      this.catalog = catalog;
   }

   @Override
   public void setGenerateSource(boolean generateSource) {
      this.generateSource = generateSource;
   }

   @Override
   public void setMessageStream(PrintStream messageStream) {
      // TODO Auto-generated method stub
      this.messageStream = messageStream;
   }

   @Override
   public void setOutputDirectory(File directory) {
      // TODO Auto-generated method stub
      outputDir = directory;
   }

   @Override
   public void setSourceDirectory(File directory) {
      sourceDir = directory;
   }

   @Override
   public void setTargetPackage(String targetPackage) {
      this.targetPackage = targetPackage;
   }

   @Override
   public void setWsdlLocation(String wsdlLocation) {
      this.wsdlLocation = wsdlLocation;
   }

   public void setAdditionalCompilerClassPath(List<String> additionalCompilerClassPath) {
      this.additionalCompilerClassPath = additionalCompilerClassPath;
   }

   @Override
   public void consume(URL wsdl) {
      List<String> args = new ArrayList<String>();
      if (bindingFiles != null) {
         for (File file : bindingFiles) {
            args.add("-b");
            args.add(file.getAbsolutePath());

         }
      }

      if (catalog != null) {
         args.add("-catalog");
         args.add(catalog.getAbsolutePath());
      }

      if (generateSource) {
         args.add("-keep");
         if (sourceDir != null) {
            if (!sourceDir.exists() && !sourceDir.mkdirs())
               throw new IllegalStateException("Could not make directory: " + sourceDir.getName());

            args.add("-s");
            args.add(sourceDir.getAbsolutePath());
         }
      }

      if (targetPackage != null) {
         args.add("-p");
         args.add(targetPackage);
      }

      if (wsdlLocation != null) {
         args.add("-wsdllocation");
         args.add(wsdlLocation);
      }

      PrintStream stream = messageStream;
      if (stream != null) {
         args.add("-verbose");
      } else {
         stream = new NullPrintStream();
      }

      if (!outputDir.exists() && !outputDir.mkdirs())
         throw new IllegalStateException("Could not make directory: " + outputDir.getName());

      // Always add the output directory and the wsdl location
      args.add("-d");
      args.add(outputDir.getAbsolutePath());
      args.add(wsdl.toString());

      try
      {
         // enforce woodstox
         if(null == System.getProperty("javax.xml.stream.XMLInputFactory"))
            System.setProperty("javax.xml.stream.XMLInputFactory", "com.ctc.wstx.stax.WstxInputFactory");
         
         WsimportTool compileTool = new WsimportTool(stream);
         boolean success = compileTool.run(args.toArray(new String[0]));

         if(!success) throw new IllegalStateException("WsImport invocation failed");
      }
      catch (Throwable t)
      {
         messageStream.println("Failed to invoke WsImport");
         t.printStackTrace(messageStream);
      }

   }
}

