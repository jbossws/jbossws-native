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
package org.jboss.ws.extensions.wsrm.common;

import java.util.Date;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.jboss.ws.extensions.wsrm.api.RMException;

/**
 * RM utility library
 *
 * @author richard.opalka@jboss.com
 *
 * @since Nov 29, 2007
 */
public final class RMHelper
{
   
   private RMHelper()
   {
      // no instances allowed
   }
   
   private static final DatatypeFactory factory;
   
   static
   {
      try
      {
         factory = DatatypeFactory.newInstance();
      }
      catch (DatatypeConfigurationException dce)
      {
         throw new RMException(dce.getMessage(), dce);
      }
   }
   
   public static Duration stringToDuration(String s)
   {
      return factory.newDuration(s);
   }
   
   public static String durationToString(Duration d)
   {
      return d.toString();
   }
   
   public static long durationToLong(Duration d)
   {
      if (d == null)
         return -1L;
      
      return d.getTimeInMillis(new Date());
   }
   
}
