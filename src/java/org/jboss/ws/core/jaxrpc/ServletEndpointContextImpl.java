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
package org.jboss.ws.core.jaxrpc;

// $Id$

import java.security.Principal;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.server.ServletEndpointContext;

import org.jboss.ws.core.server.EndpointContext;
import org.jboss.ws.core.soap.MessageContextAssociation;

/**
 * Implementation of <code>ServletEndpointContext</code>
 *
 * @author <a href="mailto:jason@stacksmash.com">Jason T. Greene</a>
 */
public class ServletEndpointContextImpl implements ServletEndpointContext
{
   private ServletContext context;
   private HttpServletRequest request;
   private HttpServletResponse response;

   public ServletEndpointContextImpl(EndpointContext context)
   {
      this.context = context.getServletContext();
      this.request = context.getHttpServletRequest();
      this.response = context.getHttpServletResponse();
   }

   public HttpSession getHttpSession()
   {
      return request.getSession();
   }

   public MessageContext getMessageContext()
   {
      return (MessageContext)MessageContextAssociation.peekMessageContext();
   }

   public ServletContext getServletContext()
   {
      return context;
   }

   public Principal getUserPrincipal()
   {
      return request.getUserPrincipal();
   }

   public boolean isUserInRole(String role)
   {
      return request.isUserInRole(role);
   }

   // BEGIN non-standard access methods

   public HttpServletRequest getHttpServletRequest()
   {
      return request;
   }

   public HttpServletResponse getHttpServletResponse()
   {
      return response;
   }

   // END non-standard access methods
}