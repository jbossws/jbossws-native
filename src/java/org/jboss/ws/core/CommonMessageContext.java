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
package org.jboss.ws.core;

// $Id$

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext.Scope;

import org.jboss.logging.Logger;
import org.jboss.ws.core.jaxrpc.binding.SerializationContext;
import org.jboss.ws.core.server.PropertyCallback;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.OperationMetaData;
import org.jboss.xb.binding.NamespaceRegistry;

/**
 * The common JAXRPC/JAXWS MessageContext
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 1-Sep-2006
 */
public abstract class CommonMessageContext implements Map<String, Object>
{
   private static Logger log = Logger.getLogger(CommonMessageContext.class);
   
   // expandToDOM in the SOAPContentElement should not happen during normal operation 
   // This property should be set the message context when it is ok to do so.
   public static String ALLOW_EXPAND_TO_DOM = "org.jboss.ws.allow.expand.dom";

   // The serialization context for this message ctx
   private SerializationContext serContext;
   // The operation for this message ctx
   private EndpointMetaData epMetaData;
   // The operation for this message ctx
   private OperationMetaData opMetaData;
   // The SOAPMessage in this message context
   private SOAPMessage soapMessage;
   // The map of scoped properties
   protected Map<String, ScopedProperty> scopedProps = new HashMap<String, ScopedProperty>();
   // The current property scope
   protected Scope currentScope = Scope.APPLICATION;

   public CommonMessageContext()
   {
   }

   // Copy constructor
   public CommonMessageContext(CommonMessageContext msgContext)
   {
      this.epMetaData = msgContext.epMetaData;
      this.opMetaData = msgContext.opMetaData;
      this.soapMessage = msgContext.soapMessage;
      this.serContext = msgContext.serContext;
      this.scopedProps = msgContext.scopedProps;
      this.currentScope = msgContext.currentScope;
   }

   public Scope getCurrentScope()
   {
      return currentScope;
   }

   public void setCurrentScope(Scope currentScope)
   {
      this.currentScope = currentScope;
   }

   public EndpointMetaData getEndpointMetaData()
   {
      if (epMetaData == null && opMetaData != null)
         epMetaData = opMetaData.getEndpointMetaData();

      return epMetaData;
   }

   public void setEndpointMetaData(EndpointMetaData epMetaData)
   {
      this.epMetaData = epMetaData;
   }

   public OperationMetaData getOperationMetaData()
   {
      return opMetaData;
   }

   public void setOperationMetaData(OperationMetaData opMetaData)
   {
      this.opMetaData = opMetaData;
   }

   public SOAPMessage getSOAPMessage()
   {
      return soapMessage;
   }

   public void setSOAPMessage(SOAPMessage soapMessage)
   {
      this.soapMessage = soapMessage;
   }

   public SerializationContext getSerializationContext()
   {
      if (serContext == null)
      {
         serContext = createSerializationContext();
      }
      return serContext;
   }

   public abstract SerializationContext createSerializationContext();

   public void setSerializationContext(SerializationContext serContext)
   {
      this.serContext = serContext;
   }

   /** Gets the namespace registry for this message context */
   public NamespaceRegistry getNamespaceRegistry()
   {
      return getSerializationContext().getNamespaceRegistry();
   }

   /** Get the message context properties */
   public Map<String, Object> getProperties()
   {
      Map<String, Object> props = new HashMap<String, Object>();
      for (String key : keySet())
      {
         Object value = get(key);
         props.put(key, value);
      }
      return props;
   }

   /**
    * Returns true if the MessageContext contains a property with the specified name.
    */
   public boolean containsProperty(String name)
   {
      return containsKey(name);
   }

   /**
    * Gets the value of a specific property from the MessageContext
    */
   public Object getProperty(String name)
   {
      return get(name);
   }

   /**
    * Returns an Iterator view of the names of the properties in this MessageContext
    */
   public Iterator getPropertyNames()
   {
      return keySet().iterator();
   }

   /**
    * Removes a property (name-value pair) from the MessageContext
    */
   public void removeProperty(String name)
   {
      remove(name);
   }

   /**
    * Sets the name and value of a property associated with the MessageContext.
    * If the MessageContext contains a value of the same property, the old value is replaced.
    */
   public void setProperty(String name, Object value)
   {
      put(name, value);
   }

   // Map interface

   public int size()
   {
      return scopedProps.size();
   }

   public boolean isEmpty()
   {
      return scopedProps.isEmpty();
   }

   public boolean containsKey(Object key)
   {
      ScopedProperty prop = scopedProps.get(key);
      return isValidInScope(prop);
   }

   private boolean isValidInScope(ScopedProperty prop)
   {
      // A property of scope APPLICATION is always visible
      boolean valid = (prop != null && (prop.getScope() == Scope.APPLICATION || currentScope == Scope.HANDLER));
      return valid;
   }

   public boolean containsValue(Object value)
   {
      boolean valueFound = false;
      for (ScopedProperty prop : scopedProps.values())
      {
         if (prop.getValue().equals(value) && isValidInScope(prop))
         {
            valueFound = true;
            break;
         }
      }
      return valueFound;
   }

   public Object get(Object key)
   {
      Object value = null;

      ScopedProperty prop = scopedProps.get(key);
      if (isValidInScope(prop))
         value = prop.getValue();

      return value;
   }

   public Object put(String key, Object value)
   {
      ScopedProperty prevProp = scopedProps.get(key);
      if (prevProp != null && !isValidInScope(prevProp))
         throw new IllegalArgumentException("Cannot set value for HANDLER scoped property: " + key);

      scopedProps.put(key, new ScopedProperty(key, value, currentScope));
      return prevProp != null ? prevProp.getValue() : null;
   }

   public Object remove(Object key)
   {
      ScopedProperty prevProp = scopedProps.get(key);
      if (prevProp != null && !isValidInScope(prevProp))
         throw new IllegalArgumentException("Cannot set remove for HANDLER scoped property: " + key);

      return scopedProps.remove(key);
   }

   public void putAll(Map<? extends String, ? extends Object> srcMap)
   {
      for (String key : srcMap.keySet())
      {
         try
         {
            Object value = srcMap.get(key);
            put(key, value);
         }
         catch (IllegalArgumentException ex)
         {
            log.debug("Ignore: " + ex.getMessage());
         }
      }
   }

   public void clear()
   {
      scopedProps.clear();
   }

   public Set<String> keySet()
   {
      return scopedProps.keySet();
   }

   public Collection<Object> values()
   {
      Collection<Object> values = new HashSet<Object>();
      for (ScopedProperty prop : scopedProps.values())
      {
         if (isValidInScope(prop))
            values.add(prop.getValue());

      }
      return values;
   }

   public Set<Entry<String, Object>> entrySet()
   {
      Set<Entry<String, Object>> entries = new HashSet<Entry<String, Object>>();
      for (ScopedProperty prop : scopedProps.values())
      {
         if (isValidInScope(prop))
         {
            String name = prop.getName();
            Object value = prop.getValue();
            Entry<String, Object> entry = new ImmutableEntry<String, Object>(name, value);
            entries.add(entry);
         }
      }
      return entries;
   }

   private static class ImmutableEntry<K, V> implements Map.Entry<K, V>
   {
      final K k;
      final V v;

      ImmutableEntry(K key, V value)
      {
         k = key;
         v = value;
      }

      public K getKey()
      {
         return k;
      }

      public V getValue()
      {
         return v;
      }

      public V setValue(V value)
      {
         throw new UnsupportedOperationException();
      }
   }

   public static class ScopedProperty
   {
      private Scope scope;
      private String name;
      private Object value;

      public ScopedProperty(String name, Object value, Scope scope)
      {
         this.scope = scope;
         this.name = name;
         this.value = value;
      }

      public String getName()
      {
         return name;
      }

      public Scope getScope()
      {
         return scope;
      }

      public Object getValue()
      {
         Object realValue = value;
         if (value instanceof PropertyCallback)
            realValue = ((PropertyCallback)value).get();

         return realValue;
      }
      
      public String toString()
      {
         return scope + ":" + name + "=" + value;
      }
   }
}
