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

import java.util.Observable;

import javax.xml.ws.EndpointReference;
import javax.xml.ws.soap.SOAPBinding;

import org.jboss.logging.Logger;
import org.jboss.ws.core.jaxrpc.SOAP11BindingJAXRPC;
import org.jboss.ws.core.jaxrpc.SOAP12BindingJAXRPC;
import org.jboss.ws.metadata.config.Configurable;
import org.jboss.ws.metadata.umdm.EndpointMetaData;

/**
 * Provides access to the protocol binding.
 *
 * @author Thomas.Diesler@jboss.com
 * @author Heiko.Braun@jboss.com
 * @since 04-Jul-2006
 */
public class CommonBindingProvider implements Configurable
{
   private static Logger log = Logger.getLogger(CommonBindingProvider.class);

   protected EndpointMetaData epMetaData;
   protected CommonBinding binding;

   public CommonBindingProvider(EndpointMetaData epMetaData)
   {
      this.epMetaData = epMetaData;
      initBinding(epMetaData.getBindingId());

      this.epMetaData.registerConfigObserver(this);
      configure();
   }

   public CommonBindingProvider(String bindingId)
   {
      initBinding(bindingId);
      configure();
   }

   private void configure()
   {
      if (epMetaData != null)
      {
         epMetaData.configure(this);
      }
   }

   protected void initBinding(String bindingId)
   {
      if (SOAPBinding.SOAP11HTTP_BINDING.equals(bindingId))
      {
         binding = new SOAP11BindingJAXRPC();
      }
      else if (SOAPBinding.SOAP12HTTP_BINDING.equals(bindingId))
      {
         binding = new SOAP12BindingJAXRPC();
      }
   }

   public CommonBinding getCommonBinding()
   {
      return binding;
   }

   public EndpointReference getEndpointReference()
   {
      throw new UnsupportedOperationException();
   }

   public void update(Observable observable, Object object)
   {
      if(log.isDebugEnabled()) log.debug("Update config: " + object);
      configure();
   }
}
