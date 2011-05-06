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
package org.jboss.test.ws.jaxws.jbws1909;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.management.ObjectName;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.jboss.logging.Logger;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.invocation.RequestHandler;
import org.jboss.wsf.spi.management.EndpointRegistry;
import org.jboss.wsf.spi.management.EndpointRegistryFactory;
import org.jboss.ws.api.util.DOMUtils;
import org.w3c.dom.Element;

@WebService(serviceName = "TestEndpointService", name = "TestEndpoint", targetNamespace = "http://org.jboss.ws/jbws1909")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT)
@Stateless
public class TestEndpointImpl implements TestEndpoint
{
   // provide logging
   private final static Logger log = Logger.getLogger(TestEndpointImpl.class);

   @Resource
   WebServiceContext context;

   @WebMethod
   public String echo(String input)
   {
      MessageContext msgContext = context.getMessageContext();
      for (String key : msgContext.keySet())
      {
         log.info(key + "=" + msgContext.get(key));
      }

      ClassLoader cl = ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader();
      SPIProvider spiProvider = SPIProviderResolver.getInstance(cl).getProvider();
      EndpointRegistry registry = spiProvider.getSPI(EndpointRegistryFactory.class, cl).getEndpointRegistry();
      
      try
      {
         ObjectName oname = new ObjectName("jboss.ws:context=jaxws-jbws1909,endpoint=TestEndpointImpl");
         Endpoint endpoint = registry.getEndpoint(oname);
         RequestHandler reqHandler = endpoint.getRequestHandler();
         
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         reqHandler.handleWSDLRequest(endpoint, baos, null); // The context is null
         
         ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
         Element root = DOMUtils.parse(bais, getDocumentBuilder());
         Element serviceEl = DOMUtils.getFirstChildElement(root, new QName("http://schemas.xmlsoap.org/wsdl/", "service"));
         String serviceName = DOMUtils.getAttributeValue(serviceEl, "name");
         
         input += "|" + serviceName;
      }
      catch (Exception ex)
      {
         throw new RuntimeException(ex);
      }
      return input;
   }
   
   private DocumentBuilder getDocumentBuilder()
   {
      DocumentBuilderFactory factory = null;
      try
      {
         factory = DocumentBuilderFactory.newInstance();
         factory.setValidating(false);
         factory.setNamespaceAware(true);
         factory.setExpandEntityReferences(false);
         factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
         DocumentBuilder builder = factory.newDocumentBuilder();
         return builder;
      }
      catch (Exception e)
      {
         throw new RuntimeException("Unable to create document builder", e);
      }
   }

}
