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
package org.jboss.ws.extensions.wsrm.protocol.spi;

/**
 * <p><b>CloseSequenceResponse</b> element is sent in the body of a message in response to receipt of a <b>CloseSequence</b>
 * request message. It indicates that the responder has closed the Sequence.</p>
 * 
 * The following infoset defines its syntax:
 * <p><blockquote><pre>
 * &lt;wsrm:CloseSequenceResponse ...&gt;
 *     &lt;wsrm:Identifier ...&gt; xs:anyURI &lt;/wsrm:Identifier&gt;
 *     ... 
 * &lt;/wsrm:CloseSequenceResponse&gt;
 * </pre></blockquote></p>
 *  
 * @author richard.opalka@jboss.com
 */
public interface RMCloseSequenceResponse extends RMSerializable
{
   /**
    * The responder (RM Source or RM Destination) MUST include this element in any
    * <b>CloseSequenceResponse</b> message it sends. The responder MUST set the value of this
    * element to the absolute URI (conformant with RFC3986) of the closing Sequence.
    * @param identifier
    */
   void setIdentifier(String identifier);

   /**
    * Getter
    * @return sequence identifier
    */
   String getIdentifier();
}
