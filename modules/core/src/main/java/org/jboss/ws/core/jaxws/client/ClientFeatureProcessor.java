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
package org.jboss.ws.core.jaxws.client;

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.RespectBindingFeature;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.soap.MTOMFeature;
import javax.xml.ws.soap.SOAPBinding;

import org.jboss.logging.Logger;
import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.common.DOMWriter;
import org.jboss.ws.core.StubExt;
import org.jboss.ws.core.jaxws.binding.BindingExt;
import org.jboss.ws.extensions.addressing.jaxws.WSAddressingClientHandler;
import org.jboss.ws.feature.ChunkedEncodingFeature;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.FeatureAwareEndpointMetaData;
import org.jboss.ws.metadata.umdm.FeatureSet;
import org.jboss.ws.metadata.umdm.ServiceMetaData;
import org.jboss.ws.metadata.wsdl.Extendable;
import org.jboss.ws.metadata.wsdl.WSDLBinding;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.metadata.wsdl.WSDLEndpoint;
import org.jboss.ws.metadata.wsdl.WSDLExtensibilityElement;
import org.jboss.ws.metadata.wsdl.WSDLService;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerMetaData.HandlerType;

/**
 * Process WebServiceFeature provided on client side
 * 
 * @author alessio.soldano@jboss.com
 * @since 14-Jan-2009
 *
 */
public class ClientFeatureProcessor
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(ClientFeatureProcessor.class);
   private static Logger log = Logger.getLogger(ClientFeatureProcessor.class);
   
   private static FeatureSet supportedFeatures = new FeatureSet();
   static
   {
      supportedFeatures.addFeature(new AddressingFeature());
      supportedFeatures.addFeature(new MTOMFeature());
      supportedFeatures.addFeature(new RespectBindingFeature());
      supportedFeatures.addFeature(new ChunkedEncodingFeature());
   }
   
   public static <T> void processFeature(WebServiceFeature feature, EndpointMetaData epMetaData, T stub)
   {
      if (!supportedFeatures.hasFeature(feature.getClass()))
      {
         throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "UNSUPPORTED_FEATURE",  feature));
      }
      processAddressingFeature(feature, epMetaData, stub);
      processMTOMFeature(feature, epMetaData, stub);
      processRespectBindingFeature(feature, epMetaData, stub);
      processChunkedEncodingFeature(feature, epMetaData, stub);

      // overriding feature for this stub
      FeatureAwareEndpointMetaData featureOverridableMetaData = (FeatureAwareEndpointMetaData)stub;
      featureOverridableMetaData.setFeature(feature);
   }
   
   /**
    * Setup addressing
    * 
    * @param <T>
    * @param feature
    * @param epMetaData
    * @param stub
    * @return
    */
   @SuppressWarnings("unchecked")
   private static <T> void processAddressingFeature(WebServiceFeature feature, EndpointMetaData epMetaData, T stub)
   {
      if (feature instanceof AddressingFeature)
      {
         BindingExt bindingExt = (BindingExt) ((BindingProvider) stub).getBinding();
         List<Handler> handlers = bindingExt.getHandlerChain(HandlerType.POST);
         int addressingHandlerIndex = getAddressingHandlerIndex(handlers);

         if (feature.isEnabled())
         {
            if (addressingHandlerIndex == -1)
            {
               handlers.add(new WSAddressingClientHandler());
               bindingExt.setHandlerChain(handlers, HandlerType.POST);
            }
         }
         else if (addressingHandlerIndex != -1)
         {
            handlers.remove(addressingHandlerIndex);
            bindingExt.setHandlerChain(handlers, HandlerType.POST);
         }
      }
   }
   
   private static int getAddressingHandlerIndex(final List<Handler> handlers)
   {
      if (handlers != null)
      {
         for (int i = 0; i < handlers.size(); i++)
         {
            if (handlers.get(i) instanceof WSAddressingClientHandler)
               return i;
         }
      }
      
      return -1;
   }
   
   /**
    * Setup http chunked encoding
    * 
    * @param <T>
    * @param feature
    * @param epMetaData
    * @param stub
    * @return
    */
   private static <T> void processChunkedEncodingFeature(WebServiceFeature feature, EndpointMetaData epMetaData, T stub)
   {
      if (feature instanceof ChunkedEncodingFeature)
      {
         Map<String, Object> ctx = ((BindingProvider)stub).getRequestContext();
         ctx.put(StubExt.PROPERTY_CHUNKED_ENCODING_SIZE, ((ChunkedEncodingFeature)feature).getChunkSize());
      }
   }
   
   /**
    * Setup MTOM
    * 
    * @param <T>
    * @param feature
    * @param epMetaData
    * @param stub
    * @return
    */
   private static <T> void processMTOMFeature(WebServiceFeature feature, EndpointMetaData epMetaData, T stub)
   {
      if (feature instanceof MTOMFeature)
      {
         SOAPBinding binding = (SOAPBinding)((BindingProvider)stub).getBinding();
         binding.setMTOMEnabled(feature.isEnabled());
      }
   }
   
   /**
    * 
    * Perform respect binding checks
    * 
    * @param <T>
    * @param feature
    * @param epMetaData
    * @param stub
    * @return
    */
   private static <T> void processRespectBindingFeature(WebServiceFeature feature, EndpointMetaData epMetaData, T stub)
   {
      if (feature instanceof RespectBindingFeature && feature.isEnabled())
      {
         ServiceMetaData serviceMetaData = epMetaData.getServiceMetaData();
         WSDLDefinitions wsdlDefinitions = serviceMetaData.getWsdlDefinitions();
         
         WSDLService wsdlService = wsdlDefinitions.getService(serviceMetaData.getServiceName());
         if (wsdlService != null)
         {
            WSDLEndpoint wsdlEndpoint = wsdlService.getEndpoint(epMetaData.getPortName());
            if (wsdlEndpoint != null)
            {
               // Conformance 6.11 (javax.xml.ws.RespectBindingFeature): When the javax.xml.ws.RespectBindingFeature
               // is enabled, a JAX-WS implementation MUST inspect the wsdl:binding at runtime to determine
               // result and parameter bindings as well as any wsdl:extensions that have the required=true attribute.
               // All required wsdl:extensions MUST be supported and honored by a JAX-WS implementation unless a
               // specific wsdl:extension has be explicitly disabled via a WebServiceFeature.
               checkNotUnderstoodExtElements(wsdlEndpoint, epMetaData);
               WSDLBinding wsdlBinding = wsdlDefinitions.getBinding(wsdlEndpoint.getBinding());
               checkNotUnderstoodExtElements(wsdlBinding, epMetaData);
            }
            else
            {
               log.warn(BundleUtils.getMessage(bundle, "CANNOT_FIND_PORT",  epMetaData.getPortName()));
            }
         }
      }
   }
   
   private static void checkNotUnderstoodExtElements(Extendable extendable, EndpointMetaData epMetaData)
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
