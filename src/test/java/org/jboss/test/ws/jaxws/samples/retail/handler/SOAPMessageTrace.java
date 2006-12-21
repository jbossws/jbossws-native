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
package org.jboss.test.ws.jaxws.samples.retail.handler;

import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.jaxws.handler.GenericSOAPHandler;
import org.jboss.ws.core.soap.MessageContextAssociation;
import org.jboss.ws.core.soap.SOAPElementImpl;
import org.jboss.ws.core.soap.SOAPElementWriter;
import org.jboss.ws.core.soap.SOAPMessageImpl;
import org.jboss.logging.Logger;

import javax.xml.ws.handler.MessageContext;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPEnvelope;

public class SOAPMessageTrace extends GenericSOAPHandler
{
   private static final Logger log = Logger.getLogger(SOAPMessageTrace.class);

   private Timer timer = Timer.getInstance();

   @Override
   public boolean handleInbound(MessageContext msgContext)
   {
      timer.push(System.currentTimeMillis());
      return trace();
   }

   @Override
   public boolean handleOutbound(MessageContext msgContext)
   {
      trace();
      log.info("Exectime time: " + timer.pop() + " ms");
      return true;
   }

   private boolean trace() {
      String envStr = getCurrentSOAPEnvelope();
      if(envStr!=null)
      {
         log.info("\n"+envStr);
      }

      return true;
   }

   /**
    * Dump the current message into a string
    */
   private String getCurrentSOAPEnvelope()
   {

      String env = null;
      CommonMessageContext msgContext = MessageContextAssociation.peekMessageContext();

      try
      {
         SOAPMessageImpl soapMsg = (SOAPMessageImpl)msgContext.getSOAPMessage();
         SOAPEnvelope soapReqEnv = soapMsg.getSOAPPart().getEnvelope();
         env = SOAPElementWriter.writeElement((SOAPElementImpl)soapReqEnv, true);
      }
      catch (SOAPException e)
      {
         //
      }

      return env ;
   }

}
