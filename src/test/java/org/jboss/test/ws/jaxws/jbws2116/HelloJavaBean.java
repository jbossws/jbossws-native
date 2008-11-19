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
package org.jboss.test.ws.jaxws.jbws2116;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.WebServiceContext;

import org.jboss.logging.Logger;
import org.jboss.ws.annotation.EndpointConfig;
import org.jboss.wsf.spi.annotation.WebContext;


@Stateless
@WebService(name = "Hello", serviceName = "HelloService", targetNamespace = "http://org.jboss.ws/jbws2116")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@WebContext(contextRoot = "/jaxws-jbws2116", urlPattern = "/*")
@EndpointConfig(configName = "Standard WSSecurity Endpoint")
//We're declaring the domain in the jboss.xml since we have different annotation packages for AS5 and AS42
//@SecurityDomain("JBossWS")
public class HelloJavaBean
{
   private Logger log = Logger.getLogger(HelloJavaBean.class);
   @Resource
   private WebServiceContext ctx;

   @WebMethod
   @RolesAllowed({"friend"})
   public String echo(String par)
   {
      log.info("User principal: " + ctx.getUserPrincipal());
      return par;
   }
   
   @WebMethod
   @RolesAllowed({"girlfriend"})
   public String echo2(String par)
   {
      log.info("User principal: " + ctx.getUserPrincipal());
      return par;
   }
}
