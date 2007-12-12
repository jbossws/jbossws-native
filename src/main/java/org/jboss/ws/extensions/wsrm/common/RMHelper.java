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
package org.jboss.ws.extensions.wsrm.common;

import java.util.Date;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;

import org.jboss.logging.Logger;
import org.jboss.ws.extensions.wsrm.RMConstant;
import org.jboss.ws.extensions.wsrm.RMClientSequenceImpl;
import org.jboss.ws.extensions.wsrm.api.RMException;
import org.jboss.ws.extensions.wsrm.jaxws.RMHandler;
import org.jboss.ws.extensions.wsrm.spi.RMProvider;
import org.jboss.ws.extensions.wsrm.spi.protocol.RMAckRequested;
import org.jboss.ws.extensions.wsrm.spi.protocol.RMSequence;
import org.jboss.ws.extensions.wsrm.spi.protocol.RMSequenceAcknowledgement;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.OperationMetaData;

/**
 * RM utility library
 *
 * @author richard.opalka@jboss.com
 *
 * @since Nov 29, 2007
 */
public final class RMHelper
{
   
   private static final Logger logger = Logger.getLogger(RMHelper.class);

   private RMHelper()
   {
      // no instances allowed
   }
   
   private static final DatatypeFactory factory;
   
   static
   {
      try
      {
         factory = DatatypeFactory.newInstance();
      }
      catch (DatatypeConfigurationException dce)
      {
         throw new RMException(dce.getMessage(), dce);
      }
   }
   
   public static Duration stringToDuration(String s)
   {
      return factory.newDuration(s);
   }
   
   public static String durationToString(Duration d)
   {
      return d.toString();
   }
   
   public static long durationToLong(Duration d)
   {
      if (d == null)
         return -1L;
      
      return d.getTimeInMillis(new Date());
   }
   
   public static void handleSequenceAcknowledgementHeader(RMSequenceAcknowledgement seqAckHeader, RMClientSequenceImpl sequence)
   {
      String seqId = seqAckHeader.getIdentifier();
      if (sequence.getOutboundId().equals(seqId))
      {
         List<RMSequenceAcknowledgement.RMAcknowledgementRange> ranges = seqAckHeader.getAcknowledgementRanges();
         for (RMSequenceAcknowledgement.RMAcknowledgementRange range : ranges)
         {
            for (long i = range.getLower(); i <= range.getUpper(); i++)
            {
               sequence.addReceivedOutboundMessage(i);
            }
         }
         if (seqAckHeader.isFinal())
         {
            sequence.setFinal();
         }
      }
      else
      {
         logger.warn("Expected outbound sequenceId:" + sequence.getOutboundId() + " , but was: " + seqId);
         throw new RMException("Expected outbound sequenceId:" + sequence.getOutboundId() + " , but was: " + seqId);
      }
   }
   
   public static void handleAckRequestedHeader(RMAckRequested ackReqHeader, RMClientSequenceImpl sequence)
   {
      String inboundSeqId = ackReqHeader.getIdentifier();
      if (false == sequence.getInboundId().equals(inboundSeqId))
      {
         logger.warn("Expected inbound sequenceId:" + sequence.getInboundId() + " , but was: " + inboundSeqId);
         throw new RMException("Expected inbound sequenceId:" + sequence.getInboundId() + " , but was: " + inboundSeqId);
      }
      
      sequence.ackRequested(true);
   }
   
   public static void handleSequenceHeader(RMSequence seqHeader, RMClientSequenceImpl sequence)
   {
      String inboundSeqId = seqHeader.getIdentifier();
      if (null == sequence.getInboundId())
      {
         sequence.setInboundId(inboundSeqId);
      }
      else
      {
         if (false == sequence.getInboundId().equals(inboundSeqId))
         {
            logger.warn("Expected inbound sequenceId:" + sequence.getInboundId() + " , but was: " + inboundSeqId);
            throw new RMException("Expected inbound sequenceId:" + sequence.getInboundId() + " , but was: " + inboundSeqId);
         }
      }
      sequence.addReceivedInboundMessage(seqHeader.getMessageNumber());
   }
   
   public static void setupRMOperations(EndpointMetaData endpointMD)
   {
      RMProvider rmProvider = RMProvider.get();
      
      // register createSequence method
      QName createSequenceQName = rmProvider.getConstants().getCreateSequenceQName();
      OperationMetaData createSequenceMD = new OperationMetaData(endpointMD, createSequenceQName, "createSequence");
      createSequenceMD.setOneWay(false);
      endpointMD.addOperation(createSequenceMD);
      
      // register sequenceAcknowledgement method
      QName sequenceAcknowledgementQName = rmProvider.getConstants().getSequenceAcknowledgementQName();
      OperationMetaData sequenceAcknowledgementMD = new OperationMetaData(endpointMD, sequenceAcknowledgementQName, "sequenceAcknowledgement");
      sequenceAcknowledgementMD.setOneWay(true);
      endpointMD.addOperation(sequenceAcknowledgementMD);
      
      // register closeSequence method
      QName closeSequenceQName = rmProvider.getConstants().getCloseSequenceQName();
      OperationMetaData closeSequenceMD = new OperationMetaData(endpointMD, closeSequenceQName, "closeSequence");
      closeSequenceMD.setOneWay(false);
      endpointMD.addOperation(closeSequenceMD);
      
      // register terminateSequence method
      QName terminateSequenceQName = rmProvider.getConstants().getTerminateSequenceQName();
      OperationMetaData terminateSequenceMD = new OperationMetaData(endpointMD, terminateSequenceQName, "terminateSequence");
      terminateSequenceMD.setOneWay(false);
      endpointMD.addOperation(terminateSequenceMD);
   }
   
   public static boolean isRMOperation(QName operationQName)
   {
      return RMConstant.PROTOCOL_OPERATION_QNAMES.contains(operationQName);
   }
   
}
