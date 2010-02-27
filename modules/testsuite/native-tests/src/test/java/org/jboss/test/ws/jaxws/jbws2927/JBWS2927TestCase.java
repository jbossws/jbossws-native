/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.test.ws.jaxws.jbws2927;

import java.net.URL;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.addressing.JAXWSAConstants;

import org.jboss.ws.core.jaxws.handler.SOAPMessageContextJAXWS;
import org.jboss.ws.extensions.addressing.jaxws.WSAddressingClientHandler;
import org.jboss.ws.extensions.addressing.soap.SOAPAddressingPropertiesImpl;

import org.jboss.wsf.test.JBossWSTest;

/**
 * A JBWS2927TestCase.
 * 
 * @author <a href="ema@redhat.com">Jim Ma</a>
 */
public class JBWS2927TestCase extends JBossWSTest
{
   public void testHandleInboundMessage() throws Exception
   {
      WSAddressingClientHandler wsHandler = new WSAddressingClientHandler();
      SOAPMessageContextJAXWS context = new SOAPMessageContextJAXWS();
      context.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, new Boolean(false));
      context.put(BindingProvider.SOAPACTION_URI_PROPERTY, "inputAction");
      context.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES, new SOAPAddressingPropertiesImpl());
      context.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND, new SOAPAddressingPropertiesImpl());
      MessageFactory factory = MessageFactory.newInstance();
      
      URL reqMessage = getResourceFile("jaxws/jbws2927/request-message.xml").toURL();
      MessageFactory msgFactory = MessageFactory.newInstance();

      SOAPMessage soapMsg = msgFactory.createMessage(null, reqMessage.openStream());
      context.setMessage(soapMsg);
  
      wsHandler.handleMessage(context);
   }
}
