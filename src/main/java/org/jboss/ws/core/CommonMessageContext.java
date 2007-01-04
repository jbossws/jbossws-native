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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.soap.SOAPMessage;

import org.jboss.ws.core.jaxrpc.binding.SerializationContext;
import org.jboss.ws.core.server.MessageContextPropertyHelper;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.OperationMetaData;
import org.jboss.xb.binding.NamespaceRegistry;

/**
 * The common JAXRPC/JAXWS MessageContext
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 1-Sep-2006
 */
public abstract class CommonMessageContext 
{
   // expandToDOM in the SOAPContentElement should not happen during normal operation 
   // This property should be set the message context when it is ok to do so.
   public static String ALLOW_EXPAND_TO_DOM = "org.jboss.ws.allow.expand.dom";
   
   // The operation for this message ctx
   private EndpointMetaData epMetaData;
   // The operation for this message ctx
   private OperationMetaData opMetaData;
   // The SOAPMessage in this message context
   private SOAPMessage soapMessage;
   // The map of the properties
   protected Map<String, Object> props = new HashMap<String, Object>();

   public CommonMessageContext()
   {
   }

   // Copy constructor
   public CommonMessageContext(CommonMessageContext msgContext)
   {
      this.epMetaData = msgContext.epMetaData;
      this.opMetaData = msgContext.opMetaData;
      this.soapMessage = msgContext.soapMessage;
      this.props = msgContext.props;
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

   public abstract SerializationContext getSerializationContext();
   
   /** Gets the namespace registry for this message context */
   public NamespaceRegistry getNamespaceRegistry()
   {
      return getSerializationContext().getNamespaceRegistry();
   }
   
   public Map<String, Object> getProperties()
   {
      return props;
   }

   public void setProperties(Map<String, Object> props)
   {
      this.props = props;
   }

   /**
    * Returns true if the MessageContext contains a property with the specified name.
    */
   public boolean containsProperty(String name)
   {
      return props.containsKey(name);
   }

   /**
    * Gets the value of a specific property from the MessageContext
    */
   public Object getProperty(String name)
   {
      Object value = props.get(name);
      
      if (value instanceof MessageContextPropertyHelper)
      {
         return ((MessageContextPropertyHelper)value).get();
      }
      
      return value;
   }

   /**
    * Returns an Iterator view of the names of the properties in this MessageContext
    */
   public Iterator getPropertyNames()
   {
      return props.keySet().iterator();
   }

   /**
    * Removes a property (name-value pair) from the MessageContext
    */
   public void removeProperty(String name)
   {
      props.remove(name);
   }

   /**
    * Sets the name and value of a property associated with the MessageContext.
    * If the MessageContext contains a value of the same property, the old value is replaced.
    */
   public void setProperty(String name, Object value)
   {
      props.put(name, value);
   }
}
