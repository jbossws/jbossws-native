package org.jboss.test.ws.jaxws.samples.xop.doclit;

import org.jboss.logging.Logger;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.jaxws.handler.GenericSOAPHandler;
import org.jboss.ws.core.soap.SOAPElementImpl;
import org.jboss.ws.core.soap.SOAPElementWriter;
import org.jboss.ws.core.soap.SOAPMessageImpl;

import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.ws.handler.MessageContext;

/**
 * A MTOM handler should see the conceptual payload,
 * which means an inlined representation of the binary data.
 */
class MTOMProtocolHandler extends GenericSOAPHandler
{
   private Logger log = Logger.getLogger(MTOMProtocolHandler.class);

   protected boolean handleOutbound(MessageContext msgContext) {
      return dumpMessage(msgContext);
   }

   protected boolean handleInbound(MessageContext msgContext) {
      return dumpMessage(msgContext);
   }

   private boolean dumpMessage(MessageContext context) {
      try
      {
         CommonMessageContext msgContext = (CommonMessageContext)context;
         SOAPMessageImpl soapMsg = (SOAPMessageImpl)msgContext.getSOAPMessage();
         SOAPEnvelope soapReqEnv = soapMsg.getSOAPPart().getEnvelope();
         String xml = SOAPElementWriter.writeElement((SOAPElementImpl)soapReqEnv, true);
         log.info(xml.substring(0, 50)+"[...]");
      }
      catch (SOAPException e)
      {
         //
      }

      return true;
   }
}