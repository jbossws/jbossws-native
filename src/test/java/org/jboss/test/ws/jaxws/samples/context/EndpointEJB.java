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
package org.jboss.test.ws.jaxws.samples.context;

// $Id$

import java.security.Principal;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.annotation.security.SecurityDomain;
import org.jboss.ws.annotation.WebContext;

@WebService(name = "TestEndpoint", targetNamespace = "http://org.jboss.ws/jaxws/context")
@SOAPBinding(style = Style.RPC)
@Stateless

@WebContext(contextRoot = "/jaxws-samples-context", urlPattern = "/*", authMethod = "BASIC", transportGuarantee = "NONE", secureWSDLAccess = false)
@SecurityDomain("JBossWS")
@RolesAllowed("friend")
public class EndpointEJB
{
   @Resource
   WebServiceContext wsCtx;

   @WebMethod
   public String testGetMessageContext()
   {
      SOAPMessageContext jaxwsContext = (SOAPMessageContext)wsCtx.getMessageContext();
      return jaxwsContext != null ? "pass" : "fail";
   }

   @WebMethod
   public String testGetUserPrincipal()
   {
      Principal principal = wsCtx.getUserPrincipal();
      return principal.getName();
   }

   @WebMethod
   public boolean testIsUserInRole(String role)
   {
      return wsCtx.isUserInRole(role);
   }
}
