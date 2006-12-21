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
package org.jboss.ws.integration.tomcat;

// $Id$

import java.io.File;
import java.net.URL;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.core.CommonContextServlet;
import org.jboss.ws.core.server.ServiceEndpointManagerFactory;

/**
 * The servlet that that is associated with context /jbossws
 *
 * @author Thomas.Diesler@jboss.org
 * @since 21-Mar-2005
 */
public class TomcatContextServlet extends CommonContextServlet
{
   // provide logging
   protected final Logger log = Logger.getLogger(TomcatContextServlet.class);

   protected void initServiceEndpointManager()
   {
      try
      {
         String beansPath = getServletContext().getRealPath("/META-INF/jboss-beans.xml");
         URL beansXML = new File(beansPath).toURL();
         if (beansXML == null)
            throw new IllegalStateException("Cannot find: " + beansPath);

         new KernelBootstrap().bootstrap(beansXML);
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         throw new WSException("Cannot bootstrap kernel", ex);
      }

      // Initialize the ServiceEndpointManager
      ServiceEndpointManagerFactory factory = ServiceEndpointManagerFactory.getInstance();
      epManager = factory.getServiceEndpointManager();
   }
}
