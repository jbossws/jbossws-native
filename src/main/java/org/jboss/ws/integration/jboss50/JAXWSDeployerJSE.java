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
package org.jboss.ws.integration.jboss50;

//$Id$

import java.util.Iterator;
import java.util.Set;

import javax.jws.WebService;
import javax.xml.ws.WebServiceProvider;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentUnit;
import org.jboss.metadata.WebMetaData;
import org.jboss.metadata.web.Servlet;
import org.jboss.ws.core.server.JAXWSDeployment;
import org.jboss.ws.core.server.UnifiedDeploymentInfo;
import org.jboss.ws.core.server.UnifiedDeploymentInfo.DeploymentType;

/**
 * A deployer JAXWS JSE Endpoints
 *
 * @author Thomas.Diesler@jboss.org
 * @since 31-Oct-2005
 */
public class JAXWSDeployerJSE extends AbstractJSEDeployer
{
   @Override
   protected DeploymentType getDeploymentType()
   {
      return DeploymentType.JAXWS_JSE;
   }

   @Override
   public boolean isWebServiceDeployment(DeploymentUnit unit)
   {
      boolean isWebServiceDeployment = false;

      Set<? extends WebMetaData> allMetaData = unit.getAllMetaData(WebMetaData.class);
      if (allMetaData.size() > 0)
      {
         WebMetaData webMetaData = allMetaData.iterator().next();
         try
         {
            Iterator it = webMetaData.getServlets().iterator();
            while (it.hasNext())
            {
               Servlet servlet = (Servlet)it.next();
               String servletClassName = servlet.getServletClass();
               
               // Skip JSPs
               if (servletClassName == null)
                  continue;

               Class<?> servletClass = null;
               try
               {
                  ClassLoader loader = unit.getClassLoader();
                  servletClass = loader.loadClass(servletClassName.trim());
               }
               catch (ClassNotFoundException ex)
               {
                  log.warn("Cannot load servlet class: " + servletClassName);
                  continue;
               }
               
               if (servletClass.isAnnotationPresent(WebService.class) || servletClass.isAnnotationPresent(WebServiceProvider.class))
               {
                  isWebServiceDeployment = true;
                  break;
               }
            }
         }
         catch (Exception ex)
         {
            log.error("Cannot process web deployment", ex);
         }
      }

      return isWebServiceDeployment;
   }

   @Override
   protected UnifiedDeploymentInfo createUnifiedDeploymentInfo(DeploymentUnit unit) throws DeploymentException
   {
      UnifiedDeploymentInfo udi = new JAXWSDeployment(getDeploymentType());
      DeploymentInfoAdaptor.buildDeploymentInfo(udi, unit);
      return udi;
   }
}