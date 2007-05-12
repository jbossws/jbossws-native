package org.jboss.test.ws.jaxrpc.xop.rpclit;

import javax.xml.namespace.QName;
import javax.xml.rpc.handler.GenericHandler;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPMessage;

import org.jboss.logging.Logger;
import org.jboss.ws.core.soap.NameImpl;

/**
 * @author Heiko Braun <heiko.braun@jboss.com>
 * @since Jun 16, 2006
 */
public class InlineHandler extends GenericHandler
{
   HandlerInfo config;

   private static Logger log = Logger.getLogger(InlineHandler.class);

   public QName[] getHeaders()
   {
      return new QName[0];
   }

   public void init(HandlerInfo config) {
      this.config = config;
   }

   public boolean handleRequest(MessageContext messageContext)
   {
      dumpMessage(messageContext, "parameters");
      return true;
   }

   public boolean handleResponse(MessageContext messageContext)
   {
      dumpMessage(messageContext, "result");
      return true;
   }

   private void dumpMessage(MessageContext messageContext, String parentName)
   {
      try
      {
         SOAPMessage soapMessage = ((SOAPMessageContext)messageContext).getMessage();
         SOAPBody soapBody = soapMessage.getSOAPBody();

         SOAPElement bodyElement = (SOAPElement)soapBody.getChildElements().next();
         SOAPElement wrapper = (SOAPElement)bodyElement.getChildElements(new NameImpl(parentName)).next();
         SOAPElement xopElement = (SOAPElement)wrapper.getChildElements(new NameImpl("xopContent")).next();
         String base64Value = xopElement.getValue();
         log.debug("base64Value: " + base64Value);
         messageContext.setProperty("xop.inline.value", base64Value);
      }
      catch (Exception e)
      {
         // ingore, happens when simple types are send
      }
   }
}
