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
package org.jboss.ws.core.soap;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.soap.SOAPMessage;

import org.jboss.logging.Logger;
import org.jboss.remoting.InvocationRequest;
import org.jboss.remoting.invocation.OnewayInvocation;
import org.jboss.remoting.marshal.Marshaller;

/**
 * @author Thomas.Diesler@jboss.org
 * @since 25-Nov-2004
 */
public class SOAPMessageMarshaller implements Marshaller
{
   // Provide logging
   private static Logger log = Logger.getLogger(SOAPMessageMarshaller.class);

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
      if (dataObject instanceof InvocationRequest)
         dataObject = ((InvocationRequest)dataObject).getParameter();

      if (dataObject instanceof OnewayInvocation)
         dataObject = ((OnewayInvocation)dataObject).getParameters()[0];

      if ((dataObject instanceof SOAPMessage) == false)
         throw new IllegalArgumentException("Not a SOAPMessage: " + dataObject);

      SOAPMessageImpl soapMessage = (SOAPMessageImpl)dataObject;
      soapMessage.writeTo(output);
   }

   public Marshaller cloneMarshaller() throws CloneNotSupportedException
   {
      return new SOAPMessageMarshaller();
   }
}
