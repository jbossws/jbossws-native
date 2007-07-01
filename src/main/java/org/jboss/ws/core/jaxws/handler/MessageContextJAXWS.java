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
package org.jboss.ws.core.jaxws.handler;

// $Id: MessageContextImpl.java 275 2006-05-04 21:36:29Z jason.greene@jboss.com $

import javax.xml.ws.handler.MessageContext;

import org.jboss.logging.Logger;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.binding.SerializationContext;
import org.jboss.ws.core.jaxws.SerializationContextJAXWS;
import org.jboss.ws.core.soap.MessageContextAssociation;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.ServiceMetaData;
import org.jboss.xb.binding.NamespaceRegistry;

/**
 * The interface MessageContext abstracts the message context that is processed by a handler in the handle  method.
 * 
 * The MessageContext interface provides methods to manage a property set. 
 * MessageContext properties enable handlers in a handler chain to share processing related state.
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 25-Jul-2006
 */
public abstract class MessageContextJAXWS extends CommonMessageContext implements MessageContext
{
   private static Logger log = Logger.getLogger(MessageContextJAXWS.class);

   public MessageContextJAXWS()
   {
   }

   public MessageContextJAXWS(CommonMessageContext msgContext)
   {
      super(msgContext);
   }

   /** Create the serialization context
    */
   public SerializationContext createSerializationContext()
   {
      EndpointMetaData epMetaData = getEndpointMetaData();
      ServiceMetaData serviceMetaData = epMetaData.getServiceMetaData();

      SerializationContextJAXWS jaxwsContext = new SerializationContextJAXWS();
      jaxwsContext.setTypeMapping(serviceMetaData.getTypeMapping());
      return jaxwsContext;
   }

   /** Gets the namespace registry for this message context */
   public NamespaceRegistry getNamespaceRegistry()
   {
      return getSerializationContext().getNamespaceRegistry();
   }

   /** Sets the scope of a property. */
   public void setScope(String key, Scope scope)
   {
      ScopedProperty prop = scopedProps.get(key);
      if (prop == null)
         throw new IllegalArgumentException("Cannot find scoped property: " + key);

      scopedProps.put(key, new ScopedProperty(key, prop.getValue(), scope));
   }

   /** Gets the scope of a property. */
   public Scope getScope(String key)
   {
      ScopedProperty prop = scopedProps.get(key);
      if (prop == null)
         throw new IllegalArgumentException("Cannot find scoped property: " + key);

      return prop.getScope();
   }

   public static MessageContextJAXWS processPivot(CommonMessageContext reqContext)
   {
      log.debug("Begin response processing");

      Boolean outbound = (Boolean)reqContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
      if (outbound == null)
         throw new IllegalStateException("Cannot find property: " + MessageContext.MESSAGE_OUTBOUND_PROPERTY);

      MessageContextAssociation.popMessageContext();
      SOAPMessageContextJAXWS resContext = new SOAPMessageContextJAXWS(reqContext);
      resContext.setSOAPMessage(null);
      
      // Reverse the direction
      resContext.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, new Boolean(!outbound));
      
      MessageContextAssociation.pushMessageContext(resContext);

      return resContext;
   }
}
