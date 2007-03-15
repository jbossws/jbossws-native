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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.ws.addressing.JAXWSAConstants;
import javax.xml.ws.addressing.soap.SOAPAddressingProperties;
import javax.xml.ws.handler.MessageContext;

import org.jboss.logging.Logger;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.StubExt;
import org.jboss.ws.core.jaxrpc.binding.SerializationContext;
import org.jboss.ws.core.jaxws.SerializationContextJAXWS;
import org.jboss.ws.core.soap.MessageContextAssociation;
import org.jboss.ws.extensions.xop.XOPContext;
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
public class MessageContextJAXWS extends CommonMessageContext implements MessageContext
{
   private static Logger log = Logger.getLogger(MessageContextJAXWS.class);

   // The map of property scopes
   private HashMap<String, Scope> scopes = new HashMap<String, Scope>();

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
      scopes.put(key, scope);
   }

   /** Gets the scope of a property. */
   public Scope getScope(String key)
   {
      return scopes.get(key);
   }

   public static CommonMessageContext processPivot(CommonMessageContext reqContext)
   {
      log.debug("Begin response processing");

      // MTOM setting need to pass past pivot
      boolean mtomEnabled = XOPContext.isMTOMEnabled();
      
      // Reverse the direction
      Boolean outbound = (Boolean)reqContext.getProperty(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
      outbound = new Boolean(!outbound.booleanValue());
      
      // Preserve addressing properties
      SOAPAddressingProperties addrProps = (SOAPAddressingProperties)reqContext.getProperty(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
      
      MessageContextAssociation.popMessageContext();
      SOAPMessageContextJAXWS resContext = new SOAPMessageContextJAXWS(reqContext);
      resContext.setSOAPMessage(null);
      resContext.clear();

      resContext.setProperty(StubExt.PROPERTY_MTOM_ENABLED, mtomEnabled);
      resContext.setProperty(MessageContext.MESSAGE_OUTBOUND_PROPERTY, outbound);
      resContext.setProperty(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND, addrProps);
      MessageContextAssociation.pushMessageContext(resContext);

      return resContext;
   }

   // Map interface

   public int size()
   {
      return props.size();
   }

   public boolean isEmpty()
   {
      return props.isEmpty();
   }

   public boolean containsKey(Object key)
   {
      return props.containsKey(key);
   }

   public boolean containsValue(Object value)
   {
      return props.containsValue(value);
   }

   public Object get(Object key)
   {
      return props.get(key);
   }

   public Object put(String key, Object value)
   {
      return props.put(key, value);
   }

   public Object remove(Object key)
   {
      return props.remove(key);
   }

   public void putAll(Map<? extends String, ? extends Object> srcMap)
   {
      props.putAll(srcMap);
   }

   public void clear()
   {
      props.clear();
   }

   public Set<String> keySet()
   {
      return props.keySet();
   }

   public Collection<Object> values()
   {
      return props.values();
   }

   public Set<Entry<String, Object>> entrySet()
   {
      return props.entrySet();
   }
}
