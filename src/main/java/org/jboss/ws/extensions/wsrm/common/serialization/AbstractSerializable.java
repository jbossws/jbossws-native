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

import javax.xml.soap.SOAPMessage;

import org.jboss.ws.extensions.wsrm.client_api.RMException;
import org.jboss.ws.extensions.wsrm.spi.Provider;
import org.jboss.ws.extensions.wsrm.spi.protocol.Serializable;

/**
 * Utility class which should be subclassed by all WS-RM protocol providers.
 * @author richard.opalka@jboss.com
 * @see org.jboss.ws.extensions.wsrm.spi.protocol.Serializable
 */
public abstract class AbstractSerializable implements Serializable
{
   
   protected AbstractSerializable()
   {
      // intended to be subclassed
   }
 
   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.Serializable#deserializeFrom(javax.xml.soap.SOAPMessage)
    */
   public final void deserializeFrom(SOAPMessage soapMessage) throws RMException
   {
      SerializationRepository.deserialize(this, soapMessage);
      validate(); // finally ensure that object is in correct state
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.Serializable#serializeTo(javax.xml.soap.SOAPMessage)
    */
   public final void serializeTo(SOAPMessage soapMessage) throws RMException
   {
      validate(); // ensure object is in correct state first
      SerializationRepository.serialize(this, soapMessage);
   }
   
   /**
    * Each subclass must implement this method
    * @return RM provider to be used for de/serialization purposes
    */
   public abstract Provider getProvider();

}
