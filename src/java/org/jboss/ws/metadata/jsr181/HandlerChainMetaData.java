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
package org.jboss.ws.metadata.jsr181;

// $Id$

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.jboss.logging.Logger;
import org.jboss.ws.metadata.j2ee.UnifiedHandlerMetaData;

/**
 * XML Binding element for handler-config/handler-chain elements
 *
 * @author Thomas.Diesler@jboss.org
 * @since 15-Oct-2005
 */
public class HandlerChainMetaData
{
   // provide logging
   private static final Logger log = Logger.getLogger(HandlerChainMetaData.class);

   // The parent element
   private HandlerChainsMetaData handlerChainsMetaData;

   private String protocolBindings;
   private QName serviceNamePattern;
   private QName portNamePattern;
   private ArrayList<UnifiedHandlerMetaData> handlers = new ArrayList<UnifiedHandlerMetaData>();

   public HandlerChainMetaData(HandlerChainsMetaData handlerConfig)
   {
      this.handlerChainsMetaData = handlerConfig;
   }

   public HandlerChainsMetaData getHandlerChainsMetaData()
   {
      return handlerChainsMetaData;
   }

   public void addHandler(UnifiedHandlerMetaData handlerMetaData)
   {
      handlers.add(handlerMetaData);
   }

   public List<UnifiedHandlerMetaData> getHandlers()
   {
      return handlers;
   }

   public QName getPortNamePattern()
   {
      return portNamePattern;
   }

   public void setPortNamePattern(QName portNamePattern)
   {
      this.portNamePattern = portNamePattern;
   }

   public String getProtocolBindings()
   {
      return protocolBindings;
   }

   public void setProtocolBindings(String protocolBindings)
   {
      this.protocolBindings = protocolBindings;
   }

   public QName getServiceNamePattern()
   {
      return serviceNamePattern;
   }

   public void setServiceNamePattern(QName serviceNamePattern)
   {
      this.serviceNamePattern = serviceNamePattern;
   }

}
