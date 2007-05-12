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
package org.jboss.test.ws.jaxws.samples.handlerchain;

import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.logging.Logger;
import org.jboss.ws.core.jaxws.handler.GenericSOAPHandler;

/**
 * A client side handler
 *
 * @author Thomas.Diesler@jboss.org
 * @since 08-Oct-2005
 */
public class ClientMimeHandler extends GenericSOAPHandler
{
   // Provide logging
   private static Logger log = Logger.getLogger(ClientMimeHandler.class);

   protected boolean handleOutbound(MessageContext msgContext)
   {
      log.info("handleOutbound");

      SOAPMessage soapMessage = ((SOAPMessageContext)msgContext).getMessage();
      MimeHeaders mimeHeaders = soapMessage.getMimeHeaders();
      mimeHeaders.setHeader("Cookie", "client-cookie=true");

      return true;
   }

   protected boolean handleInbound(MessageContext msgContext)
   {
      log.info("handleInbound");

      SOAPMessage soapMessage = ((SOAPMessageContext)msgContext).getMessage();
      MimeHeaders mimeHeaders = soapMessage.getMimeHeaders();
      String[] cookies = mimeHeaders.getHeader("Set-Cookie");
      if (cookies == null || cookies.length != 1 || !cookies[0].equals("server-cookie=true"))
         throw new IllegalStateException("Unexpected cookie list: " + mimeHeaders);

      return true;
   }
}
