/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws1172.types;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

@WebService(targetNamespace = "http://www.my-company.it/ws/my-test", name = "MyTest")
@XmlSeeAlso({ObjectFactory.class})
public interface MyTest {

    @RequestWrapper(localName = "performTest", targetNamespace = "http://www.my-company.it/ws/my-test", className = "org.jboss.test.ws.jaxws.jbws1172.types.PerformTest")
    @WebMethod(action = "urn:performTest")
    @ResponseWrapper(localName = "performTestResponse", targetNamespace = "http://www.my-company.it/ws/my-test", className = "org.jboss.test.ws.jaxws.jbws1172.types.PerformTestResponse")
    public void performTest(
        @WebParam(name = "Code", targetNamespace = "")
        java.lang.Integer code
    ) throws MyWSException_Exception;
}
