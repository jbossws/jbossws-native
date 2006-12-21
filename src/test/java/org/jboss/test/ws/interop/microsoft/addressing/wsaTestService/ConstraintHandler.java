package org.jboss.test.ws.interop.microsoft.addressing.wsaTestService;

import org.jboss.logging.Logger;

import javax.xml.rpc.handler.GenericHandler;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.namespace.QName;
import javax.xml.ws.addressing.soap.SOAPAddressingProperties;
import javax.xml.ws.addressing.JAXWSAConstants;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeaderElement;
import java.util.Iterator;

/**
 * See http://jira.jboss.org/jira/browse/JBWS-734
 * 
 * @author Heiko Braun, <heiko@openj.net>
 * @since 08-Mar-2006
 */
public class ConstraintHandler extends GenericHandler {

   private static Logger log = Logger.getLogger(ConstraintHandler.class);

   public QName[] getHeaders() {
      return new QName[0];
   }

   public boolean handleRequest(MessageContext msgContext) {


      SOAPMessage soapMessage = ((SOAPMessageContext)msgContext).getMessage();

      SOAPHeaderElement[] scannedHeaders;
      int headerCount = 0;
      Iterator it;

      try
      {
         int numHeaders = soapMessage.getSOAPHeader().getChildNodes().getLength();
         scannedHeaders = new SOAPHeaderElement[numHeaders];
         headerCount = 0;

         it = soapMessage.getSOAPHeader().examineAllHeaderElements();
      }
      catch (SOAPException e)
      {
         log.error("Failed to examine header elements",e );
         return false;
      }

      while(it.hasNext()) {

         SOAPHeaderElement currentHeader = (SOAPHeaderElement)it.next();
         for(SOAPHeaderElement previousHeader : scannedHeaders)
         {
            if(previousHeader !=null &&
                  previousHeader.getElementName().equals(currentHeader.getElementName()))
            {
               throw new SOAPFaultException(
                     new QName("http://www.w3.org/2005/08/addressing", "InvalidAddressingHeader", "wsa"),
                     "A header representing a Message Addressing Property is not valid and the message cannot be processed",
                     null, null
               );
            }
         }

         scannedHeaders[headerCount] = currentHeader;
         headerCount++;

      }

      return true;
   }
}
