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
package org.jboss.ws.extensions.eventing.jaxws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.xml.ws.addressing.Action;


/**
 * This class was generated by the JAXWS SI.
 * JAX-WS RI 2.0-b26-ea3
 * Generated source version: 2.0
 * 
 */
@WebService(name = "EventSource", targetNamespace = "http://schemas.xmlsoap.org/ws/2004/08/eventing")
@SOAPBinding(parameterStyle = ParameterStyle.WRAPPED)
public interface EventSourceEndpoint {


    /**
     * 
     * @param body
     * @return
     *     returns org.jboss.ws.extensions.eventing.jaxws.SubscribeResponse
     */
    @WebMethod(operationName = "SubscribeOp")
    @WebResult(name = "SubscribeResponse", targetNamespace = "http://schemas.xmlsoap.org/ws/2004/08/eventing", partName = "body")
    @Action(
       input = "http://schemas.xmlsoap.org/ws/2004/08/eventing/Subscribe",
       output = "http://schemas.xmlsoap.org/ws/2004/08/eventing/SubscribeResponse"
    )
    public SubscribeResponse subscribeOp(
        @WebParam(name = "Subscribe", targetNamespace = "http://schemas.xmlsoap.org/ws/2004/08/eventing", partName = "body")
        Subscribe body);

}
