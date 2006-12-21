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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
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
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.jboss.logging.Logger;
import org.jboss.util.NotImplementedException;
import org.jboss.ws.Constants;
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
import org.xml.sax.InputSource;

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
      log.debug("setPayload: " + payload.getClass().getName());
      removeContents();
      this.payload = payload;
      this.isDOMValid = false;
   }

   /** Convert the child into a SOAPBodyElement */
   public SOAPElement addChildElement(SOAPElement child) throws SOAPException
   {
      log.trace("addChildElement: " + child.getElementName());
      expandToDOM();
      if ((child instanceof SOAPBodyElement) == false)
         child = convertToBodyElement(child);

      child = super.addChildElement(child);
      return child;
   }

   public SOAPBodyElement addBodyElement(Name name) throws SOAPException
   {
      log.trace("addBodyElement: " + name);
      expandToDOM();
      SOAPBodyElement child = new SOAPBodyElementDoc(name);
      return (SOAPBodyElement)addChildElement(child);
   }

   public SOAPBodyElement addBodyElement(QName qname) throws SOAPException
   {
      log.trace("addBodyElement: " + qname);
      expandToDOM();
      SOAPBodyElement child = new SOAPBodyElementDoc(qname);
      return (SOAPBodyElement)addChildElement(child);
   }

   public SOAPBodyElement addDocument(Document doc) throws SOAPException
   {
      log.trace("addDocument");
      expandToDOM();
      Element rootElement = doc.getDocumentElement();
      SOAPFactoryImpl soapFactory = new SOAPFactoryImpl();
      SOAPElement soapElement = soapFactory.createElement(rootElement);
      return (SOAPBodyElement)addChildElement(soapElement);
   }

   public SOAPFault addFault() throws SOAPException
   {
      log.trace("addFault");
      expandToDOM();
      if (hasFault())
         throw new SOAPException("A SOAPBody may contain at most one SOAPFault child element");

      return addFault(new NameImpl(Constants.SOAP11_FAULT_CODE_SERVER), "Generic server fault");
   }

   public SOAPFault addFault(Name faultCode, String faultString) throws SOAPException
   {
      log.trace("addFault");
      expandToDOM();
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
      expandToDOM();
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
      expandToDOM();
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
      expandToDOM();
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
      expandToDOM();
      Iterator it = getChildElements(new NameImpl("Fault", Constants.PREFIX_ENV, getSOAPEnvelope().getNamespaceURI()));
      return (it.hasNext() ? (SOAPFault)it.next() : null);
   }

   public boolean hasFault()
   {
      log.trace("hasFault");
      expandToDOM();
      return getChildElements(Constants.SOAP11_FAULT).hasNext();
   }

   public Node appendChild(Node newChild) throws DOMException
   {
      log.trace("appendChild: " + newChild.getNodeName());
      expandToDOM();
      Node retNode;
      if (!(newChild instanceof SOAPBodyElement || newChild instanceof DocumentFragment))
      {
         newChild = convertToBodyElement(newChild);
      }
      retNode = super.appendChild(newChild);
      return retNode;
   }

   public Node insertBefore(Node newChild, Node refChild) throws DOMException
   {
      log.trace("insertBefore: " + newChild.getNodeName());
      expandToDOM();
      if (!(newChild instanceof SOAPBodyElement || newChild instanceof DocumentFragment))
         newChild = convertToBodyElement(newChild);

      return super.insertBefore(newChild, refChild);
   }

   public Node replaceChild(Node newChild, Node oldChild) throws DOMException
   {
      log.trace("replaceChild: " + newChild.getNodeName());
      expandToDOM();
      if (!(newChild instanceof SOAPBodyElement || newChild instanceof DocumentFragment))
      {
         newChild = convertToBodyElement(newChild);
      }

      return super.replaceChild(newChild, oldChild);
   }

   public Iterator getChildElements()
   {
      log.trace("getChildElements");
      expandToDOM();
      return super.getChildElements();
   }

   public Iterator getChildElements(Name name)
   {
      log.trace("getChildElements: " + name);
      expandToDOM();
      return super.getChildElements(name);
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

   public boolean hasChildNodes()
   {
      log.trace("hasChildNodes");
      expandToDOM();
      return super.hasChildNodes();
   }

   private SOAPBodyElementDoc convertToBodyElement(Node node)
   {
      if (!(node instanceof SOAPElementImpl))
         throw new IllegalArgumentException("SOAPElement expected");

      SOAPElementImpl element = (SOAPElementImpl)node;
      element.detachNode();
      return new SOAPBodyElementDoc(element);
   }

   private void expandToDOM()
   {
      if (isDOMValid == false)
      {
         // DOM expansion should only happen when a handler accesses the DOM API.
         // We do not allow DOM expansion on a dev release.
         CommonMessageContext msgContext = MessageContextAssociation.peekMessageContext();
         if (msgContext != null && UnifiedMetaData.isFinalRelease() == false)
         {
            Boolean allowExpand = (Boolean)msgContext.getProperty(CommonMessageContext.ALLOW_EXPAND_TO_DOM);
            if (Boolean.TRUE.equals(allowExpand) == false)
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
      Element child = null;
      try
      {
         if (payload instanceof StreamSource)
         {
            StreamSource streamSource = (StreamSource)payload;

            InputStream ins = streamSource.getInputStream();
            if (ins != null)
            {
               child = DOMUtils.parse(ins);
            }
            else
            {
               Reader reader = streamSource.getReader();
               child = DOMUtils.parse(new InputSource(reader));
            }

            // reset the excausted input stream  
            String xmlStr = DOMWriter.printNode(child, false);
            payload = new StreamSource(new ByteArrayInputStream(xmlStr.getBytes()));
         }
         else if (payload instanceof DOMSource)
         {
            DOMSource domSource = (DOMSource)payload;
            Node node = domSource.getNode();
            if (node instanceof Element)
            {
               child = (Element)node;
            }
            else if (node instanceof Document)
            {
               child = ((Document)node).getDocumentElement();
            }
            else
            {
               throw new WSException("Unsupported Node type: " + node.getClass().getName());
            }
         }
         else if (payload instanceof SAXSource)
         {
            // The fact that JAXBSource derives from SAXSource is an implementation detail. 
            // Thus in general applications are strongly discouraged from accessing methods defined on SAXSource. 
            // The XMLReader object obtained by the getXMLReader method shall be used only for parsing the InputSource object returned by the getInputSource method.

            TransformerFactory tf = TransformerFactory.newInstance();
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            tf.newTransformer().transform(payload, new StreamResult(baos));

            child = DOMUtils.parse(new ByteArrayInputStream(baos.toByteArray()));
         }
         else
         {
            throw new WSException("Source type not implemented: " + payload.getClass().getName());
         }
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         throw new WSException("Cannot get root element from Source" + ex);
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
      //TODO: SAAJ 1.3
      throw new NotImplementedException();
   }
}
