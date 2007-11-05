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
package org.jboss.ws.metadata.wsrm;

import javax.xml.namespace.QName;

/**
 * Port customization metadata
 * 
 * @author richard.opalka@jboss.com
 */
public final class PortMetaData
{

   private QName portName;
   private DeliveryAssuranceMetaData deliveryAssurance;
   
   public final void setPortName(QName portName)
   {
      if (portName == null)
         throw new IllegalArgumentException();
    
      this.portName = portName;
   }
   
   public final QName getPortName()
   {
      return this.portName;
   }
   
   public final void setDeliveryAssurance(DeliveryAssuranceMetaData deliveryAssurance)
   {
      if (deliveryAssurance == null)
         throw new IllegalArgumentException();
      
      this.deliveryAssurance = deliveryAssurance;
   }
   
   public final DeliveryAssuranceMetaData getDeliveryAssurance()
   {
      return this.deliveryAssurance;
   }
   
   public final String toString()
   {
      return "portName=\"" + this.portName + "\", deliveryAssurance={" + this.deliveryAssurance + "}";
   }

}
