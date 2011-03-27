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
package org.jboss.test.ws.jaxws.jbws2969;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.Holder;

@WebService(name = "Endpoint", targetNamespace = "http://ws.jboss.org")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface Endpoint
{

   /**
    * 
    * @param in
    * @return
    *     returns java.lang.String
    */
   @WebMethod
   @WebResult(name = "out", partName = "out")
   public String testPassNull(@WebParam(name = "in", partName = "in") String in);

   /**
    * 
    * @param in
    * @return
    *     returns java.lang.String
    */
   @WebMethod
   @WebResult(name = "out", partName = "out")
   public String testReturnNull(@WebParam(name = "in", partName = "in") String in);

   @WebMethod
   public void testInHolderNull(@WebParam(name = "arg0", partName = "arg0") String arg0,
         @WebParam(name = "arg1", mode = WebParam.Mode.INOUT, partName = "arg1") Holder<String> arg1,
         @WebParam(name = "arg2", mode = WebParam.Mode.OUT, partName = "arg2") Holder<String> arg2,
         @WebParam(name = "arg3", mode = WebParam.Mode.OUT, partName = "arg3") Holder<String> arg3);

   /**
    * 
    * @param arg3
    * @param arg2
    * @param arg1
    * @param arg0
    */
   @WebMethod
   public void testInOutHolderNull(@WebParam(name = "arg0", partName = "arg0") String arg0,
         @WebParam(name = "arg1", mode = WebParam.Mode.INOUT, partName = "arg1") Holder<String> arg1,
         @WebParam(name = "arg2", mode = WebParam.Mode.OUT, partName = "arg2") Holder<String> arg2,
         @WebParam(name = "arg3", mode = WebParam.Mode.OUT, partName = "arg3") Holder<String> arg3);

   /**
    * 
    * @param arg3
    * @param arg2
    * @param arg1
    * @param arg0
    */
   @WebMethod
   public void testOutHolderNull(@WebParam(name = "arg0", partName = "arg0") String arg0,
         @WebParam(name = "arg1", mode = WebParam.Mode.INOUT, partName = "arg1") Holder<String> arg1,
         @WebParam(name = "arg2", mode = WebParam.Mode.OUT, partName = "arg2") Holder<String> arg2,
         @WebParam(name = "arg3", mode = WebParam.Mode.OUT, partName = "arg3") Holder<String> arg3);
}
