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
package org.jboss.ws.core;

// $Id$

import org.jboss.ws.WSException;
import org.jboss.ws.core.jaxrpc.SOAP11BindingJAXRPC;
import org.jboss.ws.core.jaxrpc.SOAP12BindingJAXRPC;
import org.jboss.ws.core.jaxws.binding.SOAP11BindingJAXWS;
import org.jboss.ws.core.jaxws.binding.SOAP12BindingJAXWS;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.EndpointMetaData.Type;
import org.jboss.ws.metadata.config.Configurable;
import org.jboss.ws.metadata.config.ConfigurationProvider;

/**
 * Provides access to the protocol binding.
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 04-Jul-2006
 */
public class CommonBindingProvider implements Configurable
{
   protected EndpointMetaData epMetaData;
   protected CommonBinding binding;

   public CommonBindingProvider(EndpointMetaData epMetaData)
   {
      this.epMetaData = epMetaData;
      initBinding(epMetaData.getBindingId(), epMetaData.getType());
   }

   public CommonBindingProvider(String bindingId, Type type)
   {
      initBinding(bindingId, type);
   }

   protected void initBinding(String bindingId, Type type)
   {
      if (CommonSOAPBinding.SOAP11HTTP_BINDING.equals(bindingId)
         || CommonSOAPBinding.SOAP11HTTP_MTOM_BINDING.equals(bindingId))
      {
         binding = (type == Type.JAXWS ? new SOAP11BindingJAXWS() : new SOAP11BindingJAXRPC());
      }
      else if (CommonSOAPBinding.SOAP12HTTP_BINDING.equals(bindingId)
         || CommonSOAPBinding.SOAP12HTTP_MTOM_BINDING.equals(bindingId))
      {
         binding = (type == Type.JAXWS ? new SOAP12BindingJAXWS() : new SOAP12BindingJAXRPC());
      }
      else
      {
         throw new WSException("Unsupported binding: " + bindingId);
      }

      // process MTOM config elements
      if(epMetaData!=null)
      {
         ConfigurationProvider configProvider = (ConfigurationProvider)epMetaData;
         configProvider.configure(this);
      }
   }

   public CommonBinding getCommonBinding()
   {
      return binding;
   }
}
