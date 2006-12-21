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

//$Id$

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import org.jboss.deployment.DeploymentInfo;
import org.jboss.metadata.ApplicationMetaData;
import org.jboss.metadata.BeanMetaData;
import org.jboss.ws.WSException;
import org.jboss.ws.core.server.JAXWSDeployment;
import org.jboss.ws.core.server.UnifiedDeploymentInfo;
import org.jboss.ws.metadata.builder.jaxrpc.JAXRPCDeployment;
import org.jboss.ws.metadata.umdm.UnifiedMetaData;

/**
 * A deployer service that manages WS4EE compliant Web-Services for EJB-2.1 Endpoints
 *
 * @author Thomas.Diesler@jboss.org
 * @since 15-Jan-2005
 */
public class DeployerInterceptorEJB21 extends DeployerInterceptorEJB implements DeployerInterceptorEJB21MBean
{
   protected UnifiedDeploymentInfo createUnifiedDeploymentInfo(DeploymentInfo di) throws Exception
   {
      UnifiedDeploymentInfo udi;
      URL webservicesURL = getWebservicesDescriptor(di);
      if (webservicesURL != null)
      {
         udi = new JAXRPCDeployment(UnifiedDeploymentInfo.DeploymentType.JAXRPC_EJB21, webservicesURL);
         DeploymentInfoAdaptor.buildDeploymentInfo(udi, di);
      }
      else
      {
         udi = new JAXWSDeployment(UnifiedDeploymentInfo.DeploymentType.JAXWS_EJB21);
         DeploymentInfoAdaptor.buildDeploymentInfo(udi, di);
      }
      return udi;
   }

   /** Return true if the deployment is a web service endpoint
    */
   protected boolean isWebserviceDeployment(DeploymentInfo di)
   {
      ApplicationMetaData applMetaData = (ApplicationMetaData)di.metaData;
      boolean isWebserviceDeployment = applMetaData.isWebServiceDeployment();

      // Check if we have a webservices.xml descriptor
      if (isWebserviceDeployment == false)
      {
         isWebserviceDeployment = getWebservicesDescriptor(di) != null;
      }

      // Check if the ejb-jar contains annotated endpoints
      if (isWebserviceDeployment == false)
      {
         try
         {
            Iterator itBeans = applMetaData.getEnterpriseBeans();
            while (itBeans.hasNext() && isWebserviceDeployment == false)
            {
               BeanMetaData beanMetaData = (BeanMetaData)itBeans.next();
               String ejbClassName = beanMetaData.getEjbClass();
               Class ejbClass = di.annotationsCl.loadClass(ejbClassName);
               isWebserviceDeployment = ejbClass.isAnnotationPresent(javax.jws.WebService.class);
            }
         }
         catch (RuntimeException e)
         {
            throw e;
         }
         catch (Exception e)
         {
            throw new WSException(e);
         }
      }

      applMetaData.setWebServiceDeployment(isWebserviceDeployment);
      return isWebserviceDeployment;
   }

   /**
    * Get the resource name of the webservices.xml descriptor.
    */
   protected URL getWebservicesDescriptor(DeploymentInfo di)
   {
      return di.localCl.findResource("META-INF/webservices.xml");
   }
   
   protected URL generateWebDeployment(DeploymentInfo di, UnifiedMetaData wsMetaData) throws IOException
   {
      ServiceEndpointGeneratorEJB21 generator = new ServiceEndpointGeneratorEJB21();
      return generator.generatWebDeployment(di, wsMetaData);
   }

}
