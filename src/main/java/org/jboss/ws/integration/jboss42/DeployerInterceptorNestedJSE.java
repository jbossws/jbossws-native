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
package org.jboss.ws.integration.jboss42;

// $Id$

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.MainDeployerMBean;
import org.jboss.deployment.SubDeployerSupport;
import org.jboss.system.server.ServerConfig;
import org.jboss.system.server.ServerConfigLocator;
import org.jboss.ws.core.utils.IOUtils;

/**
 * A deployer service that manages WS4EE compliant Web Services for 
 * nested POJO endpoints.
 *
 * The WebServiceDeployerJSE attaches itself as an deployment interceptor to
 * the jboss.web:service=WebServer deployer. As a consequence, all *.war deployments 
 * that are picked up before the interceptor is installed are not treated as potential 
 * web service endpoint deployments.
 * 
 * Nested POJO endpoints can be packaged in *.jse deployments that are then picked up by this 
 * deployer.
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 03-Mar-2005
 */
public class DeployerInterceptorNestedJSE extends SubDeployerSupport implements DeployerInterceptorNestedJSEMBean
{
   private static final String NESTED_JSE_WAR_FILE = "org.jboss.ws.server.nested.jse";

   // The MainDeployer
   protected MainDeployerMBean mainDeployer;

   public void setMainDeployer(MainDeployerMBean mainDeployer)
   {
      this.mainDeployer = mainDeployer;
   }

   public boolean accepts(DeploymentInfo sdi)
   {
      setSuffixes(new String[] { ".jse" });
      return super.accepts(sdi);
   }

   /** Copy the *.jse to a *.war and deploy through the main deployer
    */
   public void create(DeploymentInfo di) throws DeploymentException
   {
      log.debug("create: " + di.url);

      try
      {
         File jseFile = new File(di.localUrl.getFile());
         if (jseFile.isFile() == false)
            throw new DeploymentException("Expected a file: " + di.localUrl);

         ServerConfig config = ServerConfigLocator.locate();
         String warFileName = config.getServerTempDir().getCanonicalPath() + "/deploy/" + di.shortName;
         warFileName = warFileName.substring(0, warFileName.length() - 4) + ".war";
         File warFile = new File(warFileName);

         FileOutputStream fos = new FileOutputStream(warFile);
         FileInputStream fis = new FileInputStream(jseFile);
         try
         {
            IOUtils.copyStream(fos, fis);
         }
         finally
         {
            fos.close();
            fis.close();
         }

         mainDeployer.deploy(warFile.toURL());

         // remember the war url that we deployed 
         di.context.put(NESTED_JSE_WAR_FILE, warFile);

         super.create(di);
      }
      catch (IOException ex)
      {
         throw new DeploymentException("Failed to create: " + di.url, ex);
      }
   }

   /** Undeploy the *.war through the main deployer
    */
   public void destroy(DeploymentInfo di) throws DeploymentException
   {
      log.debug("destroy: " + di.url);
      try
      {
         File warFile = (File)di.context.get(NESTED_JSE_WAR_FILE);
         mainDeployer.undeploy(warFile.toURL());
         warFile.delete();

         super.destroy(di);
      }
      catch (IOException ex)
      {
         throw new DeploymentException("Failed to destroy: " + di.url, ex);
      }
   }
}
