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
package org.jboss.ws.core.soap.attachment;

import org.jboss.ws.core.soap.BundleUtils;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataContentHandler;
import javax.activation.DataSource;
import javax.xml.transform.stream.StreamSource;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ResourceBundle;

/**
 * Dummy DataContentHandler only for tagging MailcapCommandMap objects containing
 * JBoss specific data handlers.  Solution to bz-1104273 issue with JDK1.7.0_55
 *
 * Date: 12/15/14
 */
public class DummyDataContentHandler implements DataContentHandler
{
   public static final String DUMMY_DATA_FLAVOR = "dummyDataFlavor/xml";
   private static final ResourceBundle bundle = BundleUtils.getBundle(XmlDataContentHandler.class);
   private DataFlavor[] flavors = new ActivationDataFlavor[]
      {
         new ActivationDataFlavor(StreamSource.class, DUMMY_DATA_FLAVOR, "XML")
      };

   /**
    * Returns a {@link StreamSource} from the specified
    * data source.
    *
    * @param ds the activation datasource
    * @return an XML stream source
    */
   @Override
   public Object getContent(DataSource ds) throws IOException
   {
      return null;
   }

   /**
    * Returns a {@link StreamSource} from the specified
    * data source. The flavor must be one of the ones returned by {@link #getTransferDataFlavors()}.
    *
    * @param df the flavor specifiying the mime type of ds
    * @param ds the activation data source
    * @return an XML stream source
    */
   @Override
   public Object getTransferData(DataFlavor df, DataSource ds) throws UnsupportedFlavorException, IOException
   {
      return null;
   }

   /**
    * Returns the acceptable data flavors that this content handler supports.
    *
    * @return array of <code>ActivationDataHandlers</code>
    */
   @Override
   public DataFlavor[] getTransferDataFlavors()
   {
      return flavors;
   }

   /**
    * Writes the passed in {@link StreamSource} object using the specified
    * mime type to the specified output stream. The mime type must be text/xml.
    *
    * @param obj an XML stream source
    * @param mimeType the string "text/xml"
    * @param os the output stream to write this xml stream to
    */
   @Override
   public void writeTo(Object obj, String mimeType, OutputStream os) throws IOException
   {
      // take no action
   }
}
