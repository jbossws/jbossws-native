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
package org.jboss.test.ws.jaxws.handlerscope;

// $Id: $

import javax.jws.HandlerChain;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.xml.ws.BindingType;

import org.jboss.logging.Logger;

@WebService(name = "SOAPEndpoint", targetNamespace = "http://org.jboss.ws/jaxws/handlerscope")
@BindingType(value = "http://www.w3.org/2003/05/soap/bindings/HTTP/") // SOAP-1.2
@HandlerChain(file = "jaxws-server-handlers.xml")
@SOAPBinding(style = Style.RPC)
public class SOAPEndpointBean
{
   private static Logger log = Logger.getLogger(SOAPEndpointBean.class);

   @WebMethod
   public String echo(String msg)
   {
      log.info("echo: " + msg);
      return msg + ":endpoint";
   }
}
