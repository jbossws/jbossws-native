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

import static org.jboss.ws.extensions.wsrm.common.serialization.SerializationHelper.getOptionalElement;
import static org.jboss.ws.extensions.wsrm.common.serialization.SerializationHelper.getRequiredElement;
import static org.jboss.ws.extensions.wsrm.common.serialization.SerializationHelper.getRequiredTextContent;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.addressing.AddressingBuilder;
import javax.xml.ws.addressing.AddressingConstants;

import org.jboss.ws.extensions.wsrm.client_api.RMException;
import org.jboss.ws.extensions.wsrm.spi.Constants;
import org.jboss.ws.extensions.wsrm.spi.Provider;
import org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequence;
import org.jboss.ws.extensions.wsrm.spi.protocol.IncompleteSequenceBehavior;
import org.jboss.ws.extensions.wsrm.spi.protocol.Serializable;

/**
 * <b>CreateSequence</b> object de/serializer
 * @author richard.opalka@jboss.com
 */
final class CreateSequenceSerializer implements Serializer
{

   private static final AddressingConstants ADDRESSING_CONSTANTS = 
      AddressingBuilder.getAddressingBuilder().newAddressingConstants();
   
   private static final Serializer INSTANCE = new CreateSequenceSerializer();
   
   private CreateSequenceSerializer()
   {
      // hide constructor
   }
   
   static Serializer getInstance()
   {
      return INSTANCE;
   }
   
   /**
    * Deserialize <b>CreateSequence</b> using <b>provider</b> from the <b>soapMessage</b>
    * @param object to be deserialized
    * @param provider wsrm provider to be used for deserialization process
    * @param soapMessage soap message from which object will be deserialized
    */
   public final void deserialize(Serializable object, Provider provider, SOAPMessage soapMessage)
   throws RMException
   {
      CreateSequence o = (CreateSequence)object;
      try
      {
         SOAPBody soapBody = soapMessage.getSOAPPart().getEnvelope().getBody();
         Constants wsrmConstants = provider.getConstants();
         
         // read required wsrm:CreateSequence element
         QName createSequenceQName = wsrmConstants.getCreateSequenceQName();
         SOAPElement createSequenceElement = getRequiredElement(soapBody, createSequenceQName, "soap body");

         // read required wsrm:AcksTo element
         QName acksToQName = wsrmConstants.getAcksToQName();
         SOAPElement acksToElement = getRequiredElement(createSequenceElement, acksToQName, createSequenceQName);
         QName addressQName = ADDRESSING_CONSTANTS.getAddressQName();
         SOAPElement acksToAddressElement = getRequiredElement(acksToElement, addressQName, acksToQName);
         String acksToAddress = getRequiredTextContent(acksToAddressElement, addressQName);
         o.setAcksTo(acksToAddress);

         // read optional wsrm:Expires element
         QName expiresQName = wsrmConstants.getExpiresQName();
         SOAPElement expiresElement = getOptionalElement(createSequenceElement, expiresQName, createSequenceQName);
         if (expiresElement != null)
         {
            String duration = getRequiredTextContent(expiresElement, expiresQName);
            o.setExpires(duration);
         }

         // read optional wsrm:Offer element
         QName offerQName = wsrmConstants.getOfferQName();
         SOAPElement offerElement = getOptionalElement(createSequenceElement, offerQName, createSequenceQName);
         if (offerElement != null)
         {
            CreateSequence.Offer offer = o.newOffer();

            // read required wsrm:Identifier element
            QName identifierQName = wsrmConstants.getIdentifierQName();
            SOAPElement identifierElement = getRequiredElement(offerElement, identifierQName, offerQName);
            String identifier = getRequiredTextContent(identifierElement, identifierQName);
            offer.setIdentifier(identifier);
            
            // read optional wsrm:Endpoint element
            QName endpointQName = wsrmConstants.getEndpointQName();
            SOAPElement endpointElement = getOptionalElement(offerElement, endpointQName, offerQName);
            if (endpointElement != null)
            {
               SOAPElement endpointAddressElement = getRequiredElement(endpointElement, addressQName, endpointQName);
               String endpointAddress = getRequiredTextContent(endpointAddressElement, addressQName);
               offer.setEndpoint(endpointAddress);
            }
            
            // read optional wsrm:Expires element
            SOAPElement offerExpiresElement = getOptionalElement(offerElement, expiresQName, offerQName);
            if (offerExpiresElement != null)
            {
               String duration = getRequiredTextContent(offerExpiresElement, expiresQName);
               offer.setExpires(duration);
            }
            
            // read optional wsrm:IncompleteSequenceBehavior element
            QName behaviorQName = wsrmConstants.getIncompleteSequenceBehaviorQName();
            SOAPElement behaviorElement = getOptionalElement(offerElement, behaviorQName, offerQName);
            if (behaviorElement != null)
            {
               String behaviorString = getRequiredTextContent(behaviorElement, behaviorQName);
               offer.setIncompleteSequenceBehavior(IncompleteSequenceBehavior.getValue(behaviorString));
            }
            
            // set created offer
            o.setOffer(offer);
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
    * Serialize <b>CreateSequence</b> using <b>provider</b> to the <b>soapMessage</b>
    * @param object to be serialized
    * @param provider wsrm provider to be used for serialization process
    * @param soapMessage soap message to which object will be serialized
    */
   public final void serialize(Serializable object, Provider provider, SOAPMessage soapMessage)
   throws RMException
   {
      CreateSequence o = (CreateSequence)object;
      try
      {
         SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart().getEnvelope();
         Constants wsrmConstants = provider.getConstants();
         
         // Add xmlns:wsrm declaration
         soapEnvelope.addNamespaceDeclaration(wsrmConstants.getPrefix(), wsrmConstants.getNamespaceURI());

         // write required wsrm:CreateSequence element
         QName createSequenceQName = wsrmConstants.getCreateSequenceQName(); 
         SOAPElement createSequenceElement = soapEnvelope.getBody().addChildElement(createSequenceQName);
         
         // write required wsrm:AcksTo element
         QName acksToQName = wsrmConstants.getAcksToQName();
         QName addressQName = ADDRESSING_CONSTANTS.getAddressQName();
         createSequenceElement.addChildElement(acksToQName)
            .addChildElement(addressQName)
               .setValue(o.getAcksTo());
         
         if (o.getExpires() != null)
         {
            // write optional wsrm:Expires element
            QName expiresQName = wsrmConstants.getExpiresQName();
            createSequenceElement.addChildElement(expiresQName).setValue(o.getExpires());
         }
         
         if (o.getOffer() != null)
         {
            CreateSequence.Offer offer = o.getOffer();
            
            // write optional wsrm:Offer element
            QName offerQName = wsrmConstants.getOfferQName();
            SOAPElement offerElement = createSequenceElement.addChildElement(offerQName);

            // write required wsrm:Identifier element
            QName identifierQName = wsrmConstants.getIdentifierQName();
            offerElement.addChildElement(identifierQName).setValue(offer.getIdentifier());
            
            if (offer.getEndpoint() != null)
            {
               // write optional wsrm:Endpoint element
               QName endpointQName = wsrmConstants.getEndpointQName();
               offerElement.addChildElement(endpointQName)
                  .addChildElement(addressQName)
                     .setValue(offer.getEndpoint());
            }
            
            if (offer.getExpires() != null)
            {
               // write optional wsrm:Expires element
               QName expiresQName = wsrmConstants.getExpiresQName();
               offerElement.addChildElement(expiresQName).setValue(offer.getExpires());
            }
            
            if (offer.getIncompleteSequenceBehavior() != null)
            {
               // write optional wsrm:IncompleteSequenceBehavior element
               IncompleteSequenceBehavior behavior = offer.getIncompleteSequenceBehavior();
               QName behaviorQName = wsrmConstants.getIncompleteSequenceBehaviorQName();
               SOAPElement behaviorElement = offerElement.addChildElement(behaviorQName);
               behaviorElement.setValue(behavior.toString());
            }
         }
      }
      catch (SOAPException se)
      {
         throw new RMException("Unable to serialize RM message", se);
      }
   }

}
