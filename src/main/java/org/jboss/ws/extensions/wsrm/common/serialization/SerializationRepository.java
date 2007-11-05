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

import org.jboss.ws.extensions.wsrm.client_api.RMException;
import org.jboss.ws.extensions.wsrm.spi.protocol.AckRequested;
import org.jboss.ws.extensions.wsrm.spi.protocol.CloseSequence;
import org.jboss.ws.extensions.wsrm.spi.protocol.CloseSequenceResponse;
import org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequence;
import org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequenceResponse;
import org.jboss.ws.extensions.wsrm.spi.protocol.Sequence;
import org.jboss.ws.extensions.wsrm.spi.protocol.SequenceAcknowledgement;
import org.jboss.ws.extensions.wsrm.spi.protocol.SequenceFault;
import org.jboss.ws.extensions.wsrm.spi.protocol.Serializable;
import org.jboss.ws.extensions.wsrm.spi.protocol.TerminateSequence;
import org.jboss.ws.extensions.wsrm.spi.protocol.TerminateSequenceResponse;

import javax.xml.soap.SOAPMessage;

import java.util.Map;
import java.util.HashMap;

/**
 * Utility class used for de/serialization
 * @author richard.opalka@jboss.com
 */
final class SerializationRepository
{

   private static final Map<Class<? extends Serializable>, Serializer> SERIALIZER_REGISTRY;
   
   static
   {
      SERIALIZER_REGISTRY = new HashMap<Class<? extends Serializable>, Serializer>();
      SERIALIZER_REGISTRY.put(AckRequested.class, AckRequestedSerializer.getInstance());
      SERIALIZER_REGISTRY.put(CloseSequence.class, CloseSequenceSerializer.getInstance());
      SERIALIZER_REGISTRY.put(CloseSequenceResponse.class, CloseSequenceResponseSerializer.getInstance());
      SERIALIZER_REGISTRY.put(CreateSequence.class, CreateSequenceSerializer.getInstance());
      SERIALIZER_REGISTRY.put(CreateSequenceResponse.class, CreateSequenceResponseSerializer.getInstance());
      SERIALIZER_REGISTRY.put(SequenceAcknowledgement.class, SequenceAcknowledgementSerializer.getInstance());
      SERIALIZER_REGISTRY.put(SequenceFault.class, SequenceFaultSerializer.getInstance());
      SERIALIZER_REGISTRY.put(Sequence.class, SequenceSerializer.getInstance());
      SERIALIZER_REGISTRY.put(TerminateSequence.class, TerminateSequenceSerializer.getInstance());
      SERIALIZER_REGISTRY.put(TerminateSequenceResponse.class, TerminateSequenceResponseSerializer.getInstance());
   }
   
   private SerializationRepository()
   {
      // no instances
   }
   
   /**
    * Serialize passed <b>object</b> data to the <b>soapMessage</b>
    * @param object to be serialized
    * @param soapMessage where to write data
    * @throws RMException if something went wrong
    */
   public static void serialize(AbstractSerializable object, SOAPMessage soapMessage)
   throws RMException
   {
      getSerializer(object).serialize(object, object.getProvider(), soapMessage);
   }

   /**
    * Initialize passed <b>object</b> using data in <b>soapMessage</b>
    * @param object to be initialized
    * @param soapMessage from which to read the data
    * @throws RMException if something went wrong
    */
   public static void deserialize(AbstractSerializable object, SOAPMessage soapMessage)
   throws RMException
   {
      getSerializer(object).deserialize(object, object.getProvider(), soapMessage);
   }
   
   /**
    * Lookups the serializer associated with the passed <b>object</b>
    * @param object to lookup serializer for
    * @return serializer to be used
    * @throws IllegalArgumentException if passed object has no defined serializer
    */
   private static Serializer getSerializer(Serializable object)
   {
      for (Class<? extends Serializable> serializable : SERIALIZER_REGISTRY.keySet())
      {
         if (serializable.isAssignableFrom(object.getClass()))
            return SERIALIZER_REGISTRY.get(serializable);
      }
      
      throw new IllegalArgumentException();
   }
   
}
