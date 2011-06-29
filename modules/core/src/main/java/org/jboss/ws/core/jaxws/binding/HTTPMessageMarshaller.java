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
package org.jboss.ws.core.jaxws.binding;

import java.io.IOException;
import java.util.ResourceBundle;
import org.jboss.ws.api.util.BundleUtils;
import java.io.OutputStream;

import org.jboss.logging.Logger;
import org.jboss.ws.core.HTTPMessageImpl;
import org.jboss.ws.core.client.Marshaller;
import org.jboss.ws.common.DOMWriter;
import org.w3c.dom.Element;

/**
 * @author Thomas.Diesler@jboss.org
 * @author alessio.soldano@jboss.com
 * @since 25-Nov-2004
 */
public class HTTPMessageMarshaller implements Marshaller
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(HTTPMessageMarshaller.class);
   // Provide logging
   private static Logger log = Logger.getLogger(HTTPMessageMarshaller.class);

   /**
    * Marshaller will need to take the dataObject and convert
    * into primitive java data types and write to the
    * given output.
    *
    * @param dataObject Object to be writen to output
    * @param output     The data output to write the object
    *                   data to.
    */
   public void write(Object dataObject, OutputStream output) throws IOException
   {
      if ((dataObject instanceof HTTPMessageImpl) == false)
         throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "NOT_A_HTTPMESSAGE",  dataObject));

      HTTPMessageImpl httpMessage = (HTTPMessageImpl)dataObject;
      Element root = httpMessage.getXmlFragment().toElement();

      // debug the outgoing message
      if (log.isTraceEnabled())
      {
         log.trace("Outgoing Message\n" + DOMWriter.printNode(root, true));
      }

      new DOMWriter(output).print(root);
   }
}
