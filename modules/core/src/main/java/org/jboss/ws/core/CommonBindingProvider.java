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
package org.jboss.ws.core;

import org.jboss.ws.core.jaxrpc.SOAP11BindingJAXRPC;
import org.jboss.ws.core.jaxrpc.SOAP12BindingJAXRPC;
import org.jboss.ws.metadata.umdm.EndpointMetaData;

/**
 * Provides access to the protocol binding.
 *
 * @author Thomas.Diesler@jboss.com
 * @author Heiko.Braun@jboss.com
 * @since 04-Jul-2006
 */
public class CommonBindingProvider
{
   /**
    * A constant representing the identity of the SOAP 1.1 over HTTP binding.
    */
   private static final String SOAP11HTTP_BINDING = "http://schemas.xmlsoap.org/wsdl/soap/http";

   /**
    * A constant representing the identity of the SOAP 1.2 over HTTP binding.
    */
   private static final String SOAP12HTTP_BINDING = "http://www.w3.org/2003/05/soap/bindings/HTTP/";

   protected EndpointMetaData epMetaData;
   protected CommonBinding binding;

   public CommonBindingProvider(EndpointMetaData epMetaData)
   {
      this.epMetaData = epMetaData;
      initBinding(epMetaData.getBindingId());
   }

   public CommonBindingProvider(String bindingId)
   {
      initBinding(bindingId);
   }

   protected void initBinding(String bindingId)
   {
      if (SOAP11HTTP_BINDING.equals(bindingId))
      {
         binding = new SOAP11BindingJAXRPC();
      }
      else if (SOAP12HTTP_BINDING.equals(bindingId))
      {
         binding = new SOAP12BindingJAXRPC();
      }
   }

   public CommonBinding getCommonBinding()
   {
      return binding;
   }
}
