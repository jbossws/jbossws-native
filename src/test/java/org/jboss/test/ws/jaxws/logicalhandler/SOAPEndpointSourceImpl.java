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
package org.jboss.test.ws.jaxws.logicalhandler;

// $Id$

import javax.jws.HandlerChain;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import org.jboss.logging.Logger;

@WebService(name = "SOAPEndpoint", targetNamespace = "http://org.jboss.ws/jaxws/logicalhandler")
@HandlerChain(file = "WEB-INF/jaxws-server-source-handlers.xml")
public class SOAPEndpointSourceImpl
{
   private static Logger log = Logger.getLogger(SOAPEndpointSourceImpl.class);

   @WebMethod
   @WebResult(targetNamespace = "http://org.jboss.ws/jaxws/logicalhandler", name="result")
   @RequestWrapper(className = "org.jboss.test.ws.jaxws.logicalhandler.Echo")
   @ResponseWrapper(className = "org.jboss.test.ws.jaxws.logicalhandler.EchoResponse")
   public String echo(@WebParam(targetNamespace = "http://org.jboss.ws/jaxws/logicalhandler", name="String_1")String msg)
   {
      log.info("echo: " + msg);
      return msg + ":endpoint";
   }
}
