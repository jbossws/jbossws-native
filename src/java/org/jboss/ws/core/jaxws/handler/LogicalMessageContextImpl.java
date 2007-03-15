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
package org.jboss.ws.core.jaxws.handler;

// $Id:LogicalMessageContextImpl.java 888 2006-09-02 00:37:13Z thomas.diesler@jboss.com $

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.LogicalMessage;
import javax.xml.ws.handler.LogicalMessageContext;

/**
 * The LogicalMessageContext interface extends MessageContext to provide access to a the 
 * contained message as a protocol neutral LogicalMessage.
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 31-Aug-2006
 */
public class LogicalMessageContextImpl extends MessageContextJAXWS implements LogicalMessageContext
{
   // The LogicalMessage in this message context
   private LogicalMessage logicalMessage;

   public LogicalMessageContextImpl(SOAPMessageContextJAXWS soapContext)
   {
      super(soapContext);
      
      SOAPMessage soapMessage = soapContext.getMessage();
      logicalMessage = new LogicalMessageImpl(soapMessage);
   }

   /**
    * Gets the message from this message context
    * @return  The contained message; returns null if no message is present in this message context
    */
   public LogicalMessage getMessage()
   {
      return logicalMessage;
   }
}
