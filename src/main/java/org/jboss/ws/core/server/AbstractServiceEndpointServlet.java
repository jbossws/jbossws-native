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

// $Id: AbstractServiceEndpointServlet.java 396 2006-05-23 09:48:45Z thomas.diesler@jboss.com $

import java.io.IOException;
import java.io.Writer;
import java.net.URL;

import javax.management.ObjectName;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.rpc.JAXRPCException;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.core.utils.ObjectNameFactory;
import org.jboss.ws.metadata.umdm.ServerEndpointMetaData;

/**
 * A servlet that is installed for every web service endpoint.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 15-Jan-2005
 */
public abstract class AbstractServiceEndpointServlet extends HttpServlet
{
   // provide logging
   private static final Logger log = Logger.getLogger(AbstractServiceEndpointServlet.class);

   protected ObjectName sepId;
   protected ServiceEndpointManager epManager;

   public void init(ServletConfig config) throws ServletException
   {
      super.init(config);
      initServiceEndpointManager();
   }

   public void destroy()
   {
      super.destroy();
   }

   public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      if (sepId == null)
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
         try
         {
            // For the base document the resourcePath should be null
            String resourcePath = (String)req.getParameter("resource");
            URL requestURL = new URL(req.getRequestURL().toString());
            epManager.processWSDLRequest(sepId, res.getOutputStream(), requestURL, resourcePath);
         }
         catch (Exception ex)
         {
            handleException(ex);
         }
      }
      else
      {
         res.setStatus(405);
         res.setContentType("text/plain");
         Writer out = res.getWriter();
         out.write("HTTP GET not supported");
         out.flush();
         out.close();
      }
   }

   public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      log.debug("doPost: " + req.getRequestURI());

      try
      {
         EndpointContext context = new EndpointContext(getServletContext(), req, res);
         epManager.processSOAPRequest(sepId, req.getInputStream(), res.getOutputStream(), context);
      }
      catch (Exception ex)
      {
         handleException(ex);
      }
   }

   private void handleException(Exception ex) throws ServletException
   {
      log.error("Error processing web service request", ex);

      if (ex instanceof JAXRPCException)
         throw (JAXRPCException)ex;

      throw new ServletException(ex);
   }

   protected void initServiceEndpointManager()
   {
      ServiceEndpointManagerFactory factory = ServiceEndpointManagerFactory.getInstance();
      epManager = factory.getServiceEndpointManager();
   }

   /** Initialize the service endpoint
    */
   protected void initServiceEndpoint(String contextPath)
   {
      String servletName = getServletName();
      if (contextPath.startsWith("/"))
         contextPath = contextPath.substring(1);

      for (ObjectName sepId : epManager.getServiceEndpoints())
      {
         String context = sepId.getKeyProperty(ServerEndpointMetaData.SEPID_PROPERTY_CONTEXT);
         String endpoint = sepId.getKeyProperty(ServerEndpointMetaData.SEPID_PROPERTY_ENDPOINT);
         if (servletName.equals(endpoint) && contextPath.equals(context))
         {
            this.sepId = sepId;
            break;
         }
      }

      if (sepId == null)
      {
         ObjectName oname = ObjectNameFactory.create(ServerEndpointMetaData.SEPID_DOMAIN + ":" + ServerEndpointMetaData.SEPID_PROPERTY_CONTEXT + "=" + contextPath
               + "," + ServerEndpointMetaData.SEPID_PROPERTY_ENDPOINT + "=" + servletName);
         throw new WSException("Cannot obtain endpoint for: " + oname);
      }
   }
}
