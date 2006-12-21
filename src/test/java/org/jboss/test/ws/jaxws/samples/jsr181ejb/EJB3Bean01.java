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
package org.jboss.test.ws.jaxws.samples.jsr181ejb;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.ws.annotation.PortComponent;

/**
 * Test the JSR-181 annotation: javax.jws.WebService
 *
 * @author Thomas.Diesler@jboss.org
 * @since 29-Apr-2005
 */

// standard JSR181 annotations
@WebService(name = "EndpointInterface", targetNamespace = "http://org.jboss.ws/samples/jsr181ejb", serviceName = "TestService")
@SOAPBinding(style = SOAPBinding.Style.RPC)

// standard EJB3 annotations
@Remote(EJB3RemoteInterface.class)
@RolesAllowed("friend")
@Stateless

// jboss propriatary annotations
@RemoteBinding(jndiBinding = "/ejb3/EJB3Bean01")
@PortComponent(authMethod="BASIC", transportGuarantee="NONE", configName="Standard WSSecurity Endpoint")
@SecurityDomain("JBossWS")
public class EJB3Bean01 implements EJB3RemoteInterface
{
   @WebMethod
   @WebResult(name = "result")
   public String echo(@WebParam(name = "String_1") String input)
   {
      return input;
   }
}
