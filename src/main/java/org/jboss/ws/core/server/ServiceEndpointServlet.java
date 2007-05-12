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
package org.jboss.ws.core.server;

// $Id$

import javax.servlet.ServletContext;

import org.jboss.logging.Logger;
import org.jboss.ws.metadata.umdm.ServerEndpointMetaData;

/**
 * A servlet that is installed for every web service endpoint.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 25-Apr-2007
 */
public class ServiceEndpointServlet extends AbstractServiceEndpointServlet
{
   // provide logging
   private static final Logger log = Logger.getLogger(ServiceEndpointServlet.class);

   /** Initialize the service endpoint
    */
   protected void initServiceEndpoint(String contextPath)
   {
      super.initServiceEndpoint(contextPath);

      // read the config name/file from web.xml
      ServletContext ctx = getServletContext();
      String configName = ctx.getInitParameter("jbossws-config-name");
      String configFile = ctx.getInitParameter("jbossws-config-file");
      if (configName != null || configFile != null)
      {
         ServerEndpointMetaData epMetaData = endpoint.getMetaData(ServerEndpointMetaData.class);
         if (epMetaData == null)
            throw new IllegalStateException("Cannot obtain endpoint meta data");

         log.debug("Updating service endpoint config\n  config-name: " + configName + "\n  config-file: " + configFile);
         epMetaData.setConfigName(configName, configFile);
      }
   }
}
