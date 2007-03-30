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
package org.jboss.ws.core.jaxws.handler;

// $Id$

import java.util.Iterator;

import javax.xml.bind.JAXBContext;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.LogicalMessage;
import javax.xml.ws.WebServiceException;

import org.jboss.logging.Logger;
import org.jboss.ws.core.jaxrpc.Style;
import org.jboss.ws.core.jaxrpc.binding.BufferedStreamSource;
import org.jboss.ws.core.soap.EnvelopeBuilderDOM;
import org.jboss.ws.core.soap.SOAPBodyImpl;
import org.jboss.ws.core.soap.SOAPContentElement;
import org.jboss.ws.core.soap.XMLFragment;
import org.w3c.dom.Element;

/**
 * The LogicalMessageContext interface extends MessageContext to provide access to a the 
 * contained message as a protocol neutral LogicalMessage.
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 31-Aug-2006
 */
public class LogicalMessageImpl implements LogicalMessage
{
   // provide logging
   private static final Logger log = Logger.getLogger(LogicalMessageImpl.class);

   private Style style;
   private SOAPBodyImpl soapBody;
   private boolean setPayloadBodyChild;

   public LogicalMessageImpl(SOAPMessage soapMessage, Style style)
   {
      this.style = style;
      try
      {
         soapBody = (SOAPBodyImpl)soapMessage.getSOAPBody();
      }
      catch (SOAPException ex)
      {
         throw new WebServiceException("Cannot obtain xml payload", ex);
      }
   }

   public Source getPayload()
   {
      Source source = soapBody.getSource();
      setPayloadBodyChild = false;
      if (source == null)
      {
         SOAPElement soapElement = (SOAPElement)soapBody.getChildElements().next();
         if (style == Style.RPC)
         {
            source = new DOMSource(soapElement);
         }
         else
         {
            SOAPContentElement contentElement = (SOAPContentElement)soapElement;
            source = contentElement.getPayload();
         }
         setPayloadBodyChild = true;
      }
      return source;
   }

   public void setPayload(Source source)
   {
      if (setPayloadBodyChild)
      {
         try
         {
            SOAPElement soapElement = (SOAPElement)soapBody.getChildElements().next();
            if (style == Style.RPC)
            {
               EnvelopeBuilderDOM builder = new EnvelopeBuilderDOM(style);
               Element domBodyElement = EnvelopeBuilderDOM.getElementFromSource(source);
               builder.buildBodyElementRpc(soapBody, domBodyElement);
            }
            else
            {
               SOAPContentElement contentElement = (SOAPContentElement)soapElement;
               contentElement.setXMLFragment(new XMLFragment(source));
            }
         }
         catch (SOAPException ex)
         {
            throw new WebServiceException("Cannot set xml payload", ex);
         }
      }
      else
      {
         soapBody.setSource(source);
      }
      
      // The body payload has been modified 
      soapBody.setModifiedFromSource(true);
   }

   public Object getPayload(JAXBContext jaxbContext)
   {
      Object payload = null;
      Iterator it = soapBody.getChildElements();
      if (it.hasNext())
      {
         SOAPContentElement bodyElement = (SOAPContentElement)it.next();
         payload = bodyElement.getObjectValue();
      }
      return payload;
   }

   public void setPayload(Object payload, JAXBContext jaxbContext)
   {
      Iterator it = soapBody.getChildElements();
      if (it.hasNext())
      {
         SOAPContentElement bodyElement = (SOAPContentElement)it.next();
         bodyElement.setObjectValue(payload);
      }
   }
}
