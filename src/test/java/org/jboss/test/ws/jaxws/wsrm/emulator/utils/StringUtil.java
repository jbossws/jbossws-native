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
package org.jboss.test.ws.jaxws.wsrm.emulator.utils;

/**
 * TODO: Add comment
 *
 * @author richard.opalka@jboss.com
 *
 * @since Nov 7, 2007
 */
public final class StringUtil
{
   private StringUtil()
   {
      
   }

   public static String replace(String oldString, String newString, String data)
   {
      int fromIndex = 0;
      int index = 0;
      StringBuffer result = new StringBuffer();
      
      while ((index = data.indexOf(oldString, fromIndex)) >= 0)
      {
         result.append(data.substring(fromIndex, index));
         result.append(newString);
         fromIndex = index + oldString.length();
      }
      result.append(data.substring(fromIndex));
      return result.toString();
   }
   
}
