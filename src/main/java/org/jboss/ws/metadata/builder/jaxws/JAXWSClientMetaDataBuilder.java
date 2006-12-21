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
package org.jboss.ws.metadata.builder.jaxws;

//$Id$

import java.io.IOException;
import java.net.URL;

import javax.xml.namespace.QName;

import org.jboss.logging.Logger;
import org.jboss.ws.Constants;
import org.jboss.ws.WSException;
import org.jboss.ws.core.jaxrpc.Style;
import org.jboss.ws.metadata.umdm.ClientEndpointMetaData;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.OperationMetaData;
import org.jboss.ws.metadata.umdm.ServiceMetaData;
import org.jboss.ws.metadata.umdm.UnifiedMetaData;
import org.jboss.ws.metadata.umdm.EndpointMetaData.Type;
import org.jboss.ws.metadata.wsdl.NCName;
import org.jboss.ws.metadata.wsdl.WSDLBinding;
import org.jboss.ws.metadata.wsdl.WSDLBindingOperation;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.metadata.wsdl.WSDLEndpoint;
import org.jboss.ws.metadata.wsdl.WSDLInterface;
import org.jboss.ws.metadata.wsdl.WSDLInterfaceOperation;
import org.jboss.ws.metadata.wsdl.WSDLService;
import org.jboss.ws.metadata.wsdl.WSDLUtils;
import org.jboss.ws.metadata.wsdl.xmlschema.JBossXSModel;

/**
 * A client side meta data builder.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 19-May-2005
 */
public class JAXWSClientMetaDataBuilder extends JAXWSMetaDataBuilder
{
   // provide logging
   private final Logger log = Logger.getLogger(JAXWSClientMetaDataBuilder.class);

   /** Build from WSDL and jaxrpc-mapping.xml
    */
   public ServiceMetaData buildMetaData(QName serviceName, URL wsdlURL, ClassLoader loader)
   {
      if (wsdlURL == null)
         throw new IllegalArgumentException("Invalid wsdlURL: " + wsdlURL);

      log.debug("START buildMetaData: [service=" + serviceName + "]");
      try
      {
         UnifiedMetaData wsMetaData = new UnifiedMetaData();

         ServiceMetaData serviceMetaData = new ServiceMetaData(wsMetaData, serviceName);
         wsMetaData.addService(serviceMetaData);

         serviceMetaData.setWsdlLocation(wsdlURL);
         WSDLDefinitions wsdlDefinitions = serviceMetaData.getWsdlDefinitions();

         buildMetaDataInternal(serviceMetaData, wsdlDefinitions);

         // Read the WSDL and initialize the schema model
         // This should only be needed for debuging purposes of the UMDM
         JBossXSModel schemaModel = WSDLUtils.getSchemaModel(wsdlDefinitions.getWsdlTypes());
         serviceMetaData.getTypesMetaData().setSchemaModel(schemaModel);

         log.debug("END buildMetaData: " + wsMetaData);
         return serviceMetaData;
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         throw new WSException("Cannot build meta data: " + ex.getMessage(), ex);
      }
   }

   private void buildMetaDataInternal(ServiceMetaData serviceMetaData, WSDLDefinitions wsdlDefinitions) throws IOException
   {
      QName serviceName = serviceMetaData.getServiceName();

      // Get the WSDL service
      WSDLService wsdlService = null;
      if (serviceName == null)
      {
         if (wsdlDefinitions.getServices().length != 1)
            throw new IllegalArgumentException("Expected a single service element");

         wsdlService = wsdlDefinitions.getServices()[0];
         serviceMetaData.setServiceName(wsdlService.getQName());
      }
      else
      {
         wsdlService = wsdlDefinitions.getService(new NCName(serviceName.getLocalPart()));
      }
      if (wsdlService == null)
         throw new IllegalArgumentException("Cannot obtain wsdl service: " + serviceName);

      // Build endpoint meta data
      for (WSDLEndpoint wsdlEndpoint : wsdlService.getEndpoints())
      {
         QName portName = wsdlEndpoint.getQName();
         QName interfaceQName = wsdlEndpoint.getInterface().getQName();
         ClientEndpointMetaData epMetaData = new ClientEndpointMetaData(serviceMetaData, portName, interfaceQName, Type.JAXWS);
         epMetaData.setEndpointAddress(wsdlEndpoint.getAddress());
         serviceMetaData.addEndpoint(epMetaData);

         // Init the endpoint binding
         initEndpointBinding(wsdlEndpoint, epMetaData);

         // Init the service encoding style
         initEndpointEncodingStyle(epMetaData);

         setupOperationsFromWSDL(epMetaData, wsdlEndpoint);
      }
   }

   protected void setupOperationsFromWSDL(EndpointMetaData epMetaData, WSDLEndpoint wsdlEndpoint)
   {
      WSDLDefinitions wsdlDefinitions = wsdlEndpoint.getInterface().getWsdlDefinitions();

      // For every WSDL interface operation build the OperationMetaData
      WSDLInterface wsdlInterface = wsdlEndpoint.getInterface();
      for (WSDLInterfaceOperation wsdlOperation : wsdlInterface.getOperations())
      {
         String opName = wsdlOperation.getName().toString();
         QName opQName = wsdlOperation.getQName();

         // Set java method name
         String javaName = opName.substring(0, 1).toLowerCase() + opName.substring(1);

         OperationMetaData opMetaData = new OperationMetaData(epMetaData, opQName, javaName);
         epMetaData.addOperation(opMetaData);

         // Set the operation style
         String style = wsdlOperation.getStyle();
         epMetaData.setStyle((Constants.URI_STYLE_IRI.equals(style) ? Style.DOCUMENT : Style.RPC));

         // Set the operation MEP
         if (Constants.WSDL20_PATTERN_IN_ONLY.equals(wsdlOperation.getPattern()))
            opMetaData.setOneWay(true);

         // Set the operation SOAPAction
         WSDLBinding wsdlBinding = wsdlDefinitions.getBindingByInterfaceName(wsdlInterface.getQName());
         WSDLBindingOperation wsdlBindingOperation = wsdlBinding.getOperationByRef(opQName);
         if (wsdlBindingOperation != null)
            opMetaData.setSOAPAction(wsdlBindingOperation.getSOAPAction());
      }
   }
}
