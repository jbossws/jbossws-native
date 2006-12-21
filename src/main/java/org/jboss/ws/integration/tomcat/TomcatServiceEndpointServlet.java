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

// $Id: ServiceEndpointServlet.java 296 2006-05-08 19:45:49Z thomas.diesler@jboss.com $

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.kernel.spi.registry.KernelRegistry;
import org.jboss.kernel.spi.registry.KernelRegistryEntry;
import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.core.server.AbstractServiceEndpointServlet;
import org.jboss.ws.core.server.JAXWSDeployment;
import org.jboss.ws.core.server.KernelLocator;
import org.jboss.ws.core.server.ServiceEndpointDeployer;
import org.jboss.ws.core.server.UnifiedDeploymentInfo;
import org.jboss.ws.metadata.builder.jaxrpc.JAXRPCDeployment;

/**
 * A servlet that is installed for every web service endpoint.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 16-May-2006
 */
public class TomcatServiceEndpointServlet extends AbstractServiceEndpointServlet
{
   // provide logging
   private static final Logger log = Logger.getLogger(TomcatServiceEndpointServlet.class);

   public void init(ServletConfig config) throws ServletException
   {
      ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
      try
      {
         CrossContextLoader jbwsLoader = CrossContextLoader.newInstance(config.getServletContext());
         Thread.currentThread().setContextClassLoader(jbwsLoader);

         super.init(config);
         deployServiceEndpoints(getServletContext());
      }
      catch (Exception ex)
      {
         String message = "Failed to initialze service endpoint";
         log.error(message, ex);
         throw new WSException(message, ex);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(ctxLoader);
      }
   }

   public void destroy()
   {
      undeployServiceEndpoints(getServletContext());
      super.destroy();
   }

   public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
      try
      {
         if ((ctxLoader instanceof CrossContextLoader) == false)
         {
            CrossContextLoader jbwsLoader = CrossContextLoader.newInstance(getServletContext());
            Thread.currentThread().setContextClassLoader(jbwsLoader);
         }
         super.service(req, res);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(ctxLoader);
      }
   }

   /**
    * Bootstrap the Microkernel and initialize the
    * ServiceEndpointManager
    */
   protected void initServiceEndpointManager()
   {
      String jbosswsPath = getServletContext().getRealPath("/") + "../jbossws";
      File jbosswsDir = new File(jbosswsPath);
      if (jbosswsDir.exists() == false || jbosswsDir.isDirectory() == false)
         throw new IllegalStateException("Cannot find expanded dir: " + jbosswsDir);
      
      File beansFile = new File(jbosswsPath + "/META-INF/jboss-beans.xml");
      if (beansFile.exists() == false)
         throw new IllegalStateException("Cannot find jboss-beans.xml at: " + beansFile);

      try
      {
         new KernelBootstrap().bootstrap(beansFile.toURL());
      }
      catch (Exception ex)
      {
         String message = "Failed to bootstrap kernel";
         log.error(message, ex);
         throw new WSException(message, ex);
      }

      // init the service endpoint manager
      super.initServiceEndpointManager();
   }

   private void deployServiceEndpoints(ServletContext servletContext)
   {
      UnifiedDeploymentInfo udi = (UnifiedDeploymentInfo)servletContext.getAttribute(UnifiedDeploymentInfo.class.getName());
      if (udi == null)
      {
         ServiceEndpointDeployer deployer = getServiceEndpointDeployer();
         try
         {
            udi = createDeploymentInfo(servletContext);
            servletContext.setAttribute(UnifiedDeploymentInfo.class.getName(), udi);

            deployer.create(udi);
            deployer.start(udi);
         }
         catch (Throwable th)
         {
            String message = "Failed to deploy service endpoint";
            log.error(message);
            throw new WSException(message, th);
         }
      }
   }

   private UnifiedDeploymentInfo createDeploymentInfo(ServletContext servletContext) throws Exception
   {
      UnifiedDeploymentInfo udi;

      URLClassLoader ctxLoader = (URLClassLoader)Thread.currentThread().getContextClassLoader();
      URL webservicesURL = ctxLoader.findResource("WEB-INF/webservices.xml");
      if (webservicesURL != null)
      {
         udi = new JAXRPCDeployment(UnifiedDeploymentInfo.DeploymentType.JAXRPC_JSE, webservicesURL);
      }
      else
      {
         udi = new JAXWSDeployment(UnifiedDeploymentInfo.DeploymentType.JAXWS_JSE);
      }

      DeploymentInfoAdaptor.buildDeploymentInfo(udi, ctxLoader, servletContext);
      return udi;
   }

   private void undeployServiceEndpoints(ServletContext servletContext)
   {
      UnifiedDeploymentInfo udi = (UnifiedDeploymentInfo)servletContext.getAttribute(UnifiedDeploymentInfo.class.getName());
      if (udi != null)
      {
         ServiceEndpointDeployer deployer = getServiceEndpointDeployer();
         try
         {
            deployer.stop(udi);
            deployer.destroy(udi);
         }
         catch (Throwable th)
         {
            String message = "Failed to undeploy service endpoint";
            log.error(message);
         }
      }
   }

   private ServiceEndpointDeployer getServiceEndpointDeployer()
   {
      KernelRegistry registry = KernelLocator.getKernel().getRegistry();
      KernelRegistryEntry entry = registry.getEntry(ServiceEndpointDeployer.BEAN_NAME);
      return (ServiceEndpointDeployer)entry.getTarget();
   }
}
