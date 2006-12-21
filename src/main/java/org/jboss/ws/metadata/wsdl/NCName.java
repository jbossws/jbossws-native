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
package org.jboss.ws.metadata.wsdl;

import java.io.Serializable;

import javax.xml.namespace.QName;

// $Id$


/**
 * The value space of the wsdls:NCName type is the subset of the value space of the wsdls:Token type
 * consisting of tokens that do not contain the space (#x20) and ':' characters.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 10-Oct-2004
 */
public class NCName implements Serializable
{
   private static final long serialVersionUID = -4997456323552864649L;
   
   private String name;

   public NCName(QName qname)
   {
      this(qname != null ? qname.getLocalPart() : null);
   }

   public NCName(String name)
   {
      if (name == null || name.indexOf(':') >= 0 || name.indexOf(' ') >= 0)
         throw new IllegalArgumentException("Illegal NCName: " + name);

      this.name = name;
   }

   public boolean equals(Object obj)
   {
      if (obj instanceof NCName)
         return name.equals(((NCName)obj).name);
      if (obj instanceof String)
         return name.equals(((String)obj));

      return false;
   }

   public int hashCode()
   {
      return name.hashCode();
   }

   public String toString()
   {
      return name;
   }
}
