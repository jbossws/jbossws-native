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

   /**
    * @param data - represents the data to be attached. Must be non-null.
    * @param elementNamespace - the namespace URI of the element that encloses the base64Binary data. Can be empty but never null.
    * @param elementLocalName - The local name of the element. Always a non-null valid string.
    *
    * @return content-id URI, cid, to the attachment containing data or null if data should be inlined.
    */
   public String addMtomAttachment(DataHandler data, String elementNamespace, String elementLocalName)
   {
      CommonMessageContext msgContext = MessageContextAssociation.peekMessageContext();
      SOAPMessageImpl soapMessage = (SOAPMessageImpl)msgContext.getSOAPMessage();

      QName xmlName = new QName(elementNamespace, elementLocalName);
      log.debug("serialize: [xmlName=" + xmlName + "]");

      String cid = soapMessage.getCidGenerator().generateFromName(xmlName.getLocalPart());
      AttachmentPart xopPart = soapMessage.createAttachmentPart(data);
      xopPart.addMimeHeader(MimeConstants.CONTENT_ID, '<' + cid + '>'); // RFC2392 requirement
      soapMessage.addAttachmentPart(xopPart);

      log.debug("Created attachment part " + cid + ", with content-type " + xopPart.getContentType());

      return "cid:" + cid;
   }

   /**
    * @param data - represents the data to be attached. Must be non-null. The actual data region is specified by (data,offset,length) tuple.
    * @param offset - The offset within the array of the first byte to be read; must be non-negative and no larger than array.length
    * @param length - The number of bytes to be read from the given array; must be non-negative and no larger than array.length
    * @param mimeType - If the data has an associated MIME type known to JAXB, that is passed as this parameter. If none is known, "application/octet-stream". This parameter may never be null.
    * @param elementNamespace - the namespace URI of the element that encloses the base64Binary data. Can be empty but never null.
    * @param elementLocalName - The local name of the element. Always a non-null valid string.
    *
    * @return content-id URI, cid, to the attachment containing data or null if data should be inlined.
    */
   public String addMtomAttachment(byte[] data, int offset, int length,
                                   String mimeType, String elementNamespace, String elementLocalName)
   {

      if(true)
         mimeType = null; // ignore the mime type. otherwise the content handlers will fail

      String contentType = mimeType != null ? mimeType : "application/octet-stream";
      DataHandler dh = new DataHandler(data, contentType);
      return addMtomAttachment(dh, elementNamespace, elementLocalName);
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
