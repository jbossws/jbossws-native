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
package org.jboss.test.ws.jaxws.jsr181.complex;

import java.util.Date;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.jboss.test.ws.jaxws.jsr181.complex.extra.Statistics;

@WebService
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL, parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
public interface Registration
{
   @WebMethod(operationName = "Register")
   @WebResult(name = "RegisteredID")
   public abstract long register(
         @WebParam(name = "Customer") Customer customer,
         @WebParam(name = "When") Date when)
      throws ValidationException, AlreadyRegisteredException;

   @WebMethod(operationName = "BulkRegister")
   @WebResult(name = "RegisteredIDs")
   public abstract long[] bulkRegister(
         @WebParam(name = "Customers") Customer[] customers,
         @WebParam(name = "When")Date when)
      throws ValidationException, AlreadyRegisteredException;

   @WebMethod(operationName = "RegisterForInvoice")
   @WebResult(name = "done")
   public abstract boolean registerForInvoice(
         @WebParam(name = "InvoiceCustomer") InvoiceCustomer customer)
      throws ValidationException, AlreadyRegisteredException;

   @WebMethod(operationName = "GetStatistics")
   @WebResult(name = "Statistics")
   public abstract Statistics getStatistics(
         @WebParam(name = "Customer") Customer customer);
}