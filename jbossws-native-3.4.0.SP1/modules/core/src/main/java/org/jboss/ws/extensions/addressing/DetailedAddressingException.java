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
package org.jboss.ws.extensions.addressing;

import javax.xml.namespace.QName;
import javax.xml.ws.addressing.AddressingException;

/**
 * Addressing exception that allows better addressing details setup.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class DetailedAddressingException extends AddressingException
{

   private static final long serialVersionUID = 1L;

   public DetailedAddressingException(final QName code, final String reason, final Object detail)
   {
      super();
      
      this.setInternals(code, reason, detail);
   }

   public DetailedAddressingException(final String message, final Throwable cause, final QName code, final String reason, final Object detail)
   {
      super(message, cause);
      
      this.setInternals(code, reason, detail);
   }

   public DetailedAddressingException(final String message, final QName code, final String reason, final Object detail)
   {
      super(message);
      
      this.setInternals(code, reason, detail);
   }

   public DetailedAddressingException(final Throwable cause, final QName code, final String reason, final Object detail)
   {
      super(cause);
      
      this.setInternals(code, reason, detail);
   }
   
   private void setInternals(final QName code, final String reason, final Object detail)
   {
      if (code == null)
         throw new IllegalArgumentException("code must be specified");
      if (reason == null)
         throw new IllegalArgumentException("reason must be specified");
      if (detail == null)
         throw new IllegalArgumentException("detail must be specified");
      
      this.code = code;
      this.reason = reason;
      this.detail = detail;
   }
   
}
