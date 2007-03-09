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
package org.jboss.ws.metadata.j2ee.serviceref;

// $Id$

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.jboss.ws.integration.ServiceRefElement;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.HandlerMetaDataJAXRPC;
import org.jboss.ws.metadata.umdm.HandlerMetaDataJAXWS;
import org.jboss.ws.metadata.umdm.HandlerMetaData.HandlerType;
import org.w3c.dom.Element;

/** The unified metdata data for a handler element
 * 
 * @author Thomas.Diesler@jboss.org
 * @version $Revision$
 */
public class UnifiedHandlerMetaData extends ServiceRefElement
{
   private UnifiedHandlerChainMetaData handlerChain;
   
   // The required <handler-name> element
   private String handlerName;
   // The required <handler-class> element
   private String handlerClass;
   // The optional <init-param> elements
   private List<UnifiedInitParamMetaData> initParams = new ArrayList<UnifiedInitParamMetaData>();
   // The optional <soap-header> elements
   private Set<QName> soapHeaders = new HashSet<QName>();
   // The optional <soap-role> elements
   private Set<String> soapRoles = new HashSet<String>();
   // The optional <port-name> elements, these only apply to webserve clients
   private Set<String> portNames = new HashSet<String>();

   public UnifiedHandlerMetaData(UnifiedHandlerChainMetaData handlerChain)
   {
      this.handlerChain = handlerChain;
   }

   public UnifiedHandlerMetaData()
   {
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

   public void addInitParam(UnifiedInitParamMetaData param)
   {
      initParams.add(param);
   }

   public List<UnifiedInitParamMetaData> getInitParams()
   {
      return initParams;
   }

   public void addSoapHeader(QName qName)
   {
      soapHeaders.add(qName);
   }

   public Set<QName> getSoapHeaders()
   {
      return soapHeaders;
   }

   public void addSoapRole(String value)
   {
      soapRoles.add(value);
   }

   public Set<String> getSoapRoles()
   {
      return soapRoles;
   }

   public Set<String> getPortNames()
   {
      return portNames;
   }

   public void addPortName(String value)
   {
      portNames.add(value);
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
      if (handlerChain != null)
      {
         hmd.setProtocolBindings(handlerChain.getProtocolBindings());
         hmd.setServiceNamePattern(handlerChain.getServiceNamePattern());
         hmd.setPortNamePattern(handlerChain.getPortNamePattern());
      }
      return hmd;
   }

   public void importStandardXml(Element root)
   {
      new ServiceRefMetaDataParser().importStandardXml(root, this);
   }
}
