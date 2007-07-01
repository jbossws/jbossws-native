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
package org.jboss.test.ws.jaxws.jbws1283;

import org.jboss.wsf.spi.test.JBossWSTest;
import org.jboss.wsf.spi.test.JBossWSTestSetup;
import org.jboss.wsf.spi.jaxws.handler.GenericSOAPHandler;
import org.jboss.test.ws.jaxws.jbws1357.JBWS1357;
import junit.framework.Test;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;
import javax.activation.DataHandler;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Attachment dropped on outbound messages if they have been
 * added thorugh a handler
 *
 * http://jira.jboss.org/jira/browse/JBWS-1283
 * 
 * @version $Revision:1370 $
 */
public class JBWS1283TestCase extends JBossWSTest
{
   private String targetNS = "http://org.jboss.test.ws/jbws1283";
   private JBWS1283Endpoint port;

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS1283TestCase.class, "jaxws-jbws1283.jar");
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      QName serviceName = new QName(targetNS, "JBWS1283Service");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws1283/JBWS1283EndpointImpl?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      port = (JBWS1283Endpoint)service.getPort(JBWS1283Endpoint.class);
   }

   public void testAttachmentResponse() throws Exception
   {
		// Add a client-side handler that verifes existence of the attachment
		 BindingProvider bindingProvider = (BindingProvider)port;
      List<Handler> handlerChain = new ArrayList<Handler>();
      handlerChain.add(new VerifyAttachmentHandler());
      bindingProvider.getBinding().setHandlerChain(handlerChain);

		port.requestAttachmentData();
   }

	// handler that verifies the attachment that have been added on the server-side
	class VerifyAttachmentHandler extends GenericSOAPHandler
	{

		protected boolean handleInbound(MessageContext msgContext)
		{
			SOAPMessage soapMessage = ((SOAPMessageContext)msgContext).getMessage();
			Iterator it = soapMessage.getAttachments();
			while(it.hasNext())
			{
				try
				{
					AttachmentPart attachment = (AttachmentPart)it.next();
					System.out.println("Recv " + attachment.getContentType() + " attachment:");
					System.out.println("'"+attachment.getContent()+"'");
					return true;
				}
				catch (SOAPException e)
				{
					throw new RuntimeException("Failed to access attachment data");
				}
			}

			throw new IllegalStateException("Missing attachment on the client side");
		}
	}

}
