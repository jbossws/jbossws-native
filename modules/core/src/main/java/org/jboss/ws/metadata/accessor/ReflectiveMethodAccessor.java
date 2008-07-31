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
package org.jboss.ws.metadata.accessor;

import java.lang.reflect.Method;

import org.jboss.ws.WSException;

/**
 * A simple JavaBean accessor that uses ordinary reflection.
 *
 * @author <a href="jason.greene@jboss.com">Jason T. Greene</a>
 */
@SuppressWarnings("unchecked")
public class ReflectiveMethodAccessor implements Accessor
{
   private Method getter;
   private Method setter;

   ReflectiveMethodAccessor(Method getter, Method setter)
   {
      this.getter = getter;
      this.setter = setter;
   }

   public Object get(Object bean)
   {
      try
      {
         return getter.invoke(bean);
      }
      catch (Throwable e)
      {
         WSException ex = new WSException(e.getMessage());
         ex.setStackTrace(ex.getStackTrace());
         throw ex;
      }
   }

   public void set(Object bean, Object value)
   {
      try
      {
         setter.invoke(bean, value);
      }
      catch (Throwable e)
      {
         WSException ex = new WSException(e.getMessage());
         ex.setStackTrace(ex.getStackTrace());
         throw ex;
      }
   }
}