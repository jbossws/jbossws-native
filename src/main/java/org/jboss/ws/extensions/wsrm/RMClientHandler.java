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
import org.jboss.ws.extensions.wsrm.client_api.RMException;
import org.jboss.ws.extensions.wsrm.spi.Constants;
import org.jboss.ws.extensions.wsrm.spi.MessageFactory;
import org.jboss.ws.extensions.wsrm.spi.Provider;
import org.jboss.ws.extensions.wsrm.spi.protocol.AckRequested;
import org.jboss.ws.extensions.wsrm.spi.protocol.CloseSequence;
import org.jboss.ws.extensions.wsrm.spi.protocol.CloseSequenceResponse;
import org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequence;
import org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequenceResponse;
import org.jboss.ws.extensions.wsrm.spi.protocol.Sequence;
import org.jboss.ws.extensions.wsrm.spi.protocol.SequenceAcknowledgement;
import org.jboss.ws.extensions.wsrm.spi.protocol.SequenceFault;
import org.jboss.ws.extensions.wsrm.spi.protocol.Serializable;
import org.jboss.ws.extensions.wsrm.spi.protocol.TerminateSequence;
import org.jboss.ws.extensions.wsrm.spi.protocol.TerminateSequenceResponse;

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
   private static final Set headers = RMConstant.PROTOCOL_OPERATION_QNAMES;

   public Set getHeaders()
   {
      return headers;
   }

   protected boolean handleOutbound(MessageContext msgContext)
   {
      log.debug("handling outbound message");
      
      CommonMessageContext commonMsgContext = (CommonMessageContext)msgContext;
      SOAPAddressingProperties addrProps = (SOAPAddressingProperties)commonMsgContext.get(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND);
      if (addrProps == null)
         throw new RMException("WS-Addressing properties not found in message context");
      
      Map rmRequestContext = (Map)commonMsgContext.get(RMConstant.REQUEST_CONTEXT);
      List<QName> outMsgs = (List<QName>)rmRequestContext.get(RMConstant.PROTOCOL_MESSAGES);
      Map<QName, Serializable> data = new HashMap<QName, Serializable>();
      rmRequestContext.put(RMConstant.WSA_MESSAGE_ID, addrProps.getMessageID().getURI().toString());
      rmRequestContext.put(RMConstant.PROTOCOL_MESSAGES_MAPPING, data);
      SOAPMessage soapMessage = ((SOAPMessageContext)commonMsgContext).getMessage();
      
      QName msgQName = rmConstants.getCreateSequenceQName();
      if (outMsgs.contains(msgQName))
      {
         // try to serialize CreateSequence to message
         String replyTo = addrProps.getReplyTo().getAddress().getURI().toString();
         CreateSequence createSequence = rmFactory.newCreateSequence();
         createSequence.setAcksTo(replyTo);
         createSequence.serializeTo(soapMessage);
         data.put(msgQName, createSequence);
         log.debug(msgQName.getLocalPart() + " WSRM message was serialized to payload");
      }
         
      msgQName = rmConstants.getSequenceQName();
      if (outMsgs.contains(msgQName))
      {
         // try to serialize Sequence to message
         RMSequenceImpl sequenceImpl = (RMSequenceImpl)rmRequestContext.get(RMConstant.SEQUENCE_REFERENCE);
         Sequence sequence = rmFactory.newSequence();
         sequence.setIdentifier(sequenceImpl.getId());
         sequence.setMessageNumber(sequenceImpl.newMessageNumber());
         sequence.serializeTo(soapMessage);
         data.put(msgQName, sequence);
         log.debug(msgQName.getLocalPart() + " WSRM message was serialized to payload");
      }
      
      msgQName = rmConstants.getAckRequestedQName();
      if (outMsgs.contains(msgQName))
      {
         // try to serialize AckRequested to message
         RMSequenceImpl sequenceImpl = (RMSequenceImpl)rmRequestContext.get(RMConstant.SEQUENCE_REFERENCE);
         AckRequested ackRequested = rmFactory.newAckRequested();
         ackRequested.setIdentifier(sequenceImpl.getId());
         ackRequested.setMessageNumber(sequenceImpl.getLastMessageNumber());
         ackRequested.serializeTo(soapMessage);
         data.put(msgQName, ackRequested);
         log.debug(msgQName.getLocalPart() + " WSRM message was serialized to payload");
      }
         
      msgQName = rmConstants.getCloseSequenceQName();
      if (outMsgs.contains(msgQName))
      {
         // try to serialize CloseSequence to message
         RMSequenceImpl sequenceImpl = (RMSequenceImpl)rmRequestContext.get(RMConstant.SEQUENCE_REFERENCE);
         CloseSequence closeSequence = rmFactory.newCloseSequence();
         closeSequence.setIdentifier(sequenceImpl.getId());
         closeSequence.setLastMsgNumber(sequenceImpl.getLastMessageNumber());
         closeSequence.serializeTo(soapMessage);
         data.put(msgQName, closeSequence);
         log.debug(msgQName.getLocalPart() + " WSRM message was serialized to payload");
      }

      msgQName = rmConstants.getCloseSequenceResponseQName();
      if (outMsgs.contains(msgQName))
      {
         // try to serialize CloseSequenceResponse to message
         RMSequenceImpl sequenceImpl = (RMSequenceImpl)rmRequestContext.get(RMConstant.SEQUENCE_REFERENCE);
         CloseSequenceResponse closeSequenceResponse = rmFactory.newCloseSequenceResponse();
         closeSequenceResponse.setIdentifier(sequenceImpl.getId());
         data.put(msgQName, closeSequenceResponse);
         log.debug(msgQName.getLocalPart() + " WSRM message was serialized to payload");
      }

      msgQName = rmConstants.getTerminateSequenceQName();
      if (outMsgs.contains(msgQName))
      {
         // try to serialize TerminateSequence to message
         RMSequenceImpl sequenceImpl = (RMSequenceImpl)rmRequestContext.get(RMConstant.SEQUENCE_REFERENCE);
         TerminateSequence terminateSequence = rmFactory.newTerminateSequence();
         terminateSequence.setIdentifier(sequenceImpl.getId());
         terminateSequence.setLastMsgNumber(sequenceImpl.getLastMessageNumber());
         terminateSequence.serializeTo(soapMessage);
         data.put(msgQName, terminateSequence);
         log.debug(msgQName.getLocalPart() + " WSRM message was serialized to payload");
      }

      msgQName = rmConstants.getTerminateSequenceResponseQName();
      if (outMsgs.contains(msgQName))
      {
         // try to serialize terminateSequenceResponse to message
         RMSequenceImpl sequenceImpl = (RMSequenceImpl)rmRequestContext.get(RMConstant.SEQUENCE_REFERENCE);
         TerminateSequenceResponse terminateSequenceResponse = rmFactory.newTerminateSequenceResponse();
         terminateSequenceResponse.setIdentifier(sequenceImpl.getId());
         data.put(msgQName, terminateSequenceResponse);
         log.debug(msgQName.getLocalPart() + " WSRM message was serialized to payload");
      }
      
      // TODO: implement SequenceAcknowledgement handler part

      return true;
   }

   protected boolean handleInbound(MessageContext msgContext)
   {
      log.debug("handling inbound message");
      
      SOAPMessage soapMessage = ((SOAPMessageContext)msgContext).getMessage();
      Map rmResponseContext = new HashMap();
      List<QName> messages = new LinkedList<QName>();
      rmResponseContext.put(RMConstant.PROTOCOL_MESSAGES, messages);
      Map<QName, Serializable> data = new HashMap<QName, Serializable>();
      rmResponseContext.put(RMConstant.PROTOCOL_MESSAGES_MAPPING, data);
      msgContext.put(RMConstant.RESPONSE_CONTEXT, rmResponseContext);
      msgContext.setScope(RMConstant.RESPONSE_CONTEXT, Scope.APPLICATION);

      try
      {
         // try to deserialize CreateSequenceResponse from message
         QName msgQName = rmConstants.getCreateSequenceResponseQName();
         CreateSequenceResponse wsrmMsg = rmFactory.newCreateSequenceResponse();
         wsrmMsg.deserializeFrom(soapMessage);
         messages.add(msgQName);
         data.put(msgQName, wsrmMsg);
         log.debug(msgQName.getLocalPart() + " WSRM message was deserialized from payload");
      }
      catch (RMException ignore) {}
      
      try
      {
         // try to deserialize AckRequested from message
         QName msgQName = rmConstants.getAckRequestedQName();
         AckRequested wsrmMsg = rmFactory.newAckRequested();
         wsrmMsg.deserializeFrom(soapMessage);
         messages.add(msgQName);
         data.put(msgQName, wsrmMsg);
         log.debug(msgQName.getLocalPart() + " WSRM message was deserialized from payload");
      }
      catch (RMException ignore) {}
      
      try
      {
         // try to deserialize Sequence from message
         QName msgQName = rmConstants.getSequenceQName();
         Sequence wsrmMsg = rmFactory.newSequence();
         wsrmMsg.deserializeFrom(soapMessage);
         messages.add(msgQName);
         data.put(msgQName, wsrmMsg);
         log.debug(msgQName.getLocalPart() + " WSRM message was deserialized from payload");
      }
      catch (RMException ignore) {}
      
      try
      {
         // try to deserialize SequenceAcknowledgement from message
         QName msgQName = rmConstants.getSequenceAcknowledgementQName();
         SequenceAcknowledgement wsrmMsg = rmFactory.newSequenceAcknowledgement();
         wsrmMsg.deserializeFrom(soapMessage);
         messages.add(msgQName);
         data.put(msgQName, wsrmMsg);
         log.debug(msgQName.getLocalPart() + " WSRM message was deserialized from payload");
      }
      catch (RMException ignore) {}

      try
      {
         // try to deserialize CloseSequence from message
         QName msgQName = rmConstants.getCloseSequenceQName();
         CloseSequence wsrmMsg = rmFactory.newCloseSequence();
         wsrmMsg.deserializeFrom(soapMessage);
         messages.add(msgQName);
         data.put(msgQName, wsrmMsg);
         log.debug(msgQName.getLocalPart() + " WSRM message was deserialized from payload");
      }
      catch (RMException ignore) {}
      
      try
      {
         // try to deserialize CloseSequence from message
         QName msgQName = rmConstants.getCloseSequenceResponseQName();
         CloseSequenceResponse wsrmMsg = rmFactory.newCloseSequenceResponse();
         wsrmMsg.deserializeFrom(soapMessage);
         messages.add(msgQName);
         data.put(msgQName, wsrmMsg);
         log.debug(msgQName.getLocalPart() + " WSRM message was deserialized from payload");
      }
      catch (RMException ignore) {}
      
      try
      {
         // try to deserialize TerminateSequence from message
         QName msgQName = rmConstants.getTerminateSequenceQName();
         TerminateSequence wsrmMsg = rmFactory.newTerminateSequence();
         wsrmMsg.deserializeFrom(soapMessage);
         messages.add(msgQName);
         data.put(msgQName, wsrmMsg);
         log.debug(msgQName.getLocalPart() + " WSRM message was deserialized from payload");
      }
      catch (RMException ignore) {}
      
      try
      {
         // try to deserialize TerminateSequenceResponse from message
         QName msgQName = rmConstants.getTerminateSequenceResponseQName();
         TerminateSequenceResponse wsrmMsg = rmFactory.newTerminateSequenceResponse();
         wsrmMsg.deserializeFrom(soapMessage);
         messages.add(msgQName);
         data.put(msgQName, wsrmMsg);
         log.debug(msgQName.getLocalPart() + " WSRM message was deserialized from payload");
      }
      catch (RMException ignore) {}
      
      // TODO: implement SequenceFault deserialization
      
      if (data.size() == 0)
         throw new RMException("RM handler was not able to find WS-RM message in the payload");
      
      return true;
   }

}
