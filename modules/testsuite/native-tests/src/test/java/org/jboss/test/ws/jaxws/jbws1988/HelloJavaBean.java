/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.test.ws.jaxws.jbws1988;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.WebServiceContext;

import org.jboss.logging.Logger;
import org.jboss.wsf.spi.annotation.EndpointConfig;
import org.jboss.wsf.spi.annotation.WebContext;


@Stateless
@WebService(name = "Hello", serviceName = "HelloService", targetNamespace = "http://org.jboss.ws/jbws1988")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@WebContext(contextRoot = "/jaxws-jbws1988", urlPattern = "/*")
@EndpointConfig(configName = "Standard WSSecurity Endpoint")
@RolesAllowed({"friend"}) 
public class HelloJavaBean
{
   private Logger log = Logger.getLogger(HelloJavaBean.class);
   @Resource
   private WebServiceContext ctx;

   @WebMethod
   public String echo(String par)
   {
      log.info("User principal: " + ctx.getUserPrincipal());
      return par;
   }
}
