/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.ws.core.soap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.ResourceBundle;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.jboss.logging.Logger;
import org.jboss.ws.core.soap.BundleUtils;
import org.jboss.ws.core.client.UnMarshaller;

/**
 * @author Thomas.Diesler@jboss.org
 * @author alessio.soldano@jboss.com
 * @since 25-Nov-2004
 */
public class SOAPMessageUnMarshaller implements UnMarshaller
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(SOAPMessageUnMarshaller.class);
   // Provide logging
   private static Logger log = Logger.getLogger(SOAPMessageUnMarshaller.class);

   public Object read(InputStream inputStream, Map<String, Object> metadata, Map<String, Object> headers) throws IOException
   {
      if (log.isTraceEnabled())
         log.trace("Read input stream with metadata=" + metadata);

      try
      {
         SOAPMessage soapMsg = getMessageFactory().createMessage(null, inputStream, false);

         return soapMsg;
      }
      catch (SOAPException e)
      {
         log.error(BundleUtils.getMessage(bundle, "CANNOT_UNMARSHALL_SOAPMESSAGE"),  e);
         IOException e2 = new IOException(e.toString());
         e2.initCause(e);
         throw e2;
      }
   }

   protected MessageFactoryImpl getMessageFactory()
   {
      return new MessageFactoryImpl();
   }
}
