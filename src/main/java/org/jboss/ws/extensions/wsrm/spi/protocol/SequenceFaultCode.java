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
package org.jboss.ws.extensions.wsrm.spi.protocol;

/**
 * WS-ReliableMessaging nodes that generate a <b>SequenceFault</b> MUST set the value of this
 * element to a qualified name from the set of faults [Subcodes] defined below.
 * 
 * @author richard.opalka@jboss.com
 */
public enum SequenceFaultCode
{
   /**
    * Sequence terminated
    */
   SEQUENCE_TERMINATED,
   
   /**
    * Unknown sequence
    */
   UNKNOWN_SEQUENCE,
   
   /**
    * Invalid acknowledgement
    */
   INVALID_ACKNOWLEDGEMENT,
   
   /**
    * Message number rollover
    */
   MESSAGE_NUMBER_ROLLOVER,
   
   /**
    * Create sequence refused
    */
   CREATE_SEQUENCE_REFUSED,
   
   /**
    * Sequence closed
    */
   SEQUENCE_CLOSED,
   
   /**
    * WSRM required 
    */
   WSRM_REQUIRED,
   
   /**
    * Last message number exceeded
    */
   LAST_MESSAGE_NUMBER_EXCEEDED
}
