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
package org.jboss.ws.extensions.eventing.jaxrpc.element;

// $Id$

import java.net.URI;

import javax.xml.soap.SOAPElement;

/**
 * A Boolean expression in some dialect,
 * either as a string or as an XML fragment. If the expression evaluates to false
 * for a notification, the notification MUST NOT be sent to the event sink.
 * <p/>
 * The value must be a XPath predicate expression (PredicateExpr).
 * <p/>
 * <br>
 * For further information see <code>http://www.w3.org/TR/xpath#predicates</code>.
 *
 * @see org.jboss.ws.extensions.eventing.mgmt.Filter
 * 
 * @author Heiko Braun, <heiko@openj.net>
 * @since 24-Nov-2005
 */
public class FilterType
{
   private URI dialect;
   private SOAPElement[] _any;
   public String _value;

   public URI getDialect()
   {
      return dialect;
   }

   public void setDialect(URI dialect)
   {
      this.dialect = dialect;
   }

   public SOAPElement[] get_any()
   {
      return _any;
   }

   public void set_any(SOAPElement[] _any)
   {
      this._any = _any;
   }

   public String get_value()
   {
      return _value;
   }

   public void set_value(String _value)
   {
      this._value = _value;
   }
}
