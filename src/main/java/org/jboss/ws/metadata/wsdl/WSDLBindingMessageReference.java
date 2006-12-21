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
package org.jboss.ws.metadata.wsdl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A Binding Message Reference component describes a concrete binding of a
 * particular message participating in an operation to a particular concrete
 * message format.
 *
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @version $Revision$
 */
public abstract class WSDLBindingMessageReference
{
   // The parent WSDL binding operation
   private WSDLBindingOperation wsdlBindingOperation;
   private NCName messageLabel;
   private List<WSDLSOAPHeader> soapHeaders = new ArrayList<WSDLSOAPHeader>();
   private List<WSDLMIMEPart> mimeParts = new ArrayList<WSDLMIMEPart>();

   public WSDLBindingMessageReference(WSDLBindingOperation wsdlBindingOperation)
   {
      this.wsdlBindingOperation = wsdlBindingOperation;
   }

   public WSDLBindingOperation getWsdlBindingOperation()
   {
      return wsdlBindingOperation;
   }

   /**
    * Gets the property that identifies the role that the message for which
    * binding details are being specified. The role is part of the {message
    * exchange pattern} of the Interface Operation component being bound by the
    * containing Binding Operation component.
    */
   public NCName getMessageLabel()
   {
      return messageLabel;
   }

   /**
    * Sets the property that identifies the role that the message for which
    * binding details are being specified. The role is part of the {message
    * exchange pattern} of the Interface Operation component being bound by the
    * containing Binding Operation component.
    */
   public void setMessageLabel(NCName messageLabel)
   {
      this.messageLabel = messageLabel;
   }

   /**
    * Gets the list of SOAP headers associated with this message reference.
    *
    * @return a list of soap headers
    */
   public List<WSDLSOAPHeader> getSoapHeaders()
   {
      return soapHeaders;
   }

   /**
    * Sets the list of SOAP headers associated with this message reference.
    *
    * @param soapHeaders The soapHeaders to set.
    */
   public void setSoapHeaders(List<WSDLSOAPHeader> soapHeaders)
   {
      this.soapHeaders = soapHeaders;
   }

   /**
    * Adds a SOAP header to the SOAP header list that is associated with this
    * message reference.
    *
    * @param soapHeader the SOAP header to add
    */
   public void addSoapHeader(WSDLSOAPHeader soapHeader)
   {
      this.soapHeaders.add(soapHeader);
   }

   /**
    * Adds a MIME part to this message reference. This is only used for WSDL 1.1.
    *
    * @param mimePart the mime part to add
    */
   public void addMimePart(WSDLMIMEPart mimePart)
   {
      this.mimeParts.add(mimePart);
   }

   /**
    * Returns a list of mime parts on this message
    *
    * @return
    */
   public Collection<WSDLMIMEPart> getMimeParts()
   {
      return mimeParts;
   }
}