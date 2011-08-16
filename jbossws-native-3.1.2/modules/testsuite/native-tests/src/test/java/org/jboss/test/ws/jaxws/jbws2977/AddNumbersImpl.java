/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws2977;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.Action;
import javax.xml.ws.BindingType;
import javax.xml.ws.FaultAction;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.soap.Addressing;
import javax.xml.ws.soap.SOAPBinding;

/**
 * 
 * @author <a href="ema@redhat.com">Jim Ma</a>
 */
@WebService(name = "AddNumbers", portName = "AddNumbersPort", targetNamespace = "http://ws.jboss.org", serviceName = "AddNumbers")
@BindingType(value = SOAPBinding.SOAP11HTTP_BINDING)
@Addressing(enabled = true, required = false)
public class AddNumbersImpl
{
   @Resource
   WebServiceContext wsc;

   @Action(input = "inputAction", output = "outputAction", fault =
   {@FaultAction(className = AddNumbersException.class, value = "http://faultAction")})
   public int addNumbersFault1(int number1, int number2) throws AddNumbersException
   {
      throw new AddNumbersException("AddNumbersTestException", "testFault");
   }
}
