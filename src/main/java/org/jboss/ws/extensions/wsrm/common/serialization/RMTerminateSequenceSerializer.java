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
package org.jboss.ws.extensions.wsrm.common.serialization;

import static org.jboss.ws.extensions.wsrm.common.serialization.RMSerializationHelper.stringToLong;
import static org.jboss.ws.extensions.wsrm.common.serialization.RMSerializationHelper.getOptionalElement;
import static org.jboss.ws.extensions.wsrm.common.serialization.RMSerializationHelper.getRequiredElement;
import static org.jboss.ws.extensions.wsrm.common.serialization.RMSerializationHelper.getRequiredTextContent;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.jboss.ws.extensions.wsrm.api.RMException;
import org.jboss.ws.extensions.wsrm.spi.RMConstants;
import org.jboss.ws.extensions.wsrm.spi.RMProvider;
import org.jboss.ws.extensions.wsrm.spi.protocol.RMSerializable;
import org.jboss.ws.extensions.wsrm.spi.protocol.RMTerminateSequence;

/**
 * <b>TerminateSequence</b> object de/serializer
 * @author richard.opalka@jboss.com
 */
final class RMTerminateSequenceSerializer implements RMSerializer
{

   private static final RMSerializer INSTANCE = new RMTerminateSequenceSerializer();
   
   private RMTerminateSequenceSerializer()
   {
      // hide constructor
   }
   
   static RMSerializer getInstance()
   {
      return INSTANCE;
   }
   
   /**
    * Deserialize <b>TerminateSequence</b> using <b>provider</b> from the <b>soapMessage</b>
    * @param object to be deserialized
    * @param provider wsrm provider to be used for deserialization process
    * @param soapMessage soap message from which object will be deserialized
    */
   public final void deserialize(RMSerializable object, RMProvider provider, SOAPMessage soapMessage)
   throws RMException
   {
      RMTerminateSequence o = (RMTerminateSequence)object;
      try
      {
         SOAPBody soapBody = soapMessage.getSOAPPart().getEnvelope().getBody();
         RMConstants wsrmConstants = provider.getConstants();
         
         // read required wsrm:TerminateSequence element
         QName terminateSequenceQName = wsrmConstants.getTerminateSequenceQName();
         SOAPElement terminateSequenceElement = getRequiredElement(soapBody, terminateSequenceQName, "soap body");

         // read required wsrm:Identifier element
         QName identifierQName = wsrmConstants.getIdentifierQName();
         SOAPElement identifierElement = getRequiredElement(terminateSequenceElement, identifierQName, terminateSequenceQName);
         String identifier = getRequiredTextContent(identifierElement, identifierQName);
         o.setIdentifier(identifier);
         
         // read optional wsrm:LastMsgNumber element
         QName lastMsgNumberQName = wsrmConstants.getLastMsgNumberQName();
         SOAPElement lastMsgNumberElement = getOptionalElement(terminateSequenceElement, lastMsgNumberQName, terminateSequenceQName);
         if (lastMsgNumberElement != null)
         {
            String lastMsgNumberString = getRequiredTextContent(lastMsgNumberElement, lastMsgNumberQName);
            long lastMsgNumberValue = stringToLong(lastMsgNumberString, "Unable to parse LastMsgNumber element text content");
            o.setLastMsgNumber(lastMsgNumberValue);
         }
      }
      catch (SOAPException se)
      {
         throw new RMException("Unable to deserialize RM message", se);
      }
      catch (RuntimeException re)
      {
         throw new RMException("Unable to deserialize RM message", re);
      }
   }

   /**
    * Serialize <b>TerminateSequence</b> using <b>provider</b> to the <b>soapMessage</b>
    * @param object to be serialized
    * @param provider wsrm provider to be used for serialization process
    * @param soapMessage soap message to which object will be serialized
    */
   public final void serialize(RMSerializable object, RMProvider provider, SOAPMessage soapMessage)
   throws RMException
   {
      RMTerminateSequence o = (RMTerminateSequence)object;
      try
      {
         SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart().getEnvelope();
         RMConstants wsrmConstants = provider.getConstants();
         
         // Add xmlns:wsrm declaration
         soapEnvelope.addNamespaceDeclaration(wsrmConstants.getPrefix(), wsrmConstants.getNamespaceURI());

         // write required wsrm:TerminateSequence element
         QName terminateSequenceQName = wsrmConstants.getTerminateSequenceQName(); 
         SOAPElement terminateSequenceElement = soapEnvelope.getBody().addChildElement(terminateSequenceQName);

         // write required wsrm:Identifier element
         QName identifierQName = wsrmConstants.getIdentifierQName();
         terminateSequenceElement.addChildElement(identifierQName).setValue(o.getIdentifier());
         
         if (o.getLastMsgNumber() != 0)
         {
            // write optional wsrm:LastMsgNumber element
            QName lastMsgNumberQName = wsrmConstants.getLastMsgNumberQName();
            SOAPElement lastMsgNumberElement = terminateSequenceElement.addChildElement(lastMsgNumberQName);
            lastMsgNumberElement.setValue(String.valueOf(o.getLastMsgNumber()));
         }
      }
      catch (SOAPException se)
      {
         throw new RMException("Unable to serialize RM message", se);
      }
   }

}
