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
package org.jboss.ws.extensions.wsrm;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.addressing.JAXWSAConstants;
import javax.xml.ws.addressing.soap.SOAPAddressingProperties;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.logging.Logger;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.jaxws.handler.GenericSOAPHandler;
import org.jboss.ws.extensions.wsrm.spi.Constants;
import org.jboss.ws.extensions.wsrm.spi.MessageFactory;
import org.jboss.ws.extensions.wsrm.spi.Provider;
import org.jboss.ws.extensions.wsrm.spi.protocol.AckRequested;
import org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequence;
import org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequenceResponse;
import org.jboss.ws.extensions.wsrm.spi.protocol.Sequence;
import org.jboss.ws.extensions.wsrm.spi.protocol.Serializable;
import org.jboss.ws.extensions.wsrm.spi.protocol.TerminateSequence;

/**
 * TODO: add comment
 *
 * @author richard.opalka@jboss.com
 *
 * @since Oct 23, 2007
 */
@SuppressWarnings("unchecked")
public final class RMClientHandler extends GenericSOAPHandler
{
   private static final Logger log = Logger.getLogger(RMClientHandler.class);
   private static final MessageFactory rmFactory = Provider.get().getMessageFactory();
   private static final Constants rmConstants = Provider.get().getConstants();
   private static final Set headers;

   static
   {
      Set temp = new HashSet();
      temp.add(rmConstants.getCreateSequenceQName());
      temp.add(rmConstants.getCloseSequenceQName());
      temp.add(rmConstants.getTerminateSequenceQName());
      temp.add(rmConstants.getCreateSequenceResponseQName());
      temp.add(rmConstants.getCloseSequenceResponseQName());
      temp.add(rmConstants.getTerminateSequenceResponseQName());
      headers = Collections.unmodifiableSet(temp);
   }

   public Set getHeaders()
   {
      return headers;
   }

   protected boolean handleOutbound(MessageContext msgContext)
   {
      log.debug("WS-RM handleOutbound");
      
      CommonMessageContext commonMsgContext = (CommonMessageContext)msgContext;
      SOAPAddressingProperties addrProps = (SOAPAddressingProperties)commonMsgContext.get(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND);
      Map rmRequestContext = (Map)commonMsgContext.get(RMConstant.REQUEST_CONTEXT);
      QName operation = (QName)rmRequestContext.get(RMConstant.OPERATION_QNAME);
      if (addrProps != null)
      {
         SOAPMessage soapMessage = ((SOAPMessageContext)commonMsgContext).getMessage();
         if (rmConstants.getCreateSequenceQName().equals(operation))
         {
            String replyTo = addrProps.getReplyTo().getAddress().getURI().toString();
            CreateSequence createSequence = rmFactory.newCreateSequence();
            createSequence.setAcksTo(replyTo);
            createSequence.serializeTo(soapMessage);
            List<Serializable> data = new LinkedList<Serializable>();
            data.add(createSequence);
            rmRequestContext.put(RMConstant.DATA, data);
            
            return true;
         }
         
         if (rmConstants.getSequenceQName().equals(operation))
         {
            RMSequenceImpl sequenceImpl = (RMSequenceImpl)rmRequestContext.get(RMConstant.SEQUENCE_REFERENCE);
            Sequence sequence = rmFactory.newSequence();
            sequence.setIdentifier(sequenceImpl.getId());
            sequence.setMessageNumber(sequenceImpl.newMessageNumber());
            sequence.serializeTo(soapMessage);
            
            List<Serializable> data = new LinkedList<Serializable>();
            data.add(sequence);
            
            // TODO: ask msgStore if there are other sequences related to the same
            // endpoint that requires ack and serialize it here
            AckRequested ackRequested = rmFactory.newAckRequested();
            ackRequested.setIdentifier(sequenceImpl.getId());
            ackRequested.setMessageNumber(sequenceImpl.getLastMessageNumber());
            ackRequested.serializeTo(soapMessage);
            data.add(ackRequested);

            rmRequestContext.put(RMConstant.DATA, data);
            
            return true;
         }
         
         if (rmConstants.getTerminateSequenceQName().equals(operation))
         {
            RMSequenceImpl sequenceImpl = (RMSequenceImpl)rmRequestContext.get(RMConstant.SEQUENCE_REFERENCE);
            TerminateSequence terminateSequence = rmFactory.newTerminateSequence();
            terminateSequence.setIdentifier(sequenceImpl.getId());
            terminateSequence.setLastMsgNumber(sequenceImpl.getLastMessageNumber());
            terminateSequence.serializeTo(soapMessage);
            
            List<Serializable> data = new LinkedList<Serializable>();
            data.add(terminateSequence);
            rmRequestContext.put(RMConstant.DATA, data);
            
            return true;
         }
      }
      else
      {
         throw new IllegalStateException();
      }

      return true;
   }

   protected boolean handleInbound(MessageContext msgContext)
   {
      log.debug("WS-RM handleInbound");
      
      SOAPMessage soapMessage = ((SOAPMessageContext)msgContext).getMessage();
      // TODO: inspect operation type different way - don't forget on piggy-backing
      Map rmRequestContext = (Map)msgContext.get(RMConstant.REQUEST_CONTEXT);
      QName operation = (QName)rmRequestContext.get(RMConstant.OPERATION_QNAME);
      if (rmConstants.getCreateSequenceQName().equals(operation))
      {
         CreateSequenceResponse createSequenceResponse = rmFactory.newCreateSequenceResponse();
         createSequenceResponse.deserializeFrom(soapMessage);
         List<Serializable> data = new LinkedList<Serializable>();
         data.add(createSequenceResponse);
         Map rmResponseContext = new HashMap();
         rmResponseContext.put(RMConstant.OPERATION_QNAME, rmConstants.getCreateSequenceResponseQName());
         rmResponseContext.put(RMConstant.DATA, data);
         msgContext.put(RMConstant.RESPONSE_CONTEXT, rmResponseContext);
         msgContext.setScope(RMConstant.RESPONSE_CONTEXT, Scope.APPLICATION);
      }

      return true;
   }

}
