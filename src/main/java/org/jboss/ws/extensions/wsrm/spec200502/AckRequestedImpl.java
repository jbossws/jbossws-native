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
package org.jboss.ws.extensions.wsrm.spec200502;

import org.jboss.ws.extensions.wsrm.client_api.RMException;
import org.jboss.ws.extensions.wsrm.common.serialization.AbstractSerializable;
import org.jboss.ws.extensions.wsrm.spi.Provider;
import org.jboss.ws.extensions.wsrm.spi.protocol.AckRequested;

/*
 * @author richard.opalka@jboss.com
 * @see org.jboss.ws.extensions.wsrm.spi.protocol.AckRequested
 */
final class AckRequestedImpl extends AbstractSerializable implements AckRequested
{
   
   // provider used by de/serialization framework
   private static final Provider PROVIDER = ProviderImpl.getInstance();
   // internal fields
   private String identifier;
   private long lastMessageNumber;
   
   AckRequestedImpl()
   {
      // allow inside package use only
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.AckRequested#getIdentifier()
    */
   public String getIdentifier()
   {
      return this.identifier;
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.AckRequested#getMessage()
    */
   public long getMessageNumber()
   {
      return this.lastMessageNumber;
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.AckRequested#setIdentifier(java.lang.String)
    */
   public void setIdentifier(String identifier)
   {
      if ((identifier == null) || (identifier.trim().equals("")))
         throw new IllegalArgumentException("Identifier cannot be null nor empty string");
      if (this.identifier != null)
         throw new UnsupportedOperationException("Value already set, cannot be overriden");
      
      this.identifier = identifier;
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.protocol.AckRequested#setMessage(long)
    */
   public void setMessageNumber(long lastMessageNumber)
   {
      if (lastMessageNumber <= 0)
         throw new IllegalArgumentException("Value must be greater than 0");
      if (this.lastMessageNumber > 0)
         throw new UnsupportedOperationException("Value already set, cannot be overriden");
      
      this.lastMessageNumber = lastMessageNumber;
   }

   /*
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
      result = prime * result + (int)(lastMessageNumber ^ (lastMessageNumber >>> 32));
      return result;
   }

   /*
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (!(obj instanceof AckRequestedImpl))
         return false;
      final AckRequestedImpl other = (AckRequestedImpl)obj;
      if (identifier == null)
      {
         if (other.identifier != null)
            return false;
      }
      else if (!identifier.equals(other.identifier))
         return false;
      if (lastMessageNumber != other.lastMessageNumber)
         return false;
      return true;
   }
   
   public Provider getProvider()
   {
      return PROVIDER;
   }

   public void validate()
   {
      if (this.identifier == null)
         throw new RMException("Identifier not set");
   }

}
