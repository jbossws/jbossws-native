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
package org.jboss.ws.core.soap;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.ResourceBundle;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

import org.jboss.logging.Logger;
import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.common.Constants;
import org.jboss.ws.common.DOMWriter;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.soap.SOAPContent.State;
import org.jboss.ws.core.soap.utils.MessageContextAssociation;
import org.jboss.ws.core.soap.utils.XMLFragment;
import org.jboss.ws.metadata.umdm.ParameterMetaData;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;

/**
 * A SOAPElement that gives access to its content as XML fragment or Java object.<p>
 *
 * The SOAPContentElement has three content representations, which may not exist in parallel.
 * The getter and setter of the content properties perform the conversions.
 * <pre>
 * +---------+         +-------------+          +-------------+
 * | Object  | <-----> | XMLFragment |  <-----> | DOMTree     |
 * +---------+         +-------------+          +-------------+
 * </pre>
 * The idea is, that handlers can work with both the object and the dom view of this SOAPElement.
 * Note that transitions may be expensive.
 *
 * @see ObjectContent
 * @see XMLContent
 * @see DOMContent
 *
 * @author Thomas.Diesler@jboss.org
 * @author Heiko.Braun@jboss.org
 * @since 13-Dec-2004
 */
public class SOAPContentElement extends SOAPElementImpl implements SOAPContentAccess
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(SOAPContentElement.class);
   // provide logging
   private static Logger log = Logger.getLogger(SOAPContentElement.class);

   public static final QName GENERIC_PARAM_NAME = new QName("genericParam");
   public static final QName GENERIC_RETURN_NAME = new QName("genericReturn");

   // The associated parameter
   private ParameterMetaData paramMetaData;

   // content soapContent
   protected SOAPContent soapContent;

   // while transitioning DOM expansion needs to be locked
   private boolean lockDOMExpansion = false;

   /** Construct a SOAPContentElement
    */
   public SOAPContentElement(Name name)
   {
      super(name);
      this.soapContent = new DOMContent(this);
   }

   public SOAPContentElement(QName qname)
   {
      super(qname);
      this.soapContent = new DOMContent(this);
   }

   public SOAPContentElement(SOAPElementImpl element)
   {
      super(element);
      this.soapContent = new DOMContent(this);
   }

   public ParameterMetaData getParamMetaData()
   {
      if (paramMetaData == null)
         throw new IllegalStateException(BundleUtils.getMessage(bundle, "PARAMETER_META_DATA_NOT_AVAILABLE"));

      return paramMetaData;
   }

   public void setParamMetaData(ParameterMetaData paramMetaData)
   {
      this.paramMetaData = paramMetaData;
   }

   public QName getXmlType()
   {
      return getParamMetaData().getXmlType();
   }

   public Class getJavaType()
   {
      return getParamMetaData().getJavaType();
   }

   protected State transitionTo(State nextState)
   {
      State prevState = soapContent.getState();
      if (nextState != prevState)
      {
         if (log.isDebugEnabled())
         {
            log.debug("-----------------------------------");
            log.debug("Transitioning from " + prevState + " to " + nextState);
         }
         lockDOMExpansion = true;

         soapContent = soapContent.transitionTo(nextState);

         lockDOMExpansion = false;
         log.debug("-----------------------------------");
      }
      return prevState;
   }

   /** Get the payload as source.
    */
   public XMLFragment getXMLFragment()
   {
      transitionTo(State.XML_VALID);
      return soapContent.getXMLFragment();
   }

   public void setXMLFragment(XMLFragment xmlFragment)
   {
      soapContent = new XMLContent(this);
      soapContent.setXMLFragment(xmlFragment);
   }

   public Object getObjectValue()
   {
      transitionTo(State.OBJECT_VALID);
      return soapContent.getObjectValue();
   }

   public void setObjectValue(Object objValue)
   {
      soapContent = new ObjectContent(this);
      soapContent.setObjectValue(objValue);
   }

   // SOAPElement interface ********************************************************************************************

   public SOAPElement addChildElement(SOAPElement child) throws SOAPException
   {
      if (log.isTraceEnabled())
         log.trace("addChildElement: " + child);
      expandToDOM();
      return super.addChildElement(child);
   }

   public SOAPElement addChildElement(String localName, String prefix) throws SOAPException
   {
      if (log.isTraceEnabled())
         log.trace("addChildElement: [localName=" + localName + ",prefix=" + prefix + "]");
      expandToDOM();
      return super.addChildElement(localName, prefix);
   }

   public SOAPElement addChildElement(String localName, String prefix, String uri) throws SOAPException
   {
      if (log.isTraceEnabled())
         log.trace("addChildElement: [localName=" + localName + ",prefix=" + prefix + ",uri=" + uri + "]");
      expandToDOM();
      return super.addChildElement(localName, prefix, uri);
   }

   public SOAPElement addChildElement(Name name) throws SOAPException
   {
      if (log.isTraceEnabled())
         log.trace("addChildElement: [name=" + name + "]");
      expandToDOM();
      return super.addChildElement(name);
   }

   public SOAPElement addChildElement(String name) throws SOAPException
   {
      if (log.isTraceEnabled())
         log.trace("addChildElement: [name=" + name + "]");
      expandToDOM();
      return super.addChildElement(name);
   }

   public SOAPElement addTextNode(String value) throws SOAPException
   {
      if (log.isTraceEnabled())
         log.trace("addTextNode: [value=" + value + "]");
      expandToDOM();
      return super.addTextNode(value);
   }

   public Iterator<org.w3c.dom.Node> getChildElements()
   {
      log.trace("getChildElements");
      expandToDOM();
      return super.getChildElements();
   }

   public Iterator<SOAPElement> getChildElements(Name name)
   {
      if (log.isTraceEnabled())
         log.trace("getChildElements: [name=" + name + "]");
      expandToDOM();
      return super.getChildElements(name);
   }

   public void removeContents()
   {
      log.trace("removeContents");
      expandToDOM();
      super.removeContents();
   }

   public Iterator<Name> getAllAttributes()
   {
      return super.getAllAttributes();
   }

   public String getAttribute(String name)
   {
      return super.getAttribute(name);
   }

   public Attr getAttributeNode(String name)
   {
      return super.getAttributeNode(name);
   }

   public Attr getAttributeNodeNS(String namespaceURI, String localName)
   {
      return super.getAttributeNodeNS(namespaceURI, localName);
   }

   public String getAttributeNS(String namespaceURI, String localName)
   {
      return super.getAttributeNS(namespaceURI, localName);
   }

   public String getAttributeValue(Name name)
   {
      return super.getAttributeValue(name);
   }

   public SOAPElement addAttribute(Name name, String value) throws SOAPException
   {
      if (log.isTraceEnabled())
         log.trace("addAttribute: [name=" + name + ",value=" + value + "]");
      expandToDOM();
      return super.addAttribute(name, value);
   }

   public SOAPElement addNamespaceDeclaration(String prefix, String nsURI)
   {
      if (log.isTraceEnabled())
         log.trace("addNamespaceDeclaration: [prefix=" + prefix + ",nsURI=" + nsURI + "]");
      expandToDOM();
      return super.addNamespaceDeclaration(prefix, nsURI);
   }

   public Name getElementName()
   {
      return super.getElementName();
   }

   public NodeList getElementsByTagName(String name)
   {
      if (log.isTraceEnabled())
         log.trace("getElementsByTagName: [name=" + name + "]");
      expandToDOM();
      return super.getElementsByTagName(name);
   }

   public NodeList getElementsByTagNameNS(String namespaceURI, String localName)
   {
      if (log.isTraceEnabled())
         log.trace("getElementsByTagName: [nsURI=" + namespaceURI + ",localName=" + localName + "]");
      expandToDOM();
      return super.getElementsByTagNameNS(namespaceURI, localName);
   }

   public String getEncodingStyle()
   {
      return super.getEncodingStyle();
   }

   public Iterator<String> getNamespacePrefixes()
   {
      return super.getNamespacePrefixes();
   }

   public String getNamespaceURI(String prefix)
   {
      return super.getNamespaceURI(prefix);
   }

   public TypeInfo getSchemaTypeInfo()
   {
      return super.getSchemaTypeInfo();
   }

   public String getTagName()
   {
      return super.getTagName();
   }

   public Iterator<String> getVisibleNamespacePrefixes()
   {
      return super.getVisibleNamespacePrefixes();
   }

   public boolean hasAttribute(String name)
   {
      return super.hasAttribute(name);
   }

   public boolean hasAttributeNS(String namespaceURI, String localName)
   {
      return super.hasAttributeNS(namespaceURI, localName);
   }

   public boolean removeAttribute(Name name)
   {
      if (log.isTraceEnabled())
         log.trace("removeAttribute: " + name.getQualifiedName());
      expandToDOM();
      return super.removeAttribute(name);
   }

   public void removeAttribute(String name) throws DOMException
   {
      if (log.isTraceEnabled())
         log.trace("removeAttribute: " + name);
      expandToDOM();
      super.removeAttribute(name);
   }

   public Attr removeAttributeNode(Attr oldAttr) throws DOMException
   {
      if (log.isTraceEnabled())
         log.trace("removeAttribute: " + oldAttr.getNodeName());
      expandToDOM();
      return super.removeAttributeNode(oldAttr);
   }

   public void removeAttributeNS(String namespaceURI, String localName) throws DOMException
   {
      if (log.isTraceEnabled())
         log.trace("removeAttributeNS: {" + namespaceURI + "}" + localName);
      expandToDOM();
      super.removeAttributeNS(namespaceURI, localName);
   }

   public boolean removeNamespaceDeclaration(String prefix)
   {
      if (log.isTraceEnabled())
         log.trace("removeNamespaceDeclaration: " + prefix);
      expandToDOM();
      return super.removeNamespaceDeclaration(prefix);
   }

   public void setAttribute(String name, String value) throws DOMException
   {
      if (log.isTraceEnabled())
         log.trace("setAttribute: [name=" + name + ",value=" + value + "]");
      expandToDOM();
      super.setAttribute(name, value);
   }

   public Attr setAttributeNode(Attr newAttr) throws DOMException
   {
      if (log.isTraceEnabled())
         log.trace("setAttributeNode: " + newAttr);
      expandToDOM();
      return super.setAttributeNode(newAttr);
   }

   public Attr setAttributeNodeNS(Attr newAttr) throws DOMException
   {
      if (log.isTraceEnabled())
         log.trace("setAttributeNodeNS: " + newAttr);
      expandToDOM();
      return super.setAttributeNodeNS(newAttr);
   }

   public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException
   {
      if (log.isTraceEnabled())
         log.trace("setAttribute: [nsURI=" + namespaceURI + ",name=" + qualifiedName + ",value=" + value + "]");
      expandToDOM();
      super.setAttributeNS(namespaceURI, qualifiedName, value);
   }

   public void setIdAttribute(String name, boolean isId) throws DOMException
   {
      if (log.isTraceEnabled())
         log.trace("setIdAttribute: [name=" + name + ",value=" + isId + "]");
      expandToDOM();
      super.setIdAttribute(name, isId);
   }

   public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException
   {
      if (log.isTraceEnabled())
         log.trace("setIdAttributeNode: [idAttr=" + idAttr + ",value=" + isId + "]");
      expandToDOM();
      super.setIdAttributeNode(idAttr, isId);
   }

   public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException
   {
      if (log.isTraceEnabled())
         log.trace("setIdAttributeNS: [nsURI=" + namespaceURI + ",name=" + localName + ",value=" + isId + "]");
      expandToDOM();
      super.setIdAttributeNS(namespaceURI, localName, isId);
   }

   // Node interface **************************************************************************************************

   public Node appendChild(Node newChild) throws DOMException
   {
      if (log.isTraceEnabled())
         log.trace("appendChild: " + newChild);
      expandToDOM();
      return super.appendChild(newChild);
   }

   public Node cloneNode(boolean deep)
   {
      if (log.isTraceEnabled())
         log.trace("cloneNode: deep=" + deep);
      expandToDOM();
      return super.cloneNode(deep);
   }

   public NodeList getChildNodes()
   {
      log.trace("getChildNodes");
      expandToDOM();
      return super.getChildNodes();
   }

   public Node getFirstChild()
   {
      log.trace("getFirstChild");
      expandToDOM();
      return super.getFirstChild();
   }

   public Node getLastChild()
   {
      log.trace("getLastChild");
      expandToDOM();
      return super.getLastChild();
   }

   public String getValue()
   {
      log.trace("getValue");
      expandToDOM();
      return super.getValue();
   }

   public boolean hasChildNodes()
   {
      log.trace("hasChildNodes");
      expandToDOM();
      return super.hasChildNodes();
   }

   public Node removeChild(Node oldChild) throws DOMException
   {
      if (log.isTraceEnabled())
         log.trace("removeChild: " + oldChild);
      expandToDOM();
      return super.removeChild(oldChild);
   }

   public Node replaceChild(Node newChild, Node oldChild) throws DOMException
   {
      if (log.isTraceEnabled())
         log.trace("replaceChild: [new=" + newChild + ",old=" + oldChild + "]");
      expandToDOM();
      return super.replaceChild(newChild, oldChild);
   }

   private void expandToDOM()
   {
      if (!lockDOMExpansion)
         transitionTo(State.DOM_VALID);
   }

   public void setValue(String value)
   {
      if (log.isTraceEnabled())
         log.trace("setValue: " + value);
      expandToDOM();
      super.setValue(value);
   }

   public NamedNodeMap getAttributes()
   {
      return super.getAttributes();
   }

   public boolean hasAttributes()
   {
      return super.hasAttributes();
   }

   public org.w3c.dom.Node getPreviousSibling()
   {
      log.trace("getPreviousSibling");
      expandToDOM();
      return super.getPreviousSibling();
   }

   public org.w3c.dom.Node getNextSibling()
   {
      log.trace("getNextSibling");
      expandToDOM();
      return super.getNextSibling();
   }

   // END Node interface ***********************************************************************************************
   
   public void writeElement(Writer writer) throws IOException
   {      
      if (soapContent instanceof DOMContent)
      {
         DOMWriter dw = new DOMWriter(writer);
         CommonMessageContext ctx = MessageContextAssociation.peekMessageContext();
         if (ctx != null && Boolean.TRUE == ctx.get(Constants.DOM_CONTENT_CANONICAL_NORMALIZATION))
         {
            log.trace("Forcing canonical normalization of DOMContent...");
            dw.setCanonical(true);
         }
         dw.print(this);
      }
      else
      {
         transitionTo(State.XML_VALID);
         soapContent.getXMLFragment().writeTo(writer);

      }
   }

   public void accept(SAAJVisitor visitor)
   {
      visitor.visitSOAPContentElement(this);
   }
}
