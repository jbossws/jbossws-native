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
package org.jboss.ws.core.jaxws.handler;

// $Id:HandlerDelegateJAXWS.java 710 2006-08-08 20:19:52Z thomas.diesler@jboss.com $

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.PortInfo;

import org.jboss.logging.Logger;
import org.jboss.ws.core.server.HandlerDelegate;
import org.jboss.ws.core.server.ServiceEndpointInfo;
import org.jboss.ws.core.soap.MessageContextAssociation;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.ServerEndpointMetaData;
import org.jboss.ws.metadata.umdm.HandlerMetaData.HandlerType;

/**
 * Delegates to JAXWS handlers
 *
 * @author Thomas.Diesler@jboss.org
 * @since 19-Jan-2005
 */
public class HandlerDelegateJAXWS implements HandlerDelegate
{
   // provide logging
   private static Logger log = Logger.getLogger(HandlerDelegateJAXWS.class);
   private HandlerResolverImpl resolver = new HandlerResolverImpl();

   public boolean callRequestHandlerChain(ServiceEndpointInfo seInfo, HandlerType type)
   {
      log.debug("callRequestHandlerChain: " + type);
      SOAPMessageContextJAXWS msgContext = (SOAPMessageContextJAXWS)MessageContextAssociation.peekMessageContext();
      EndpointMetaData epMetaData = seInfo.getServerEndpointMetaData();

      // Initialize the handler chain
      if (epMetaData.isHandlersInitialized() == false)
      {
         initResolverChain(epMetaData);
      }

      List<Handler> handlerChain = getHandlerChain(epMetaData, type);
      boolean status = new HandlerChainExecutor(epMetaData, handlerChain).handleRequest(msgContext);
      return status;
   }

   public boolean callResponseHandlerChain(ServiceEndpointInfo seInfo, HandlerType type)
   {
      log.debug("callResponseHandlerChain: " + type);
      SOAPMessageContextJAXWS msgContext = (SOAPMessageContextJAXWS)MessageContextAssociation.peekMessageContext();
      ServerEndpointMetaData epMetaData = seInfo.getServerEndpointMetaData();
      List<Handler> handlerChain = getHandlerChain(epMetaData, type);
      boolean status = new HandlerChainExecutor(epMetaData, handlerChain).handleResponse(msgContext);
      return status;
   }

   public boolean callFaultHandlerChain(ServiceEndpointInfo seInfo, HandlerType type, Exception ex)
   {
      log.debug("callFaultHandlerChain: " + type);
      SOAPMessageContextJAXWS msgContext = (SOAPMessageContextJAXWS)MessageContextAssociation.peekMessageContext();
      ServerEndpointMetaData epMetaData = seInfo.getServerEndpointMetaData();
      List<Handler> handlerChain = getHandlerChain(epMetaData, type);
      boolean status = new HandlerChainExecutor(epMetaData, handlerChain).handleFault(msgContext);
      return status;
   }

   private List<Handler> getHandlerChain(EndpointMetaData epMetaData, HandlerType type)
   {
      PortInfo info = getPortInfo(epMetaData);
      return resolver.getHandlerChain(info, type);
   }

   private void initResolverChain(EndpointMetaData epMetaData)
   {
      resolver.initHandlerChain(epMetaData, HandlerType.PRE);
      resolver.initHandlerChain(epMetaData, HandlerType.ENDPOINT);
      resolver.initHandlerChain(epMetaData, HandlerType.POST);
   }

   private PortInfo getPortInfo(EndpointMetaData epMetaData)
   {
      QName serviceName = epMetaData.getServiceMetaData().getServiceName();
      QName portName = epMetaData.getPortName();
      String bindingId = epMetaData.getBindingId();
      PortInfo info = new PortInfoImpl(serviceName, portName, bindingId);
      return info;
   }
}
