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
package org.jboss.ws.metadata.config.jaxrpc;

import java.util.ArrayList;
import java.util.List;

import org.jboss.ws.metadata.config.CommonConfig;
import org.jboss.ws.metadata.j2ee.serviceref.UnifiedHandlerChainMetaData;
import org.jboss.ws.metadata.j2ee.serviceref.UnifiedHandlerMetaData;
import org.jboss.ws.metadata.j2ee.serviceref.UnifiedHandlerMetaData.HandlerType;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.HandlerMetaData;
import org.jboss.ws.metadata.umdm.HandlerMetaDataJAXRPC;


// $Id$

/** 
 * A JBossWS client configuration 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 18-Dec-2005
 */
public abstract class CommonConfigJAXRPC extends CommonConfig
{
   private UnifiedHandlerChainMetaData preHandlerChain;
   private UnifiedHandlerChainMetaData postHandlerChain;

   public UnifiedHandlerChainMetaData getPostHandlerChain()
   {
      return postHandlerChain;
   }

   public void setPostHandlerChain(UnifiedHandlerChainMetaData postHandlerChain)
   {
      this.postHandlerChain = postHandlerChain;
   }

   public UnifiedHandlerChainMetaData getPreHandlerChain()
   {
      return preHandlerChain;
   }

   public void setPreHandlerChain(UnifiedHandlerChainMetaData preHandlerChain)
   {
      this.preHandlerChain = preHandlerChain;
   }
   
   @Override
   public List<HandlerMetaData> getHandlers(EndpointMetaData epMetaData, HandlerType type)
   {
      List<HandlerMetaData> handlers = new ArrayList<HandlerMetaData>();
      
      UnifiedHandlerChainMetaData handlerChain;
      if (type == HandlerType.PRE)
         handlerChain = getPreHandlerChain();
      else if (type == HandlerType.POST)
         handlerChain = getPostHandlerChain();
      else
         throw new IllegalArgumentException("Invalid handler type: " + type);
      
      if (handlerChain != null)
      {
         for (UnifiedHandlerMetaData uhmd : handlerChain.getHandlers())
         {
            HandlerMetaDataJAXRPC hmd = HandlerMetaDataJAXRPC.newInstance(uhmd, type);
            handlers.add(hmd);
         }
      }
      
      return handlers;
   }
}
