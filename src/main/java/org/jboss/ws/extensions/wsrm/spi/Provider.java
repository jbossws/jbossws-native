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
package org.jboss.ws.extensions.wsrm.spi;

import java.util.Map;
import java.util.HashMap;

/**
 * WS-RM Provider SPI facade. Each WS-RM provider must override this class.
 *
 * @author richard.opalka@jboss.com
 */
public abstract class Provider
{
   
   private static final Map<String, Provider> REGISTERED_PROVIDERS = new HashMap<String, Provider>();
   
   static
   {
      REGISTERED_PROVIDERS.put(
         org.jboss.ws.extensions.wsrm.spec200702.ProviderImpl.getInstance().getNamespaceURI(),
         org.jboss.ws.extensions.wsrm.spec200702.ProviderImpl.getInstance()
      );
      REGISTERED_PROVIDERS.put(
         org.jboss.ws.extensions.wsrm.spec200502.ProviderImpl.getInstance().getNamespaceURI(),
         org.jboss.ws.extensions.wsrm.spec200502.ProviderImpl.getInstance()
      );
   }
   
   /**
    * Must be overriden in subclasses
    * @param targetNamespace
    */
   protected Provider()
   {
   }
   
   /**
    * Returns the namespace associated with current WS-RM provider implementation
    * @return
    */
   public abstract String getNamespaceURI();
   
   /**
    * Returns WS-RM provider specific message factory
    * @return message factory
    */
   public abstract MessageFactory getMessageFactory();
   
   /**
    * Returns WS-RM provider specific constants
    * @return constants
    */
   public abstract Constants getConstants();
   
   /**
    * Gets WS-RM provider by <b>wsrmNamespace</b>
    * @param namespace associated with the WS-RM provider
    * @return WS-RM provider instance
    * @throws IllegalArgumentException if specified <b>wsrmNamespace</b> has no associated WS-RM provider 
    */
   public static final Provider getInstance(String wsrmNamespace)
   {
      if (!REGISTERED_PROVIDERS.keySet().contains(wsrmNamespace))
         throw new IllegalArgumentException("No WS-RM provider registered for namespace " + wsrmNamespace);

      return REGISTERED_PROVIDERS.get(wsrmNamespace);
   }
   
   public static final Provider get()
   {
      return org.jboss.ws.extensions.wsrm.spec200702.ProviderImpl.getInstance();
   }
   
}
