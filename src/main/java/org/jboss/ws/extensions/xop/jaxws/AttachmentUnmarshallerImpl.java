package org.jboss.ws.extensions.xop.jaxws;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.activation.DataHandler;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;

import org.jboss.ws.WSException;
import org.jboss.ws.core.soap.attachment.ContentHandlerRegistry;
import org.jboss.ws.core.soap.MessageContextAssociation;
import org.jboss.ws.core.soap.SOAPMessageImpl;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.extensions.xop.XOPContext;

public class AttachmentUnmarshallerImpl extends AttachmentUnmarshaller
{

   static
   {
      // Load JAF content handlers
      ContentHandlerRegistry.register();
   }

   public AttachmentUnmarshallerImpl()
   {
      super();
   }

   public boolean isXOPPackage()
   {
      return XOPContext.isXOPMessage();           
   }

   public DataHandler getAttachmentAsDataHandler(String cid)
   {
      try
      {
         AttachmentPart part = XOPContext.getAttachmentByCID(cid);
         return part.getDataHandler();
      }
      catch (SOAPException e)
      {
         throw new WSException("Failed to access attachment part", e);
      }
   }

   public byte[] getAttachmentAsByteArray(String cid)
   {
      try
      {
         AttachmentPart part = XOPContext.getAttachmentByCID(cid);
         DataHandler dh = part.getDataHandler();
         ByteArrayOutputStream bout = new ByteArrayOutputStream();
         dh.writeTo(bout);

         return bout.toByteArray();
      }
      catch (SOAPException ex)
      {
         throw new WSException(ex);
      }
      catch (IOException e)
      {
         throw new WSException(e);
      }
   }
}
