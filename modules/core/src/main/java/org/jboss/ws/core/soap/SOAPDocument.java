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

import java.util.ResourceBundle;

import org.apache.xerces.dom.DocumentImpl;
import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.common.DOMUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.UserDataHandler;

/**
 * <code>SOAPDocument</code> ensures that the propper SAAJ elements are
 * returned when Document calls are made from a DOM client. This implementation
 * enscapsulates a single ThreadLocal Document object.
 *
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
@SuppressWarnings("serial")
public class SOAPDocument extends DocumentImpl implements Document
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(SOAPDocument.class);

   private Document doc = DOMUtils.getOwnerDocument();

   // Document methods

   public DocumentType getDoctype()
   {
      return null;
   }

   public DOMImplementation getImplementation()
   {
      return this.doc.getImplementation();
   }

   public Element getDocumentElement()
   {
      // The base SOAPDocument does not have an element, only SOAPPart will have
      return null;
   }

   public Element createElement(String tagName) throws DOMException
   {
      return new SOAPElementImpl(tagName);
   }

   public DocumentFragment createDocumentFragment()
   {
      return this.doc.createDocumentFragment();
   }

   public Text createTextNode(String data)
   {
      return this.doc.createTextNode(data);
   }

   public Comment createComment(String data)
   {
      return this.doc.createComment(data);
   }

   public CDATASection createCDATASection(String data) throws DOMException
   {
      return this.doc.createCDATASection(data);
   }

   public ProcessingInstruction createProcessingInstruction(String target, String data) throws DOMException
   {
      return this.doc.createProcessingInstruction(target, data);
   }

   public Attr createAttribute(String name) throws DOMException
   {
      return this.doc.createAttribute(name);
   }

   public EntityReference createEntityReference(String name) throws DOMException
   {
      throw new UnsupportedOperationException(BundleUtils.getMessage(bundle, "ENTITY_REFERENCES_ARE_NOT_ALLOWED"));
   }

   public NodeList getElementsByTagName(String tagname)
   {
      return this.doc.getElementsByTagName(tagname);
   }

   public Node importNode(Node importedNode, boolean deep) throws DOMException
   {
      return this.doc.importNode(importedNode, deep);
   }

   public Element createElementNS(String namespaceURI, String qualifiedName) throws DOMException
   {
      int loc = qualifiedName.indexOf(":");

      if (loc == -1)
         return new SOAPElementImpl(qualifiedName, null, namespaceURI);

      if (loc == qualifiedName.length() - 1)
         throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "INVALID_QUALIFIED_NAME"));

      return new SOAPElementImpl(qualifiedName.substring(loc + 1), qualifiedName.substring(0, loc), namespaceURI);
   }

   public Attr createAttributeNS(String namespaceURI, String qualifiedName) throws DOMException
   {
      return this.doc.createAttributeNS(namespaceURI, qualifiedName);
   }

   public NodeList getElementsByTagNameNS(String namespaceURI, String localName)
   {
      return this.doc.getElementsByTagNameNS(namespaceURI, localName);
   }

   // Node methods
   public String getNodeName()
   {
      return this.doc.getNodeName();
   }

   public String getNodeValue() throws DOMException
   {
      return this.doc.getNodeValue();
   }

   public void setNodeValue(String nodeValue) throws DOMException
   {
      this.doc.setNodeValue(nodeValue);
   }

   public short getNodeType()
   {
      return this.doc.getNodeType();
   }

   public Node getParentNode()
   {
      return this.doc.getParentNode();
   }

   public NodeList getChildNodes()
   {
      return this.doc.getChildNodes();
   }

   public Node getFirstChild()
   {
      return this.doc.getFirstChild();
   }

   public Node getLastChild()
   {
      return this.doc.getLastChild();
   }

   public Node getPreviousSibling()
   {
      return this.doc.getPreviousSibling();
   }

   public Node getNextSibling()
   {
      return this.doc.getNextSibling();
   }

   public NamedNodeMap getAttributes()
   {
      return this.doc.getAttributes();
   }

   public Node insertBefore(Node newChild, Node refChild) throws DOMException
   {
      return this.doc.insertBefore(newChild, refChild);
   }

   public Node replaceChild(Node newChild, Node oldChild) throws DOMException
   {
      return this.doc.replaceChild(newChild, oldChild);
   }

   public Node removeChild(Node oldChild) throws DOMException
   {
      return this.doc.removeChild(oldChild);
   }

   public Node appendChild(Node newChild) throws DOMException
   {
      return this.doc.appendChild(newChild);
   }

   public boolean hasChildNodes()
   {
      return this.doc.hasChildNodes();
   }

   public Node cloneNode(boolean deep)
   {
      return this.doc.cloneNode(deep);
   }

   public void normalize()
   {
      this.doc.normalize();
   }

   public boolean isSupported(String feature, String version)
   {
      return this.doc.isSupported(feature, version);
   }

   public String getNamespaceURI()
   {
      return this.doc.getNamespaceURI();
   }

   public String getPrefix()
   {
      return this.doc.getPrefix();
   }

   public void setPrefix(String prefix) throws DOMException
   {
      this.doc.setPrefix(prefix);
   }

   public String getLocalName()
   {
      return this.doc.getLocalName();
   }

   public boolean hasAttributes()
   {
      return this.doc.hasAttributes();
   }

   // DOM3 methods

   public String getInputEncoding()
   {
      return this.doc.getInputEncoding();
   }

   public String getXmlEncoding()
   {
      return this.doc.getXmlEncoding();
   }

   public boolean getXmlStandalone()
   {
      return this.doc.getXmlStandalone();
   }

   public void setXmlStandalone(boolean xmlStandalone) throws DOMException
   {
      this.doc.setXmlStandalone(xmlStandalone);
   }

   public String getXmlVersion()
   {
      return this.doc.getXmlVersion();
   }

   public void setXmlVersion(String xmlVersion) throws DOMException
   {
      this.doc.setXmlVersion(xmlVersion);
   }

   public boolean getStrictErrorChecking()
   {
      return this.doc.getStrictErrorChecking();
   }

   public void setStrictErrorChecking(boolean strictErrorChecking)
   {
      this.doc.setStrictErrorChecking(strictErrorChecking);
   }

   public String getDocumentURI()
   {
      return this.doc.getDocumentURI();
   }

   public void setDocumentURI(String documentURI)
   {
      this.doc.setDocumentURI(documentURI);
   }

   public Node adoptNode(Node source) throws DOMException
   {
      return this.doc.adoptNode(source);
   }

   public DOMConfiguration getDomConfig()
   {
      return this.doc.getDomConfig();
   }

   public void normalizeDocument()
   {
      this.doc.normalizeDocument();
   }

   public Node renameNode(Node n, String namespaceURI, String qualifiedName) throws DOMException
   {
      return this.doc.renameNode(n, namespaceURI, qualifiedName);
   }

   public String getBaseURI()
   {
      return this.doc.getBaseURI();
   }

   public short compareDocumentPosition(Node other) throws DOMException
   {
      return this.doc.compareDocumentPosition(other);
   }

   public String getTextContent() throws DOMException
   {
      return this.doc.getTextContent();
   }

   public void setTextContent(String textContent) throws DOMException
   {
      this.doc.setTextContent(textContent);
   }

   public boolean isSameNode(Node other)
   {
      return this.doc.isSameNode(other);
   }

   public String lookupPrefix(String namespaceURI)
   {
      return this.doc.lookupPrefix(namespaceURI);
   }

   public boolean isDefaultNamespace(String namespaceURI)
   {
      return this.doc.isDefaultNamespace(namespaceURI);
   }

   public String lookupNamespaceURI(String prefix)
   {
      return this.doc.lookupNamespaceURI(prefix);
   }

   public boolean isEqualNode(Node arg)
   {
      return this.doc.isEqualNode(arg);
   }

   public Object getFeature(String feature, String version)
   {
      return this.doc.getFeature(feature, version);
   }

   public Object setUserData(String key, Object data, UserDataHandler handler)
   {
      return this.doc.setUserData(key, data, handler);
   }

   public Object getUserData(String arg0)
   {
      return this.doc.getUserData(arg0);
   }

   public Element getElementById(String elementId)
   {
      return this.doc.getElementById(elementId);
   }

}
