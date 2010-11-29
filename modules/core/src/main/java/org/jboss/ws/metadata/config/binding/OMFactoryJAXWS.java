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
package org.jboss.ws.metadata.config.binding;

import javax.xml.namespace.QName;

import org.jboss.logging.Logger;
import org.jboss.ws.metadata.config.EndpointProperty;
import org.jboss.ws.metadata.config.jaxws.ClientConfigJAXWS;
import org.jboss.ws.metadata.config.jaxws.CommonConfigJAXWS;
import org.jboss.ws.metadata.config.jaxws.ConfigRootJAXWS;
import org.jboss.ws.metadata.config.jaxws.EndpointConfigJAXWS;
import org.jboss.ws.metadata.config.jaxws.HandlerChainsConfigJAXWS;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerChainMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerChainsMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedInitParamMetaData;
import org.jboss.xb.binding.ObjectModelFactory;
import org.jboss.xb.binding.UnmarshallingContext;
import org.xml.sax.Attributes;
import org.jboss.ws.extensions.wsrm.config.RMBackPortsServerConfig;
import org.jboss.ws.extensions.wsrm.config.RMDeliveryAssuranceConfig;
import org.jboss.ws.extensions.wsrm.config.RMMessageRetransmissionConfig;
import org.jboss.ws.extensions.wsrm.config.RMConfig;
import org.jboss.ws.extensions.wsrm.config.RMPortConfig;

/**
 * ObjectModelFactory for JAXWS configurations.
 * @deprecated This is to be replaced by a stax based configuration parser
 *
 * @author Thomas.Diesler@jboss.org
 * @author Heiko.Braun@jboss.org
 * @since 18-Dec-2005
 */
@Deprecated
public class OMFactoryJAXWS implements ObjectModelFactory
{
   // provide logging
   private final Logger log = Logger.getLogger(OMFactoryJAXWS.class);

   public Object newRoot(Object root, UnmarshallingContext ctx, String namespaceURI, String localName, Attributes attrs)
   {
      return new ConfigRootJAXWS();
   }

   public Object completeRoot(Object root, UnmarshallingContext ctx, String namespaceURI, String localName)
   {
      return root;
   }

   /**
    * Called when parsing of a new element started.
    */
   public Object newChild(ConfigRootJAXWS config, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      log.trace("WSConfig newChild: " + localName);
      if ("endpoint-config".equals(localName))
      {
         EndpointConfigJAXWS wsEndpointConfig = new EndpointConfigJAXWS();
         config.getEndpointConfig().add(wsEndpointConfig);
         return wsEndpointConfig;
      }
      if ("client-config".equals(localName))
      {
         ClientConfigJAXWS clientConfig = new ClientConfigJAXWS();
         config.getClientConfig().add(clientConfig);
         return clientConfig;
      }
      return null;
   }

   /**
    * Called when a new simple child element with text value was read from the XML content.
    */
   public void setValue(CommonConfigJAXWS commonConfig, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (log.isTraceEnabled())
         log.trace("CommonConfig setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);

      if (localName.equals("config-name"))
         commonConfig.setConfigName(value);
      if(localName.equals("feature"))
         commonConfig.setFeature(value, true);

      if("property-name".equals(localName))
      {
         commonConfig.addProperty(value,  null);
      }
      else if("property-value".equals(localName))
      {
         int lastEntry = commonConfig.getProperties().isEmpty() ? 0 : commonConfig.getProperties().size()-1;
         EndpointProperty p = commonConfig.getProperties().get(lastEntry);
         p.value = value;
      }
   }

   /**
    * Called when parsing of a new element started.
    */
   public Object newChild(CommonConfigJAXWS commonConfig, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      log.trace("CommonConfig newChild: " + localName);

      if ("pre-handler-chains".equals(localName))
      {
         HandlerChainsConfigJAXWS preHandlerChains = new HandlerChainsConfigJAXWS();
         commonConfig.setPreHandlerChains(preHandlerChains);
         return preHandlerChains;
      }
      if ("post-handler-chains".equals(localName))
      {
         HandlerChainsConfigJAXWS postHandlerChains = new HandlerChainsConfigJAXWS();
         commonConfig.setPostHandlerChains(postHandlerChains);
         return postHandlerChains;
      }
      if ("reliable-messaging".equals(localName))
      {
         RMConfig wsrmCfg = new RMConfig();
         commonConfig.setRMMetaData(wsrmCfg);
         return wsrmCfg;
      }

      return null;
   }
   
   public Object newChild(RMConfig wsrmConfig, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      int countOfAttributes = attrs.getLength();

      if (localName.equals("delivery-assurance"))
      {
         RMDeliveryAssuranceConfig deliveryAssurance = getDeliveryAssurance(attrs);
         wsrmConfig.setDeliveryAssurance(deliveryAssurance);
         return deliveryAssurance;
      }
      if (localName.equals("message-retransmission"))
      {
         int interval = 0, attempts = 0, timeout=0;
         for (int i = 0; i < countOfAttributes; i++)
         {
            String attrLocalName = attrs.getLocalName(i); 
            if (attrLocalName.equals("interval"))
               interval = Integer.valueOf(attrs.getValue(i));
            if (attrLocalName.equals("attempts"))
               attempts = Integer.valueOf(attrs.getValue(i));
            if (attrLocalName.equals("timeout"))
               timeout = Integer.valueOf(attrs.getValue(i));
         }
         
         RMMessageRetransmissionConfig retransmissionConfig = new RMMessageRetransmissionConfig();
         retransmissionConfig.setCountOfAttempts(attempts);
         retransmissionConfig.setRetransmissionInterval(interval);
         retransmissionConfig.setMessageTimeout(timeout);
         wsrmConfig.setMessageRetransmission(retransmissionConfig);
         return retransmissionConfig;
      }
      if (localName.equals("backports-server"))
      {
         String host = null, port = null;
         for (int i = 0; i < countOfAttributes && (host == null || port == null); i++)
         {
            String attrLocalName = attrs.getLocalName(i); 
            if (attrLocalName.equals("host"))
               host = attrs.getValue(i);
            if (attrLocalName.equals("port"))
               port = attrs.getValue(i);
         }
         
         RMBackPortsServerConfig backportsServer = new RMBackPortsServerConfig();
         backportsServer.setHost(host);
         backportsServer.setPort(port);
         wsrmConfig.setBackPortsServer(backportsServer);
         return backportsServer;
      }
      if (localName.equals("port"))
      {
         String portName = null;
         for (int i = 0; i < countOfAttributes; i++)
         {
            if (attrs.getLocalName(i).equals("name"))
            {
               portName = attrs.getValue(i);
               break;
            }
         }
         RMPortConfig port = new RMPortConfig();
         port.setPortName(QName.valueOf(portName));
         wsrmConfig.getPorts().add(port);
         return port;
      }
      
      return null;
   }
   
   public Object newChild(RMPortConfig port, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      if (localName.equals("delivery-assurance"))
      {
         RMDeliveryAssuranceConfig deliveryAssurance = getDeliveryAssurance(attrs);
         port.setDeliveryAssurance(deliveryAssurance);
         return deliveryAssurance;
      }
      
      return null;
   }
   
   private RMDeliveryAssuranceConfig getDeliveryAssurance(Attributes attrs)
   {
      String inOrder = null, quality = null;
      for (int i = 0; i < attrs.getLength() && (inOrder == null || quality == null); i++)
      {
         String attrLocalName = attrs.getLocalName(i); 
         if (attrLocalName.equals("inOrder"))
            inOrder = attrs.getValue(i);
         if (attrLocalName.equals("quality"))
            quality = attrs.getValue(i);
      }
      RMDeliveryAssuranceConfig deliveryAssurance = new RMDeliveryAssuranceConfig();
      deliveryAssurance.setQuality(quality);
      deliveryAssurance.setInOrder(inOrder);
      return deliveryAssurance;
   }
   
   /**
    * Called when parsing of a new element started.
    */
   public Object newChild(HandlerChainsConfigJAXWS handlerChains, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      log.trace("WSHandlerChainsConfig newChild: " + localName);

      if ("handler-chain".equals(localName))
      {
         UnifiedHandlerChainMetaData handlerChain = new UnifiedHandlerChainMetaData(null);
         handlerChains.getHandlerChains().add(handlerChain);
         return handlerChain;
      }
      return null;
   }
   
   //here below are methods that used to be inherited from import org.jboss.wsf.spi.metadata.j2ee.serviceref.HandlerChainsObjectFactory
   //which is not in this class' type hierarchy any more given it has been deprecated in jbossws-spi in order to remove JBossXB dependency
   
   /**
    * Called when parsing of a new element started.
    */
   public Object newChild(UnifiedHandlerChainsMetaData handlerConfig, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      if ("handler-chain".equals(localName))
         return new UnifiedHandlerChainMetaData(handlerConfig);
      else return null;
   }

   /**
    * Called when parsing character is complete.
    */
   public void addChild(UnifiedHandlerChainsMetaData handlerConfig, UnifiedHandlerChainMetaData handlerChain, UnmarshallingContext navigator, String namespaceURI,
         String localName)
   {
      if (!handlerChain.isExcluded()) handlerConfig.addHandlerChain(handlerChain);
   }

   /**
    * Called when parsing of a new element started.
    */
   public Object newChild(UnifiedHandlerChainMetaData chainConfig, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      if ("handler".equals(localName))
         return new UnifiedHandlerMetaData(chainConfig);
      else return null;
   }

   /**
    * Called when parsing character is complete.
    */
   public void addChild(UnifiedHandlerChainMetaData handlerConfig, UnifiedHandlerMetaData handler, UnmarshallingContext navigator, String namespaceURI, String localName)
   {
      handlerConfig.addHandler(handler);
   }

   /**
    * Called when parsing of a new element started.
    */
   public Object newChild(UnifiedHandlerMetaData handler, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      if ("init-param".equals(localName))
         return new UnifiedInitParamMetaData();
      else return null;
   }

   /**
    * Called when parsing character is complete.
    */
   public void addChild(UnifiedHandlerMetaData handler, UnifiedInitParamMetaData param, UnmarshallingContext navigator, String namespaceURI, String localName)
   {
      handler.addInitParam(param);
   }

   /**
    * Called when a new simple child element with text value was read from the XML content.
    */
   public void setValue(UnifiedHandlerChainMetaData handlerChain, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (log.isTraceEnabled())
         log.trace("UnifiedHandlerChainMetaData setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);
      try
      {
         if (localName.equals("protocol-bindings"))
            handlerChain.setProtocolBindings(value);
         else if (localName.equals("service-name-pattern"))
            handlerChain.setServiceNamePattern(navigator.resolveQName(value));
         else if (localName.equals("port-name-pattern"))
            handlerChain.setPortNamePattern(navigator.resolveQName(value));
      }
      catch (java.lang.IllegalStateException ex)
      {
         log.warn("Could not get " + localName + " value : " 
               + ex.getMessage() + ", this handler chain will be ingored");
         handlerChain.setExcluded(true);
      }
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
   public void setValue(UnifiedInitParamMetaData param, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      if (log.isTraceEnabled())
         log.trace("UnifiedInitParamMetaData setValue: nuri=" + namespaceURI + " localName=" + localName + " value=" + value);

      if (localName.equals("param-name"))
         param.setParamName(value);
      else if (localName.equals("param-value"))
         param.setParamValue(value);
   }
}
