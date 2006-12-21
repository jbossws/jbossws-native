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

// $Id$

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jboss.deployers.spi.deployer.DeploymentUnit;
import org.jboss.ejb3.Container;
import org.jboss.ejb3.EJBContainer;
import org.jboss.ejb3.Ejb3Deployment;
import org.jboss.ejb3.SessionContainer;
import org.jboss.ejb3.mdb.MessagingContainer;
import org.jboss.logging.Logger;
import org.jboss.ws.metadata.j2ee.UnifiedApplicationMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedBeanMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedMessageDrivenMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedSessionMetaData;

/**
 * Build container independent application meta data 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 05-May-2006
 */
public class ApplicationMetaDataAdaptor
{
   // logging support
   private static Logger log = Logger.getLogger(ApplicationMetaDataAdaptor.class);
   
   public static UnifiedApplicationMetaData buildUnifiedApplicationMetaData(DeploymentUnit unit)
   {
      UnifiedApplicationMetaData umd = new UnifiedApplicationMetaData();
      buildUnifiedBeanMetaData(umd, unit);
      //umd.setConfigName(apmd.getConfigName());
      //umd.setConfigFile(apmd.getConfigFile());
      //umd.setWebServiceContextRoot(apmd.getWebServiceContextRoot());
      //umd.setSecurityDomain(apmd.getSecurityDomain());
      //umd.setWsdlPublishLocationMap(apmd.getWsdlPublishLocationMap());
      return umd;
   }

   private static void buildUnifiedBeanMetaData(UnifiedApplicationMetaData umd, DeploymentUnit unit)
   {
      List<UnifiedBeanMetaData> beans = new ArrayList<UnifiedBeanMetaData>();
      Ejb3Deployment ejb3Deployment = unit.getAttachment(Ejb3Deployment.class);
      Iterator<Container> it = ejb3Deployment.getEjbContainers().values().iterator();
      while (it.hasNext())
      {
         EJBContainer container = (EJBContainer)it.next();
         UnifiedBeanMetaData ubmd = buildUnifiedBeanMetaData(container);
         if (ubmd != null)
         {
            beans.add(ubmd);
         }
      }
      umd.setEnterpriseBeans(beans);
   }

   private static UnifiedBeanMetaData buildUnifiedBeanMetaData(EJBContainer container)
   {
      UnifiedBeanMetaData ubmd = null;
      if (container instanceof SessionContainer)
      {
         ubmd = new UnifiedSessionMetaData();
      }
      else if (container instanceof MessagingContainer)
      {
         ubmd = new UnifiedMessageDrivenMetaData();
         log.warn ("No implemented: initialize MDB destination");
         //((UnifiedMessageDrivenMetaData)ubmd).setDestinationJndiName(((MessagingContainer)container).getDestination());
      }

      if (ubmd != null)
      {
         ubmd.setEjbName(container.getEjbName());
         ubmd.setEjbClass(container.getBeanClassName());
//         ubmd.setServiceEndpoint(container.getServiceEndpoint());
//         ubmd.setHome(container.getHome());
//         ubmd.setLocalHome(container.getLocalHome());
//         ubmd.setJndiName(container.getJndiName());
//         ubmd.setLocalJndiName(container.getLocalJndiName());

//         EjbPortComponentMetaData pcmd = container.getPortComponent();
//         if (pcmd != null)
//         {
//            UnifiedEjbPortComponentMetaData upcmd = new UnifiedEjbPortComponentMetaData();
//            upcmd.setPortComponentName(pcmd.getPortComponentName());
//            upcmd.setPortComponentURI(pcmd.getPortComponentURI());
//            upcmd.setAuthMethod(pcmd.getAuthMethod());
//            upcmd.setTransportGuarantee(pcmd.getTransportGuarantee());
//            ubmd.setPortComponent(upcmd);
//         }
      }
      return ubmd;
   }
}
