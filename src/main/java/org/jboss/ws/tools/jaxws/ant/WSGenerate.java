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
package org.jboss.ws.tools.jaxws.ant;

import java.io.File;
import java.io.PrintStream;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.ExecuteJava;
import org.apache.tools.ant.taskdefs.LogOutputStream;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.jboss.ws.tools.jaxws.api.WebServiceGenerator;

/**
 * Ant task which invokes WebServiceGenerate.
 * 
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 */
public class WSGenerate extends Task
{
   private Path classpath = new Path(getProject());
   private String sei = null;
   private File destdir = null;
   private File resourcedestdir = null;
   private File sourcedestdir = null;
   private boolean keep = false;
   private boolean genwsdl = false;
   private boolean verbose = false;
   private boolean fork = false;
   
   public void setClasspath(Path classpath)
   {
      this.classpath = classpath;
   }
   
   public void setClasspathRef(Reference ref)
   {
      createClasspath().setRefid(ref);
   }
   
   public Path createClasspath()
   {
      return classpath;
   }
   
   public void setDestdir(File destdir)
   {
      this.destdir = destdir;
   }

   public void setKeep(boolean keep)
   {
      this.keep = keep;
   }
   
   public void setSei(String sei)
   {
      this.sei = sei;
   }
   
   public void setEndpoint(String endpoint)
   {
      this.sei = endpoint;
   }

   public void setFork(boolean fork)
   {
      this.fork = fork;
   }

   public void setResourcedestdir(File resourcedestdir)
   {
      this.resourcedestdir = resourcedestdir;
   }

   public void setSourcedestdir(File sourcedestdir)
   {
      this.sourcedestdir = sourcedestdir;
   }

   public void setVerbose(boolean verbose)
   {
      this.verbose = verbose;
   }

   public void setGenwsdl(boolean genwsdl)
   {
      this.genwsdl = genwsdl;
   }
   
   private ClassLoader getClasspathLoader(ClassLoader parent)
   {
      return new AntClassLoader(parent, getProject(), classpath, false);
   }
   
   public void executeNonForked()
   {
      ClassLoader prevCL = Thread.currentThread().getContextClassLoader();
      ClassLoader antLoader = this.getClass().getClassLoader();
      Thread.currentThread().setContextClassLoader(antLoader);
      try
      {
         WebServiceGenerator gen = WebServiceGenerator.newInstance();
         gen.setClassLoader(getClasspathLoader(antLoader));
         if (verbose)
            gen.setMessageStream(new PrintStream(new LogOutputStream(this, Project.MSG_INFO)));
         gen.setGenerateSource(keep);
         gen.setGenerateWsdl(genwsdl);
         if (destdir != null)
            gen.setOutputDirectory(destdir);
         if (resourcedestdir != null)
            gen.setResourceDirectory(resourcedestdir);
         if (sourcedestdir != null)
            gen.setSourceDirectory(sourcedestdir);
         if (verbose)
            log("Generating from endpoint: " + sei, Project.MSG_INFO);
         
         gen.generate(sei);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(prevCL);
      }
   }
   
   public void execute() throws BuildException
   {
      if (fork)
         executeForked();
      else
         executeNonForked();
   }
   
   private Path getTaskClassPath()
   {
      // Why is everything in the Ant API a big hack???
      ClassLoader cl = this.getClass().getClassLoader();
      if (cl instanceof AntClassLoader)
      {
         return new Path(getProject(), ((AntClassLoader)cl).getClasspath());
      }
      
      return new Path(getProject());
   }

   private void executeForked() throws BuildException
   {
      CommandlineJava command = new CommandlineJava();
      command.setClassname(org.jboss.ws.tools.jaxws.WSGenerate.class.getName());
      
      Path path = command.createClasspath(getProject());
      path.append(getTaskClassPath());
      path.append(classpath);
     
      if (keep)
         command.createArgument().setValue("-k");
      
      if (genwsdl)
         command.createArgument().setValue("-w");
      
      if (destdir != null)
      {
         command.createArgument().setValue("-o");
         command.createArgument().setFile(destdir);
      }
      if (resourcedestdir != null)
      {
         command.createArgument().setValue("-r");
         command.createArgument().setFile(resourcedestdir);
      }
      if (sourcedestdir != null)
      {
         command.createArgument().setValue("-s");
         command.createArgument().setFile(sourcedestdir);
      }
      
      if (!verbose)
         command.createArgument().setValue("-q");
      
      // Always dump traces
      command.createArgument().setValue("-t");
      command.createArgument().setValue(sei);
      
      if (verbose)
         log("Command invoked: " + command.getJavaCommand().toString());
      
      ExecuteJava execute = new ExecuteJava();
      execute.setClasspath(path);
      execute.setJavaCommand(command.getJavaCommand());
      if (execute.fork(this) != 0)
         throw new BuildException("Could not invoke wsgen", getLocation());
   }
}