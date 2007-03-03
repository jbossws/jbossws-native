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
package org.jboss.ws.core.soap.attachment;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.activation.DataHandler;
import javax.mail.internet.MimeMultipart;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;

import org.jboss.util.NotImplementedException;

/**
 * Implementation of the <code>AttachmentPart</code> interface.
 * @see javax.xml.soap.AttachmentPart
 *
 * @author <a href="mailto:jason@stacksmash.com">Jason T. Greene</a>
 * @author Thomas.Diesler@jboss.org
 */
public class AttachmentPartImpl extends AttachmentPart
{
   private MimeHeaders mimeHeaders = new MimeHeaders();

   private DataHandler dataHandler;

   private String cachedContentId;

   private String cachedContentType;

   private String cachedContentLocation;

   static
   {
      // Load JAF content handlers
      ContentHandlerRegistry.register();
   }

   public AttachmentPartImpl()
   {
   }

   public AttachmentPartImpl(DataHandler handler)
   {
      this.dataHandler = handler;
   }

   private void clearHeaderCache()
   {
      cachedContentId = null;
      cachedContentType = null;
      cachedContentLocation = null;
   }

   public void addMimeHeader(String name, String value)
   {
      clearHeaderCache();
      mimeHeaders.addHeader(name, value);
   }

   public void clearContent()
   {
      dataHandler = null;
   }

   public Iterator getAllMimeHeaders()
   {
      clearHeaderCache();
      return mimeHeaders.getAllHeaders();
   }

   public Object getContent() throws SOAPException
   {
      if (dataHandler == null)
         throw new SOAPException("No content available");

      try
      {
         return dataHandler.getContent();
      }
      catch (IOException e)
      {
         throw new SOAPException(e);
      }
   }

   public DataHandler getDataHandler() throws SOAPException
   {
      if (dataHandler == null)
         throw new SOAPException("No data handler on attachment");

      return dataHandler;
   }

   public Iterator getMatchingMimeHeaders(String[] names)
   {
      clearHeaderCache();
      return mimeHeaders.getMatchingHeaders(names);
   }

   public String[] getMimeHeader(String name)
   {
      return mimeHeaders.getHeader(name);
   }

   /**
    * Returns the first occurence of a MIME header.
    *
    * @param header the mime header
    * @return the value of the first occurence of a MIME header
    */
   public String getFirstMimeHeader(String header)
   {
      String[] values = mimeHeaders.getHeader(header.toLowerCase());
      if ((values != null) && (values.length > 0))
      {
         return values[0];
      }
      return null;
   }

   public Iterator getNonMatchingMimeHeaders(String[] names)
   {
      clearHeaderCache();
      return mimeHeaders.getNonMatchingHeaders(names);
   }

   public int getSize() throws SOAPException
   {
      if (dataHandler == null)
         return 0;

      // In order to be accurate this method must be somewhat inefficient
      // TODO optimize this for specific data sources
      int count = 0, ret = 0;
      byte[] buffer = new byte[256];

      try
      {
         InputStream stream = dataHandler.getInputStream();

         do
         {
            count += ret;
            ret = stream.read(buffer);
         }
         while (ret != -1);
      }
      catch (IOException e)
      {
         throw new SOAPException(e);
      }

      return count;
   }

   public void removeAllMimeHeaders()
   {
      clearHeaderCache();
      mimeHeaders.removeAllHeaders();
   }

   public void removeMimeHeader(String name)
   {
      clearHeaderCache();
      mimeHeaders.removeHeader(name);
   }

   public void setContent(Object object, String contentType)
   {

      // Override the content type if its a mime multipart object because we need to preserve
      // the all of the content type parameters
      if (object instanceof MimeMultipart)
         contentType = ((MimeMultipart)object).getContentType();

      dataHandler = new DataHandler(object, contentType);

      setContentType(contentType);
   }

   public void setDataHandler(DataHandler dataHandler)
   {
      if (dataHandler == null)
         throw new IllegalArgumentException("Null data handler");

      this.dataHandler = dataHandler;
      setContentType(dataHandler.getContentType());
   }

   public void setMimeHeader(String name, String value)
   {
      clearHeaderCache();
      mimeHeaders.setHeader(name, value);
   }

   public String getContentId()
   {
      if (cachedContentId == null)
      {
         cachedContentId = getFirstMimeHeader(MimeConstants.CONTENT_ID);
      }

      return cachedContentId;
   }

   public String getContentLocation()
   {
      if (cachedContentLocation == null)
      {
         cachedContentLocation = getFirstMimeHeader(MimeConstants.CONTENT_LOCATION);
      }

      return cachedContentLocation;
   }

   public String getContentType()
   {
      if (cachedContentType == null)
      {
         cachedContentType = getFirstMimeHeader(MimeConstants.CONTENT_TYPE);
      }

      return cachedContentType;
   }

   public void setContentId(String contentId)
   {
      setMimeHeader(MimeConstants.CONTENT_ID, contentId);
      cachedContentId = contentId;
   }

   public void setContentLocation(String contentLocation)
   {
      setMimeHeader(MimeConstants.CONTENT_LOCATION, contentLocation);
      cachedContentLocation = contentLocation;
   }

   public void setContentType(String contentType)
   {
      setMimeHeader(MimeConstants.CONTENT_TYPE, contentType);
      cachedContentType = contentType;
   }

   @Override
   public InputStream getBase64Content() throws SOAPException
   {
      //TODO: SAAJ 1.3
      throw new NotImplementedException();
   }

   @Override
   public InputStream getRawContent() throws SOAPException
   {
      //TODO: SAAJ 1.3
      throw new NotImplementedException();
   }

   @Override
   public byte[] getRawContentBytes() throws SOAPException
   {
      //TODO: SAAJ 1.3
      throw new NotImplementedException();
   }

   @Override
   public void setBase64Content(InputStream content, String contentType) throws SOAPException
   {
      //TODO: SAAJ 1.3
      throw new NotImplementedException();
   }

   @Override
   public void setRawContent(InputStream content, String contentType) throws SOAPException
   {
      //TODO: SAAJ 1.3
      throw new NotImplementedException();
   }

   @Override
   public void setRawContentBytes(byte[] content, int offset, int len, String contentType) throws SOAPException
   {
      //TODO: SAAJ 1.3
      throw new NotImplementedException();
   }
}