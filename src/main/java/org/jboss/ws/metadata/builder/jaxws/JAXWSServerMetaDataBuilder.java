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
package org.jboss.ws.metadata.builder.jaxws;

// $Id$

import java.io.IOException;

import javax.jws.WebService;
import javax.xml.ws.WebServiceProvider;

import org.jboss.ws.core.server.UnifiedDeploymentInfo;
import org.jboss.ws.metadata.umdm.UnifiedMetaData;

/** An abstract annotation meta data builder.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 27-Jun-2006
 */
public abstract class JAXWSServerMetaDataBuilder extends JAXWSMetaDataBuilder
{
   public abstract UnifiedMetaData buildMetaData(UnifiedDeploymentInfo udi);

   protected JAXWSEndpointMetaDataBuilder getEndpointBuilder(Class<?> bean)
   {
      if (bean.isAnnotationPresent(WebServiceProvider.class))
         return new JAXWSProviderMetaDataBuilder();

      if (bean.isAnnotationPresent(WebService.class))
         return new JAXWSWebServiceMetaDataBuilder();

      return null;
   }

   protected void setupEndpoint(UnifiedMetaData umd, UnifiedDeploymentInfo udi, Class<?> beanClass, String beanName)
      throws SecurityException, ClassNotFoundException, NoSuchMethodException, IOException
   {
      JAXWSEndpointMetaDataBuilder builder = getEndpointBuilder(beanClass);
      if (builder != null)
         builder.buildEndpointMetaData(umd, udi, beanClass, beanName);
   }
}
