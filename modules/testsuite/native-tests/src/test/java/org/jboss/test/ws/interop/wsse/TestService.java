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
package org.jboss.test.ws.interop.wsse;

import org.jboss.ws.api.annotation.EndpointConfig;

import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.WebService;

/**
 * @author Heiko Braun <heiko.braun@jboss.com>
 * @since Aug 24, 2006
 */
@WebService(
   serviceName = "PingService10",
   name = "IPingService",
   targetNamespace = "http://tempuri.org/",
   endpointInterface = "org.jboss.test.ws.interop.wsse.IPingService",
   portName = "MutualCertificate10_IPingService")
@EndpointConfig(configName = "Standard WSSecurity Endpoint")
public class TestService implements IPingService {

   @WebMethod(operationName = "Ping", action = "http://xmlsoap.org/Ping")
   public PingResponse ping(Ping request) {
      System.out.println("Ping: "+ request.getText());
      return new PingResponse();
   }

   @WebMethod(action = "http://InteropBaseAddress/interop/echo")
   @WebResult(name = "echoResult", targetNamespace = "http://InteropBaseAddress/interop")
   public EchoResponse echo(Echo request) {
      System.out.println("Echo: " +request.getRequest());
      EchoResponse response = new EchoResponse();
      response.setEchoResult(request.getRequest());
      return response;
   }
}
