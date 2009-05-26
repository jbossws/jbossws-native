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
package org.jboss.ws.extensions.addressing.map;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.addressing.AddressingBuilder;
import javax.xml.ws.addressing.AddressingConstants;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.EndpointReference;
import javax.xml.ws.handler.MessageContext;

import org.jboss.wsf.common.addressing.MAP;
import org.jboss.wsf.common.addressing.MAPBuilder;
import org.jboss.wsf.common.addressing.MAPConstants;
import org.jboss.wsf.common.addressing.MAPEndpoint;
import org.jboss.wsf.common.addressing.MAPRelatesTo;

/**
 * 
 * @author Andrew Dinn - adinn@redhat.com
 * @author alessio.soldano@jboss.com
 * @since 25-May-2009
 *
 */
public class NativeMAPBuilder implements MAPBuilder
{
   private AddressingBuilder addressingBuilder;

   private static MAPBuilder theBuilder = new NativeMAPBuilder();

   private NativeMAPBuilder()
   {
      AddressingBuilder implementation = AddressingBuilder.getAddressingBuilder();
      this.addressingBuilder = implementation;
   }

   public static MAPBuilder getBuilder()
   {
      return theBuilder;
   }

   public MAP newMap()
   {
      AddressingProperties implementation = addressingBuilder.newAddressingProperties();
      return new NativeMAP(implementation);
   }

   /**
    * retrieve the inbound server message address properties attached to a message context
    * @param ctx the server message context
    * @return
    */
   public MAP inboundMap(MessageContext ctx)
   {
      AddressingProperties implementation = (AddressingProperties)ctx.get(NativeMAPConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
      return newMap(implementation);
   }

   /**
    * retrieve the outbound client message address properties attached to a message request map
    * @param ctx the client request properties map
    * @return
    */
   public MAP outboundMap(Map<String, Object> ctx)
   {
      AddressingProperties implementation = (AddressingProperties)ctx.get(NativeMAPConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND);
      return newMap(implementation);
   }

   // n.b. this is package public only!
   MAP newMap(AddressingProperties implementation)
   {
      return new NativeMAP(implementation);
   }

   public MAPConstants newConstants()
   {
      AddressingConstants implementation = addressingBuilder.newAddressingConstants();
      return new NativeMAPConstants(implementation);
   }

   public MAPEndpoint newEndpoint(String address)
   {
      try
      {
         URI uri = new URI(address);
         EndpointReference implementation = addressingBuilder.newEndpointReference(uri);
         return new NativeMAPEndpoint(implementation);
      }
      catch (URISyntaxException e)
      {
         return null;
      }
   }

   public MAPRelatesTo newRelatesTo(String id, QName type)
   {
      return new NativeMAPRelatesTo(id, type);
   }

}
