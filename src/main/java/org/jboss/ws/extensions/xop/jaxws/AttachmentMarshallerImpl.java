package org.jboss.ws.extensions.xop.jaxws;

import javax.activation.DataHandler;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.soap.MessageContextAssociation;
import org.jboss.ws.core.soap.SOAPMessageImpl;
import org.jboss.ws.core.soap.attachment.ContentHandlerRegistry;
import org.jboss.ws.core.soap.attachment.MimeConstants;
import org.jboss.ws.extensions.xop.XOPContext;

public class AttachmentMarshallerImpl extends AttachmentMarshaller
{
   // provide logging
   private static final Logger log = Logger.getLogger(AttachmentMarshallerImpl.class);

   static
   {
      // Load JAF content handlers
      ContentHandlerRegistry.register();
   }

   public AttachmentMarshallerImpl()
   {
      super();
   }

   public String addMtomAttachment(DataHandler dataHandler, String string, String string1)
   {
      CommonMessageContext msgContext = MessageContextAssociation.peekMessageContext();
      SOAPMessageImpl soapMessage = (SOAPMessageImpl)msgContext.getSOAPMessage();

      QName xmlName = new QName(string, string1);
      log.debug("serialize: [xmlName=" + xmlName + "]");

      String cid = soapMessage.getCidGenerator().generateFromName(xmlName.getLocalPart());
      AttachmentPart xopPart = soapMessage.createAttachmentPart(dataHandler);
      xopPart.addMimeHeader(MimeConstants.CONTENT_ID, '<' + cid + '>'); // RFC2392 requirement
      soapMessage.addAttachmentPart(xopPart);

      log.debug("Created attachment part " + cid + ", with content-type " + xopPart.getContentType());

      return "cid:" + cid;
   }

   public String addMtomAttachment(byte[] bytes, int i, int i1, String string, String string1, String string2)
   {
      throw new WSException("Not yet implemented");
   }

   public String addSwaRefAttachment(DataHandler dataHandler)
   {
      throw new WSException("Not yet implemented");
   }

   public boolean isXOPPackage()
   {
      return XOPContext.isXOPMessage();
   }
}
