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
package org.jboss.ws.core.server;

// $Id$

import org.jboss.ws.metadata.config.Configurable;
import org.jboss.ws.metadata.umdm.ServerEndpointMetaData;
import org.jboss.ws.metadata.umdm.HandlerMetaData.HandlerType;

/** 
 * @author Thomas.Diesler@jboss.org
 * @since 19-Jan-2005
 */
public abstract class HandlerDelegate implements Configurable 
{
   private ServerEndpointMetaData sepMetaData;
   
   public HandlerDelegate(ServerEndpointMetaData sepMetaData)
   {
      this.sepMetaData = sepMetaData;
   }

   public abstract boolean callRequestHandlerChain(ServerEndpointMetaData sepMetaData, HandlerType type);

   public abstract boolean callResponseHandlerChain(ServerEndpointMetaData sepMetaData, HandlerType type);
   
   public abstract boolean callFaultHandlerChain(ServerEndpointMetaData sepMetaData, HandlerType type, Exception ex);

   public abstract void closeHandlerChain(ServerEndpointMetaData sepMetaData, HandlerType type);

   protected boolean isInitialized()
   {
      return sepMetaData.isHandlersInitialized();
   }

   protected void setInitialized(boolean flag)
   {
      sepMetaData.setHandlersInitialized(flag);
   }
}
