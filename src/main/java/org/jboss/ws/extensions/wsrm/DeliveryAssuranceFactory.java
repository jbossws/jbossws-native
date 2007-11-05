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

package org.jboss.ws.extensions.wsrm;

/**
 * Encapsulates generation of reliable messaging quality assurance configurations
 * 
 * @author richard.opalka@jboss.com
 * @see org.jboss.ws.extensions.wsrm.DeliveryQuality
 */
public final class DeliveryAssuranceFactory
{
   
   private static final DeliveryAssuranceFactory INSTANCE = new DeliveryAssuranceFactory();
   private static final String[] ACCEPTABLE_IN_ORDER_VALUES = { "0", "1", "false", "true" };
   private static final DeliveryAssurance EXACTLY_ONCE_WITH_ORDER;
   private static final DeliveryAssurance EXACTLY_ONCE_WITHOUT_ORDER;
   private static final DeliveryAssurance AT_MOST_ONCE_WITH_ORDER;
   private static final DeliveryAssurance AT_MOST_ONCE_WITHOUT_ORDER;
   private static final DeliveryAssurance AT_LEAST_ONCE_WITH_ORDER;
   private static final DeliveryAssurance AT_LEAST_ONCE_WITHOUT_ORDER;
   
   static
   {
      EXACTLY_ONCE_WITH_ORDER = new DeliveryAssuranceImpl(DeliveryQuality.EXACTLY_ONCE, true);
      EXACTLY_ONCE_WITHOUT_ORDER = new DeliveryAssuranceImpl(DeliveryQuality.EXACTLY_ONCE, false);
      AT_MOST_ONCE_WITH_ORDER = new DeliveryAssuranceImpl(DeliveryQuality.AT_MOST_ONCE, true);
      AT_MOST_ONCE_WITHOUT_ORDER = new DeliveryAssuranceImpl(DeliveryQuality.AT_MOST_ONCE, false);
      AT_LEAST_ONCE_WITH_ORDER = new DeliveryAssuranceImpl(DeliveryQuality.AT_LEAST_ONCE, true);
      AT_LEAST_ONCE_WITHOUT_ORDER = new DeliveryAssuranceImpl(DeliveryQuality.AT_LEAST_ONCE, false);
   }
   
   // immutable object instance
   private static final class DeliveryAssuranceImpl implements DeliveryAssurance
   {

      private final DeliveryQuality quality;
      private final boolean inOrder;
      
      private DeliveryAssuranceImpl(DeliveryQuality quality, boolean inOrder)
      {
         this.quality = quality;
         this.inOrder = inOrder;
      }
      
      /*
       * @see org.jboss.ws.extensions.wsrm.DeliveryAssurance#getDeliveryQuality()
       */
      public DeliveryQuality getDeliveryQuality()
      {
         return quality;
      }

      /*
       * @see org.jboss.ws.extensions.wsrm.DeliveryAssurance#inOrder()
       */
      public boolean inOrder()
      {
         return inOrder;
      }
      
   }
   
   private DeliveryAssuranceFactory()
   {
      // no instances
   }
   
   /**
    * Factory getter  
    * @return factory instance
    */
   public static DeliveryAssuranceFactory getInstance()
   {
      return INSTANCE;
   }
   
   /**
    * Returns constructed DeliveryAssurance object
    * @param quality string representing quality value
    * @param inOrder string representing inOrder value 
    * @return DeliveryAssurance object
    * @throws IllegalArgumentException if <b>quality</b> or <b>inOrder</b> are null or contain incorrect values
    */
   public static DeliveryAssurance getDeliveryAssurance(String quality, String inOrder)
   {
      if ((quality == null) || (inOrder == null))
         throw new IllegalArgumentException("Neither quality nor inOrder parameter cannot be null");
      
      Boolean inOrderBoolean = null;
      for (int i = 0; i < ACCEPTABLE_IN_ORDER_VALUES.length; i++)
      {
         if (ACCEPTABLE_IN_ORDER_VALUES[i].equals(inOrder))
         {
            inOrderBoolean = (i % 2 == 0) ? Boolean.FALSE : Boolean.TRUE;
            break;
         }
      }
      
      if (inOrderBoolean == null)
         throw new IllegalArgumentException("Incorrect inOrder value: " + inOrder);
      
      return getDeliveryAssurance(DeliveryQuality.parseDeliveryQuality(quality), inOrderBoolean);
   }
   
   /**
    * Returns constructed DeliveryAssurance object
    * @param quality object representing required quality
    * @param inOrder string representing required inOrder value 
    * @return DeliveryAssurance object
    * @throws IllegalArgumentException if <b>quality</b> is null
    */
   public static DeliveryAssurance getDeliveryAssurance(DeliveryQuality quality, boolean inOrder)
   {
      if (quality == null)
         throw new IllegalArgumentException("Quality cannot be null");
      
      if (inOrder)
      {
         if (quality == DeliveryQuality.EXACTLY_ONCE) return EXACTLY_ONCE_WITH_ORDER;
         if (quality == DeliveryQuality.AT_LEAST_ONCE) return AT_LEAST_ONCE_WITH_ORDER;
         if (quality == DeliveryQuality.AT_MOST_ONCE) return AT_MOST_ONCE_WITH_ORDER;
      }
      else
      {
         if (quality == DeliveryQuality.EXACTLY_ONCE) return EXACTLY_ONCE_WITHOUT_ORDER;
         if (quality == DeliveryQuality.AT_LEAST_ONCE) return AT_LEAST_ONCE_WITHOUT_ORDER;
         if (quality == DeliveryQuality.AT_MOST_ONCE) return AT_MOST_ONCE_WITHOUT_ORDER;
      }
      
      return null; // never happens
   }
   
}
