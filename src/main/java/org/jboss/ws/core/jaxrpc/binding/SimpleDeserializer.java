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

// $Id$

import javax.xml.namespace.QName;

import org.jboss.logging.Logger;
import org.jboss.xb.binding.SimpleTypeBindings;

/**
 * A deserializer that can handle XMLSchema simple types.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 22-Oct-2004
 */
public class SimpleDeserializer extends DeserializerSupport
{
   // provide logging
   private static final Logger log = Logger.getLogger(SimpleDeserializer.class);

   public Object deserialize(QName xmlName, QName xmlType, String xmlFragment, SerializationContext serContext) throws BindingException
   {
      if(log.isDebugEnabled()) log.debug("deserialize: [xmlName=" + xmlName + ",xmlType=" + xmlType + "]");

      Object value = null;
      String valueStr = unwrapValueStr(xmlFragment);

      if (valueStr != null)
      {
         value = SimpleTypeBindings.unmarshal(xmlType.getLocalPart(), valueStr, serContext.getNamespaceRegistry());
      }

      return value;
   }
}
