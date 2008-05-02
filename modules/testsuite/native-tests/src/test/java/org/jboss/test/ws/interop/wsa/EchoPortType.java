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
package org.jboss.test.ws.interop.wsa;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;


/**
 * This class was generated by the JAXWS SI.
 * JAX-WS RI 2.0-b26-ea3
 * Generated source version: 2.0
 * 
 */
@WebService(name = "EchoPortType", targetNamespace = "http://example.org/echo", wsdlLocation = "/WEB-INF/wsdl/service.wsdl")
@SOAPBinding(parameterStyle = ParameterStyle.BARE)
public interface EchoPortType {


    /**
     * 
     * @param parameters
     * @return
     *     returns org.jboss.test.ws.interop.wsa.EchoOutMessage
     */
    @WebMethod(operationName = "EchoOp", action = "http://example.org/action/echoIn")
    @WebResult(name = "EchoOutMessage", targetNamespace = "http://example.org/echo", partName = "parameters")
    public EchoOutMessage echoOp(
        @WebParam(name = "EchoInMessage", targetNamespace = "http://example.org/echo", partName = "parameters")
        EchoInMessage parameters);

}