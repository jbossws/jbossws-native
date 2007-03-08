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
package org.jboss.ws.metadata.builder.jaxrpc;

import java.io.InputStream;
import java.net.URL;

import org.jboss.ws.WSException;
import org.jboss.ws.core.server.UnifiedDeploymentInfo;
import org.jboss.ws.integration.UnifiedVirtualFile;
import org.jboss.ws.metadata.webservices.WebservicesFactory;
import org.jboss.ws.metadata.webservices.WebservicesMetaData;
import org.jboss.xb.binding.ObjectModelFactory;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;

// $Id$

/**
 * The container independent deployment info. 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 05-May-2006
 */
public class JAXRPCDeployment extends UnifiedDeploymentInfo
{
   private WebservicesMetaData jsr109MetaData;

   public JAXRPCDeployment(DeploymentType type, UnifiedVirtualFile vfWebservices)
   {
      super(type);

      try
      {
         // Unmarshall webservices.xml
         URL webservicesURL = vfWebservices.toURL();
         InputStream is = webservicesURL.openStream();
         try
         {
            Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
            ObjectModelFactory factory = new WebservicesFactory(webservicesURL);
            jsr109MetaData = (WebservicesMetaData)unmarshaller.unmarshal(is, factory, null);
         }
         finally
         {
            is.close();
         }
      }
      catch (Exception ex)
      {
         WSException.rethrow(ex);
      }
   }

   public WebservicesMetaData getWebservicesMetaData()
   {
      return jsr109MetaData;
   }
}
