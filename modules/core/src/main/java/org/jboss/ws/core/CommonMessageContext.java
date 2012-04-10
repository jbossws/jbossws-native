/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.jboss.logging.Logger;
import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.core.binding.SerializationContext;
import org.jboss.ws.core.soap.attachment.SwapableMemoryDataSource;
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
   private static final ResourceBundle bundle = BundleUtils.getBundle(CommonMessageContext.class);
   private static Logger log = Logger.getLogger(CommonMessageContext.class);

   public static final String REMOTING_METADATA = "org.jboss.ws.remoting.metadata";

   // The serialization context for this message ctx
   private SerializationContext serContext;
   // The operation for this message ctx
   private EndpointMetaData epMetaData;
   // The operation for this message ctx
   private OperationMetaData opMetaData;
   // The Message in this message context
   private MessageAbstraction message;
   // The map of scoped properties
   protected Map<String, Object> props = new HashMap<String, Object>();

   private boolean isModified;

   public CommonMessageContext()
   {
   }

   // Copy constructor
   public CommonMessageContext(CommonMessageContext msgContext)
   {
      this.epMetaData = msgContext.epMetaData;
      this.opMetaData = msgContext.opMetaData;
      this.message = msgContext.message;
      this.serContext = msgContext.serContext;
      this.props = new HashMap<String, Object>(msgContext.props);
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
      if(message!=null && ((message instanceof SOAPMessage) == false))
         throw new UnsupportedOperationException(BundleUtils.getMessage(bundle, "NO_SOAPMESSAGE_AVILABLE",  message.getClass()));
      return (SOAPMessage)message;
   }

   public void setSOAPMessage(SOAPMessage soapMessage)
   {
      this.message = (MessageAbstraction)soapMessage;
      this.setModified(true);
   }

   public MessageAbstraction getMessageAbstraction()
   {
      return message;
   }

   public void setMessageAbstraction(MessageAbstraction message)
   {
      this.message = message;
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

   public boolean isModified()
   {
      return isModified;
   }

   /**
    * Mark a message as 'modified' when the SAAJ model becomes stale.    
    * This may be the case when:
    * <ul>
    * <li>the complete message is replaced at MessageContext level
    * <li>the payload is set on a LogicalMessage
    * <li>The SAAJ model is changed though the DOM or SAAJ API (handler)
    * </ul>
    *
    * In any of these cases another 'unbind' invocation is required.
    */
   public void setModified(boolean modified)
   {
      isModified = modified;
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
      for (String key : srcMap.keySet())
      {
         try
         {
            Object value = srcMap.get(key);
            put(key, value);
         }
         catch (IllegalArgumentException ex)
         {
            if (log.isDebugEnabled())
               log.debug("Ignore: " + ex.getMessage());
         }
      }
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

   public static void cleanupAttachments(CommonMessageContext messageContext)
   {
      // cleanup attachments
      MessageAbstraction msg = messageContext.getMessageAbstraction();

      if(msg!=null && (msg instanceof SOAPMessage)) // in case of http binding
      {
         Iterator it = ((SOAPMessage)msg).getAttachments();
         while(it.hasNext())
         {
            AttachmentPart attachment = (AttachmentPart)it.next();
            try
            {
               if(attachment.getDataHandler().getDataSource() instanceof SwapableMemoryDataSource)
               {
                  SwapableMemoryDataSource swapFile = (SwapableMemoryDataSource)attachment.getDataHandler().getDataSource();
                  swapFile.cleanup();
               }
            }
            catch (SOAPException e)
            {
               log.warn(BundleUtils.getMessage(bundle, "FAILED_TO_CLEANUP_ATTACHMENT_PART"),  e);
            }
         }
      }
   }
}
