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
// $Id$
package org.jboss.ws.metadata.jsr181;

//$Id$

import java.util.ArrayList;

import org.jboss.ws.metadata.umdm.HandlerMetaData.HandlerType;

/**
 * XML Binding root element for JSR-181 HandlerChain
 *
 * @author Thomas.Diesler@jboss.org
 * @since 15-Oct-2005
 */
public class HandlerChainsMetaData
{
   // The required handler type
   private HandlerType handlerType;
   // The required <handler-chain> elements
   private ArrayList<HandlerChainMetaData> handlerChains = new ArrayList<HandlerChainMetaData>();

   public HandlerChainsMetaData(HandlerType handlerType)
   {
      this.handlerType = handlerType;
   }

   public HandlerType getHandlerType()
   {
      return handlerType;
   }


   public void addHandlerChain(HandlerChainMetaData handlerChain)
   {
      handlerChains.add(handlerChain);
   }

   public HandlerChainMetaData[] getHandlerChains()
   {
      HandlerChainMetaData[] array = new HandlerChainMetaData[handlerChains.size()];
      handlerChains.toArray(array);
      return array;
   }
}
