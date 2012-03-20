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
package org.jboss.ws.metadata.umdm;

import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;
import org.jboss.ws.metadata.config.jaxrpc.CommonConfigJAXRPC;
import org.jboss.wsf.spi.metadata.config.CommonConfig;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerChainMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerMetaData.HandlerType;

/**
 * The configurable EndpointMetaData.
 * 
 * This class allows the configurable items to be separated
 * from the parent EndpointMetaData allowing different clients
 * to load their configuration independently while sharing a 
 * common EndpointMetaData.   
 * 
 * @author darran.lofthouse@jboss.com
 * @since 18th July 2008
 */
public class EndpointConfigMetaData
{

   private static Logger log = Logger.getLogger(EndpointConfigMetaData.class);

   private final EndpointMetaData epMetaData;
   // The REQUIRED config-name
   protected String configName;
   // The REQUIRED config-file
   protected String configFile;

   // The REQUIRED endpoint config
   private CommonConfig config;

   // The optional handlers
   private List<HandlerMetaData> handlers = new ArrayList<HandlerMetaData>();
   // True if the handlers are initialized
   private boolean handlersInitialized;

   public EndpointConfigMetaData(EndpointMetaData parent)
   {
      this.epMetaData = parent;
   }

   void addHandlers(List<HandlerMetaData> configHandlers)
   {
      for (HandlerMetaData handler : configHandlers)
         handler.setEndpointMetaData(epMetaData);
      handlers.addAll(configHandlers);
   }

   void addHandler(HandlerMetaData handler)
   {
      handler.setEndpointMetaData(epMetaData);
      handlers.add(handler);
   }

   void clearHandlers()
   {
      handlers.clear();
      handlersInitialized = false;
   }

   public List<HandlerMetaData> getHandlerMetaData(HandlerType type)
   {
      List<HandlerMetaData> typeHandlers = new ArrayList<HandlerMetaData>();
      for (HandlerMetaData hmd : handlers)
      {
         if (hmd.getHandlerType() == type || type == HandlerType.ALL)
            typeHandlers.add(hmd);
      }
      return typeHandlers;
   }

   public boolean isHandlersInitialized()
   {
      return handlersInitialized;
   }

   public void setHandlersInitialized(boolean flag)
   {
      this.handlersInitialized = flag;
   }

   void configHandlerMetaData()
   {
      log.debug("Configure EndpointMetaData");

      List<HandlerMetaData> sepHandlers = getHandlerMetaData(HandlerType.ENDPOINT);
      clearHandlers();

      List<HandlerMetaData> preHandlers = getHandlers(epMetaData, HandlerType.PRE);
      List<HandlerMetaData> postHandlers = getHandlers(epMetaData, HandlerType.POST);

      addHandlers(preHandlers);
      addHandlers(sepHandlers);
      addHandlers(postHandlers);

      if (log.isDebugEnabled())
      {
         log.debug("Added " + preHandlers.size() + " PRE handlers");
         log.debug("Added " + sepHandlers.size() + " ENDPOINT handlers");
         log.debug("Added " + postHandlers.size() + " POST handlers");
      }
   }
   
   private List<HandlerMetaData> getHandlers(EndpointMetaData epMetaData, HandlerType type)
   {
      List<UnifiedHandlerChainMetaData> handlerChains = config.getHandlers(type);
      boolean isJAXRPCConfig = (config instanceof CommonConfigJAXRPC);
      List<HandlerMetaData> handlers = new ArrayList<HandlerMetaData>();
      if (handlerChains != null)
      {
         for (UnifiedHandlerChainMetaData handlerChain : handlerChains)
         {
            for (UnifiedHandlerMetaData uhmd : handlerChain.getHandlers())
            {
               HandlerMetaData hmd = isJAXRPCConfig ? HandlerMetaDataJAXRPC.newInstance(uhmd, type) : HandlerMetaDataJAXWS.newInstance(uhmd, type);
               hmd.setEndpointMetaData(epMetaData);
               handlers.add(hmd);
            }
         }
      }
      return handlers;
   }

   public EndpointMetaData getEndpointMetaData()
   {
      return epMetaData;
   }

   void initializeInternal()
   {
      // Initialize handlers
      for (HandlerMetaData handler : handlers)
         handler.eagerInitialize();
   }

   public CommonConfig getConfig()
   {
      return config;
   }

   void setConfig(CommonConfig config)
   {
      this.config = config;
   }

   public String getConfigName()
   {
      return configName;
   }

   public void setConfigName(String configName)
   {
      this.configName = configName;
   }

   public String getConfigFile()
   {
      return configFile;
   }

   public void setConfigFile(String configFile)
   {
      this.configFile = configFile;
   }
}
