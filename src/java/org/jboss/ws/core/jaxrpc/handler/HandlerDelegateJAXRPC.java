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
package org.jboss.ws.core.jaxrpc.handler;

// $Id$

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.rpc.handler.HandlerChain;
import javax.xml.rpc.handler.HandlerInfo;

import org.jboss.logging.Logger;
import org.jboss.ws.core.server.HandlerDelegate;
import org.jboss.ws.core.server.ServiceEndpointInfo;
import org.jboss.ws.core.soap.MessageContextAssociation;
import org.jboss.ws.extensions.xop.XOPContext;
import org.jboss.ws.metadata.j2ee.serviceref.UnifiedInitParamMetaData;
import org.jboss.ws.metadata.umdm.HandlerMetaData;
import org.jboss.ws.metadata.umdm.HandlerMetaDataJAXRPC;
import org.jboss.ws.metadata.umdm.ServerEndpointMetaData;
import org.jboss.ws.metadata.umdm.HandlerMetaData.HandlerType;

/** Delegates to JAXRPC handlers
 *
 * @author Thomas.Diesler@jboss.org
 * @since 19-Jan-2005
 */
public class HandlerDelegateJAXRPC implements HandlerDelegate
{
   // provide logging
   private static Logger log = Logger.getLogger(HandlerDelegateJAXRPC.class);

   // This endpoints handler chain
   private ServerHandlerChain preHandlerChain;
   // This endpoints handler chain
   private ServerHandlerChain jaxrpcHandlerChain;
   // This endpoints handler chain
   private ServerHandlerChain postHandlerChain;

   public HandlerDelegateJAXRPC()
   {
   }

   public void closeHandlerChain(ServiceEndpointInfo seInfo)
   {
      // nothing to do for JAXRPC
   }

   public boolean callRequestHandlerChain(ServiceEndpointInfo seInfo, HandlerType type)
   {
      SOAPMessageContextJAXRPC msgContext = (SOAPMessageContextJAXRPC)MessageContextAssociation.peekMessageContext();
      ServerEndpointMetaData sepMetaData = seInfo.getServerEndpointMetaData();

      // Initialize the handler chain
      if (sepMetaData.isHandlersInitialized() == false)
      {
         initHandlerChain(seInfo, HandlerType.PRE);
         initHandlerChain(seInfo, HandlerType.ENDPOINT);
         initHandlerChain(seInfo, HandlerType.POST);
         sepMetaData.setHandlersInitialized(true);
      }

      boolean status = true;
      String[] roles = null;

      HandlerChain handlerChain = null;
      if (type == HandlerType.PRE)
         handlerChain = preHandlerChain;
      else if (type == HandlerType.ENDPOINT)
         handlerChain = jaxrpcHandlerChain;
      else if (type == HandlerType.POST)
         handlerChain = postHandlerChain;
      
      if (handlerChain != null)
      {
         roles = handlerChain.getRoles();
         status = handlerChain.handleRequest(msgContext);
      }

      // BP-1.0 R1027
      if (type == HandlerType.POST)
         HandlerChainBaseImpl.checkMustUnderstand(msgContext, roles);
      
      return status;
   }

   public boolean callResponseHandlerChain(ServiceEndpointInfo seInfo, HandlerType type)
   {
      SOAPMessageContextJAXRPC msgContext = (SOAPMessageContextJAXRPC)MessageContextAssociation.peekMessageContext();
      
      HandlerChain handlerChain = null;
      if (type == HandlerType.PRE)
         handlerChain = preHandlerChain;
      else if (type == HandlerType.ENDPOINT)
         handlerChain = jaxrpcHandlerChain;
      else if (type == HandlerType.POST)
         handlerChain = postHandlerChain;
      
      boolean status = (handlerChain != null ? handlerChain.handleResponse(msgContext) : true);

      if(type == HandlerType.ENDPOINT)
         XOPContext.visitAndRestoreXOPData();

      return status;
   }

   public boolean callFaultHandlerChain(ServiceEndpointInfo seInfo, HandlerType type, Exception ex)
   {
      SOAPMessageContextJAXRPC msgContext = (SOAPMessageContextJAXRPC)MessageContextAssociation.peekMessageContext();

      HandlerChain handlerChain = null;
      if (type == HandlerType.PRE)
         handlerChain = preHandlerChain;
      else if (type == HandlerType.ENDPOINT)
         handlerChain = jaxrpcHandlerChain;
      else if (type == HandlerType.POST)
         handlerChain = postHandlerChain;
      
      boolean status = (handlerChain != null ? handlerChain.handleFault(msgContext) : true);

      if(type == HandlerType.ENDPOINT)
         XOPContext.visitAndRestoreXOPData();

      return status;
   }

   /**
    * Init the handler chain
    */
   private void initHandlerChain(ServiceEndpointInfo seInfo, HandlerType type)
   {
      Set<String> handlerRoles = new HashSet<String>();
      List<HandlerInfo> hInfos = new ArrayList<HandlerInfo>();

      ServerEndpointMetaData sepMetaData = seInfo.getServerEndpointMetaData();
      for (HandlerMetaData handlerMetaData : sepMetaData.getHandlerMetaData(type))
      {
         HandlerMetaDataJAXRPC jaxrpcMetaData = (HandlerMetaDataJAXRPC)handlerMetaData;
         handlerRoles.addAll(jaxrpcMetaData.getSoapRoles());

         HashMap<String, Object> hConfig = new HashMap<String, Object>();
         for (UnifiedInitParamMetaData param : jaxrpcMetaData.getInitParams())
         {
            hConfig.put(param.getParamName(), param.getParamValue());
         }
         
         Set<QName> headers = jaxrpcMetaData.getSoapHeaders();
         QName[] headerArr = new QName[headers.size()];
         headers.toArray(headerArr);
         
         Class hClass = jaxrpcMetaData.getHandlerClass();
         hConfig.put(HandlerType.class.getName(), jaxrpcMetaData.getHandlerType());
         HandlerInfo info = new HandlerInfo(hClass, hConfig, headerArr);

         if(log.isDebugEnabled()) log.debug("Adding server side handler to service '" + sepMetaData.getPortName() + "': " + info);
         hInfos.add(info);
      }

      initHandlerChain(seInfo, hInfos, handlerRoles, type);
   }

   private void initHandlerChain(ServiceEndpointInfo seInfo, List<HandlerInfo> hInfos, Set<String> handlerRoles, HandlerType type)
   {
      if(log.isDebugEnabled()) log.debug("Init handler chain with [" + hInfos.size() + "] handlers");

      ServerHandlerChain handlerChain = new ServerHandlerChain(hInfos, handlerRoles, type);
      if (type == HandlerType.PRE)
         preHandlerChain = handlerChain;
      else if (type == HandlerType.ENDPOINT)
         jaxrpcHandlerChain = handlerChain;
      else if (type == HandlerType.POST)
         postHandlerChain = handlerChain;

      if (handlerChain.getState() == ServerHandlerChain.STATE_CREATED)
      {
         // what is the config for a handler chain?
         handlerChain.init(null);
      }
   }
}
