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
package org.jboss.ws.extensions.wsrm.spec200502;

import org.jboss.ws.extensions.wsrm.spi.MessageFactory;
import org.jboss.ws.extensions.wsrm.spi.protocol.AckRequested;
import org.jboss.ws.extensions.wsrm.spi.protocol.CloseSequence;
import org.jboss.ws.extensions.wsrm.spi.protocol.CloseSequenceResponse;
import org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequence;
import org.jboss.ws.extensions.wsrm.spi.protocol.CreateSequenceResponse;
import org.jboss.ws.extensions.wsrm.spi.protocol.Sequence;
import org.jboss.ws.extensions.wsrm.spi.protocol.SequenceAcknowledgement;
import org.jboss.ws.extensions.wsrm.spi.protocol.SequenceFault;
import org.jboss.ws.extensions.wsrm.spi.protocol.TerminateSequence;
import org.jboss.ws.extensions.wsrm.spi.protocol.TerminateSequenceResponse;

/*
 * @author richard.opalka@jboss.com
 * @see org.jboss.ws.extensions.wsrm.spi.MessageFactory
 */
final class MessageFactoryImpl implements MessageFactory
{
   
   private static final MessageFactory INSTANCE = new MessageFactoryImpl();
   
   private MessageFactoryImpl()
   {
      // forbidden inheritance
   }
   
   public static MessageFactory getInstance()
   {
      return INSTANCE;
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.MessageFactory#newAckRequested()
    */
   public AckRequested newAckRequested()
   {
      return new AckRequestedImpl();
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.MessageFactory#newCloseSequence()
    */
   public CloseSequence newCloseSequence()
   {
      return null; // not supported by this version of the RM protocol
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.MessageFactory#newCloseSequenceResponse()
    */
   public CloseSequenceResponse newCloseSequenceResponse()
   {
      return null; // not supported by this version of the RM protocol
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.MessageFactory#newCreateSequence()
    */
   public CreateSequence newCreateSequence()
   {
      return new CreateSequenceImpl();
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.MessageFactory#newCreateSequenceResponse()
    */
   public CreateSequenceResponse newCreateSequenceResponse()
   {
      return new CreateSequenceResponseImpl();
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.MessageFactory#newSequence()
    */
   public Sequence newSequence()
   {
      return new SequenceImpl();
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.MessageFactory#newSequenceAcknowledgement()
    */
   public SequenceAcknowledgement newSequenceAcknowledgement()
   {
      return new SequenceAcknowledgementImpl();
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.MessageFactory#newSequenceFault()
    */
   public SequenceFault newSequenceFault()
   {
      return new SequenceFaultImpl();
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.MessageFactory#newTerminateSequence()
    */
   public TerminateSequence newTerminateSequence()
   {
      return new TerminateSequenceImpl();
   }

   /*
    * @see org.jboss.ws.extensions.wsrm.spi.MessageFactory#newTerminateSequenceResponse()
    */
   public TerminateSequenceResponse newTerminateSequenceResponse()
   {
      return null; // not supported by this version of the RM protocol
   }
   
}
