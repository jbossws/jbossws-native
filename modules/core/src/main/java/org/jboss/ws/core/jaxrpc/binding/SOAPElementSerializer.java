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

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.transform.Result;

import org.jboss.logging.Logger;
import org.jboss.ws.core.binding.BindingException;
import org.jboss.ws.core.binding.BufferedStreamResult;
import org.jboss.ws.core.binding.SerializationContext;
import org.jboss.ws.core.binding.SerializerSupport;
import org.jboss.wsf.common.DOMWriter;
import org.w3c.dom.NamedNodeMap;

/**
 * A serializer that can handle xsd:anyType
 *
 * @author Thomas.Diesler@jboss.org
 * @since 18-Oct-2004
 */
public class SOAPElementSerializer extends SerializerSupport
{
   // provide logging
   private static final Logger log = Logger.getLogger(SOAPElementSerializer.class);

   /** Marshal the value for a given XMLSchema type
    * @param xmlType local part of the schema type
    * @param value the value to marshal
    * @param serContext
    * @return the string representation od the value
    */
   public Result serialize(QName xmlName, QName xmlType, Object value, SerializationContext serContext, NamedNodeMap attributes) throws BindingException
   {
      log.debug("serialize: [xmlName=" + xmlName + ",xmlType=" + xmlType + "]");
      if (value == null)
         throw new IllegalArgumentException("SOAPElement value cannot be null");
      if ((value instanceof SOAPElement) == false)
         throw new IllegalArgumentException("Value is not a SOAPElement: " + value.getClass().getName());

      String xmlFragment = DOMWriter.printNode((SOAPElement)value, false);
      return new BufferedStreamResult(xmlFragment);
   }
}
