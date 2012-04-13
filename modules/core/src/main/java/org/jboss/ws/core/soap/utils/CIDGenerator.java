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
package org.jboss.ws.core.soap.utils;

import org.jboss.ws.core.soap.attachment.MimeConstants;

/**
 * A common CID generator
 *
 * @author Thomas.Diesler@jboss.org
 * @since 17-Jan-2006
 */
public final class CIDGenerator
{
   private static int count = 0;

   private CIDGenerator() {
	   // forbidden instantiation
   }

   public static synchronized String generateFromCount()
   {
	  if (count == 1000) count = 0;
      StringBuilder cid = new StringBuilder();
      long time = System.currentTimeMillis();

      cid.append(count++).append("-").append(time).append("-")
         .append(cid.hashCode()).append("@").append(MimeConstants.CID_DOMAIN);

      return cid.toString();
   }
}
