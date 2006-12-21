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

import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentUnit;
import org.jboss.metadata.NameValuePair;
import org.jboss.metadata.WebMetaData;
import org.jboss.metadata.web.Servlet;
import org.jboss.ws.core.server.ServiceEndpointPublisher;

/**
 * An abstract deployer for JSE Endpoints
 *
 * @author Thomas.Diesler@jboss.org
 * @since 31-Oct-2006
 */
public abstract class AbstractJSEDeployer extends AbstractDeployer
{
   public AbstractJSEDeployer()
   {
      super();
      super.setRelativeOrder(CLASSLOADER_DEPLOYER + 1);
   }

   /** 
    * After the service endpoint has successfully been deployed to the ServiceEndpointManager,
    *  
    */
   @Override
   protected void deployServiceEndpoint(DeploymentUnit unit) throws Exception
   {
      // Call the super implementation
      super.deployServiceEndpoint(unit);

      // FIXME: JBAS-3812 - TomcatDeployment should use modified WebMetaData
      InputStream stream = unit.getDeploymentContext().getRoot().findChild("WEB-INF/web.xml").openStream();
      URL webXml = getServiceEndpointPublisher().rewriteWebXml(stream, null, unit.getClassLoader());

      modifyWebMetaData(unit, webXml);
   }

   private void modifyWebMetaData(DeploymentUnit unit, URL altDD) throws DeploymentException
   {
      try
      {
         Set<? extends WebMetaData> allMetaData = unit.getAllMetaData(WebMetaData.class);
         if (allMetaData.size() > 0)
         {
            WebMetaData webMetaData = allMetaData.iterator().next();
            String serviceEndpointServlet = getServiceEndpointPublisher().getServiceEndpointServlet();

            Iterator it = webMetaData.getServlets().iterator();
            while (it.hasNext())
            {
               Servlet servlet = (Servlet)it.next();
               String servletClassName = servlet.getServletClass();

               // JSP
               if (servletClassName == null)
                  continue;

               // Nothing to do if we have an <init-param>
               if (isAlreadyModified(servlet) == false)
               {
                  servlet.setServletClass(serviceEndpointServlet);
                  NameValuePair initParam = new NameValuePair(ServiceEndpointPublisher.INIT_PARAM_SERVICE_ENDPOINT_IMPL, servletClassName);
                  servlet.addInitParam(initParam);
               }
            }

            // FIXME: JBAS-3812 - TomcatDeployment should use modified WebMetaData
            webMetaData.setAltDDPath(altDD.toExternalForm());
         }
      }
      catch (Exception ex)
      {
         DeploymentException.rethrowAsDeploymentException(ex.getMessage(), ex);
      }
   }

   private boolean isAlreadyModified(Servlet servlet)
   {
      Iterator itParams = servlet.getInitParams().iterator();
      while (itParams.hasNext())
      {
         NameValuePair pair = (NameValuePair)itParams.next();
         if (ServiceEndpointPublisher.INIT_PARAM_SERVICE_ENDPOINT_IMPL.equals(pair.getName()))
            return true;
      }
      return false;
   }
}
