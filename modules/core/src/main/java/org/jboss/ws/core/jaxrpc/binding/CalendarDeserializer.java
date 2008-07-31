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
package org.jboss.ws.core.jaxrpc.binding;

import java.util.Calendar;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.jboss.logging.Logger;
import org.jboss.ws.Constants;
import org.jboss.ws.core.binding.BindingException;
import org.jboss.ws.core.binding.DeserializerSupport;
import org.jboss.ws.core.binding.SerializationContext;
import org.jboss.xb.binding.SimpleTypeBindings;

/**
 * @author Thomas.Diesler@jboss.org
 * @since 04-Dec-2004
 * @see <a href="http://www.w3.org/TR/xmlschema-2/#dateTime">XML Schema 3.2.16</a>
 */
public class CalendarDeserializer extends DeserializerSupport
{
   // provide logging
   private static final Logger log = Logger.getLogger(CalendarDeserializer.class);

   public Object deserialize(QName xmlName, QName xmlType, Source xmlFragment, SerializationContext serContext) throws BindingException
   {
      return deserialize(xmlName, xmlType, sourceToString(xmlFragment), serContext);
   }

   private Object deserialize(QName xmlName, QName xmlType, String xmlFragment, SerializationContext serContext) throws BindingException
   {
      if (log.isDebugEnabled())
         log.debug("deserialize: [xmlName=" + xmlName + ",xmlType=" + xmlType + "]");

      Calendar value = null;

      String valueStr = unwrapValueStr(xmlFragment);
      if (valueStr != null)
      {
         if (Constants.TYPE_LITERAL_DATE.equals(xmlType))
            value = SimpleTypeBindings.unmarshalDate(valueStr);
         else if (Constants.TYPE_LITERAL_TIME.equals(xmlType))
            value = SimpleTypeBindings.unmarshalTime(valueStr);
         else if (Constants.TYPE_LITERAL_DATETIME.equals(xmlType))
            value = SimpleTypeBindings.unmarshalDateTime(valueStr);
         else throw new IllegalArgumentException("Invalid xmlType: " + xmlType);
      }

      return value;
   }
}
