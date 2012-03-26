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
package org.jboss.ws.metadata.builder.jaxws;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.ResourceBundle;

import javax.xml.ws.RespectBinding;
import javax.xml.ws.RespectBindingFeature;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.Addressing;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.spi.WebServiceFeatureAnnotation;

import org.jboss.logging.Logger;
import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.common.DOMWriter;
import org.jboss.ws.extensions.addressing.jaxws.WSAddressingServerHandler;
import org.jboss.ws.metadata.umdm.HandlerMetaDataJAXWS;
import org.jboss.ws.metadata.umdm.ServerEndpointMetaData;
import org.jboss.ws.metadata.umdm.ServiceMetaData;
import org.jboss.ws.metadata.wsdl.Extendable;
import org.jboss.ws.metadata.wsdl.WSDLBinding;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.metadata.wsdl.WSDLEndpoint;
import org.jboss.ws.metadata.wsdl.WSDLExtensibilityElement;
import org.jboss.ws.metadata.wsdl.WSDLService;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerMetaData.HandlerType;

/**
 * Process EndpointFeature annotations
 *
 * @author Thomas.Diesler@jboss.com
 * @since 12-Mar-2008
 */
public class EndpointFeatureProcessor
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(EndpointFeatureProcessor.class);
   private static final Logger log = Logger.getLogger(EndpointFeatureProcessor.class);
   
   protected void processEndpointFeatures(Deployment dep, ServerEndpointMetaData sepMetaData, Class<?> sepClass)
   {
      for (Annotation an : sepClass.getAnnotations())
      {
         WebServiceFeatureAnnotation wsfa = an.annotationType().getAnnotation(WebServiceFeatureAnnotation.class);
         if (wsfa != null)
         {
            if (an.annotationType() == Addressing.class)
            {
               Addressing anFeature = sepClass.getAnnotation(Addressing.class);
               AddressingFeature feature = new AddressingFeature(anFeature.enabled(), anFeature.required(), anFeature.responses());
               sepMetaData.addFeature(feature);
            }
            else if (an.annotationType() == RespectBinding.class)
            {
               RespectBinding anFeature = sepClass.getAnnotation(RespectBinding.class);
               RespectBindingFeature feature = new RespectBindingFeature(anFeature.enabled());
               sepMetaData.addFeature(feature);
            }
            else
            {
               throw new WebServiceException(BundleUtils.getMessage(bundle, "UNSUPPORTED_FEATURE",  wsfa.bean()));
            }
         }
      }
   }
   
   protected void setupEndpointFeatures(ServerEndpointMetaData sepMetaData)
   {
      setupAddressingFeature(sepMetaData);
      setupRespectBindingFeature(sepMetaData);
   }
   
   private static void setupAddressingFeature(ServerEndpointMetaData sepMetaData)
   {
      AddressingFeature addressingFeature = sepMetaData.getFeature(AddressingFeature.class);
      if (addressingFeature != null && addressingFeature.isEnabled())
      {
         log.debug("AddressingFeature found, installing WS-Addressing post-handler");
         HandlerMetaDataJAXWS hmd = new HandlerMetaDataJAXWS(HandlerType.POST);
         hmd.setEndpointMetaData(sepMetaData);
         hmd.setHandlerClassName(WSAddressingServerHandler.class.getName());
         hmd.setHandlerName("WSAddressing Handler");
         hmd.setProtocolBindings("##SOAP11_HTTP ##SOAP12_HTTP");
         sepMetaData.addHandler(hmd);
      }
   }
   
   private static void setupRespectBindingFeature(ServerEndpointMetaData sepMetaData)
   {
      RespectBindingFeature respectBindingFeature = sepMetaData.getFeature(RespectBindingFeature.class);
      if (respectBindingFeature != null && respectBindingFeature.isEnabled())
      {
         log.debug("RespectBindingFeature found, looking for required not understood extensibility elements...");
         ServiceMetaData serviceMetaData = sepMetaData.getServiceMetaData();
         WSDLDefinitions wsdlDefinitions = serviceMetaData.getWsdlDefinitions();
         
         WSDLService wsdlService = wsdlDefinitions.getService(serviceMetaData.getServiceName());
         if (wsdlService != null)
         {
            WSDLEndpoint wsdlEndpoint = wsdlService.getEndpoint(sepMetaData.getPortName());
            if (wsdlEndpoint != null)
            {
               // Conformance 6.11 (javax.xml.ws.RespectBindingFeature): When the javax.xml.ws.RespectBindingFeature
               // is enabled, a JAX-WS implementation MUST inspect the wsdl:binding at runtime to determine
               // result and parameter bindings as well as any wsdl:extensions that have the required=true attribute.
               // All required wsdl:extensions MUST be supported and honored by a JAX-WS implementation unless a
               // specific wsdl:extension has be explicitly disabled via a WebServiceFeature.
               checkNotUnderstoodExtElements(wsdlEndpoint, sepMetaData);
               WSDLBinding wsdlBinding = wsdlDefinitions.getBinding(wsdlEndpoint.getBinding());
               checkNotUnderstoodExtElements(wsdlBinding, sepMetaData);
            }
            else
            {
               log.warn(BundleUtils.getMessage(bundle, "CANNOT_FIND_PORT",  sepMetaData.getPortName()));
            }
         }
      }
   }
   
   private static void checkNotUnderstoodExtElements(Extendable extendable, ServerEndpointMetaData sepMetaData)
   {
      List<WSDLExtensibilityElement> notUnderstoodList = extendable.getNotUnderstoodExtElements();
      for (WSDLExtensibilityElement el : notUnderstoodList)
      {
         boolean disabledByFeature = false; //TODO [JBWS-2459]
         if (el.isRequired() && !disabledByFeature)
         {
            String s = DOMWriter.printNode(el.getElement(), true);
            throw new WebServiceException(BundleUtils.getMessage(bundle, "NOT_UNDERSTOOD_ELEMENT_WAS_FOUND",  s));
         }
      }
   }

}
