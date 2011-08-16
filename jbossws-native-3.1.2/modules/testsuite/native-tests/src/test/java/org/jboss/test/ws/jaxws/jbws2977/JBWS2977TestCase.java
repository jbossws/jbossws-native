/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws2977;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import junit.framework.Test;

import org.jboss.ws.WSException;
import org.jboss.wsf.common.handler.GenericSOAPHandler;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * A JBWS2977TestCase.
 * 
 * @author <a href="ema@redhat.com">Jim Ma</a>
 */
public class JBWS2977TestCase extends JBossWSTest
{

   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-jbws2977";

   public String actionURL = null;

   private static AddNumbers port;

   public static Test suite() throws Exception
   {
      return new JBossWSTestSetup(JBWS2977TestCase.class, "jaxws-jbws2977.war");
   }

   public void setUp() throws Exception
   {
      super.setUp();
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
      QName serviceName = new QName("http://ws.jboss.org", "AddNumbers");

      Service service = Service.create(wsdlURL, serviceName);
      port = service.getPort(AddNumbers.class);
   }

   public void testCall()
   {
      List<Handler> handlers = new ArrayList<Handler>();
      handlers.add(new ClientSOAPHandler());
      ((BindingProvider) port).getBinding().setHandlerChain(handlers);
      try
      {
         port.addNumbersFault1(1, 0);
      }
      catch (AddNumbersException e)
      {
         // do nothing
      }

      assertEquals("http://faultAction", actionURL);
   }

   class ClientSOAPHandler extends GenericSOAPHandler<LogicalMessageContext>
   {
      @Override
      public boolean handleFault(MessageContext msgContext)
      {
         try
         {
            SOAPMessageContext smc = (SOAPMessageContext) msgContext;
            SOAPMessage message = smc.getMessage();
            Iterator iterator = message.getSOAPHeader().getChildElements(
                  new QName("http://www.w3.org/2005/08/addressing", "Action"));
            if (iterator.hasNext())
            {
               SOAPElement element = (SOAPElement) iterator.next();
               actionURL = element.getValue();
            }
         }
         catch (SOAPException e)
         {
            throw new WSException("Error in Handler", e);
         }
         return true;
      }
   }

}