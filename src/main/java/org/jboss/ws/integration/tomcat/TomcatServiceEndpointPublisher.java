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

// $Id: WebServiceDeployer.java 312 2006-05-11 10:49:22Z thomas.diesler@jboss.com $

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.logging.Logger;
import org.jboss.util.NotImplementedException;
import org.jboss.ws.core.server.ServiceEndpointPublisher;
import org.jboss.ws.core.server.UnifiedDeploymentInfo;

/**
 * Publish the HTTP service endpoint to Tomcat 
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 12-May-2006
 */
public class TomcatServiceEndpointPublisher extends ServiceEndpointPublisher
{
   // logging support
   private static Logger log = Logger.getLogger(TomcatServiceEndpointPublisher.class);
   
   private boolean isRunning;
   private File targetDir;
   private File deployDir;
   private long interval;

   private Set<File> deployedFiles = new HashSet<File>();

   public void setDeployDir(String dirName)
   {
      this.deployDir = new File(dirName);
      if (deployDir.exists() == false)
      {
         System.out.println("create directory: " + deployDir);
         deployDir.mkdirs();
      }
   }

   public void setTargetDir(String dirName)
   {
      this.targetDir = new File(dirName);
      if (targetDir.isDirectory() == false)
         throw new IllegalArgumentException ("Target dir does not exist: " + targetDir);
   }

   public void setInterval(long interval)
   {
      this.interval = interval;
   }

   public String publishServiceEndpoint(URL warURL) throws Exception
   {
      new wspublish().process(warURL, targetDir, servletName);
      return "OK";
   }

   public String destroyServiceEndpoint(URL warURL) throws Exception
   {
      File targetFile = new File(targetDir.getAbsolutePath() + "/" + new File(warURL.getFile()).getName());
      if (targetFile.delete())
         return "OK";
      else
         return "NOT FOUND";
   }

   public String publishServiceEndpoint(UnifiedDeploymentInfo udi)
   {
      throw new NotImplementedException();
   }

   public String destroyServiceEndpoint(UnifiedDeploymentInfo udi)
   {
      throw new NotImplementedException();
   }
   
   private void scanDeployDir()
   {
      List<File> fileList = Arrays.asList(deployDir.listFiles());
      
      // deploy new files
      for (File file : fileList)
      {
         if (deployedFiles.contains(file) == false)
         {
            deployFile(file);
            deployedFiles.add(file);
         }
      }
      
      // undeploy files
      for (File file : deployedFiles)
      {
         if (fileList.contains(file) == false)
         {
            undeployFile(file);
            deployedFiles.remove(file);
         }
      }
   }

   private void deployFile(File file)
   {
      try
      {
         if (new File(targetDir.getAbsolutePath() + "/" + file.getName()).exists() == false)
         {
            System.out.println("Deploy file: " + file);
            publishServiceEndpoint(file.toURL());
         }    
         else
         {
            System.out.println("Ignore file: " + file);
         }
      }
      catch (Exception ex)
      {
         log.error("Cannot deploy file: " + file, ex);
      }      
   }

   private void undeployFile(File file)
   {
      try
      {
         if (new File(targetDir.getAbsolutePath() + "/" + file.getName()).exists())
         {
            System.out.println("Undeploy file: " + file);
            destroyServiceEndpoint(file.toURL());
         }    
         else
         {
            System.out.println("Ignore file: " + file);
         }
      }
      catch (Exception ex)
      {
         log.error("Cannot undeploy file: " + file, ex);
      }      
   }

   // bean lifecycle start
   public void start()
   {
      System.out.println("Start scanning: " + deployDir);
      Scanner s = new Scanner();
      new Thread(s).start();
   }

   // bean lifecycle stop
   public void stop()
   {
      System.out.println("Stop scanning: " + deployDir);
      isRunning = false;
   }

   class Scanner implements Runnable
   {
      public void run()
      {
         isRunning = true;
         try
         {
            while (isRunning)
            {
               scanDeployDir();
               Thread.sleep(Math.max(100, interval));
            }
         }
         catch (InterruptedException ex)
         {
            // ignore
         }
      }
   }
}
