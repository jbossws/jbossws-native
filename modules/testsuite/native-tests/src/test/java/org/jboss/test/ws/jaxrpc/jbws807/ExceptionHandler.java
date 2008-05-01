package org.jboss.test.ws.jaxrpc.jbws807;

import javax.xml.namespace.QName;
import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.handler.GenericHandler;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;

/**
 * @author Heiko Braun, <heiko@openj.net>
 * @since 12-Apr-2006
 */
public class ExceptionHandler extends GenericHandler {

    public QName[] getHeaders() {
        return new QName[0];
    }

    public boolean handleFault(MessageContext messageContext) {
        try
      {
         SOAPMessage soapMessage = ((SOAPMessageContext)messageContext).getMessage();

         SOAPFault soapFault = soapMessage.getSOAPBody().getFault();
         soapFault.setFaultString("ExceptionHandler processed this message");
         return true;

      }
      catch (SOAPException e)
      {
         throw new JAXRPCException(e.toString(), e);
      }
    }
}
