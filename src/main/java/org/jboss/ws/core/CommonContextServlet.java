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
package org.jboss.ws.core;

// $Id$

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.logging.Logger;
import org.jboss.ws.core.server.ServiceEndpointManager;

/**
 * The servlet that that is associated with context /jbossws
 *
 * @author Thomas.Diesler@jboss.org
 * @since 21-Mar-2005
 */
public abstract class CommonContextServlet extends HttpServlet
{
   // provide logging
   protected final Logger log = Logger.getLogger(CommonContextServlet.class);

   protected ServiceEndpointManager epManager;

   public void init(ServletConfig config) throws ServletException
   {
      super.init(config);
      initServiceEndpointManager();
   }

   protected abstract void initServiceEndpointManager();

   /** Process GET requests.
    */
   public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      PrintWriter writer = res.getWriter();
      res.setContentType("text/html");

      writer.print("<html>");
      setupHTMLResponseHeader(writer);

      writer.print("<body>");
      //writer.print(epManager.showServiceEndpointTable());
      writer.print(epManager.showServiceEndpointTable(new URL(req.getRequestURL().toString())));
      writer.print("</body>");
      writer.print("</html>");
      writer.close();
   }

   private void setupHTMLResponseHeader(PrintWriter writer)
   {
      Package wsPackage = Package.getPackage("org.jboss.ws");
      writer.println("<head>");
      writer.println("<meta http-equiv='Content-Type content='text/html; charset=iso-8859-1'>");
      writer.println("<title>JBossWS / "+wsPackage.getImplementationVersion()+"</title>");
      writer.println("<link rel='stylesheet' href='./styles.css'>");
      writer.println("</head>");
   }
}
