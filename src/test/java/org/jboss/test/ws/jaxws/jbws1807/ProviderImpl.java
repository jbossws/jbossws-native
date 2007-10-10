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
package org.jboss.test.ws.jaxws.jbws1807;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.BindingType;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.http.HTTPBinding;

import org.jboss.logging.Logger;
import org.jboss.wsf.common.DOMUtils;
import org.jboss.wsf.common.DOMWriter;

@WebServiceProvider(wsdlLocation = "WEB-INF/wsdl/provider.wsdl", portName = "ProviderPort", serviceName = "Provider", targetNamespace = "http://ws.com/")
@ServiceMode(value = Service.Mode.PAYLOAD)
@BindingType(value = HTTPBinding.HTTP_BINDING)
public class ProviderImpl implements Provider<Source>
{
   // provide logging
   private final static Logger log = Logger.getLogger(ProviderImpl.class);

   public Source invoke(Source source)
   {
      try
      {
         String input = DOMWriter.printNode(DOMUtils.sourceToElement(source), false);
         log.info("invoke: " + input);
         
         String reply = "<reply>" + input + "</reply>";
         return new StreamSource(new ByteArrayInputStream(reply.getBytes()));
      }
      catch (IOException ex)
      {
         throw new RuntimeException(ex);
      }
   }
}