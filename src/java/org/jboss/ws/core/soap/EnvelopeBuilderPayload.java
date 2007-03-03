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
import javax.xml.soap.Name;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.dom.DOMSource;

import org.jboss.ws.core.utils.DOMUtils;
import org.jboss.ws.core.utils.DOMWriter;
import org.w3c.dom.Element;

/**
 * A SOAPEnvelope builder for JAXWS in service mode PAYLOAD 
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 30-Jun-2006
 */
public class EnvelopeBuilderPayload implements EnvelopeBuilder
{
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

      String envNS = domEnv.getNamespaceURI();

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
            XMLFragment xmlFragment = new XMLFragment( new DOMSource(srcElement) );

            Name name = new NameImpl(srcElement.getLocalName(), srcElement.getPrefix(), srcElement.getNamespaceURI());
            SOAPContentElement destElement = new SOAPHeaderElementImpl(name);
            soapHeader.addChildElement(destElement);

            DOMUtils.copyAttributes(destElement, srcElement);
            destElement.setXMLFragment(xmlFragment);
         }
      }

      // Add the body elements
      Element domBody = DOMUtils.getFirstChildElement(domEnv, new QName(envNS, "Body"));
      SOAPBodyImpl soapBody = (SOAPBodyImpl)soapEnv.getBody();

      DOMUtils.copyAttributes(soapBody, domBody);

      Iterator itBody = DOMUtils.getChildElements(domBody);
      if (itBody.hasNext())
      {
         Element domBodyElement = (Element)itBody.next();
         soapBody.setPayload(new DOMSource(domBodyElement));
      }
      
      return soapEnv;
   }
}
