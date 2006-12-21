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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.jws.WebService;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.deployment.DeploymentInfo;
import org.jboss.ejb3.Ejb3ModuleMBean;
import org.jboss.ejb3.stateless.StatelessContainer;
import org.jboss.mx.util.MBeanProxy;
import org.jboss.mx.util.MBeanProxyCreationException;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.ws.WSException;
import org.jboss.ws.core.server.JAXWSDeployment;
import org.jboss.ws.core.server.UnifiedDeploymentInfo;
import org.jboss.ws.metadata.j2ee.UnifiedApplicationMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedBeanMetaData;
import org.jboss.ws.metadata.umdm.UnifiedMetaData;

/**
 * A deployer service that manages WS4EE compliant Web-Services for EJB3 Endpoints
 *
 * @author Thomas.Diesler@jboss.org
 * @since 10-May-2005
 */
public class DeployerInterceptorEJB3 extends DeployerInterceptorEJB implements DeployerInterceptorEJB3MBean
{
   protected UnifiedDeploymentInfo createUnifiedDeploymentInfo(DeploymentInfo di) throws Exception
   {
      UnifiedDeploymentInfo udi = new JAXWSDeployment(UnifiedDeploymentInfo.DeploymentType.JAXWS_EJB3);
      DeploymentInfoAdaptor.buildDeploymentInfo(udi, di);

      Ejb3ModuleMBean ejb3Module = getEJB3Module(udi.deployedObject);

      // The container objects below provide access to all of the ejb metadata
      ArrayList<UnifiedBeanMetaData> beans = new ArrayList<UnifiedBeanMetaData>();
      for (Object container : ejb3Module.getContainers().values())
      {
         if (container instanceof StatelessContainer)
         {
            StatelessContainer slc = (StatelessContainer)container;
            UnifiedBeanMetaData uslc = new UnifiedBeanMetaData();
            uslc.setEjbName(slc.getEjbName());
            uslc.setEjbClass(slc.getBeanClassName());
            beans.add(uslc);
         }
      }

      UnifiedApplicationMetaData appMetaData = new UnifiedApplicationMetaData();
      appMetaData.setEnterpriseBeans(beans);
      udi.metaData = appMetaData;

      return udi;
   }

   /** Return true if the deployment is a web service endpoint
    */
   protected boolean isWebserviceDeployment(DeploymentInfo di)
   {
      boolean isWebserviceDeployment = false;

      // Check if the ejb3 contains annotated endpoints
      Ejb3ModuleMBean ejb3Module = getEJB3Module(di.deployedObject);
      for (Object manager : ejb3Module.getContainers().values())
      {
         if (manager instanceof StatelessContainer)
         {
            StatelessContainer container = (StatelessContainer)manager;
            if (container.resolveAnnotation(WebService.class) != null)
            {
               isWebserviceDeployment = true;
               break;
            }
         }
      }

      return isWebserviceDeployment;
   }

   private Ejb3ModuleMBean getEJB3Module(ObjectName objectName)
   {
      Ejb3ModuleMBean ejb3Module;
      try
      {
         MBeanServer server = MBeanServerLocator.locateJBoss();
         ejb3Module = (Ejb3ModuleMBean)MBeanProxy.get(Ejb3ModuleMBean.class, objectName, server);
         if (ejb3Module == null)
            throw new WSException("Cannot obtain EJB3 module: " + objectName);

         return ejb3Module;
      }
      catch (MBeanProxyCreationException ex)
      {
         throw new WSException("Cannot obtain proxy to EJB3 module");
      }
   }

   protected URL generateWebDeployment(DeploymentInfo di, UnifiedMetaData wsMetaData) throws IOException
   {
      return new ServiceEndpointGeneratorEJB3().generatWebDeployment(di, wsMetaData);
   }
}
