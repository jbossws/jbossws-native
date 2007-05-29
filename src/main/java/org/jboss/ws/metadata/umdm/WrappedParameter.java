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
package org.jboss.ws.metadata.umdm;

import javax.xml.namespace.QName;

import org.jboss.wsf.spi.utils.JavaUtils;

/**
 * WrappedParameter represents a document/literal wrapped parameter.
 *
 * @author <a href="jason.greene@jboss.com">Jason T. Greene</a>
 * @version $Revision$
 */
public class WrappedParameter
{
   public static final int RETURN = -1;
   private QName name;
   private String type;
   private String[] typeArguments;
   private String variable;
   private boolean holder = false;
   private int index = -2;
   private Accessor accessor;
   private boolean swaRef;
   private boolean xop;

   public WrappedParameter(WrappedParameter wrapped)
   {
      this.name = wrapped.name;
      this.type = wrapped.type;
      this.typeArguments = wrapped.typeArguments;
      this.variable = wrapped.variable;
      this.holder = wrapped.holder;
      this.index = wrapped.index;
      this.accessor = wrapped.accessor;
   }
   
   public WrappedParameter(QName name, String type, String variable, int index)
   {
      this.setName(name);
      this.setType(type);
      this.setVariable(variable);
      this.setIndex(index);
   }

   public Accessor accessor()
   {
      return accessor;
   }

   public void setName(QName name)
   {
      this.name = name;
   }

   public QName getName()
   {
      return name;
   }

   public void setType(String type)
   {
      this.type = type;
   }

   public String getType()
   {
      return type;
   }

   public void setTypeArguments(String[] typeArguments)
   {
      this.typeArguments = typeArguments;
   }

   public String[] getTypeArguments()
   {
      return typeArguments;
   }

   public void setVariable(String variable)
   {
      this.variable = variable;
   }

   public String getVariable()
   {
      return variable;
   }

   public void setHolder(boolean holder)
   {
      this.holder = holder;
   }

   public boolean isHolder()
   {
      return holder;
   }

   public void setIndex(int index)
   {
      this.index = index;
   }

   public int getIndex()
   {
      return index;
   }

   void setAccessor(Accessor accessor)
   {
      this.accessor = accessor;
   }

   public boolean isSwaRef()
   {
      return swaRef;
   }

   public void setSwaRef(boolean swaRef)
   {
      this.swaRef = swaRef;
   }

   public boolean isXop()
   {
      return xop;
   }

   public void setXOP(boolean xop)
   {
      this.xop = xop;
   }

   public String toString()
   {
      return "[name = " + getName() + ", type = " + getType() + ", typeArgs = " + JavaUtils.printArray(getTypeArguments()) + ", variable = " + getVariable()
            + ", index = " + getIndex() + "]";
   }
}
