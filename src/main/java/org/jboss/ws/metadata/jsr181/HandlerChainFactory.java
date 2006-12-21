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

import org.jboss.logging.Logger;
import org.jboss.ws.metadata.j2ee.UnifiedHandlerMetaData;
import org.jboss.ws.metadata.umdm.HandlerMetaData.HandlerInitParam;
import org.jboss.ws.metadata.umdm.HandlerMetaData.HandlerType;
import org.jboss.xb.binding.ObjectModelFactory;
import org.jboss.xb.binding.UnmarshallingContext;
import org.xml.sax.Attributes;

/**
 * A JBossXB factory for jsr181 the HandlerChain annotation
 *
 * @author Thomas.Diesler@jboss.org
 * @since 15-Oct-2005
 */
public class HandlerChainFactory implements ObjectModelFactory
{
   // provide logging
   private static final Logger log = Logger.getLogger(HandlerChainFactory.class);

   public HandlerChainFactory()
   {
   }

   /**
    * This method is called on the factory by the object model builder when the parsing starts.
    *
    * @return the root of the object model.
    */
   public Object newRoot(Object root, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      HandlerChainsMetaData handlerConfigMetaData = new HandlerChainsMetaData(HandlerType.ENDPOINT);
      return handlerConfigMetaData;
   }

   public Object completeRoot(Object root, UnmarshallingContext ctx, String uri, String name)
   {
      return root;
   }

   /**
    * Called when parsing of a new element started.
    */
   public Object newChild(HandlerChainsMetaData handlerConfig, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      if ("handler-chain".equals(localName))
         return new HandlerChainMetaData(handlerConfig);
      else 
         return null;
   }

   /**
    * Called when parsing character is complete.
    */
   public void addChild(HandlerChainsMetaData handlerConfig, HandlerChainMetaData handlerChain, UnmarshallingContext navigator, String namespaceURI, String localName)
   {
      handlerConfig.addHandlerChain(handlerChain);
   }

   /**
    * Called when parsing of a new element started.
    */
   public Object newChild(HandlerChainMetaData chainConfig, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      if ("handler".equals(localName))
         return new UnifiedHandlerMetaData(chainConfig);
      else 
         return null;
   }

   /**
    * Called when parsing character is complete.
    */
   public void addChild(HandlerChainMetaData handlerConfig, UnifiedHandlerMetaData handler, UnmarshallingContext navigator, String namespaceURI, String localName)
   {
      handlerConfig.addHandler(handler);
   }

   /**
    * Called when parsing of a new element started.
    */
   public Object newChild(UnifiedHandlerMetaData handler, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      if ("init-param".equals(localName))
         return new HandlerInitParam();
      else return null;
   }

   /**
    * Called when parsing character is complete.
    */
   public void addChild(UnifiedHandlerMetaData handler, HandlerInitParam param, UnmarshallingContext navigator, String namespaceURI, String localName)
   {
      handler.addInitParam(param);
   }

   /**
    * Called when a new simple child element with text value was read from the XML content.
    */
   public void setValue(HandlerChainMetaData handlerChain, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (log.isTraceEnabled())
         log.trace("HandlerChainMetaData setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);

      if (localName.equals("protocol-bindings"))
         handlerChain.setProtocolBindings(value);
      else if (localName.equals("service-name-pattern"))
         handlerChain.setServiceNamePattern(navigator.resolveQName(value));
      else if (localName.equals("port-name-pattern"))
         handlerChain.setPortNamePattern(navigator.resolveQName(value));
   }

   /**
    * Called when a new simple child element with text value was read from the XML content.
    */
   public void setValue(UnifiedHandlerMetaData handler, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (log.isTraceEnabled())
         log.trace("UnifiedHandlerMetaData setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);

      if (localName.equals("handler-name"))
         handler.setHandlerName(value);
      else if (localName.equals("handler-class"))
         handler.setHandlerClass(value);
   }

   /**
    * Called when a new simple child element with text value was read from the XML content.
    */
   public void setValue(HandlerInitParam param, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (log.isTraceEnabled())
         log.trace("UnifiedInitParamMetaData setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);

      if (localName.equals("param-name"))
         param.setParamName(value);
      else if (localName.equals("param-value"))
         param.setParamValue(value);
   }
}
