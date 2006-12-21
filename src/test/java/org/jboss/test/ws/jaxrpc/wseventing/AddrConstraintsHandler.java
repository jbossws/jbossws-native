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
package org.jboss.test.ws.jaxrpc.wseventing;

import java.net.URI;

import javax.xml.namespace.QName;
import javax.xml.rpc.handler.GenericHandler;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.JAXWSAConstants;

import org.jboss.ws.WSException;

/**
 * A client side handler that checks the addressing constraints.
 *
 * @author Heiko Braun, <heiko@openj.net>
 * @since 13-Jan-2006
 */
public class AddrConstraintsHandler extends GenericHandler {

   URI wsaExpectedReplyAction = null;
   URI wsaExpectedRelatesTo = null;

   public QName[] getHeaders() {
      return new QName[0];
   }


   public boolean handleRequest(MessageContext msgContext) {

      // addressing constraints
      /*URI wsaTo = (URI)msgContext.getProperty(SOAPClientHandler.CLIENT_ADDRESSING_REQUEST_TO);
      URI wsaAction = (URI)msgContext.getProperty(SOAPClientHandler.CLIENT_ADDRESSING_REQUEST_ACTION);
      wsaExpectedReplyAction = (URI)msgContext.getProperty("wsa:expectedReplyAction");
      wsaExpectedRelatesTo = (URI)msgContext.getProperty("wsa:expectedRelatesTo");
      if(wsaTo == null || wsaAction == null)
         throw new IllegalArgumentException("Adressing properties are missing from message context");
       */
      return true;
   }

   public boolean handleResponse(MessageContext msgContext) {

      AddressingProperties addrProps = (AddressingProperties)
            msgContext.getProperty(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_INBOUND);
      if (addrProps == null)
         throw new WSException("Cannot obtain AddressingProperties");

      assertEquals(wsaExpectedReplyAction, getReplyAction(addrProps));
      assertEquals(wsaExpectedRelatesTo, getRelatesTo(addrProps));

      return true;

   }

   private void assertEquals(URI expectedURI, URI actualURI) {
      if ( (expectedURI != null && actualURI!=null) && !expectedURI.equals(actualURI))
         throw new IllegalArgumentException(
               "Received : " + actualURI.toString() + ", but expected " + expectedURI.toString()
         );
   }

   private URI getReplyAction(AddressingProperties addrProps) {
      if(null == addrProps.getAction())
         throw new IllegalArgumentException("wsa:action not set on reply");
      return addrProps.getAction().getURI();
   }

   private URI getRelatesTo(AddressingProperties addrProps) {
      boolean containsRelationShip = (addrProps.getRelatesTo() != null && addrProps.getRelatesTo().length>1);
      URI wsaRelatesTo = containsRelationShip ? addrProps.getRelatesTo()[0].getID() : null;
      return wsaRelatesTo;
   }
}
