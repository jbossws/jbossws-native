/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.ws.core.binding;

import java.util.ResourceBundle;

import javax.xml.namespace.QName;
import javax.xml.rpc.encoding.Deserializer;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.jboss.logging.Logger;
import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.common.DOMUtils;
import org.jboss.ws.common.DOMWriter;
import org.jboss.ws.core.soap.SOAPContentElement;
import org.jboss.ws.util.xml.BufferedStreamSource;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** The base class for all Deserializers.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 04-Dec-2004
 */
public abstract class DeserializerSupport implements Deserializer
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(DeserializerSupport.class);
   private static final Logger log = Logger.getLogger(DeserializerSupport.class);
   private static final QName XSI_NIL = new QName("http://www.w3.org/2001/XMLSchema-instance", "nil");

   public Object deserialize(SOAPContentElement soapElement, SerializationContext serContext) throws BindingException
   {
      QName xmlName = soapElement.getElementQName();
      QName xmlType = soapElement.getXmlType();

      Source source = soapElement.getXMLFragment().getSource();
      return deserialize(xmlName, xmlType, source, serContext);
   }

   /** Deserialize an XML fragment to an object value
    *
    * @param xmlName The root element name of the resulting fragment
    * @param xmlType The associated schema type
    * @param xmlFragment The XML fragment to deserialize
    * @param serContext The serialization context
    */
   public abstract Object deserialize(QName xmlName, QName xmlType, Source xmlFragment, SerializationContext serContext) throws BindingException;

   protected String sourceToString(final Source source)
   {
      String xmlFragment = null;
      if (source instanceof DOMSource)
      {
         Node node = ((DOMSource)source).getNode();
         xmlFragment = DOMWriter.printNode(node, false);
      }
      else if (source instanceof BufferedStreamSource)
      {
         xmlFragment = ((BufferedStreamSource)source).toString();
      }
      else
      {
         throw new UnsupportedOperationException();
      }

      return xmlFragment;
   }
   
   protected Element sourceToElement(final Source source)
   {
      if (source instanceof DOMSource)
      {
         Node node = ((DOMSource)source).getNode();
         int nodeType = node.getNodeType();
         if (nodeType == Node.ELEMENT_NODE)
         {
            return (Element)node;
         } else throw new UnsupportedOperationException(BundleUtils.getMessage(bundle, "ONLY_ELEMENT_NODES_ARE_SUPPORTED"));
      }
      else throw new UnsupportedOperationException(BundleUtils.getMessage(bundle, "ONLY_DOMSOURCE_IS_SUPPORTED"));
   }
   

  /** Unwrap the value string from the XML fragment
   *
   * @return The value string or null if the startTag contains a xsi:nil='true' attribute
   */
  protected String unwrapValueStr(Element xmlFragment)
  {
     String content = DOMUtils.getTextContent(xmlFragment);
     if (content == null)
     {
        // We only scan for :nil if the xmlFragment is an empty element
        return (isNil(xmlFragment) ? null : "");
     }

     return content;
  }
 
   private boolean isNil(final Element element)
   {
      final String nilValue = DOMUtils.getAttributeValue(element, XSI_NIL);
      return "1".equals(nilValue) || "true".equals(nilValue);
   }

   public String getMechanismType()
   {
      throw new UnsupportedOperationException();
   }
}
