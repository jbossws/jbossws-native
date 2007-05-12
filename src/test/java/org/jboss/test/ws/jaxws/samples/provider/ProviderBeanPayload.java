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
package org.jboss.test.ws.jaxws.samples.provider;

// $Id$

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import javax.jws.HandlerChain;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceProvider;

/**
 * Test a Provider<Source>
 *
 * @author Thomas.Diesler@jboss.org
 * @since 29-Jun-2006
 */

@HandlerChain(file = "provider-handlers.xml")
@WebServiceProvider(serviceName = "ProviderService", portName = "ProviderPort", targetNamespace = "http://org.jboss.ws/provider", wsdlLocation = "WEB-INF/wsdl/Provider.wsdl")
// @ServiceMode(value = Service.Mode.PAYLOAD) - PAYLOAD is implicit
public class ProviderBeanPayload implements Provider<Source>
{
   public Source invoke(Source req)
   {
      try
      {
         Transformer transformer = TransformerFactory.newInstance().newTransformer();
         transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
         transformer.setOutputProperty(OutputKeys.METHOD, "xml");
         OutputStream out = new ByteArrayOutputStream();
         StreamResult streamResult = new StreamResult();
         streamResult.setOutputStream(out);
         transformer.transform(req, streamResult);
         String xmlReq = streamResult.getOutputStream().toString();

         String expReq = "<ns1:somePayload xmlns:ns1=\"http://org.jboss.ws/provider\">Hello:InboundLogicalHandler</ns1:somePayload>";
         if (!expReq.equals(xmlReq))
            throw new WebServiceException("Unexpected payload: " + xmlReq);

         return new StreamSource(new ByteArrayInputStream(xmlReq.getBytes()));
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception e)
      {
         throw new WebServiceException(e);
      }
   }

}