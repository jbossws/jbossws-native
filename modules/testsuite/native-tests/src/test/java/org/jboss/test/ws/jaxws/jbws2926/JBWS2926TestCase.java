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
package org.jboss.test.ws.jaxws.jbws2926;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.addressing.JAXWSAConstants;
import javax.xml.ws.addressing.soap.SOAPAddressingProperties;
import javax.xml.ws.handler.MessageContext;

import junit.framework.TestCase;

import org.jboss.ws.core.jaxws.handler.SOAPMessageContextJAXWS;
import org.jboss.ws.extensions.addressing.jaxws.WSAddressingClientHandler;
import org.jboss.ws.extensions.addressing.soap.SOAPAddressingPropertiesImpl;

/**
 * JBWS2926TestCase.
 * 
 * @author <a href="ema@redhat.com">Jim Ma</a>
 */
public class JBWS2926TestCase extends TestCase
{


   public void testWsaAction() throws Exception
   {
      WSAddressingClientHandler wsHandler = new WSAddressingClientHandler();
      SOAPMessageContextJAXWS context = new SOAPMessageContextJAXWS();
      context.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, new Boolean(true));
      context.put(BindingProvider.SOAPACTION_URI_PROPERTY, "inputAction");
      context.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES, new SOAPAddressingPropertiesImpl());
      context.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND, new SOAPAddressingPropertiesImpl());
      MessageFactory factory = MessageFactory.newInstance();
      SOAPMessage soapMsg = factory.createMessage();
      context.setMessage(soapMsg);
      wsHandler.handleMessage(context);
      SOAPAddressingProperties addrProps = (SOAPAddressingProperties)context.get(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND);
      assertEquals(addrProps.getAction().getURI().toString(), "inputAction");
      
   }
}