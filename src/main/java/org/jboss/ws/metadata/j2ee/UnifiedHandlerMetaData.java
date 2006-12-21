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
package org.jboss.ws.metadata.j2ee;

//$Id$

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.jboss.ws.metadata.jsr181.HandlerChainMetaData;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.HandlerMetaDataJAXRPC;
import org.jboss.ws.metadata.umdm.HandlerMetaDataJAXWS;
import org.jboss.ws.metadata.umdm.HandlerMetaData.HandlerInitParam;
import org.jboss.ws.metadata.umdm.HandlerMetaData.HandlerType;

/**
 * The container independent metdata data for a handler element
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 05-May-2006
 */
public class UnifiedHandlerMetaData implements Serializable
{
   private static final long serialVersionUID = -3019416564080333900L;

   private HandlerChainMetaData chainConfig;

   // The required <handler-name> element
   private String handlerName;
   // The required <handler-class> element
   private String handlerClass;
   // The optional <init-param> elements
   private ArrayList<HandlerInitParam> initParams = new ArrayList<HandlerInitParam>();
   // The optional <soap-header> elements
   private Set<QName> soapHeaders = new HashSet<QName>();
   // The optional <soap-role> elements
   private Set<String> soapRoles = new HashSet<String>();
   // The optional <port-name> elements
   private Set<String> portNames = new HashSet<String>();

   public UnifiedHandlerMetaData(HandlerChainMetaData handlerChainMetaData)
   {
      this.chainConfig = handlerChainMetaData;
   }

   public void setHandlerName(String value)
   {
      this.handlerName = value;
   }

   public String getHandlerName()
   {
      return handlerName;
   }

   public void setHandlerClass(String handlerClass)
   {
      this.handlerClass = handlerClass;
   }

   public String getHandlerClass()
   {
      return handlerClass;
   }

   public void addInitParam(HandlerInitParam param)
   {
      initParams.add(param);
   }

   public List<HandlerInitParam> getInitParams()
   {
      return initParams;
   }

   public void addSoapRole(String value)
   {
      soapRoles.add(value);
   }

   public Set<String> getSoapRoles()
   {
      return soapRoles;
   }

   public void addSoapHeader(QName qName)
   {
      soapHeaders.add(qName);
   }

   public Set<QName> getSoapHeaders()
   {
      return soapHeaders;
   }

   public String getProtocolBindings()
   {
      return (chainConfig != null ? chainConfig.getProtocolBindings() : null);
   }

   public QName getServiceNamePattern()
   {
      return (chainConfig != null ? chainConfig.getServiceNamePattern() : null);
   }

   public QName getPortNamePattern()
   {
      return (chainConfig != null ? chainConfig.getPortNamePattern() : null);
   }

   public void addPortName(String portName)
   {
      portNames.add(portName);
   }

   public Set<String> getPortNames()
   {
      return portNames;
   }
   
   public HandlerMetaDataJAXRPC getHandlerMetaDataJAXRPC (EndpointMetaData epMetaData, HandlerType type)
   {
      HandlerMetaDataJAXRPC hmd = new HandlerMetaDataJAXRPC(epMetaData, type);
      hmd.setHandlerName(getHandlerName());
      hmd.setHandlerClassName(getHandlerClass());
      hmd.seiInitParams(getInitParams());
      hmd.setSoapHeaders(getSoapHeaders());
      hmd.setSoapRoles(getSoapRoles());
      hmd.setPortNames(getPortNames());
      return hmd;
   }

   public HandlerMetaDataJAXWS getHandlerMetaDataJAXWS (EndpointMetaData epMetaData, HandlerType type)
   {
      HandlerMetaDataJAXWS hmd = new HandlerMetaDataJAXWS(epMetaData, type);
      hmd.setHandlerName(getHandlerName());
      hmd.setHandlerClassName(getHandlerClass());
      hmd.seiInitParams(getInitParams());
      hmd.setProtocolBindings(getProtocolBindings());
      hmd.setServiceNamePattern(getServiceNamePattern());
      hmd.setPortNamePattern(getPortNamePattern());
      return hmd;
   }

   public String toString()
   {
      StringBuffer buffer = new StringBuffer("\nUnifiedHandlerMetaData:");
      buffer.append("\n name=" + getHandlerName());
      buffer.append("\n class=" + getHandlerClass());
      buffer.append("\n params=" + getInitParams());
      buffer.append("\n headers=" + getSoapHeaders());
      buffer.append("\n roles=" + getSoapRoles());
      buffer.append("\n protocols=" + getProtocolBindings());
      buffer.append("\n services=" + getServiceNamePattern());
      buffer.append("\n ports=" + (getPortNamePattern() != null ? getPortNamePattern() : portNames));
      return buffer.toString();
   }
}
