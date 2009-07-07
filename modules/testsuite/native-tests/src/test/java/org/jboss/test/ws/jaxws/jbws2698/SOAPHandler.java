/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws2698;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.ws.WSException;
import org.jboss.wsf.common.handler.GenericSOAPHandler;

/**
 * Handler implementation.
 * 
 * @author darran.lofthouse@jboss.com
 * @since 7th July 2009
 */
public class SOAPHandler extends GenericSOAPHandler
{
   private static final String PREFIX = "JBWS2698";

   protected boolean handleOutbound(MessageContext msgContext)
   {
      try
      {
         SOAPMessage message = ((SOAPMessageContext)msgContext).getMessage();
         SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
         SOAPHeader header = message.getSOAPHeader();
         SOAPBody body = message.getSOAPBody();

         envelope.addNamespaceDeclaration(PREFIX, SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE);
         envelope.setPrefix(PREFIX);
         checkPrefix(envelope);
         header.setPrefix(PREFIX);
         checkPrefix(header);
         body.setPrefix(PREFIX);
         checkPrefix(body);

         message.saveChanges();
      }
      catch (SOAPException se)
      {
         throw new WSException(se);
      }
      return true;
   }

   protected boolean handleInbound(MessageContext msgContext)
   {
      try
      {
         SOAPMessage message = ((SOAPMessageContext)msgContext).getMessage();

         SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
         checkPrefix(envelope);
         SOAPHeader header = message.getSOAPHeader();
         checkPrefix(header);
         SOAPBody body = message.getSOAPBody();
         checkPrefix(body);
      }
      catch (SOAPException se)
      {
         throw new WSException(se);
      }

      return true;
   }

   private void checkPrefix(final SOAPElement element)
   {
      String prefix = element.getPrefix();
      if (PREFIX.equals(prefix) == false)
      {
         throw new WSException("Expected prefix '" + PREFIX + "' found prefix '" + prefix + "' for element '" + element.getElementName());
      }

   }
}
