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

import java.util.ResourceBundle;

import org.jboss.ws.api.util.BundleUtils;

/** A type-safe enumeration for encoding style.
 *  
 * @author Thomas.Diesler@jboss.org
 * @since 14-Oct-2004
 */
public class Style
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(Style.class);
   private String style;

   public static final Style RPC = new Style("rpc");
   public static final Style DOCUMENT = new Style("document");

   private Style(String style)
   {
      this.style = style;
   }

   public static Style getDefaultStyle()
   {
      return DOCUMENT;
   }

   public static Style valueOf(String style)
   {
      if (RPC.style.equals(style))
         return RPC;
      if (DOCUMENT.style.equals(style))
         return DOCUMENT;
      
      throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "UNSUPPORTED_STYLE",  style));
   }

   public String toString()
   {
      return style;
   }
}
