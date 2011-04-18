/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.ws.tools.ant;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

/**
 *  Ant task for jbossws tools
 *  @author <mailto:Anil.Saldhana@jboss.org>Anil Saldhana
 *  @since  Oct 5, 2005
 */
public class wstools extends MatchingTask
{
   protected Path compileClasspath;
   private boolean verbose;
   private String dest;
   private String config;

   /**
    * Creates a nested classpath element.
    */
   public Path createClasspath()
   {
      if (compileClasspath == null)
      {
         compileClasspath = new Path(project);
      }
      return compileClasspath.createPath();
   }

   /**
    * Adds a reference to a CLASSPATH defined elsewhere.
    */
   public void setClasspathRef(Reference r)
   {
      createClasspath().setRefid(r);
   }

   public String getConfig()
   {
      return config;
   }

   public void setConfig(String config)
   {
      this.config = config;
   }

   public String getDest()
   {
      return dest;
   }

   public void setDest(String dest)
   {
      this.dest = dest;
   }

   public boolean isVerbose()
   {
      return verbose;
   }

   public void setVerbose(boolean verbose)
   {
      this.verbose = verbose;
   }

   public void addConfiguredSysproperty(SysProperty prop)
   {
      System.setProperty(prop.getKey(), prop.getValue());
   }

   public void execute() throws BuildException
   {
      ClassLoader prevCL = getContextClassLoader();
      setContextClassLoader(this.getClass().getClassLoader());
      try
      {
         String[] args = new String[] { "-dest", dest, "-config", config };
         org.jboss.ws.tools.WSTools tools = new org.jboss.ws.tools.WSTools();
         tools.generate(args);
      }
      catch (Exception ex)
      {
         if (ex instanceof BuildException)
         {
            throw (BuildException)ex;
         }
         else
         {
            throw new BuildException("Error running jbossws: ", ex, getLocation());
         }
      }
      finally
      {
         setContextClassLoader(prevCL);
      }
   }

   private static ClassLoader getContextClassLoader()
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm == null)
      {
         return Thread.currentThread().getContextClassLoader();
      }
      else
      {
         return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run()
            {
               return Thread.currentThread().getContextClassLoader();
            }
         });
      }
   }

   /**
    * Set context classloader.
    *
    * @param cl the classloader
    * @return previous context classloader
    * @throws Throwable for any error
    */
   private static ClassLoader setContextClassLoader(final ClassLoader cl)
   {
      if (System.getSecurityManager() == null)
      {
         ClassLoader result = Thread.currentThread().getContextClassLoader();
         if (cl != null)
            Thread.currentThread().setContextClassLoader(cl);
         return result;
      }
      else
      {
         try
         {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<ClassLoader>() {
               public ClassLoader run() throws Exception
               {
                  try
                  {
                     ClassLoader result = Thread.currentThread().getContextClassLoader();
                     if (cl != null)
                        Thread.currentThread().setContextClassLoader(cl);
                     return result;
                  }
                  catch (Exception e)
                  {
                     throw e;
                  }
                  catch (Error e)
                  {
                     throw e;
                  }
                  catch (Throwable e)
                  {
                     throw new RuntimeException("Error setting context classloader", e);
                  }
               }
            });
         }
         catch (PrivilegedActionException e)
         {
            throw new RuntimeException("Error running privileged action", e.getCause());
         }
      }
   }
}
