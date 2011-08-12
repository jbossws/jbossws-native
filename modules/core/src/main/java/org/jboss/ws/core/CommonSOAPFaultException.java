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
package org.jboss.ws.core;

import javax.xml.namespace.QName;

/** 
 * The SOAPFaultException exception represents a SOAP fault.
 * 
 * @author Thomas.Diesler@jboss.org
 */
public class CommonSOAPFaultException extends RuntimeException
{
   private QName faultCode;
   private String faultString;
   private Throwable throwable;

   public CommonSOAPFaultException(QName faultCode, String faultString)
   {
      super(faultString);

      this.faultCode = faultCode;
      this.faultString = faultString;
   }

   public CommonSOAPFaultException(QName faultCode, Throwable throwable) {
      
      super(throwable.getMessage(), throwable);
      
      this.faultCode = faultCode;
      this.throwable = throwable;
      
   }

   public QName getFaultCode()
   {
      return faultCode;
   }

   public String getFaultString()
   {
      return faultString;
   }
}
