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

import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.rpc.encoding.Serializer;

import org.jboss.util.NotImplementedException;
import org.jboss.ws.Constants;
import org.jboss.xb.binding.NamespaceRegistry;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * The base class for all Serializers.
 *
 * @author Thomas.Diesler@jboss.org
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @since 04-Dec-2004
 */
public abstract class SerializerSupport implements Serializer
{
   /** Serialize an object value to an XML fragment
    *
    * @param xmlName The root element name of the resulting fragment
    * @param xmlType The associated schema type
    * @param value The value to serialize
    * @param serContext The serialization context
    * @param attributes TODO
    * @param attributes The attributes on this element
    */
   public abstract String serialize(QName xmlName, QName xmlType, Object value, SerializationContext serContext, NamedNodeMap attributes) throws BindingException;

   /** Wrap the value string in a XML fragment with the given name
    */
   protected String wrapValueStr(QName xmlName, String valueStr, NamespaceRegistry nsRegistry, Set<String> additionalNamespaces, NamedNodeMap attributes, boolean normalize)
   {
      String nsURI = xmlName.getNamespaceURI();
      String localPart = xmlName.getLocalPart();

      StringBuilder nsAttr = new StringBuilder("");
      if (attributes != null)
      {
         for (int i = 0; i < attributes.getLength(); i++)
         {
            Node attr = attributes.item(i);
            String attrName = attr.getNodeName();
            String attrValue = attr.getNodeValue();
            nsAttr.append(" " + attrName + "='" + attrValue + "'");
         }
      }

      String elName;
      if (nsURI.length() > 0)
      {
         xmlName = nsRegistry.registerQName(xmlName);
         String prefix = xmlName.getPrefix();
         elName = prefix + ":" + localPart;

         nsAttr.append(" xmlns:" + prefix + "='" + nsURI + "'");
      }
      else
      {
         elName = localPart;
      }

      if (additionalNamespaces != null)
      {
         for (String ns : additionalNamespaces)
         {
            if (ns.equals(nsURI))
               continue;

            String prefix = nsRegistry.getPrefix(ns);
            nsAttr.append(" xmlns:" + prefix + "='" + ns + "'");
         }
      }

      String xmlFragment;
      if (valueStr == null)
      {
         String xmlns = " xmlns:" + Constants.PREFIX_XSI + "='" + Constants.NS_SCHEMA_XSI + "'";
         xmlFragment = "<" + elName + nsAttr + " " + Constants.PREFIX_XSI + ":nil='1'" + xmlns + "/>";
      }
      else
      {
         if(normalize)
            valueStr = normalize(valueStr);
         xmlFragment = "<" + elName + nsAttr + ">" + valueStr + "</" + elName + ">";
      }

      return xmlFragment;
   }

   public String getMechanismType()
   {
      throw new NotImplementedException();
   }

   private String normalize(String valueStr)
   {
      // We assume most strings will not contain characters that need "escaping",
      // and optimize for this case.
      boolean found = false;
      int i = 0;

      outer: for (; i < valueStr.length(); i++)
      {
         switch (valueStr.charAt(i))
         {
            case '<':
            case '>':
            case '&':
            case '"':
               found = true;
               break outer;
         }
      }

      if (!found)
         return valueStr;

      // Resume where we left off
      StringBuilder builder = new StringBuilder();
      builder.append(valueStr.substring(0, i));
      for (; i < valueStr.length(); i++)
      {
         char c = valueStr.charAt(i);
         switch (c)
         {
            case '<':
               builder.append("&lt;");
               break;
            case '>':
               builder.append("&gt;");
               break;
            case '&':
               builder.append("&amp;");
               break;
            case '"':
               builder.append("&quot;");
               break;
            default:
               builder.append(c);
         }
      }

      return builder.toString();
   }
}