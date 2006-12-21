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

import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import org.jboss.deployment.DeploymentInfo;
import org.jboss.metadata.WebMetaData;
import org.jboss.mx.server.Invocation;
import org.jboss.ws.WSException;
import org.jboss.ws.core.server.JAXWSDeployment;
import org.jboss.ws.core.server.ServiceEndpointPublisher;
import org.jboss.ws.core.server.UnifiedDeploymentInfo;
import org.jboss.ws.core.server.UnifiedDeploymentInfo.DeploymentType;
import org.jboss.ws.metadata.builder.jaxrpc.JAXRPCDeployment;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.ServerEndpointMetaData;
import org.jboss.ws.metadata.umdm.ServiceMetaData;
import org.jboss.ws.metadata.umdm.UnifiedMetaData;

/**
 * A deployer service that manages WS4EE compliant Web Services for WAR
 *
 * @author Thomas.Diesler@jboss.org
 * @since 15-Jan-2005
 */
public class DeployerInterceptorJSE extends DeployerInterceptor implements DeployerInterceptorJSEMBean
{
   protected UnifiedDeploymentInfo createUnifiedDeploymentInfo(DeploymentInfo di) throws Exception
   {
      UnifiedDeploymentInfo udi;
      
      DeploymentType type = (DeploymentType)di.context.get("UnifiedDeploymentInfo.Type");
      if (type == UnifiedDeploymentInfo.DeploymentType.JAXRPC_JSE)
      {
         URL webservicesURL = getWebservicesDescriptor(di);
         udi = new JAXRPCDeployment(UnifiedDeploymentInfo.DeploymentType.JAXRPC_JSE, webservicesURL);
         DeploymentInfoAdaptor.buildDeploymentInfo(udi, di);
         
      }
      else if (type == UnifiedDeploymentInfo.DeploymentType.JAXWS_JSE)
      {
         udi = new JAXWSDeployment(UnifiedDeploymentInfo.DeploymentType.JAXWS_JSE);
         DeploymentInfoAdaptor.buildDeploymentInfo(udi, di);
      }
      else 
      {
         throw new WSException("Unexpected type:  " + type);
      }
      
      return udi;
   }

   /** Overwrite to create the webservice
    *
    * This implemantation modifies the servlet entries in web.xml
    */
   protected Object create(Invocation invocation, DeploymentInfo di) throws Throwable
   {
      Object retn = super.create(invocation, di);

      UnifiedDeploymentInfo udi = getServiceEndpointDeployment(di);
      if (udi != null)
      {
         ServiceEndpointPublisher endpointPublisher = getServiceEndpointPublisher();
         endpointPublisher.rewriteWebXml(udi);
      }
      return retn;
   }
     
   /** Return true if the deployment is a web service endpoint
    */
   protected boolean isWebserviceDeployment(DeploymentInfo di)
   {
      WebMetaData webMetaData = (WebMetaData)di.metaData;
      boolean isWebserviceDeployment = webMetaData.isWebServiceDeployment();

      // Check if we have a webservices.xml descriptor
      if (isWebserviceDeployment == false)
      {
         if (getWebservicesDescriptor(di) != null)
         {
            di.context.put("UnifiedDeploymentInfo.Type", UnifiedDeploymentInfo.DeploymentType.JAXRPC_JSE);
            isWebserviceDeployment = true;
         }
      }

      // Check if the web.xml contains annotated endpoint impl
      if (isWebserviceDeployment == false)
      {
         Map servletClassMap = webMetaData.getServletClassMap();
         Iterator<String> it = servletClassMap.values().iterator();
         while (it.hasNext() && isWebserviceDeployment == false)
         {
            String servletClassName = it.next();
            try
            {
               Class servletClass = di.annotationsCl.loadClass(servletClassName);
               if (servletClass.isAnnotationPresent(javax.jws.WebService.class))
               {
                  di.context.put("UnifiedDeploymentInfo.Type", UnifiedDeploymentInfo.DeploymentType.JAXWS_JSE);
                  isWebserviceDeployment = true;
               }
            }
            catch (ClassNotFoundException ex)
            {
               log.warn("Cannot load servlet class: " + servletClassName);
            }
         }
      }

      webMetaData.setWebServiceDeployment(isWebserviceDeployment);
      return isWebserviceDeployment;
   }

   /**
    * Get the resource name of the webservices.xml descriptor.
    */
   protected URL getWebservicesDescriptor(DeploymentInfo di)
   {
      return di.localCl.findResource("WEB-INF/webservices.xml");
   }
}
