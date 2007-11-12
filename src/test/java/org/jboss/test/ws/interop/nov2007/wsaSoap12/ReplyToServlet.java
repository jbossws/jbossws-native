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
package org.jboss.test.ws.interop.nov2007.wsaSoap12;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.BufferedReader;

/**
 * @author Heiko.Braun@jboss.org
 * @version $Id$
 * @since 24.01.2007
 */
public class ReplyToServlet extends HttpServlet {

   protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
      dump(httpServletRequest, httpServletResponse);
   }

   protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
      dump(httpServletRequest, httpServletResponse);
   }

   private void dump(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
      System.out.println("ReplyTo sink:");

      try {
         BufferedReader reader = httpServletRequest.getReader();
         String inputLine;

         while ((inputLine = reader.readLine()) != null) {
            System.out.println(inputLine);
         }
         reader.close();
      } catch (IOException e) {
         e.printStackTrace();
      }


      httpServletResponse.setStatus(200);
   }
}

