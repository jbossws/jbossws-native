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
package org.jboss.ws.extensions.wsrm.server;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.addressing.AddressingBuilder;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.JAXWSAConstants;
import javax.xml.ws.addressing.Relationship;
import javax.xml.ws.handler.MessageContext.Scope;

import org.jboss.logging.Logger;
import org.jboss.util.NotImplementedException;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.soap.MessageContextAssociation;
import org.jboss.ws.extensions.addressing.AddressingPropertiesImpl;
import org.jboss.ws.extensions.wsrm.RMAddressingConstants;
import org.jboss.ws.extensions.wsrm.RMConstant;
import org.jboss.ws.extensions.wsrm.api.RMException;
import org.jboss.ws.extensions.wsrm.common.RMHelper;
import org.jboss.ws.extensions.wsrm.spi.RMConstants;
import org.jboss.ws.extensions.wsrm.spi.RMProvider;
import org.jboss.ws.extensions.wsrm.spi.protocol.RMCloseSequence;
import org.jboss.ws.extensions.wsrm.spi.protocol.RMSequence;
import org.jboss.ws.extensions.wsrm.spi.protocol.RMSequenceAcknowledgement;
import org.jboss.ws.extensions.wsrm.spi.protocol.RMSerializable;
import org.jboss.ws.extensions.wsrm.spi.protocol.RMTerminateSequence;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.invocation.Invocation;
import org.jboss.wsf.spi.invocation.InvocationHandler;

/**
 * RM Invocation Handler 
 *
 * @author richard.opalka@jboss.com
 *
 * @since Dec 11, 2007
 */
public final class RMInvocationHandler extends InvocationHandler
{

   private static final Logger logger = Logger.getLogger(RMInvocationHandler.class);
   private static final RMConstants rmConstants = RMProvider.get().getConstants();
   
   private final InvocationHandler delegate;
   
   RMInvocationHandler(InvocationHandler delegate)
   {
      this.delegate = delegate;
   }
   
   @Override
   public final Invocation createInvocation()
   {
      return this.delegate.createInvocation();
   }

   @Override
   public final void handleInvocationException(Throwable th) throws Exception
   {
      // TODO is it necessary to handle it specially in the case of WS-RM ?
      super.handleInvocationException(th);
   }

   @Override
   public final void init(Endpoint ep)
   {
      this.delegate.init(ep);
   }
   
   /**
    * Do RM staff before endpoint invocation
    * @param ep endpoint
    * @param inv invocation
    * @return true if endpoint have to be called too
    */
   private void beforeEndpointInvocation(Endpoint ep, Invocation inv)
   {
      CommonMessageContext msgContext = MessageContextAssociation.peekMessageContext();
      AddressingProperties addrProps = (AddressingProperties)msgContext.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
      if (addrProps == null)
         throw new RMException("WS-Addressing properties not found in server request");
      
      Map<String, Object> rmReqProps = (Map<String, Object>)msgContext.get(RMConstant.REQUEST_CONTEXT);
      if (rmReqProps == null)
         throw new RMException("WS-RM specific data not found in server request");
      List<QName> protocolMessages = new LinkedList<QName>();
      Map<String, Object> rmResponseContext = new HashMap<String, Object>();
      List<RMServerSequence> sequences = (List<RMServerSequence>)ep.getAttachment(RMServerSequence.class);
      rmResponseContext.put(RMConstant.PROTOCOL_MESSAGES, protocolMessages);
      msgContext.remove(RMConstant.RESPONSE_CONTEXT);
      RMServerSequence sequence = null;
      
      if (RMHelper.isCreateSequence(rmReqProps))
      {
         sequence = new RMServerSequence();
         sequences.add(sequence);
         protocolMessages.add(rmConstants.getCreateSequenceResponseQName());
         rmResponseContext.put(RMConstant.SEQUENCE_REFERENCE, sequence);
      }
      
      if (RMHelper.isCloseSequence(rmReqProps))
      {
         Map<QName, RMSerializable> data = (Map<QName, RMSerializable>)rmReqProps.get(RMConstant.PROTOCOL_MESSAGES_MAPPING);
         RMCloseSequence payload = (RMCloseSequence)data.get(rmConstants.getCloseSequenceQName());
         String seqIdentifier = payload.getIdentifier();
         sequence = RMHelper.getServerSequenceByInboundId(seqIdentifier, sequences);
         if (sequence == null)
         {
            throw new NotImplementedException("TODO: implement unknown sequence fault" + seqIdentifier);
         }

         sequence.close();
         protocolMessages.add(rmConstants.getCloseSequenceResponseQName());
         protocolMessages.add(rmConstants.getSequenceAcknowledgementQName());
         rmResponseContext.put(RMConstant.SEQUENCE_REFERENCE, sequence);
      }
         
      if (RMHelper.isSequenceAcknowledgement(rmReqProps))
      {
         Map<QName, RMSerializable> data = (Map<QName, RMSerializable>)rmReqProps.get(RMConstant.PROTOCOL_MESSAGES_MAPPING);
         RMSequenceAcknowledgement payload = (RMSequenceAcknowledgement)data.get(rmConstants.getSequenceAcknowledgementQName());
         String seqIdentifier = payload.getIdentifier();
         sequence = RMHelper.getServerSequenceByOutboundId(seqIdentifier, sequences);
         if (sequence == null)
         {
            throw new NotImplementedException("TODO: implement unknown sequence fault" + seqIdentifier);
         }

         for (RMSequenceAcknowledgement.RMAcknowledgementRange range : payload.getAcknowledgementRanges())
         {
            for (long i = range.getLower(); i <= range.getUpper(); i++)
            {
               sequence.addReceivedOutboundMessage(i);
            }
         }
      }
      
      if (RMHelper.isTerminateSequence(rmReqProps))
      {
         Map<QName, RMSerializable> data = (Map<QName, RMSerializable>)rmReqProps.get(RMConstant.PROTOCOL_MESSAGES_MAPPING);
         RMTerminateSequence payload = (RMTerminateSequence)data.get(rmConstants.getTerminateSequenceQName());
         String seqIdentifier = payload.getIdentifier();
         sequence = RMHelper.getServerSequenceByInboundId(seqIdentifier, sequences);
         if (sequence == null)
         {
            throw new NotImplementedException("TODO: implement unknown sequence fault" + seqIdentifier);
         }

         sequences.remove(sequence);
         protocolMessages.add(rmConstants.getTerminateSequenceResponseQName());
         protocolMessages.add(rmConstants.getSequenceAcknowledgementQName());
         rmResponseContext.put(RMConstant.SEQUENCE_REFERENCE, sequence);
      }
      
      if (RMHelper.isSequence(rmReqProps))
      {
         Map<QName, RMSerializable> data = (Map<QName, RMSerializable>)rmReqProps.get(RMConstant.PROTOCOL_MESSAGES_MAPPING);
         RMSequence payload = (RMSequence)data.get(rmConstants.getSequenceQName());
         String seqIdentifier = payload.getIdentifier();
         sequence = RMHelper.getServerSequenceByInboundId(seqIdentifier, sequences);
         if (sequence == null)
         {
            throw new NotImplementedException("TODO: implement unknown sequence fault" + seqIdentifier);
         }

         sequence.addReceivedInboundMessage(payload.getMessageNumber());
         protocolMessages.add(rmConstants.getSequenceAcknowledgementQName());
         rmResponseContext.put(RMConstant.SEQUENCE_REFERENCE, sequence);
         
         boolean retTypeIsVoid = inv.getJavaMethod().getReturnType().equals(Void.class) || inv.getJavaMethod().getReturnType().equals(Void.TYPE);
         if (false == retTypeIsVoid)
         {
            protocolMessages.add(rmConstants.getSequenceQName());
            protocolMessages.add(rmConstants.getAckRequestedQName());
         }
         else
         {
            AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
            AddressingProperties addressingProps = builder.newAddressingProperties();
            addressingProps.setTo(builder.newURI(addrProps.getReplyTo().getAddress().getURI()));
            addressingProps.setRelatesTo(new Relationship[] {builder.newRelationship(addrProps.getMessageID().getURI())});
            try
            {
               addressingProps.setAction(builder.newURI(RMAddressingConstants.SEQUENCE_ACKNOWLEDGEMENT_WSA_ACTION));
            }
            catch (URISyntaxException ignore)
            {
            }
            msgContext.put(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_OUTBOUND, addressingProps);
         }
      }
      
      msgContext.put(RMConstant.RESPONSE_CONTEXT, rmResponseContext);
   }
   
   /**
    * Do RM staff after endpoint invocation
    * @param ep endpoint
    * @param inv invocation
    */
   private void afterEndpointInvocation(Endpoint ep, Invocation inv)
   {
      // TODO: implement
   }

   @Override
   public final void invoke(Endpoint ep, Invocation inv) throws Exception
   {
      beforeEndpointInvocation(ep, inv);
      
      if (inv.getJavaMethod() != null)
      {
         logger.debug("Invoking method: " + inv.getJavaMethod().getName());
         this.delegate.invoke(ep, inv);
      }
      else
      {
         logger.debug("RM lifecycle protocol method detected");
      }
      
      afterEndpointInvocation(ep, inv);
   }
   
   public final InvocationHandler getDelegate()
   {
      return this.delegate;
   }

}
