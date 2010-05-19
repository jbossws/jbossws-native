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
package org.jboss.test.ws.jaxws.jbws2526;

import javax.jws.HandlerChain;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * Test endpoint.
 *
 */
@WebService(name = "Port", serviceName = "Service", portName = "Port", targetNamespace = "urn:JBWS-2526-Service", wsdlLocation = "WEB-INF/wsdl/jbws2526.wsdl")
@HandlerChain(file = "server-handlers.xml")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public class Endpoint
{

   @WebMethod
   @WebResult(name = "Response", targetNamespace = "urn:JBWS-2526-Schema", partName = "response")
   public Response evaluate(@WebParam(name = "Request", targetNamespace = "urn:JBWS-2526-Schema", partName = "request") Request request)
   {
      Response response = new Response();
      response.setValue(request.getValue1() * request.getValue2());
      return response;
   }

}