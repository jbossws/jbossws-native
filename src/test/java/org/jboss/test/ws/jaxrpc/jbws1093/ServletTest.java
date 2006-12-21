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
package org.jboss.test.ws.jaxrpc.jbws1093;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JBWS-1093 - This servlet is called ServletTest to check that we are
 * not identifying servlets by the classname ending in 'Servlet'.
 * 
 * @author darran.lofthouse@jboss.com
 * @since 17-October-2006
 */
public class ServletTest extends HttpServlet
{

   private static final long serialVersionUID = 8465532467878198647L;

   public static final String MESSAGE = "Success!!";

   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
   {
      PrintWriter writer = response.getWriter();
      writer.println(MESSAGE);
   }

}
