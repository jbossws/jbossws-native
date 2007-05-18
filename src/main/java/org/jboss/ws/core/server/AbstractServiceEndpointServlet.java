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

import java.io.IOException;

import javax.management.ObjectName;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.ws.WSException;
import org.jboss.wsintegration.spi.deployment.Endpoint;
import org.jboss.wsintegration.spi.invocation.RequestHandler;
import org.jboss.wsintegration.spi.management.EndpointRegistry;
import org.jboss.wsintegration.spi.management.EndpointRegistryFactory;
import org.jboss.wsintegration.spi.utils.ObjectNameFactory;

/**
 * A servlet that is installed for every web service endpoint.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 25-Apr-2007
 */
public abstract class AbstractServiceEndpointServlet extends HttpServlet
{
   protected Endpoint endpoint;
   protected EndpointRegistry epRegistry;

   public void init(ServletConfig config) throws ServletException
   {
      super.init(config);
      epRegistry = EndpointRegistryFactory.getEndpointRegistry();
   }

   public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      if (endpoint == null)
      {
         String contextPath = req.getContextPath();
         initServiceEndpoint(contextPath);
      }

      RequestHandler requestHandler = (RequestHandler)endpoint.getRequestHandler();
      requestHandler.handleHttpRequest(endpoint, req, res, getServletContext());
   }

   /** Initialize the service endpoint
    */
   protected void initServiceEndpoint(String contextPath)
   {
      String servletName = getServletName();
      if (contextPath.startsWith("/"))
         contextPath = contextPath.substring(1);

      for (ObjectName sepId : epRegistry.getEndpoints())
      {
         String propContext = sepId.getKeyProperty(Endpoint.SEPID_PROPERTY_CONTEXT);
         String propEndpoint = sepId.getKeyProperty(Endpoint.SEPID_PROPERTY_ENDPOINT);
         if (servletName.equals(propEndpoint) && contextPath.equals(propContext))
         {
            endpoint = epRegistry.getEndpoint(sepId);
            break;
         }
      }

      if (endpoint == null)
      {
         ObjectName oname = ObjectNameFactory.create(Endpoint.SEPID_DOMAIN + ":" + Endpoint.SEPID_PROPERTY_CONTEXT + "=" + contextPath + ","
               + Endpoint.SEPID_PROPERTY_ENDPOINT + "=" + servletName);
         throw new WSException("Cannot obtain endpoint for: " + oname);
      }
   }
}
