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
package org.jboss.test.ws.jaxrpc.jbws2885;

import java.rmi.RemoteException;
import org.jboss.logging.Logger;

/**
 * [JBWS-2885] http://jira.jboss.com/jira/browse/JBWS-2885
 * 
 * Service Endpoint Interface.
 * 
 * @author darran.lofthouse@jboss.com
 * @since Mar 10, 2010
 */
public class TestEndpointImpl implements TestEndpoint
{

   private static final Logger log = Logger.getLogger(TestEndpointImpl.class);

   public static final String REQUEST_TEXT = " <Text>abcDEFghiJKLmnoPQRstuVWXyz</Text> ";

   public static final String RESPONSE_TEXT = " <Numbers>1234567890</Numbers> ";
   
   public static final String WRAPPED_REQUEST_TEXT = "<![CDATA[" + REQUEST_TEXT+ "]]>";
   
   public static final String WRAPPED_RESPONSE_TEXT = "<![CDATA[" + RESPONSE_TEXT + "]]>";
   
   public Message echo(Message message) throws RemoteException
   {
      String text = message.getText();
      log.debug("Message received '" + text + "'");

      if (REQUEST_TEXT.equals(text) == false)
      {
         throw new IllegalArgumentException("Wrong message received");
      }

      Message response = new Message();
      response.setText(WRAPPED_RESPONSE_TEXT);
      return response;
   }

}
