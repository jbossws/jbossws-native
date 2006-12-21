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
package org.jboss.ws.core.jaxrpc.handler;

// $Id$

import javax.xml.rpc.handler.MessageContext;
import javax.xml.soap.SOAPMessage;

import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.jaxrpc.SerializationContextJAXRPC;
import org.jboss.ws.core.jaxrpc.binding.SerializationContext;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.ServiceMetaData;
import org.jboss.xb.binding.NamespaceRegistry;

/**
 * The message context that is processed by a handler
 * in the handle method.
 * <p/>
 * Provides methods to manage a property set.
 * MessageContext properties enable handlers in a handler chain to share
 * processing related state.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 06-May-2004
 */
public class MessageContextJAXRPC extends CommonMessageContext implements MessageContext
{
   public static final String SERVLET_CONTEXT = "javax.xml.ws.servlet.context";
   public static final String SERVLET_REQUEST = "javax.xml.ws.servlet.request";
   public static final String SERVLET_RESPONSE = "javax.xml.ws.servlet.response";
   public static final String SERVLET_SESSION = "javax.xml.ws.servlet.session";

   // The SOAP message
   private SOAPMessage message;
   // The serialization context for this message ctx
   private SerializationContext serContext;

   public SOAPMessage getMessage()
   {
      return message;
   }

   public void setMessage(SOAPMessage message)
   {
      this.message = message;
   }
   
   /** Get or create the serialization context
    */
   public SerializationContext getSerializationContext()
   {
      if (serContext == null)
      {
         EndpointMetaData epMetaData = getEndpointMetaData();
         ServiceMetaData serviceMetaData = epMetaData.getServiceMetaData();

         SerializationContextJAXRPC jaxrpcContext = new SerializationContextJAXRPC();
         jaxrpcContext.setTypeMapping(serviceMetaData.getTypeMapping());
         jaxrpcContext.setJavaWsdlMapping(serviceMetaData.getJavaWsdlMapping());
         serContext = jaxrpcContext;
      }
      return serContext;
   }

   /** Gets the namespace registry for this message context */
   public NamespaceRegistry getNamespaceRegistry()
   {
      return getSerializationContext().getNamespaceRegistry();
   }
}
