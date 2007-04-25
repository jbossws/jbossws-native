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
package org.jboss.ws.core.server;

// $Id$

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.management.ObjectName;

import org.jboss.ws.integration.UnifiedVirtualFile;
import org.jboss.ws.integration.deployment.Deployment.DeploymentType;

/**
 * The container independent deployment info.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 05-May-2006
 */
public class UnifiedDeploymentInfo
{
   public UnifiedDeploymentInfo(DeploymentType type)
   {
      this.type = type;
   }

   /** The type of this deployment */
   public DeploymentType type;
   /** Sub deployments have a parent */
   public UnifiedDeploymentInfo parent;
   /** The suffix of the deployment url */
   public String simpleName;
   /** The URL for this deployment */
   public URL url;
   /** The virtual file for the deployment root */
   public UnifiedVirtualFile vfRoot;
   /** The string identifing this deployment **/
   public String name;
   /** The URL to the expanded webapp **/
   public URL webappURL;
   /** We can hold "typed" metadata */
   public Object metaData;
   /** The deployment classloader **/
   public ClassLoader classLoader;
   /** An arbitrary map of state associated with the deployment */
   public Map<String, Object> context = new HashMap<String, Object>();
   /** An optional ObjectName of the deployed object */
   public ObjectName deployedObject;

   /** The sortName concatenated with the canonical names of all parents. */
   public String getCanonicalName()
   {
      String name = simpleName;
      if (parent != null)
         name = parent.getCanonicalName() + "/" + name;
      return name;
   }

   public URL getMetaDataFileURL(String resourcePath) throws IOException
   {
      URL resourceURL = null;
      if (resourcePath != null && resourcePath.length() > 0)
      {
         if (resourcePath.startsWith("/"))
            resourcePath = resourcePath.substring(1);

         try
         {
            // assign an absolute URL 
            resourceURL = new URL(resourcePath);
         }
         catch (MalformedURLException ex)
         {
            // ignore
         }

         if (resourceURL == null && vfRoot != null)
         {
            UnifiedVirtualFile vfResource = vfRoot.findChild(resourcePath);
            resourceURL = vfResource.toURL();
         }

         if (resourceURL == null)
         {
            String deploymentPath = url.toExternalForm();

            if (deploymentPath.startsWith("jar:") && deploymentPath.endsWith("!/") == false)
               deploymentPath += "!/";

            if (deploymentPath.endsWith("/") == false)
               deploymentPath += "/";

            // assign a relative URL
            resourceURL = new URL(deploymentPath + resourcePath);
         }
      }
      return resourceURL;
   }

   public String toString()
   {
      StringBuilder builder = new StringBuilder();
      builder.append("[");
      builder.append("type=" + type);
      builder.append(",simpleName=" + simpleName);
      builder.append(",url=" + url);
      builder.append("]");
      return builder.toString();
   }
}
