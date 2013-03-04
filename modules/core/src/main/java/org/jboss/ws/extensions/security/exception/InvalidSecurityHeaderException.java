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
package org.jboss.ws.extensions.security.exception;

import javax.xml.namespace.QName;

import org.jboss.ws.extensions.security.Constants;

/**
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 */
public class InvalidSecurityHeaderException extends WSSecurityException
{
   public static final QName faultCode = new QName(Constants.WSSE_NS, "InvalidSecurity", Constants.WSSE_PREFIX);

   public static final String faultString = "An error was dicovered processing the <wsse:Security> header.";

   public InvalidSecurityHeaderException()
   {
      super(faultString);
      setFaultCode(faultCode);
      setFaultString(faultString);
      internal = false;
   }

   public InvalidSecurityHeaderException(Throwable cause)
   {
      super(faultString);
      setFaultCode(faultCode);
      setFaultString(faultString);
      internal = false;
   }

   public InvalidSecurityHeaderException(String message)
   {
      super(message);
      setFaultCode(faultCode);
      setFaultString(message);
      internal = false;
   }

   public InvalidSecurityHeaderException(String message, Throwable cause)
   {
      super(message, cause);
      setFaultCode(faultCode);
      setFaultString(message);
      internal = false;
   }
}
