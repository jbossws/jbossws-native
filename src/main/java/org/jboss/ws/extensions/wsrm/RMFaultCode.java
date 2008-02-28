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

/**
 * RM fault constants
 *
 * @author richard.opalka@jboss.com
 * 
 * @see org.jboss.ws.extensions.wsrm.RMFault
 * @see org.jboss.ws.extensions.wsrm.RMFaultConstant
 */
public final class RMFaultCode
{
   
   private final String subcode;
   private final String reason;

   /**
    * Encountering an unrecoverable condition or detection of violation of the protocol.
    * <p>
    * <table>
    * <tr><td>[Code]</td><td>Sender or Receiver</td></tr>
    * <tr><td>[Subcode]</td><td>wsrm:SequenceTerminated</td></tr>
    * <tr><td>[Reason]</td><td>The Sequence has been terminated due to an unrecoverable error.</td></tr>
    * <tr><td>[Detail]</td><td>&lt;wsrm:Identifier...&gt; xs:anyURI &lt;/wsrm:Identifier&gt;</td></tr>
    * </table>
    * </p>
    */
   public static final RMFaultCode SEQUENCE_TERMINATED = new RMFaultCode(
      "SequenceTerminated",
      "The Sequence has been terminated due to an unrecoverable error."
   );

   /**
    * Response to a message containing an
    * unknown or terminated Sequence identifier.
    * <p>
    * <table>
    * <tr><td>[Code]</td><td>Sender</td></tr>
    * <tr><td>[Subcode]</td><td>wsrm:UnknownSequence</td></tr>
    * <tr><td>[Reason]</td><td>The value of wsrm:Identifier is not a known Sequence identifier.</td></tr>
    * <tr><td>[Detail]</td><td>&lt;wsrm:Identifier...&gt; xs:anyURI &lt;/wsrm:Identifier&gt;</td></tr>
    * </table>
    * </p>
    */
   public static final RMFaultCode UNKNOWN_SEQUENCE = new RMFaultCode(
      "UnknownSequence",
      "The value of wsrm:Identifier is not a known Sequence identifier."
   );
   
   /**
    * An example of when this fault is generated is when a message
    * is Received by the RM Source containing a SequenceAcknowledgement
    * covering messages that have not been sent.
    * <p>
    * <table>
    * <tr><td>[Code]</td><td>Sender</td></tr>
    * <tr><td>[Subcode]</td><td>wsrm:InvalidAcknowledgement</td></tr>
    * <tr><td>[Reason]</td><td>The SequenceAcknowledgement violates the cumulative Acknowledgement invariant.</td></tr>
    * <tr><td>[Detail]</td><td>&lt;wsrm:SequenceAcknowledgement ...&gt; ... &lt;/wsrm:SequenceAcknowledgement&gt;</td></tr>
    * </table>
    * </p>
    */
   public static final RMFaultCode INVALID_ACKNOWLEDGEMENT = new RMFaultCode(
      "InvalidAcknowledgement",
      "The SequenceAcknowledgement violates the cumulative Acknowledgement invariant."
   );
   
   /**
    * Message number in /wsrm:Sequence/wsrm:MessageNumber of a Received
    * message exceeds the internal limitations of an RM Destination
    * <p>
    * <table>
    * <tr><td>[Code]</td><td>Sender</td></tr>
    * <tr><td>[Subcode]</td><td>wsrm:MessageNumberRollover</td></tr>
    * <tr><td>[Reason]</td><td>The maximum value for wsrm:MessageNumber has been exceeded.</td></tr>
    * <tr><td>[Detail]</td><td>
    *   <table>
    *     <tr><td>&lt;wsrm:Identifier ...&gt; xs:anyURI &lt;/wsrm:Identifier&gt;</td></tr>
    *     <tr><td>&lt;wsrm:MaxMessageNumber&gt; wsrm:MessageNumberType &lt;/wsrm:MaxMessageNumber&gt;</td></tr>
    *   </table>
    * </td></tr></table>
    * </p>
    */
   public static final RMFaultCode MESSAGE_NUMBER_ROLLOVER = new RMFaultCode(
      "MessageNumberRollover",
      "The maximum value for wsrm:MessageNumber has been exceeded."
   );
   
   /**
    * In response to a CreateSequence message when the RM
    * Destination does not wish to create a new Sequence.
    * <p>
    * <table>
    * <tr><td>[Code]</td><td>Sender or Receiver</td></tr>
    * <tr><td>[Subcode]</td><td>wsrm:CreateSequenceRefused</td></tr>
    * <tr><td>[Reason]</td><td>The Create Sequence request has been refused by the RM Destination.</td></tr>
    * <tr><td>[Detail]</td><td>xs:any</td></tr>
    * </table>
    * </p>
    */
   public static final RMFaultCode CREATE_SEQUENCE_REFUSED = new RMFaultCode(
      "CreateSequenceRefused",
      "The Create Sequence request has been refused by the RM Destination."
   );
   
   /**
    * This fault is generated by an RM Destination to indicate
    * that the specified Sequence has been closed.
    * This fault MUST be generated when an RM Destination is asked
    * to accept a message for a Sequence that is closed.
    * <p>
    * <table>
    * <tr><td>[Code]</td><td>Sender</td></tr>
    * <tr><td>[Subcode]</td><td>wsrm:SequenceClosed</td></tr>
    * <tr><td>[Reason]</td><td>The Sequence is closed and cannot accept new messages.</td></tr>
    * <tr><td>[Detail]</td><td>&lt;wsrm:Identifier...&gt; xs:anyURI &lt;/wsrm:Identifier&gt;</td></tr>
    * </table>
    * </p>
    */
   public static final RMFaultCode SEQUENCE_CLOSED = new RMFaultCode(
      "SequenceClosed",
      "The Sequence is closed and cannot accept new messages."
   );
   
   /**
    * If an RM Destination requires the use of WS-RM,
    * this fault is generated when it Receives an incoming
    * message that did not use this protocol.
    * 
    * <p>
    * <table>
    * <tr><td>[Code]</td><td>Sender</td></tr>
    * <tr><td>[Subcode]</td><td>wsrm:WSRMRequired</td></tr>
    * <tr><td>[Reason]</td><td>The RM Destination requires the use of WSRM</td></tr>
    * <tr><td>[Detail]</td><td>xs:any</td></tr>
    * </table>
    * </p>
    */
   public static final RMFaultCode WSRM_REQUIRED = new RMFaultCode(
      "WSRMRequired",
      "The RM Destination requires the use of WSRM"
   );
   
   /**
    * Hidden constructor
    * @param subcode the subcode
    * @param reason message
    */
   private RMFaultCode(String subcode, String reason)
   {
      super();
      this.subcode = subcode;
      this.reason = reason;
   }
   
   /**
    * Gets subcode
    * @return subcode
    */
   public final String getSubcode()
   {
      return this.subcode;
   }
   
   /**
    * Gets message
    * @return message
    */
   public final String getReason()
   {
      return this.reason;
   }

}
