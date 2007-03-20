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
package org.jboss.ws.core.soap;

// $Id$

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Locale;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.Text;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.utils.DOMUtils;
import org.jboss.ws.core.utils.DOMWriter;
import org.jboss.ws.metadata.umdm.UnifiedMetaData;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An object that represents the contents of the SOAP body element in a SOAP message.
 * A SOAP body element consists of XML data that affects the way the application-specific content is processed.
 *
 * A SOAPBody object contains SOAPBodyElement objects, which have the content for the SOAP body.
 * A SOAPFault object, which carries status and/or error information, is an example of a SOAPBodyElement object.
 *
 * @author Thomas.Diesler@jboss.org
 */
public class SOAPBodyImpl extends SOAPElementImpl implements SOAPBody
{
   // provide logging
   private static Logger log = Logger.getLogger(SOAPBodyImpl.class);

   // Generic JAXWS payload
   private Source payload;
   private boolean isDOMValid = true;
   private boolean isModifiedFromSource;

   public SOAPBodyImpl(String prefix, String namespace)
   {
      super("Body", prefix, namespace);
   }

   public boolean isDOMValid()
   {
      return isDOMValid;
   }

   public boolean isModifiedFromSource()
   {
      return isModifiedFromSource;
   }

   public Source getPayload()
   {
      return payload;
   }

   public void setPayload(Source payload)
   {
      if (log.isDebugEnabled())
         log.debug("setPayload: " + payload.getClass().getName());
      removeContents();
      this.payload = payload;
      this.isDOMValid = false;
   }

   /** Convert the child into a SOAPBodyElement */
   public SOAPElement addChildElement(SOAPElement child) throws SOAPException
   {
      log.trace("addChildElement: " + child.getElementName());
      expandToDOM(false);
      if ((child instanceof SOAPBodyElement) == false)
         child = convertToBodyElement(child);

      child = super.addChildElement(child);
      return child;
   }

   public SOAPBodyElement addBodyElement(Name name) throws SOAPException
   {
      log.trace("addBodyElement: " + name);
      expandToDOM(false);
      SOAPBodyElement child = new SOAPBodyElementDoc(name);
      return (SOAPBodyElement)addChildElement(child);
   }

   public SOAPBodyElement addBodyElement(QName qname) throws SOAPException
   {
      log.trace("addBodyElement: " + qname);
      expandToDOM(false);
      SOAPBodyElement child = new SOAPBodyElementDoc(qname);
      return (SOAPBodyElement)addChildElement(child);
   }

   public SOAPBodyElement addDocument(Document doc) throws SOAPException
   {
      log.trace("addDocument");
      expandToDOM(false);
      Element rootElement = doc.getDocumentElement();
      SOAPFactoryImpl soapFactory = new SOAPFactoryImpl();
      SOAPElement soapElement = soapFactory.createElement(rootElement);
      return (SOAPBodyElement)addChildElement(soapElement);
   }

   public SOAPFault addFault() throws SOAPException
   {
      log.trace("addFault");
      expandToDOM(true);
      if (hasFault())
         throw new SOAPException("A SOAPBody may contain at most one SOAPFault child element");

      SOAPFaultImpl soapFault = new SOAPFaultImpl(getPrefix(), getNamespaceURI());
      soapFault = (SOAPFaultImpl)addChildElement(soapFault);
      soapFault.setFaultCode(soapFault.getDefaultFaultCode());
      return soapFault;
   }

   public SOAPFault addFault(Name faultCode, String faultString) throws SOAPException
   {
      log.trace("addFault");
      expandToDOM(true);
      if (hasFault())
         throw new SOAPException("A SOAPBody may contain at most one SOAPFault child element");

      SOAPFaultImpl soapFault = new SOAPFaultImpl(getPrefix(), getNamespaceURI());
      soapFault = (SOAPFaultImpl)addChildElement(soapFault);
      soapFault.setFaultCode(faultCode);
      soapFault.setFaultString(faultString);
      return soapFault;
   }

   public SOAPFault addFault(QName faultCode, String faultString) throws SOAPException
   {
      log.trace("addFault");
      expandToDOM(true);
      if (hasFault())
         throw new SOAPException("A SOAPBody may contain at most one SOAPFault child element");

      SOAPFaultImpl soapFault = new SOAPFaultImpl(getPrefix(), getNamespaceURI());
      soapFault = (SOAPFaultImpl)addChildElement(soapFault);
      soapFault.setFaultCode(faultCode);
      soapFault.setFaultString(faultString);
      return soapFault;
   }

   public SOAPFault addFault(Name faultCode, String faultString, Locale locale) throws SOAPException
   {
      log.trace("addFault");
      expandToDOM(true);
      if (hasFault())
         throw new SOAPException("A SOAPBody may contain at most one SOAPFault child element");

      SOAPFaultImpl soapFault = new SOAPFaultImpl(getPrefix(), getNamespaceURI());
      soapFault.setFaultCode(faultCode);
      soapFault.setFaultString(faultString, locale);
      addChildElement(soapFault);
      return soapFault;
   }

   public SOAPFault addFault(QName faultCode, String faultString, Locale locale) throws SOAPException
   {
      log.trace("addFault");
      expandToDOM(true);
      if (hasFault())
         throw new SOAPException("A SOAPBody may contain at most one SOAPFault child element");

      SOAPFaultImpl soapFault = new SOAPFaultImpl(getPrefix(), getNamespaceURI());
      soapFault.setFaultCode(faultCode);
      soapFault.setFaultString(faultString, locale);
      addChildElement(soapFault);
      return soapFault;
   }

   public SOAPFault getFault()
   {
      log.trace("getFault");
      Iterator it = faultIterator();
      SOAPFault soapFault = it.hasNext() ? (SOAPFault)it.next() : null;
      return soapFault;
   }

   public boolean hasFault()
   {
      log.trace("hasFault");
      return faultIterator().hasNext();
   }

   private Iterator faultIterator()
   {
      expandToDOM(true);
      return getChildElements(new QName(getNamespaceURI(), "Fault"));
   }

   public Node appendChild(Node newChild) throws DOMException
   {
      log.trace("appendChild: " + newChild.getNodeName());
      expandToDOM(false);
      if (needsConversionToBodyElement(newChild))
         newChild = convertToBodyElement(newChild);

      return super.appendChild(newChild);
   }

   public Node insertBefore(Node newChild, Node refChild) throws DOMException
   {
      log.trace("insertBefore: " + newChild.getNodeName());
      expandToDOM(false);
      if (needsConversionToBodyElement(newChild))
         newChild = convertToBodyElement(newChild);

      return super.insertBefore(newChild, refChild);
   }

   public Node replaceChild(Node newChild, Node oldChild) throws DOMException
   {
      log.trace("replaceChild: " + newChild.getNodeName());
      expandToDOM(false);
      if (needsConversionToBodyElement(newChild))
         newChild = convertToBodyElement(newChild);

      return super.replaceChild(newChild, oldChild);
   }

   public Iterator getChildElements()
   {
      log.trace("getChildElements");
      expandToDOM(false);
      return super.getChildElements();
   }

   public Iterator getChildElements(Name name)
   {
      log.trace("getChildElements: " + name);
      expandToDOM(false);
      return super.getChildElements(name);
   }

   public NodeList getChildNodes()
   {
      log.trace("getChildNodes");
      expandToDOM(false);
      return super.getChildNodes();
   }

   public Node getFirstChild()
   {
      log.trace("getFirstChild");
      expandToDOM(false);
      return super.getFirstChild();
   }

   public Node getLastChild()
   {
      log.trace("getLastChild");
      expandToDOM(false);
      return super.getLastChild();
   }

   public boolean hasChildNodes()
   {
      log.trace("hasChildNodes");
      expandToDOM(false);
      return super.hasChildNodes();
   }

   private static boolean needsConversionToBodyElement(Node node)
   {
      // JBCTS-440 #addTextNodeTest1 appends a Text node to a SOAPBody
      return !(node instanceof SOAPBodyElement || node instanceof DocumentFragment || node instanceof Text);
   }

   private static SOAPBodyElementDoc convertToBodyElement(Node node)
   {
      if (!(node instanceof SOAPElementImpl))
         throw new IllegalArgumentException("SOAPElement expected");

      SOAPElementImpl element = (SOAPElementImpl)node;
      element.detachNode();
      return new SOAPBodyElementDoc(element);
   }

   private void expandToDOM(boolean handleFault)
   {
      if (isDOMValid == false)
      {
         // DOM expansion should only happen when a handler accesses the DOM API.
         // We do not allow DOM expansion on a dev release.
         CommonMessageContext msgContext = MessageContextAssociation.peekMessageContext();
         if (msgContext != null && UnifiedMetaData.isFinalRelease() == false)
         {
            Boolean allowExpand = (Boolean)msgContext.get(CommonMessageContext.ALLOW_EXPAND_TO_DOM);
            if (handleFault == false && allowExpand != Boolean.TRUE)
               throw new WSException("Expanding content element to DOM");
         }

         log.trace("BEGIN expandToDOM");

         isDOMValid = true;
         try
         {
            Element child = getBodyElementFromSource();
            SOAPFactoryImpl soapFactory = new SOAPFactoryImpl();
            addChildElement(soapFactory.createElement(child));
            payload = null;
         }
         catch (RuntimeException rte)
         {
            isDOMValid = false;
            throw rte;
         }
         catch (Exception ex)
         {
            isDOMValid = false;
            throw new WSException("Cannot expand to DOM" + ex);
         }
         finally
         {
            isModifiedFromSource = true;
            log.trace("END expandToDOM");
         }
      }
   }

   private Element getBodyElementFromSource()
   {
      Element child = EnvelopeBuilderDOM.getElementFromSource(payload);
      if (payload instanceof StreamSource)
      {
         // reset the excausted input stream
         String xmlStr = DOMWriter.printNode(child, false);
         payload = new StreamSource(new ByteArrayInputStream(xmlStr.getBytes()));
      }
      return child;
   }

   @Override
   public void writeElementContent(Writer writer) throws IOException
   {
      if (payload != null)
      {
         Element child = getBodyElementFromSource();
         String xmlPayload = DOMWriter.printNode(child, false);
         if (log.isDebugEnabled())
            log.debug("writeElementContent from payload: " + xmlPayload);
         writer.write(xmlPayload);
      }
      else
      {
         super.writeElementContent(writer);
      }
   }

   public Document extractContentAsDocument() throws SOAPException
   {
      log.trace("extractContentAsDocument");
      expandToDOM(false);

      Iterator childElements = DOMUtils.getChildElements(this);
      // zero child elements?
      if (!childElements.hasNext())
         throw new SOAPException("there is no child SOAPElement of this SOAPBody");

      SOAPElementImpl childElement = (SOAPElementImpl)childElements.next();

      // more than one child element?
      if (childElements.hasNext())
         throw new SOAPException("there is more than one child SOAPElement of this SOAPBody");

      if (childElement instanceof SOAPContentElement)
      {
         // cause expansion to DOM
         SOAPContentElement contentElement = (SOAPContentElement)childElement;
         // TODO change visibility of SOAPContentElement.expandToDOM() to package? 
         contentElement.getPayload();
      }

      // child SOAPElement is removed as part of this process
      childElement.detachNode();

      /* child element's owner document might be shared with other elements;
       * we have to create a separate document for returning to our caller
       */
      Document newDocument = DOMUtils.getDocumentBuilder().newDocument();
      Node adoptedElement = newDocument.adoptNode(childElement.domNode);
      newDocument.appendChild(adoptedElement);

      return newDocument;
   }
}
