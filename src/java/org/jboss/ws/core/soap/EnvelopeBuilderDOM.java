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

//$Id$

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.dom.DOMSource;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.core.jaxrpc.Style;
import org.jboss.ws.core.utils.DOMUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A SOAPEnvelope builder for JAXRPC based on DOM 
 * 
 * @author Heiko Braun, <heiko.braun@jboss.com>
 * @author Thomas.Diesler@jboss.com
 * @since 19-Apr-2006
 */
public class EnvelopeBuilderDOM implements EnvelopeBuilder
{
   // provide logging
   private static Logger log = Logger.getLogger(EnvelopeBuilderDOM.class);

   private Style style = Style.DOCUMENT;

   public EnvelopeBuilderDOM(Style style)
   {
      this.style = style;
   }

   public SOAPEnvelope build(SOAPMessage soapMessage, InputStream ins, boolean ignoreParseError) throws IOException, SOAPException
   {
      // Parse the XML input stream
      Element domEnv = null;
      try
      {
         domEnv = DOMUtils.parse(ins);
      }
      catch (IOException ex)
      {
         if (ignoreParseError)
         {
            return null;
         }
         throw ex;
      }

      return build(soapMessage, domEnv);
   }

   public SOAPEnvelope build(SOAPMessage soapMessage, Element domEnv) throws SOAPException
   {
      String envNS = domEnv.getNamespaceURI();
      String envPrefix = domEnv.getPrefix();

      // Construct the envelope
      SOAPFactoryImpl soapFactory = new SOAPFactoryImpl();
      SOAPPartImpl soapPart = (SOAPPartImpl)soapMessage.getSOAPPart();
      SOAPEnvelopeImpl soapEnv = new SOAPEnvelopeImpl(soapPart, soapFactory.createElement(domEnv, false));

      DOMUtils.copyAttributes(soapEnv, domEnv);

      // Add the header elements
      Element domHeader = DOMUtils.getFirstChildElement(domEnv, new QName(envNS, "Header"));
      if (domHeader != null)
      {
         SOAPHeader soapHeader = soapEnv.getHeader();

         DOMUtils.copyAttributes(soapHeader, domHeader);

         Iterator it = DOMUtils.getChildElements(domHeader);
         while (it.hasNext())
         {
            Element srcElement = (Element)it.next();
            //registerNamespacesLocally(srcElement);
            XMLFragment xmlFragment = new XMLFragment(new DOMSource(srcElement));

            Name name = new NameImpl(srcElement.getLocalName(), srcElement.getPrefix(), srcElement.getNamespaceURI());
            SOAPContentElement destElement = new SOAPHeaderElementImpl(name);
            soapHeader.addChildElement(destElement);

            DOMUtils.copyAttributes(destElement, srcElement);
            destElement.setXMLFragment(xmlFragment);
         }
      }

      // Add the body elements
      Element domBody = DOMUtils.getFirstChildElement(domEnv, new QName(envNS, "Body"));
      SOAPBody soapBody = soapEnv.getBody();

      DOMUtils.copyAttributes(soapBody, domBody);

      Iterator itBody = DOMUtils.getChildElements(domBody);
      if (itBody.hasNext())
      {
         Element domBodyElement = (Element)itBody.next();

         String localName = domBodyElement.getLocalName();
         String prefix = domBodyElement.getPrefix();
         String nsURI = domBodyElement.getNamespaceURI();
         Name beName = new NameImpl(localName, prefix, nsURI);

         // Process a <env:Fault> message
         if (beName.equals(new NameImpl("Fault", envPrefix, envNS)))
         {
            SOAPFaultImpl soapFault = new SOAPFaultImpl(envPrefix, envNS);
            soapBody.addChildElement(soapFault);

            DOMUtils.copyAttributes(soapFault, domBodyElement);

            Element domFaultCode = DOMUtils.getFirstChildElement(domBodyElement, new QName("faultcode"));
            if (domFaultCode == null)
               throw new SOAPException("SOAPFault does not contain a <faultcode> element");

            Element domFaultString = DOMUtils.getFirstChildElement(domBodyElement, new QName("faultstring"));
            if (domFaultString == null)
               throw new SOAPException("SOAPFault does not contain a <faultstring> element");

            String faultCode = DOMUtils.getTextContent(domFaultCode);
            soapFault.setFaultCode(faultCode);

            String faultString = DOMUtils.getTextContent(domFaultString);
            soapFault.setFaultString(faultString);

            Element domFaultActor = DOMUtils.getFirstChildElement(domBodyElement, new QName("faultactor"));
            if (domFaultActor != null)
            {
               String faultActor = DOMUtils.getTextContent(domFaultActor);
               soapFault.setFaultActor(faultActor);
            }

            // Add the fault detail
            Element domFaultDetail = DOMUtils.getFirstChildElement(domBodyElement, "detail");
            if (domFaultDetail != null)
            {
               Detail detail = soapFault.addDetail();
               Iterator it = DOMUtils.getChildElements(domFaultDetail);
               while (it.hasNext())
               {
                  Element domElement = (Element)it.next();
                  SOAPElement detailEntry = new DetailEntryImpl(soapFactory.createElement(domElement));
                  detailEntry = detail.addChildElement(detailEntry);
               }
            }
         }

         // Process and RPC or DOCUMENT style message
         else
         {

            if (style == Style.RPC)
            {
               SOAPBodyElementRpc soapBodyElement = new SOAPBodyElementRpc(beName);
               soapBodyElement = (SOAPBodyElementRpc)soapBody.addChildElement(soapBodyElement);

               DOMUtils.copyAttributes(soapBodyElement, domBodyElement);

               Iterator itBodyElement = DOMUtils.getChildElements(domBodyElement);
               while (itBodyElement.hasNext())
               {
                  Element srcElement = (Element)itBodyElement.next();
                  registerNamespacesLocally(srcElement);

                  Name name = new NameImpl(srcElement.getLocalName(), srcElement.getPrefix(), srcElement.getNamespaceURI());
                  SOAPContentElement destElement = new SOAPContentElement(name);
                  destElement = (SOAPContentElement)soapBodyElement.addChildElement(destElement);

                  DOMUtils.copyAttributes(destElement, srcElement);

                  XMLFragment xmlFragment = new XMLFragment(new DOMSource(srcElement));
                  destElement.setXMLFragment(xmlFragment);
               }
            }
            else if (style == Style.DOCUMENT)
            {
               Element srcElement = (Element)domBodyElement;
               registerNamespacesLocally(srcElement);
               SOAPContentElement destElement = new SOAPBodyElementDoc(beName);
               destElement = (SOAPContentElement)soapBody.addChildElement(destElement);

               DOMUtils.copyAttributes(destElement, srcElement);

               XMLFragment xmlFragment = new XMLFragment(new DOMSource(srcElement));
               destElement.setXMLFragment(xmlFragment);
            }
            else if (style == null)
            {
               SOAPBodyElementMessage soapBodyElement = new SOAPBodyElementMessage(beName);
               soapBodyElement = (SOAPBodyElementMessage)soapBody.addChildElement(soapBodyElement);

               DOMUtils.copyAttributes(soapBodyElement, domBodyElement);

               NodeList nlist = domBodyElement.getChildNodes();
               for (int i = 0; i < nlist.getLength(); i++)
               {
                  org.w3c.dom.Node child = nlist.item(i);
                  short childType = child.getNodeType();
                  if (childType == org.w3c.dom.Node.ELEMENT_NODE)
                  {
                     SOAPElement soapElement = soapFactory.createElement((Element)child);
                     soapBodyElement.addChildElement(soapElement);
                  }
                  else if (childType == org.w3c.dom.Node.TEXT_NODE)
                  {
                     String nodeValue = child.getNodeValue();
                     soapBodyElement.addTextNode(nodeValue);
                  }
                  else if (childType == org.w3c.dom.Node.CDATA_SECTION_NODE)
                  {
                     String nodeValue = child.getNodeValue();
                     soapBodyElement.addTextNode(nodeValue);
                  }
                  else
                  {
                     log.warn("Ignore child type: " + childType);
                  }
               }
            }
            else
            {
               throw new WSException("Unsupported message style: " + style);
            }
         }
      }

      return soapEnv;
   }

   /**
    * Register globally available namespaces on element level.
    * This is necessary to ensure that each xml fragment is valid.    
    */
   private void registerNamespacesLocally(Element srcElement)
   {
      if (srcElement.getPrefix() == null)
      {
         srcElement.setAttribute("xmlns", srcElement.getNamespaceURI());
      }
      else
      {
         srcElement.setAttribute("xmlns:" + srcElement.getPrefix(), srcElement.getNamespaceURI());
      }
   }
}
