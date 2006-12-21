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

import java.io.StringReader;
import java.util.Iterator;

import javax.xml.bind.JAXBContext;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.LogicalMessage;
import javax.xml.ws.WebServiceException;

import org.jboss.ws.core.soap.SOAPBodyImpl;
import org.jboss.ws.core.soap.SOAPContentElement;

/**
 * The LogicalMessageContext interface extends MessageContext to provide access to a the 
 * contained message as a protocol neutral LogicalMessage.
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 31-Aug-2006
 */
public class LogicalMessageImpl implements LogicalMessage
{
   private SOAPBodyImpl soapBody;

   public LogicalMessageImpl(SOAPMessage soapMessage)
   {
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
      Source source = soapBody.getPayload();
      if (source == null)
      {
         SOAPContentElement soapElement = (SOAPContentElement)soapBody.getChildElements().next();
         if (soapElement.isDOMValid())
         {
            source = new DOMSource(soapElement);
         }
         else
         {
            String xmlPayload = soapElement.getXMLFragment();
            source = new StreamSource(new StringReader(xmlPayload));
         }
      }
      return source;
   }

   public void setPayload(Source source)
   {
      soapBody.setPayload(source);
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
