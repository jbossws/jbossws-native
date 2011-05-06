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
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.jboss.logging.Logger;
import org.jboss.ws.api.util.ServiceLoader;
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

/** An implementation of SOAPPart.
 * 
 * @author Thomas.Diesler@jboss.org
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public class SOAPPartImpl extends SOAPPart
{
   // provide logging
   private static Logger log = Logger.getLogger(SOAPPartImpl.class);

   private SOAPMessage soapMessage;
   private SOAPEnvelope soapEnvelope;

   private SOAPDocument doc = new SOAPDocument();

   SOAPPartImpl(SOAPMessage message)
   {
      this.soapMessage = message;
   }

   public SOAPMessage getSOAPMessage()
   {
      return soapMessage;
   }
   
   public SOAPEnvelope getEnvelope() throws SOAPException
   {
      return soapEnvelope;
   }

   public void setEnvelope(SOAPEnvelope soapEnvelope)
   {
      this.soapEnvelope = soapEnvelope;
   }

   public void removeMimeHeader(String s)
   {
      MimeHeaders mimeHeaders = soapMessage.getMimeHeaders();
      mimeHeaders.removeHeader(s);
   }

   public void removeAllMimeHeaders()
   {
      MimeHeaders mimeHeaders = soapMessage.getMimeHeaders();
      mimeHeaders.removeAllHeaders();
   }

   public String[] getMimeHeader(String name)
   {
      MimeHeaders mimeHeaders = soapMessage.getMimeHeaders();
      return mimeHeaders.getHeader(name);
   }

   public void setMimeHeader(String name, String value)
   {
      MimeHeaders mimeHeaders = soapMessage.getMimeHeaders();
      mimeHeaders.setHeader(name, value);
   }

   public void addMimeHeader(String name, String value)
   {
      MimeHeaders mimeHeaders = soapMessage.getMimeHeaders();
      mimeHeaders.addHeader(name, value);
   }

   public Iterator getAllMimeHeaders()
   {
      MimeHeaders mimeHeaders = soapMessage.getMimeHeaders();
      return mimeHeaders.getAllHeaders();
   }

   public Iterator getMatchingMimeHeaders(String names[])
   {
      MimeHeaders mimeHeaders = soapMessage.getMimeHeaders();
      return mimeHeaders.getMatchingHeaders(names);
   }

   public Iterator getNonMatchingMimeHeaders(String names[])
   {
      MimeHeaders mimeHeaders = soapMessage.getMimeHeaders();
      return mimeHeaders.getNonMatchingHeaders(names);
   }

   public void setContent(Source source) throws SOAPException
   {
      // R2714 For one-way operations, an INSTANCE MUST NOT return a HTTP response that contains a SOAP envelope. 
      // Specifically, the HTTP response entity-body must be empty.
      if (source == null)
      {
         if(log.isDebugEnabled()) log.debug("Setting content source to null removes the SOAPEnvelope");
         soapEnvelope = null;
         return;
      }

      // Start with a fresh soapMessage
      /*MessageFactory mf = MessageFactory.newInstance();
      soapMessage = mf.createMessage();
      soapMessage.getSOAPHeader().detachNode();*/

      if (source instanceof DOMSource)
      {
         Element domElement;
         DOMSource domSource = (DOMSource)source;
         Node node = domSource.getNode();
         if (node instanceof Document)
            domElement = ((Document)node).getDocumentElement();
         else if (node instanceof Element)
            domElement = (Element)node;
         else
            throw new SOAPException("Unsupported DOMSource node: " + node);

         EnvelopeBuilder envBuilder = (EnvelopeBuilder) ServiceLoader.loadService(EnvelopeBuilder.class.getName(), EnvelopeBuilderDOM.class.getName());
         envBuilder.setStyle(Style.DOCUMENT);
         envBuilder.build(soapMessage, domElement);
      }
      else if (source instanceof StreamSource)
      {
         try
         {
            StreamSource streamSource = (StreamSource)source;
            EnvelopeBuilder envBuilder = (EnvelopeBuilder)ServiceLoader.loadService(EnvelopeBuilder.class.getName(), EnvelopeBuilderDOM.class.getName());
            envBuilder.setStyle(Style.DOCUMENT);
            InputStream stream = streamSource.getInputStream();
            Reader reader = streamSource.getReader();
            if (stream != null)
               envBuilder.build(soapMessage, stream, false);
            else if (reader != null)
               envBuilder.build(soapMessage, reader, false);
         }
         catch (IOException e)
         {
            throw new SOAPException("Cannot parse stream source", e);
         }
      }
      else
      {
         throw new SOAPException("Unsupported source parameter: " + source);
      }
   }

   public Source getContent() throws SOAPException
   {
      return new DOMSource(soapEnvelope);
   }

   // Document *********************************************************************************************************

   public DOMImplementation getImplementation()
   {
      return this.doc.getImplementation();
   }

   public DocumentFragment createDocumentFragment()
   {
      return this.doc.createDocumentFragment();
   }

   public DocumentType getDoctype()
   {
      return this.doc.getDoctype();
   }

   public Element getDocumentElement()
   {
      return this.soapEnvelope;
   }

   public Attr createAttribute(String name) throws DOMException
   {
      return this.doc.createAttribute(name);
   }

   public CDATASection createCDATASection(String data) throws DOMException
   {
      return this.doc.createCDATASection(data);
   }

   public Comment createComment(String data)
   {
      return this.doc.createComment(data);
   }

   public Element createElement(String tagName) throws DOMException
   {
      return this.doc.createElement(tagName);
   }

   public Element getElementById(String elementId)
   {
      return this.doc.getElementById(elementId);
   }

   public EntityReference createEntityReference(String name) throws DOMException
   {
      return this.doc.createEntityReference(name);
   }

   public org.w3c.dom.Node importNode(org.w3c.dom.Node importedNode, boolean deep) throws DOMException
   {
      return this.doc.importNode(importedNode, deep);
   }

   public NodeList getElementsByTagName(String tagname)
   {
      return this.doc.getElementsByTagName(tagname);
   }

   public Text createTextNode(String data)
   {
      return this.doc.createTextNode(data);
   }

   public Attr createAttributeNS(String namespaceURI, String qualifiedName) throws DOMException
   {
      return this.doc.createAttributeNS(namespaceURI, qualifiedName);
   }

   public Element createElementNS(String namespaceURI, String qualifiedName) throws DOMException
   {
      return this.doc.createElementNS(namespaceURI, qualifiedName);
   }

   public NodeList getElementsByTagNameNS(String namespaceURI, String localName)
   {
      return this.doc.getElementsByTagNameNS(namespaceURI, localName);
   }

   public ProcessingInstruction createProcessingInstruction(String target, String data) throws DOMException
   {
      return this.doc.createProcessingInstruction(target, data);
   }

   // Node *********************************************************************************************************

   public org.w3c.dom.Node appendChild(org.w3c.dom.Node newChild) throws DOMException
   {
      return this.doc.appendChild(newChild);
   }

   public org.w3c.dom.Node cloneNode(boolean deep)
   {
      return this.doc.cloneNode(deep);
   }

   public NamedNodeMap getAttributes()
   {
      return this.doc.getAttributes();
   }

   public NodeList getChildNodes()
   {
      List<NodeImpl> list = new ArrayList<NodeImpl>();
      if (soapEnvelope != null)
      {
         list.add((NodeImpl)soapEnvelope);
      }      
      return new NodeListImpl(list);
   }

   public org.w3c.dom.Node getFirstChild()
   {
      return soapEnvelope;
   }

   public org.w3c.dom.Node getLastChild()
   {
      return soapEnvelope;
   }

   public String getLocalName()
   {
      return this.doc.getLocalName();
   }

   public String getNamespaceURI()
   {
      return this.doc.getNamespaceURI();
   }

   public org.w3c.dom.Node getNextSibling()
   {
      return this.doc.getNextSibling();
   }

   public String getNodeName()
   {
      return doc.getNodeName();
   }

   public short getNodeType()
   {
      return doc.getNodeType();
   }

   public String getNodeValue() throws DOMException
   {
      return this.doc.getNodeValue();
   }

   public Document getOwnerDocument()
   {
      return this.doc;
   }

   public org.w3c.dom.Node getParentNode()
   {
      return this.doc.getParentNode();
   }

   public String getPrefix()
   {
      return this.doc.getPrefix();
   }

   public org.w3c.dom.Node getPreviousSibling()
   {
      return this.doc.getPreviousSibling();
   }

   public boolean hasAttributes()
   {
      return this.doc.hasAttributes();
   }

   public boolean hasChildNodes()
   {
      return this.doc.hasChildNodes();
   }

   public org.w3c.dom.Node insertBefore(org.w3c.dom.Node newChild, org.w3c.dom.Node refChild) throws DOMException
   {
      return this.doc.insertBefore(newChild, refChild);
   }

   public boolean isSupported(String feature, String version)
   {
      return this.doc.isSupported(feature, version);
   }

   public void normalize()
   {
      this.doc.normalize();
   }

   public org.w3c.dom.Node removeChild(org.w3c.dom.Node oldChild) throws DOMException
   {
      return this.doc.removeChild(oldChild);
   }

   public org.w3c.dom.Node replaceChild(org.w3c.dom.Node newChild, org.w3c.dom.Node oldChild) throws DOMException
   {
      return this.doc.replaceChild(newChild, oldChild);
   }

   public void setNodeValue(String nodeValue) throws DOMException
   {
      this.doc.setNodeValue(nodeValue);
   }

   public void setPrefix(String s) throws DOMException
   {
      this.doc.setPrefix(s);
   }

   public Node adoptNode(Node source) throws DOMException
   {
      return this.doc.adoptNode(source);
   }

   public String getDocumentURI()
   {
      return this.doc.getDocumentURI();
   }

   public DOMConfiguration getDomConfig()
   {
      return this.doc.getDomConfig();
   }

   public String getInputEncoding()
   {
      return this.doc.getInputEncoding();
   }

   public boolean getStrictErrorChecking()
   {
      return this.doc.getStrictErrorChecking();
   }

   public String getXmlEncoding()
   {
      return this.doc.getXmlEncoding();
   }

   public boolean getXmlStandalone()
   {
      return this.doc.getXmlStandalone();
   }

   public String getXmlVersion()
   {
      return this.doc.getXmlVersion();
   }

   public void normalizeDocument()
   {
      this.doc.normalizeDocument();
   }

   public Node renameNode(Node n, String namespaceURI, String qualifiedName) throws DOMException
   {
      return this.doc.renameNode(n, namespaceURI, qualifiedName);
   }

   public void setDocumentURI(String documentURI)
   {
      this.doc.setDocumentURI(documentURI);
   }

   public void setStrictErrorChecking(boolean strictErrorChecking)
   {
      this.doc.setStrictErrorChecking(strictErrorChecking);
   }

   public void setXmlStandalone(boolean xmlStandalone) throws DOMException
   {
      this.doc.setXmlStandalone(xmlStandalone);
   }

   public void setXmlVersion(String xmlVersion) throws DOMException
   {
      this.doc.setXmlVersion(xmlVersion);
   }

   public short compareDocumentPosition(Node other) throws DOMException
   {
      return this.doc.compareDocumentPosition(other);
   }

   public String getBaseURI()
   {
      return this.doc.getBaseURI();
   }

   public Object getFeature(String feature, String version)
   {
      return this.doc.getFeature(feature, version);
   }

   public String getTextContent() throws DOMException
   {
      return this.doc.getTextContent();
   }

   public Object getUserData(String key)
   {
      return this.doc.getUserData(key);
   }

   public boolean isDefaultNamespace(String namespaceURI)
   {
      return this.doc.isDefaultNamespace(namespaceURI);
   }

   public boolean isEqualNode(Node arg)
   {
      return this.doc.isEqualNode(arg);
   }

   public boolean isSameNode(Node other)
   {
      return this.doc.isSameNode(other);
   }

   public String lookupNamespaceURI(String prefix)
   {
      return this.doc.lookupNamespaceURI(prefix);
   }

   public String lookupPrefix(String namespaceURI)
   {
      return this.doc.lookupPrefix(namespaceURI);
   }

   public void setTextContent(String textContent) throws DOMException
   {
      this.doc.setTextContent(textContent);
   }

   public Object setUserData(String key, Object data, UserDataHandler handler)
   {
      return this.doc.setUserData(key, data, handler);
   }

   public void detachNode()
   {
      // does nothing
   }

   public SOAPElement getParentElement()
   {
      return null;
   }

   public String getValue()
   {
      return null;
   }

   public void recycleNode()
   {
      // does nothing
   }

   public void setParentElement(SOAPElement parent) throws SOAPException
   {
      throw new SOAPException("The parent element of a soap part is not defined");
   }

   public void setValue(String value)
   {
      throw new IllegalStateException("Setting value of a soap part is not defined");
   }

}
