/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ws.metadata.config.jaxrpc;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.ws.metadata.config.EndpointProperty;
import org.jboss.wsf.spi.metadata.config.AbstractCommonConfig;
import org.jboss.wsf.spi.metadata.config.Feature;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerChainMetaData;

/** 
 * A JBossWS client configuration 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 18-Dec-2005
 */
public abstract class CommonConfigJAXRPC extends AbstractCommonConfig
{
   private List<EndpointProperty> properties = new ArrayList<EndpointProperty>();
   
   public UnifiedHandlerChainMetaData getPostHandlerChain()
   {
      //adapt the jbossws-spi common config to this legacy jaxrpc one
      List<UnifiedHandlerChainMetaData> chains = getPostHandlerChains();
      if (chains != null && !chains.isEmpty())
      {
         return chains.get(0);
      }
      else
      {
         return null;
      }
   }

   public void setPostHandlerChain(UnifiedHandlerChainMetaData postHandlerChain)
   {
      //adapt the jbossws-spi common config to this legacy jaxrpc one
      List<UnifiedHandlerChainMetaData> chains = new ArrayList<UnifiedHandlerChainMetaData>(1);
      chains.add(postHandlerChain);
      setPostHandlerChains(chains);
   }

   public UnifiedHandlerChainMetaData getPreHandlerChain()
   {
      //adapt the jbossws-spi common config to this legacy jaxrpc one
      List<UnifiedHandlerChainMetaData> chains = getPreHandlerChains();
      if (chains != null && !chains.isEmpty())
      {
         return chains.get(0);
      }
      else
      {
         return null;
      }
   }

   public void setPreHandlerChain(UnifiedHandlerChainMetaData preHandlerChain)
   {
      //adapt the jbossws-spi common config to this legacy jaxrpc one
      List<UnifiedHandlerChainMetaData> chains = new ArrayList<UnifiedHandlerChainMetaData>(1);
      chains.add(preHandlerChain);
      setPreHandlerChains(chains);
   }
   
   public boolean hasFeature(URI type) {
      return hasFeature(type.toString());
   }

   public boolean hasFeature(String uri)
   {
      return super.hasFeature(uri);
      //return hasFeature(nameToURI(uri));
   }
   
   public void setFeature(String type, boolean enabled) {
      super.setFeature(new Feature(type), enabled);
   }
   
   @Override
   public void setProperty(String name, String value)
   {
      addProperty(name, value);
   }

   public void addProperty(String name, String value)
   {
      EndpointProperty p = new EndpointProperty();
      p.name = nameToURI(name);
      p.value = value;
      properties.add(p);
      //keep common config in synch
      super.setProperty(name, value);
   }

   public String getProperty(String name)
   {
      String value = null;
      URI uri = nameToURI(name);

      for (EndpointProperty wsp : properties)
      {
         if (wsp.name.equals(uri))
         {
            value = wsp.value;
            break;
         }
      }
      return value;
   }

   public List<EndpointProperty> getAllProperties() {
      return properties;
   }

   private static URI nameToURI(String name)
   {
      URI uri = null;
      try {
         uri = new URI(name);
      } catch (URISyntaxException e) {
         throw new IllegalArgumentException(e.getMessage());
      }
      return uri;
   }
}
