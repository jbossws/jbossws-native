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

import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.Text;

import org.jboss.ws.core.soap.BundleUtils;
import org.jboss.ws.common.Constants;
import org.jboss.ws.common.DOMUtils;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * An object that represents the contents of the SOAP body element in a SOAP message.
 * A SOAP body element consists of XML data that affects the way the application-specific content is processed.
 *
 * A SOAPBody object contains SOAPBodyElement objects, which have the content for the SOAP body.
 * A SOAPFault object, which carries status and/or error information, is an example of a SOAPBodyElement object.
 *
 * @author Thomas.Diesler@jboss.org
 * @author <a href="jason.greene@jboss.com">Jason T. Greene</a>
 */
public class SOAPBodyImpl extends SOAPElementImpl implements SOAPBody
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(SOAPBodyImpl.class);
   public SOAPBodyImpl(String prefix, String namespace)
   {
      super("Body", prefix, namespace);
   }

   /** Convert the child into a SOAPBodyElement */
   public SOAPElement addChildElement(SOAPElement child) throws SOAPException
   {
      if (!(child instanceof SOAPBodyElement))
      {
         child = isFault(child) ? convertToSOAPFault(child) : convertToBodyElement(child);
      }

      child = super.addChildElement(child);
      return child;
   }

   private boolean isFault(Node node)
   {
      return "Fault".equals(node.getLocalName()) && getNamespaceURI().equals(node.getNamespaceURI());
   }

   private SOAPElement convertToSOAPFault(Node node)
   {
      if (!(node instanceof SOAPElementImpl))
         throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "SOAPELEMENTIMPL_EXPECTED"));

      SOAPElementImpl element = (SOAPElementImpl) node;
      element.detachNode();
      return new SOAPFaultImpl(element);
   }
   
   public SOAPBodyElement addBodyElement(Name name) throws SOAPException
   {
      SOAPBodyElement child = new SOAPBodyElementDoc(name);
      return (SOAPBodyElement)addChildElement(child);
   }

   public SOAPBodyElement addBodyElement(QName qname) throws SOAPException
   {
      SOAPBodyElement child = new SOAPBodyElementDoc(qname);
      return (SOAPBodyElement)addChildElement(child);
   }

   public SOAPBodyElement addDocument(Document doc) throws SOAPException
   {
      Element rootElement = doc.getDocumentElement();
      SOAPFactoryImpl soapFactory = new SOAPFactoryImpl();
      SOAPElement soapElement = soapFactory.createElement(rootElement);
      return (SOAPBodyElement)addChildElement(soapElement);
   }

   public SOAPFault addFault() throws SOAPException
   {
      if (hasFault())
         throw new SOAPException(BundleUtils.getMessage(bundle, "AT_MOST_ONE_SOAPFAULT"));

      SOAPFaultImpl soapFault = new SOAPFaultImpl(getPrefix(), getNamespaceURI());
      soapFault = (SOAPFaultImpl)addChildElement(soapFault);
      soapFault.setFaultCode(soapFault.getDefaultFaultCode());
      return soapFault;
   }

   public SOAPFault addFault(Name faultCode, String faultString) throws SOAPException
   {
      if (hasFault())
         throw new SOAPException(BundleUtils.getMessage(bundle, "AT_MOST_ONE_SOAPFAULT"));

      SOAPFaultImpl soapFault = new SOAPFaultImpl(getPrefix(), getNamespaceURI());
      soapFault = (SOAPFaultImpl)addChildElement(soapFault);
      soapFault.setFaultCode(faultCode);
      soapFault.setFaultString(faultString);
      return soapFault;
   }

   public SOAPFault addFault(QName faultCode, String faultString) throws SOAPException
   {
      if (hasFault())
         throw new SOAPException(BundleUtils.getMessage(bundle, "AT_MOST_ONE_SOAPFAULT"));

      SOAPFaultImpl soapFault = new SOAPFaultImpl(getPrefix(), getNamespaceURI());
      soapFault = (SOAPFaultImpl)addChildElement(soapFault);
      soapFault.setFaultCode(faultCode);
      soapFault.setFaultString(faultString);
      return soapFault;
   }

   public SOAPFault addFault(Name faultCode, String faultString, Locale locale) throws SOAPException
   {
      if (hasFault())
         throw new SOAPException(BundleUtils.getMessage(bundle, "AT_MOST_ONE_SOAPFAULT"));

      SOAPFaultImpl soapFault = new SOAPFaultImpl(getPrefix(), getNamespaceURI());
      soapFault.setFaultCode(faultCode);
      soapFault.setFaultString(faultString, locale);
      addChildElement(soapFault);
      return soapFault;
   }

   public SOAPFault addFault(QName faultCode, String faultString, Locale locale) throws SOAPException
   {
      if (hasFault())
         throw new SOAPException(BundleUtils.getMessage(bundle, "AT_MOST_ONE_SOAPFAULT"));

      SOAPFaultImpl soapFault = new SOAPFaultImpl(getPrefix(), getNamespaceURI());
      soapFault.setFaultCode(faultCode);
      soapFault.setFaultString(faultString, locale);
      addChildElement(soapFault);
      return soapFault;
   }

   public SOAPFault getFault()
   {
      Iterator it = faultIterator();
      SOAPFault soapFault = it.hasNext() ? (SOAPFault)it.next() : null;
      return soapFault;
   }

   public boolean hasFault()
   {
      return faultIterator().hasNext();
   }

   private Iterator faultIterator()
   {
      return getChildElements(new QName(getNamespaceURI(), "Fault"));
   }

   public SOAPBodyElement getBodyElement()
   {
      SOAPBodyElement bodyElement = null;
      Iterator it = getChildElements();
      while (bodyElement == null && it.hasNext())
      {
         Object next = it.next();
         if (next instanceof SOAPBodyElement)
            bodyElement = (SOAPBodyElement)next;
      }
      return bodyElement;
   }

   public Node appendChild(Node newChild) throws DOMException
   {
      if (needsConversionToBodyElement(newChild))
         newChild = isFault(newChild) ? convertToSOAPFault(newChild) : convertToBodyElement(newChild);

      return super.appendChild(newChild);
   }

   public Node insertBefore(Node newChild, Node refChild) throws DOMException
   {
      if (needsConversionToBodyElement(newChild))
         newChild = isFault(newChild) ? convertToSOAPFault(newChild) : convertToBodyElement(newChild);

      return super.insertBefore(newChild, refChild);
   }

   public Node replaceChild(Node newChild, Node oldChild) throws DOMException
   {
      if (needsConversionToBodyElement(newChild))
         newChild = isFault(newChild) ? convertToSOAPFault(newChild) : convertToBodyElement(newChild);

      return super.replaceChild(newChild, oldChild);
   }

   @Override
   public SOAPElement addAttribute(Name name, String value) throws SOAPException
   {
      String envNamespace = getNamespaceURI();
      if (Constants.NS_SOAP12_ENV.equals(envNamespace) && name.equals(new NameImpl("encodingStyle", Constants.PREFIX_ENV, envNamespace)))
         throw new SOAPException(BundleUtils.getMessage(bundle, "CANNOT_SET_ENCODINGSTYLE_ON",  getElementQName()));

      return super.addAttribute(name, value);
   }

   public Document extractContentAsDocument() throws SOAPException
   {
      Iterator childElements = getChildElements();

      SOAPElementImpl childElement = null;

      while (childElements.hasNext() == true)
      {
         Object current = childElements.next();
         if (current instanceof SOAPElementImpl)
         {
            childElement = (SOAPElementImpl)current;
            break;
         }
      }

      // zero child elements?
      if (childElement == null)
         throw new SOAPException(BundleUtils.getMessage(bundle, "CANNOT_FIND_SOAPBODYELEMENT"));

      // more than one child element?
      while (childElements.hasNext() == true)
      {
         Object current = childElements.next();
         if (current instanceof SOAPElementImpl)
            throw new SOAPException(BundleUtils.getMessage(bundle, "MULTIPLE_SOAPBODYELEMENT"));
      }

      if (childElement instanceof SOAPContentElement)
      {
         // cause expansion to DOM
         SOAPContentElement contentElement = (SOAPContentElement)childElement;
         // TODO change visibility of SOAPContentElement.expandToDOM() to package? 
         contentElement.hasChildNodes();
      }

      // child SOAPElement is removed as part of this process
      childElement.detachNode();

      // child element's owner document might be shared with other elements;
      // we have to create a separate document for returning to our caller
      Document newDocument = DOMUtils.getDocumentBuilder().newDocument();
      Node adoptedElement = newDocument.adoptNode(childElement.domNode);
      newDocument.appendChild(adoptedElement);

      return newDocument;
   }

   private static boolean needsConversionToBodyElement(Node newChild)
   {
      // JBCTS-440 #addTextNodeTest1 appends a Text node to a SOAPBody
      boolean validChild = newChild instanceof SOAPBodyElement;
      validChild = validChild || newChild instanceof DocumentFragment;
      validChild = validChild || newChild instanceof Text;
      validChild = validChild || newChild instanceof Comment;
      return validChild == false;
   }

   private static SOAPBodyElementDoc convertToBodyElement(Node node) throws DOMException
   {
      if (!(node instanceof SOAPElementImpl) && (node instanceof Element))
      {
         try
         {
            SOAPFactoryImpl soapFactory = new SOAPFactoryImpl();
            node = (SOAPElementImpl)soapFactory.createElement((Element)node);
         }
         catch (SOAPException ex)
         {
            throw new DOMException(DOMException.INVALID_STATE_ERR, "Could not convert Element to a SOAPElement");
         }
      }
      
      if (!(node instanceof SOAPElementImpl))
         throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "SOAPELEMENT_EXPECTED"));

      SOAPElementImpl element = (SOAPElementImpl)node;
      element.detachNode();
      return new SOAPBodyElementDoc(element);
   }

}
