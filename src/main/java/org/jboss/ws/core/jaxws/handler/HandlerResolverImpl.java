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

// $Id$

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.PortInfo;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.SOAPBinding;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.HandlerMetaData;
import org.jboss.ws.metadata.umdm.HandlerMetaDataJAXWS;
import org.jboss.ws.metadata.umdm.ServiceMetaData;
import org.jboss.ws.metadata.umdm.UnifiedMetaData;
import org.jboss.ws.metadata.umdm.HandlerMetaData.HandlerType;

/**
 * HandlerResolver is an interface implemented by an application to get control over 
 * the handler chain set on proxy/dispatch objects at the time of their creation.
 * 
 * A HandlerResolver may be set on a Service using the setHandlerResolver method.
 * 
 * When the runtime invokes a HandlerResolver, it will pass it a PortInfo object 
 * containing information about the port that the proxy/dispatch object will be accessing.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 08-Aug-2006
 */
public class HandlerResolverImpl implements HandlerResolver
{
   private static Logger log = Logger.getLogger(HandlerResolverImpl.class);

   private Map<PortInfo, List<Handler>> handlerMap = new HashMap<PortInfo, List<Handler>>();

   public List<Handler> getHandlerChain(PortInfo info)
   {
      log.debug("getHandlerChain: " + info);

      List<Handler> unsortedChain = new ArrayList<Handler>();

      String bindingID = info.getBindingID();
      QName serviceName = info.getServiceName();
      QName portName = info.getPortName();

      if (bindingID != null)
      {
         List<Handler> list = handlerMap.get(new PortInfoImpl(null, null, bindingID));
         if (list != null)
         {
            log.debug("add protocol handlers: " + list);
            unsortedChain.addAll(list);
         }
      }

      if (serviceName != null)
      {
         List<Handler> list = handlerMap.get(new PortInfoImpl(serviceName, null, null));
         if (list != null)
         {
            log.debug("add service handlers: " + list);
            unsortedChain.addAll(list);
         }
      }

      if (portName != null)
      {
         List<Handler> list = handlerMap.get(new PortInfoImpl(null, portName, null));
         if (list != null)
         {
            log.debug("add port handlers: " + list);
            unsortedChain.addAll(list);
         }
      }

      List<Handler> list = handlerMap.get(new PortInfoImpl(null, null, null));
      if (list != null)
      {
         log.debug("add general handlers: " + list);
         unsortedChain.addAll(list);
      }

      // Sort handler logical handlers first
      List<Handler> sortedChain = new ArrayList<Handler>();
      for (Handler handler : unsortedChain)
      {
         if (handler instanceof LogicalHandler)
            sortedChain.add(handler);
      }
      for (Handler handler : unsortedChain)
      {
         if ((handler instanceof LogicalHandler) == false)
            sortedChain.add(handler);
      }

      return Collections.unmodifiableList(sortedChain);
   }

   public void initHandlerChain(EndpointMetaData epMetaData, HandlerType type)
   {
      log.debug("initHandlerChain: " + type);

      // clear all exisisting handler to avoid double registration
      log.debug("Clear handler map: " + handlerMap);

      for (HandlerMetaData handlerMetaData : epMetaData.getHandlerMetaData(type))
      {
         HandlerMetaDataJAXWS jaxwsMetaData = (HandlerMetaDataJAXWS)handlerMetaData;
         String handlerName = jaxwsMetaData.getHandlerName();
         String className = jaxwsMetaData.getHandlerClassName();
         Set<QName> soapHeaders = jaxwsMetaData.getSoapHeaders();

         try
         {
            // Load the handler class using the deployments top level CL
            ClassLoader classLoader = epMetaData.getClassLoader();
            Class hClass = classLoader.loadClass(className);
            Handler handler = (Handler)hClass.newInstance();

            if (handler instanceof GenericHandler)
               ((GenericHandler)handler).setHandlerName(handlerName);

            if (handler instanceof GenericSOAPHandler)
               ((GenericSOAPHandler)handler).setHeaders(soapHeaders);

            List<PortInfo> infos = getPortInfos(epMetaData, jaxwsMetaData);
            for (PortInfo info : infos)
            {
               addHandler(info, handler);
            }
         }
         catch (RuntimeException rte)
         {
            throw rte;
         }
         catch (Exception ex)
         {
            throw new WSException("Cannot load handler: " + className, ex);
         }
      }
   }

   private List<PortInfo> getPortInfos(EndpointMetaData epMetaData, HandlerMetaDataJAXWS handlerMetaData)
   {
      String protocols = handlerMetaData.getProtocolBindings();
      QName services = handlerMetaData.getServiceNamePattern();
      QName ports = handlerMetaData.getPortNamePattern();

      List<PortInfo> infos = new ArrayList<PortInfo>();
      if (protocols != null)
      {
         for (String protocol : protocols.split("\\s"))
         {
            Map<String, String> protocolMap = new HashMap<String, String>();
            protocolMap.put("##SOAP11_HTTP", SOAPBinding.SOAP11HTTP_BINDING);
            protocolMap.put("##SOAP12_HTTP", SOAPBinding.SOAP12HTTP_BINDING);
            protocolMap.put("##XML_HTTP", HTTPBinding.HTTP_BINDING);

            String bindingId = protocolMap.get(protocol);
            if (bindingId != null)
            {
               if (bindingId.equals(epMetaData.getBindingId()))
                  infos.add(new PortInfoImpl(null, null, bindingId));
            }
            else
            {
               log.warn("Unsuported protocol binding: " + protocol);
            }
         }
      }
      else if (services != null)
      {
         String namespaceURI = services.getNamespaceURI();
         String localPattern = services.getLocalPart();
         if (localPattern.endsWith("*"))
         {
            localPattern = localPattern.substring(0, localPattern.length() - 1);
            UnifiedMetaData wsMetaData = epMetaData.getServiceMetaData().getUnifiedMetaData();
            for (ServiceMetaData smd : wsMetaData.getServices())
            {
               QName qname = smd.getServiceName();
               String nsURI = qname.getNamespaceURI();
               String localPart = qname.getLocalPart();
               if (nsURI.equals(namespaceURI) && localPart.startsWith(localPattern))
               {
                  infos.add(new PortInfoImpl(qname, null, null));
               }
            }
         }
         else
         {
            UnifiedMetaData wsMetaData = epMetaData.getServiceMetaData().getUnifiedMetaData();
            for (ServiceMetaData smd : wsMetaData.getServices())
            {
               QName qname = smd.getServiceName();
               if (services.equals(qname))
               {
                  infos.add(new PortInfoImpl(qname, null, null));
               }
            }
         }
      }
      else if (ports != null)
      {
         String namespaceURI = ports.getNamespaceURI();
         String localPattern = ports.getLocalPart();
         if (localPattern.endsWith("*"))
         {
            localPattern = localPattern.substring(0, localPattern.length() - 1);
            ServiceMetaData serviceMetaData = epMetaData.getServiceMetaData();
            for (EndpointMetaData epmd : serviceMetaData.getEndpoints())
            {
               QName qname = epmd.getPortName();
               String nsURI = qname.getNamespaceURI();
               String localPart = qname.getLocalPart();
               if (nsURI.equals(namespaceURI) && localPart.startsWith(localPattern))
               {
                  infos.add(new PortInfoImpl(null, qname, null));
               }
            }
         }
         else
         {
            ServiceMetaData serviceMetaData = epMetaData.getServiceMetaData();
            for (EndpointMetaData epmd : serviceMetaData.getEndpoints())
            {
               QName qname = epmd.getPortName();
               if (ports.equals(qname))
               {
                  infos.add(new PortInfoImpl(null, qname, null));
               }
            }
         }
      }
      else
      {
         // add a general handler that is not scoped
         infos.add(new PortInfoImpl());
      }

      return infos;
   }

   public boolean addHandler(PortInfo info, Handler handler)
   {
      log.debug("addHandler: " + info + ":" + handler);

      List<Handler> handlerList = handlerMap.get(info);
      if (handlerList == null)
      {
         handlerMap.put(info, handlerList = new ArrayList<Handler>());
      }
      handlerList.add(handler);

      return true;
   }
}
