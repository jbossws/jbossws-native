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
// $Id$
package org.jboss.ws.metadata.builder.jaxws;

import java.util.Iterator;

import javax.jws.WebService;
import javax.xml.ws.WebServiceProvider;

import org.jboss.annotation.security.SecurityDomain;
import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.core.server.UnifiedDeploymentInfo;
import org.jboss.ws.metadata.j2ee.UnifiedApplicationMetaData;
import org.jboss.ws.metadata.j2ee.UnifiedBeanMetaData;
import org.jboss.ws.metadata.umdm.UnifiedMetaData;

/**
 * A server side meta data builder that is based on JSR-181 annotations
 *
 * @author Thomas.Diesler@jboss.org
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @since 19-May-2005
 */
public class JAXWSMetaDataBuilderEJB3
{
   // provide logging
   private final Logger log = Logger.getLogger(JAXWSMetaDataBuilderEJB3.class);

   protected Class annotatedClass;

   /** Build from webservices.xml
    */
   public UnifiedMetaData buildMetaData(UnifiedDeploymentInfo udi)
   {
      if(log.isDebugEnabled()) log.debug("START buildMetaData: [name=" + udi.getCanonicalName() + "]");
      try
      {
         UnifiedMetaData wsMetaData = new UnifiedMetaData(udi.vfRoot);
         wsMetaData.setDeploymentName(udi.getCanonicalName());
         wsMetaData.setClassLoader(udi.classLoader);

         if (udi.classLoader == null)
            throw new WSException("Deployment class loader not initialized");

         // The container objects below provide access to all of the ejb metadata
         UnifiedApplicationMetaData appMetaData = (UnifiedApplicationMetaData)udi.metaData;
         Iterator<UnifiedBeanMetaData> it = appMetaData.getEnterpriseBeans();
         while (it.hasNext())
         {
            UnifiedBeanMetaData beanMetaData = it.next();
            String ejbClassName = beanMetaData.getEjbClass();
            Class<?> beanClass = udi.classLoader.loadClass(ejbClassName);
            if (beanClass.isAnnotationPresent(WebService.class) || beanClass.isAnnotationPresent(WebServiceProvider.class))
            {
               String ejbLink = beanMetaData.getEjbName();
               JAXWSServerMetaDataBuilder.setupProviderOrWebService(wsMetaData, udi, beanClass, ejbLink);

               // setup the security domain
               if (beanClass.isAnnotationPresent(SecurityDomain.class))
               {
                  SecurityDomain anSecurityDomain = (SecurityDomain)beanClass.getAnnotation(SecurityDomain.class);
                  String lastDomain = wsMetaData.getSecurityDomain();
                  String securityDomain = anSecurityDomain.value();
                  if (lastDomain != null && lastDomain.equals(securityDomain) == false)
                     throw new IllegalStateException("Multiple security domains not supported: " + securityDomain);

                  wsMetaData.setSecurityDomain(securityDomain);
               }
            }
         }

         if(log.isDebugEnabled()) log.debug("END buildMetaData: " + wsMetaData);
         return wsMetaData;
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         throw new WSException("Cannot build meta data: " + ex.getMessage(), ex);
      }
   }
}
