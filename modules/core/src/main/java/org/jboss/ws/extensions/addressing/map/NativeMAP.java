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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.ws.addressing.AddressingBuilder;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.AttributedURI;
import javax.xml.ws.addressing.EndpointReference;
import javax.xml.ws.addressing.Relationship;

import org.jboss.wsf.common.addressing.MAP;
import org.jboss.wsf.common.addressing.MAPBuilder;
import org.jboss.wsf.common.addressing.MAPEndpoint;
import org.jboss.wsf.common.addressing.MAPRelatesTo;
import org.w3c.dom.Element;

/**
 * 
 * @author Andrew Dinn - adinn@redhat.com
 * @alessio.soldano@jboss.com
 * @since 25-May-2009
 *
 */
public class NativeMAP implements MAP
{
   /**
    * the wrapped instance which this class delegates to
    */
   private AddressingProperties implementation;

  /**
    * JBossWS Native specific constructor
    * @param implementation
    */
   NativeMAP(AddressingProperties implementation)
   {
      this.implementation = implementation;
   }

   public String getTo()
   {
      AttributedURI to = implementation.getTo();
      return (to != null ? to.getURI().toString() : null);
   }

   public MAPEndpoint getFrom()
   {
      EndpointReference from = implementation.getFrom();
      return (from != null ? new NativeMAPEndpoint(from) : null);
   }

   public String getMessageID()
   {
      AttributedURI messageId = implementation.getMessageID();
      return (messageId != null ? messageId.getURI().toString() : null);
   }

   public String getAction()
   {
      AttributedURI action = implementation.getAction();
      return (action != null ? action.getURI().toString() : null);
   }

   public MAPEndpoint getFaultTo()
   {
      EndpointReference faultTo = implementation.getFaultTo();
      return (faultTo != null ? new NativeMAPEndpoint(faultTo) : null);
   }

   public MAPEndpoint getReplyTo()
   {
      EndpointReference replyTo = implementation.getReplyTo();
      return (replyTo != null ? new NativeMAPEndpoint(replyTo) : null);
   }

   public MAPRelatesTo getRelatesTo()
   {
      MAPBuilder builder =  NativeMAPBuilder.getBuilder();
      Relationship[] relationship = implementation.getRelatesTo();
      if (relationship != null)
      {
         Relationship relatesTo = relationship[0];
         return builder.newRelatesTo(relatesTo.getID().toString(), relatesTo.getType());
      }
      else
      {
         return null;
      }
   }

   public void setTo(String address)
   {
      if (address != null)
      {
         try
         {
            AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
            AttributedURI uri = builder.newURI(address);
            implementation.setTo(uri);
         }
         catch (URISyntaxException e)
         {
            // should not happen
         }
      }
      else
      {
         implementation.setTo(null);
      }
   }

   public void setFrom(MAPEndpoint epref)
   {
      if (epref != null)
      {
         if (epref instanceof NativeMAPEndpoint)
         {
            implementation.setFrom(((NativeMAPEndpoint)epref).getImplementation());
         }
         else
         {
            throw new IllegalArgumentException("Unsupported MAPEndpoint: " + epref);
         }
      }
      else
      {
         implementation.setFrom(null);
      }
   }

   public void setMessageID(String messageID)
   {
      if (messageID != null)
      {
         try
         {
            AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
            AttributedURI uri = builder.newURI(messageID);
            implementation.setMessageID(uri);
         }
         catch (URISyntaxException e)
         {
            // should not happen
         }
      }
      else
      {
         implementation.setMessageID(null);
      }
   }

   public void setAction(String action)
   {
      if (action != null)
      {
         try
         {
            AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
            AttributedURI uri = builder.newURI(action);
            implementation.setAction(uri);
         }
         catch (URISyntaxException e)
         {
            // should not happen
         }
      }
      else
      {
         implementation.setAction(null);
      }
   }

   public void setReplyTo(MAPEndpoint epref)
   {
      if (epref != null)
      {
         if (epref instanceof NativeMAPEndpoint)
         {
            implementation.setReplyTo(((NativeMAPEndpoint)epref).getImplementation());
         }
         else
         {
            throw new IllegalArgumentException("Unsupported MAPEndpoint: " + epref);
         }
      }
      else
      {
         implementation.setReplyTo(null);
      }
   }

   public void setFaultTo(MAPEndpoint epref)
   {
      if (epref != null)
      {
         if (epref instanceof NativeMAPEndpoint)
         {
            implementation.setFaultTo(((NativeMAPEndpoint)epref).getImplementation());
         }
         else
         {
            throw new IllegalArgumentException("Unsupported MAPEndpoint: " + epref);
         }
      }
      else
      {
         implementation.setFaultTo(null);
      }
   }

   public void setRelatesTo(MAPRelatesTo relatesTo)
   {
      if (relatesTo != null)
      {
         try
         {
            AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
            Relationship[] relationships = new Relationship[1];
            String relatesToId = relatesTo.getRelatesTo();
            URI uri = new URI(relatesToId);
            Relationship relationship = builder.newRelationship(uri);
            relationship.setType(relatesTo.getType());
            relationships[0] = relationship;
            implementation.setRelatesTo(relationships);
         }
         catch (URISyntaxException e)
         {
            // should not happen
         }
      }
      else
      {
         implementation.setRelatesTo(null);
      }
   }

   public void addReferenceParameter(Element refParam)
   {
      implementation.getReferenceParameters().addElement(refParam);
   }
   
   public List<Object> getReferenceParameters()
   {
      List<Object> list = new LinkedList<Object>();
      if (implementation.getReferenceParameters() != null)
      {
         list.addAll(implementation.getReferenceParameters().getElements());
      }
      return list;
   }

   public void initializeAsDestination(MAPEndpoint epref)
   {
      if (epref instanceof NativeMAPEndpoint)
      {
         implementation.initializeAsDestination(((NativeMAPEndpoint)epref).getImplementation());
      }
      else
      {
         throw new IllegalArgumentException("Unsupported MAPEndpoint: " + epref);
      }
   }

   public void installOutboundMapOnClientSide(Map<String, Object> requestContext, MAP map)
   {
      if (!(map instanceof NativeMAP))
      {
         throw new IllegalArgumentException("Unsupported MAP: " + map);
      }
      AddressingProperties addressingProperties = ((NativeMAP)map).implementation;

      requestContext.put(NativeMAPConstants.CLIENT_ADDRESSING_PROPERTIES, addressingProperties);
      requestContext.put(NativeMAPConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND, addressingProperties);
   }
   
   public void installOutboundMapOnServerSide(Map<String, Object> requestContext, MAP map)
   {
      if (!(map instanceof NativeMAP))
      {
         throw new IllegalArgumentException("Unsupported MAP: " + map);
      }
      AddressingProperties addressingProperties = ((NativeMAP)map).implementation;

      requestContext.put(NativeMAPConstants.SERVER_ADDRESSING_PROPERTIES_OUTBOUND, addressingProperties);
   }
}
