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
package org.jboss.wsf.stack.jbws;

import java.security.Principal;

import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.jaxws.wsaddressing.EndpointReferenceUtil;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.wsf.spi.invocation.ExtensibleWebServiceContext;
import org.w3c.dom.Element;

/**
 * A WebServiceContext implementing the getEndpointReference jaxws methods. 
 * 
 * @author alessio.soldano@jboss.com
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class NativeWebServiceContext extends ExtensibleWebServiceContext
{
   public NativeWebServiceContext(final MessageContext messageContext)
   {
      super(messageContext);
   }
   
   public EndpointReference getEndpointReference(final Element... referenceParameters)
   {
      return this.getEndpointReference(W3CEndpointReference.class, referenceParameters);
   }

   public <T extends EndpointReference> T getEndpointReference(final Class<T> clazz, final Element... referenceParameters)
   {
      EndpointMetaData epMetaData = ((CommonMessageContext)getMessageContext()).getEndpointMetaData();
      if (epMetaData == null)
      {
         throw new WebServiceException("Cannot get EndpointMetaData!");
      }
      if (HTTPBinding.HTTP_BINDING.equals(epMetaData.getBindingId()))
      {
         throw new UnsupportedOperationException("Cannot get epr when using the XML/HTTP binding");
      }
      W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
      String address = epMetaData.getEndpointAddress();
      builder.address(address);
      builder.wsdlDocumentLocation(address +  "?wsdl");
      //TODO set other parameters in the builder
      if (referenceParameters != null && W3CEndpointReference.class.getName().equals(clazz.getName()))
      {
         for (Element el : referenceParameters)
            builder.referenceParameter(el);
      }
      return EndpointReferenceUtil.transform(clazz, builder.build());
   }

   @Override
   public Principal getUserPrincipal()
   {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean isUserInRole(String role)
   {
      throw new UnsupportedOperationException();
   }
}
