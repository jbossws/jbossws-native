package org.jboss.test.ws.jaxws.xop.doclit;

import org.jboss.logging.Logger;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.jaxws.handler.GenericSOAPHandler;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import java.io.ByteArrayOutputStream;

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
         SOAPMessage msg = ((CommonMessageContext)context).getSOAPMessage();
         ByteArrayOutputStream bout = new ByteArrayOutputStream();
         msg.writeTo(bout);
         log.info(bout.toString());
      } catch (Exception e) {
         throw new RuntimeException(e);
      }

      return true;
   }
}