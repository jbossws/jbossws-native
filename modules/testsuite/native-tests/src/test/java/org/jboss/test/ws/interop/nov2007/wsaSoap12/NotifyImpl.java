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

import org.jboss.ws.annotation.EndpointConfig;

import javax.jws.WebMethod;
import javax.jws.Oneway;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.Action;
import javax.xml.ws.BindingType;

/**
 * @author Alessio Soldano, alessio.soldano@jboss.com
 * @since 31-Oct-2007
 */
@WebService(
   name = "Notify",
   targetNamespace = "http://tempuri.org/",
   endpointInterface = "org.jboss.test.ws.interop.nov2007.wsaSoap12.Notify",
   wsdlLocation = "/WEB-INF/wsdl/service.wsdl",
   portName = "CustomBinding_Notify1"
)
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
@EndpointConfig(configName = "Standard SOAP 1.2 WSAddressing Endpoint")
@BindingType(javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING)
public class NotifyImpl implements Notify {

   @WebMethod(operationName = "Notify", action = "http://example.org/action/notify")
   @Oneway
   @Action(input = "http://example.org/action/notify")
   public void notify(
       @WebParam(name = "notify", targetNamespace = "http://example.org/notify", partName = "notify")
       String notify)
   {
      System.out.println("NotifyImpl: " + notify);
   }
}
