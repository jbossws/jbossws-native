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
package org.jboss.ws.core.jaxws.binding;

// $Id$

import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.http.HTTPBinding;

import org.jboss.util.NotImplementedException;
import org.jboss.ws.core.CommonBinding;
import org.jboss.ws.core.EndpointInvocation;
import org.jboss.ws.core.jaxrpc.binding.BindingException;
import org.jboss.ws.core.soap.UnboundHeader;
import org.jboss.ws.metadata.umdm.OperationMetaData;
import org.jboss.ws.metadata.umdm.HandlerMetaData.HandlerType;

/**
 * The HTTPBinding interface is an abstraction for the XML/HTTP binding. 
 * 
 * @author Thomas.Diesler@jboss.com
 * @since 04-Jul-2006
 */
public class HTTPBindingJAXWS implements CommonBinding, BindingExt, HTTPBinding
{
   private BindingImpl delegate = new BindingImpl();
   
   public Object bindRequestMessage(OperationMetaData opMetaData, EndpointInvocation epInv, Map<QName, UnboundHeader> unboundHeaders) throws BindingException
   {
      throw new NotImplementedException();
   }

   public Object bindResponseMessage(OperationMetaData opMetaData, EndpointInvocation epInv) throws BindingException
   {
      throw new NotImplementedException();
   }

   public EndpointInvocation unbindRequestMessage(OperationMetaData opMetaData, Object reqMessage) throws BindingException
   {
      throw new NotImplementedException();
   }

   public void unbindResponseMessage(OperationMetaData opMetaData, Object resMessage, EndpointInvocation epInv, Map<QName, UnboundHeader> unboundHeaders) throws BindingException
   {
      throw new NotImplementedException();
   }

   public Object bindFaultMessage(Exception ex)
   {
      throw new NotImplementedException();
   }
   
   public List<Handler> getHandlerChain()
   {
      return delegate.getHandlerChain();
   }

   public List<Handler> getHandlerChain(HandlerType handlerType)
   {
      return delegate.getHandlerChain(handlerType);
   }

   public void setHandlerChain(List<Handler> handlerChain)
   {
      delegate.setHandlerChain(handlerChain);
   }

   public void setHandlerChain(List<Handler> handlerChain, HandlerType handlerType)
   {
      delegate.setHandlerChain(handlerChain, handlerType);
   }
   
   public String getBindingID()
   {
      throw new NotImplementedException();
   }
}
