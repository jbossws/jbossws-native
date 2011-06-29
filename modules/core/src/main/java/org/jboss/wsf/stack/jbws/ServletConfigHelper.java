/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.wsf.stack.jbws;

import java.util.ResourceBundle;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.metadata.umdm.ServerEndpointMetaData;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.metadata.config.CommonConfig;

/**
 * Native servlet configuration helper
 * @author richard.opalka@jboss.com
 */
public final class ServletConfigHelper
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(ServletConfigHelper.class);
   
   /**
    * Constructor
    */
   private ServletConfigHelper() {}
   
   /**
    * Reads the config name/file from web.xml
    * @param servletConfig servlet config
    * @param endpoint endpoint instance
    */
   public static void initEndpointConfig(ServletConfig servletConfig, Endpoint endpoint)
   {
      final ServletContext servletContext = servletConfig.getServletContext();
      final String configName = servletContext.getInitParameter(CommonConfig.JBOSSWS_CONFIG_NAME);
      final String configFile = servletContext.getInitParameter(CommonConfig.JBOSSWS_CONFIG_FILE);

      if (configName != null || configFile != null)
      {
         ServerEndpointMetaData epMetaData = endpoint.getAttachment(ServerEndpointMetaData.class);
         if (epMetaData == null)
            throw new IllegalStateException(BundleUtils.getMessage(bundle, "CANNOT_OBTAIN_ENDPOINTMD"));

         epMetaData.setConfigName(configName, configFile);
      }
   }

}
