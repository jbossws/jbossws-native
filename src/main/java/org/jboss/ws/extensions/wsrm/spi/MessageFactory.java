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
package org.jboss.ws.extensions.wsrm.spi;

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

/**
 * WS-RM protocol elements SPI facade. Each WS-RM provider must implement this interface.
 *
 * @author richard.opalka@jboss.com
 */
public interface MessageFactory
{
   /**
    * Factory method
    * @return new CreateSequence instance
    */
   CreateSequence newCreateSequence();

   /**
    * Factory method
    * @return new CreateSequenceResponse instance
    */
   CreateSequenceResponse newCreateSequenceResponse();

   /**
    * Factory method
    * @return new CloseSequence instance or null if this message is not supported by underlying WS-RM provider
    */
   CloseSequence newCloseSequence();

   /**
    * Factory method
    * @return new CloseSequenceResponse instance or null if this message is not supported by underlying WS-RM provider
    */
   CloseSequenceResponse newCloseSequenceResponse();

   /**
    * Factory method
    * @return new TerminateSequence instance
    */
   TerminateSequence newTerminateSequence();

   /**
    * Factory method
    * @return new TerminateSequenceResponse instance or null if this message is not supported by underlying WS-RM provider
    */
   TerminateSequenceResponse newTerminateSequenceResponse();

   /**
    * Factory method
    * @return new Sequence instance
    */
   Sequence newSequence();

   /**
    * Factory method
    * @return new AckRequested instance
    */
   AckRequested newAckRequested();

   /**
    * Factory method
    * @return new SequenceAcknowledgement instance
    */
   SequenceAcknowledgement newSequenceAcknowledgement();
   
   /**
    * Factory method
    * @return new SequenceFault instance
    */
   SequenceFault newSequenceFault();
}
