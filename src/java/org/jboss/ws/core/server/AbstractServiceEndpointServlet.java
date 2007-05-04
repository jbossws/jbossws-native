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
import java.io.Writer;

import javax.management.ObjectName;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.rpc.JAXRPCException;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.integration.Endpoint;
import org.jboss.ws.integration.ObjectNameFactory;
import org.jboss.ws.integration.RequestHandler;
import org.jboss.ws.integration.management.EndpointRegistry;
import org.jboss.ws.integration.management.EndpointRegistryFactory;

/**
 * A servlet that is installed for every web service endpoint.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 25-Apr-2007
 */
public abstract class AbstractServiceEndpointServlet extends HttpServlet
{
   // provide logging
   private static final Logger log = Logger.getLogger(AbstractServiceEndpointServlet.class);

   protected Endpoint endpoint;
   protected EndpointRegistry epRegistry;

   public void init(ServletConfig config) throws ServletException
   {
      super.init(config);
      epRegistry = EndpointRegistryFactory.getEndpointRegistry();
   }

   public void destroy()
   {
      super.destroy();
   }

   public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      if (endpoint == null)
      {
         String contextPath = req.getContextPath();
         initServiceEndpoint(contextPath);
      }
      super.service(req, res);
   }

   public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      // Process a WSDL request
      if (req.getParameter("wsdl") != null || req.getParameter("WSDL") != null)
      {
         res.setContentType("text/xml");
         ServletOutputStream out = res.getOutputStream();
         try
         {
            RequestHandler requestHandler = endpoint.getRequestHandler();
            ServletRequestContext context = new ServletRequestContext(getServletContext(), req, res);
            requestHandler.handleWSDLRequest(endpoint, out, context);
         }
         catch (Exception ex)
         {
            handleException(ex);
         }
         finally
         {
            try
            {
               out.close();
            }
            catch (IOException ioex)
            {
               log.error("Cannot close output stream");
            }
         }
      }
      else
      {
         res.setStatus(405);
         res.setContentType("text/plain");
         Writer out = res.getWriter();
         out.write("HTTP GET not supported");
         out.close();
      }
   }

   public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      log.debug("doPost: " + req.getRequestURI());

      ServletInputStream in = req.getInputStream();
      ServletOutputStream out = res.getOutputStream();
      try
      {
         RequestHandler requestHandler = endpoint.getRequestHandler();
         ServletRequestContext context = new ServletRequestContext(getServletContext(), req, res);
         requestHandler.handleRequest(endpoint, in, out, context);
      }
      catch (Exception ex)
      {
         handleException(ex);
      }
      finally 
      {
         try
         {
            out.close();
         }
         catch (IOException ioex)
         {
            log.error("Cannot close output stream");
         }
      }
   }

   private void handleException(Exception ex) throws ServletException
   {
      log.error("Error processing web service request", ex);

      if (ex instanceof JAXRPCException)
         throw (JAXRPCException)ex;

      throw new ServletException(ex);
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
