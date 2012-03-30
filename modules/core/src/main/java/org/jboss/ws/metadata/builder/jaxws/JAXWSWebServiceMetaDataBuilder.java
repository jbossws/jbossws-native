/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.jws.soap.SOAPMessageHandlers;
import javax.management.ObjectName;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceProvider;

import org.jboss.ws.WSException;
import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.common.Constants;
import org.jboss.ws.metadata.builder.MetaDataBuilder;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.ServerEndpointMetaData;
import org.jboss.ws.metadata.umdm.ServiceMetaData;
import org.jboss.ws.metadata.umdm.UnifiedMetaData;
import org.jboss.ws.metadata.wsdl.WSDLBinding;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.metadata.wsdl.WSDLEndpoint;
import org.jboss.ws.metadata.wsdl.WSDLService;
import org.jboss.ws.metadata.wsdl.WSDLUtils;
import org.jboss.ws.metadata.wsdl.xmlschema.JBossXSModel;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;

/**
 * An abstract annotation meta data builder.
 *
 * @author Thomas.Diesler@jboss.org
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @author Heiko.Braun@jboss.org
 * @author alessio.soldano@jboss.com
 *
 * @since 15-Oct-2005
 */
@SuppressWarnings("deprecation")
public class JAXWSWebServiceMetaDataBuilder extends JAXWSServerMetaDataBuilder
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(JAXWSWebServiceMetaDataBuilder.class);
   private PrintStream messageStream = null;

   private static class EndpointResult
   {
      private Class<?> epClass;
      private ServerEndpointMetaData sepMetaData;
      private ServiceMetaData serviceMetaData;
      private URL wsdlLocation;
   }

   public ServerEndpointMetaData buildWebServiceMetaData(Deployment dep, UnifiedMetaData wsMetaData, Class<?> sepClass, String linkName)
   {
      try
      {
         EndpointResult result = processWebService(dep, wsMetaData, sepClass, linkName);

         // Clear the java types, etc.
         ClassLoader runtimeClassLoader = dep.getRuntimeClassLoader();
         if(null == runtimeClassLoader)
            throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "RUNTIME_LOADER_CANNOT_BE_NULL"));
         
         resetMetaDataBuilder(runtimeClassLoader);

         ServerEndpointMetaData sepMetaData = result.sepMetaData;
         ServiceMetaData serviceMetaData = result.serviceMetaData;
         serviceMetaData.setWsdlLocation(result.wsdlLocation);
         Class<?> seiClass = result.epClass;

         sepMetaData.setLinkName(linkName);
         sepMetaData.setServiceEndpointImplName(sepClass.getName());
         sepMetaData.setServiceEndpointInterfaceName(seiClass.getName());

         // Process an optional @SOAPBinding annotation
         processSOAPBinding(sepMetaData, seiClass);

         // Process an optional @BindingType annotation
         processBindingType(sepMetaData, sepClass);

         // process config
         processEndpointConfig(dep, sepMetaData, sepClass, linkName);

         setupOperationsFromWSDL(serviceMetaData, sepMetaData);

         // Process web methods
         processWebMethods(sepMetaData, seiClass);
         
         processXmlSeeAlso(seiClass);

         // Init the transport guarantee
         initTransportGuaranteeJSE(dep, sepMetaData, linkName);

         // Initialize types
         createJAXBContext(sepMetaData);
         populateXmlTypes(sepMetaData);

         // Sanity check: read the generated WSDL and initialize the schema model
         // Note, this should no longer be needed, look into removing it
         WSDLDefinitions wsdlDefinitions = serviceMetaData.getWsdlDefinitions();
         JBossXSModel schemaModel = WSDLUtils.getSchemaModel(wsdlDefinitions.getWsdlTypes());
         serviceMetaData.getTypesMetaData().setSchemaModel(schemaModel);

         // Note, that @WebContext needs to be defined on the endpoint not the SEI
         processWebContext(dep, sepClass, linkName, sepMetaData);

         // setup handler chain from config
         sepMetaData.initEndpointConfig();

         // Process an optional @HandlerChain annotation
         if (sepClass.isAnnotationPresent(HandlerChain.class))
            processHandlerChain(sepMetaData, sepClass);
         else if (seiClass.isAnnotationPresent(HandlerChain.class))
            processHandlerChain(sepMetaData, seiClass);
         
         // process webservices.xml contributions
         processWSDDContribution(dep, sepMetaData);
         
         // Init the endpoint address
         initEndpointAddress(dep, sepMetaData);

         // Process an optional @SOAPMessageHandlers annotation
         if (sepClass.isAnnotationPresent(SOAPMessageHandlers.class) || seiClass.isAnnotationPresent(SOAPMessageHandlers.class))
            log.warn(BundleUtils.getMessage(bundle, "SOAPMESSAGEHANDLERS_IS_DEPRECATED"));

         MetaDataBuilder.replaceAddressLocation(sepMetaData);

         // init service endpoint id
         ObjectName sepID = MetaDataBuilder.createServiceEndpointID(dep, sepMetaData);
         sepMetaData.setServiceEndpointID(sepID);

         return sepMetaData;
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         WSException.rethrow("Cannot build meta data: " + ex.getMessage(), ex);
         return null;
      }
   }

   private void setupOperationsFromWSDL(ServiceMetaData serviceMetaData, EndpointMetaData epMetaData)
   {
      WSDLDefinitions wsdlDefinitions = serviceMetaData.getWsdlDefinitions();
      if (wsdlDefinitions == null)
         return; // nothing to parse - WSDL will be generated
      
      QName serviceName = serviceMetaData.getServiceName();

      // Get the WSDL service
      WSDLService wsdlService = null;
      if (serviceName == null)
      {
         if (wsdlDefinitions.getServices().length != 1)
            throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "EXPECTED_A_SINGLE_SERVICE_ELEMENT"));

         wsdlService = wsdlDefinitions.getServices()[0];
         serviceMetaData.setServiceName(wsdlService.getName());
      }
      else
      {
         wsdlService = wsdlDefinitions.getService(serviceName);
      }
      if (wsdlService == null)
      {
         List<QName> serviceNames = new ArrayList<QName>();
         for (WSDLService wsdls : wsdlDefinitions.getServices())
            serviceNames.add(wsdls.getName());

         log.warn(BundleUtils.getMessage(bundle, "CANNOT_OBTAIN_WSDL_SERVICE", new Object[]{ serviceName ,  serviceNames}));
         return;
      }

      WSDLEndpoint wsdlEndpoint = this.getWsdlEndpoint(wsdlDefinitions, epMetaData.getPortName());
      if (wsdlEndpoint == null)
         throw new WSException(BundleUtils.getMessage(bundle, "CANNOT_FIND_PORT_IN_WSDL",  epMetaData.getPortName()));
      
      QName bindingName = wsdlEndpoint.getBinding();
      WSDLBinding wsdlBinding = wsdlEndpoint.getWsdlService().getWsdlDefinitions().getBinding(bindingName);
      String bindingType = wsdlBinding.getType();
      if (Constants.NS_SOAP11.equals(bindingType) || Constants.NS_SOAP12.equals(bindingType))
      {
         setupOperationsFromWSDL(epMetaData, wsdlEndpoint);
      }
   }
   
   private WSDLEndpoint getWsdlEndpoint(WSDLDefinitions wsdlDefinitions, QName portName)
   {
      WSDLEndpoint wsdlEndpoint = null;
      for (WSDLService wsdlService : wsdlDefinitions.getServices())
      {
         WSDLEndpoint auxEndpoint = wsdlService.getEndpoint(portName);
         if (auxEndpoint != null)
         {
            wsdlEndpoint = auxEndpoint;
            break;
         }
      }
      return wsdlEndpoint;
   }

   private EndpointResult processWebService(Deployment dep, UnifiedMetaData wsMetaData, Class<?> sepClass, String linkName) throws ClassNotFoundException, IOException
   {
      WebService anWebService = sepClass.getAnnotation(WebService.class);
      WebServiceProvider anWebServiceProvider = sepClass.getAnnotation(WebServiceProvider.class);
      if ((anWebService == null) && (anWebServiceProvider == null))
         throw new WSException(BundleUtils.getMessage(bundle, "CANNOT_OBTAIN_ANNOTATION",  sepClass.getName()));

      Endpoint ep = dep.getService().getEndpointByName(linkName);
      
      Class<?> seiClass = null;
      String seiName;
      WSDLUtils wsdlUtils = WSDLUtils.getInstance();

      String name = (anWebService != null) ? anWebService.name() : "";
      if (name.length() == 0)
         name = WSDLUtils.getJustClassName(sepClass);

      String serviceName = (anWebService != null) ? anWebService.serviceName() : anWebServiceProvider.serviceName();
      if (serviceName.length() == 0)
         serviceName = WSDLUtils.getJustClassName(sepClass) + "Service";

      String serviceNS = (anWebService != null) ? anWebService.targetNamespace() : anWebServiceProvider.targetNamespace();
      if (serviceNS.length() == 0)
         serviceNS = wsdlUtils.getTypeNamespace(sepClass);

      String portName = (anWebService != null) ? anWebService.portName() : anWebServiceProvider.portName();
      if (portName.length() == 0)
         portName = name + "Port";

      String wsdlLocation = (anWebService != null) ? anWebService.wsdlLocation() : anWebServiceProvider.wsdlLocation();
      String interfaceNS = serviceNS; // the default, but a SEI annotation may override this

      if (anWebService != null && anWebService.endpointInterface().length() > 0)
      {
         seiName = anWebService.endpointInterface();
         ClassLoader runtimeClassLoader = dep.getRuntimeClassLoader();
         if(null == runtimeClassLoader)
            throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "RUNTIME_LOADER_CANNOT_BE_NULL"));
         
         seiClass = runtimeClassLoader.loadClass(seiName);
         WebService seiAnnotation = seiClass.getAnnotation(WebService.class);

         if (seiAnnotation == null)
            throw new WSException(BundleUtils.getMessage(bundle, "DOES_NOT_HAVE_WEBSERVICE_ANNOTATION",  seiName));

         if (seiAnnotation.portName().length() > 0 || seiAnnotation.serviceName().length() > 0 || seiAnnotation.endpointInterface().length() > 0)
            throw new WSException(BundleUtils.getMessage(bundle, "CANNOT_HAVE_ATTRIBUTES" + seiName));

         // Redefine the interface or "PortType" name
         name = seiAnnotation.name();
         if (name.length() == 0)
            name = WSDLUtils.getJustClassName(seiClass);

         interfaceNS = seiAnnotation.targetNamespace();
         if (interfaceNS.length() == 0)
            interfaceNS = wsdlUtils.getTypeNamespace(seiClass);

         // The spec states that WSDL location should be allowed on an SEI, although it
         // makes far more sense on the implementation bean, so we ONLY consider the SEI
         // wsdlLocation when it is not defined on the bean already

         if (wsdlLocation.length() == 0)
            wsdlLocation = seiAnnotation.wsdlLocation();
      }

      // Setup the ServerEndpointMetaData
      QName portQName = new QName(serviceNS, portName);
      QName portTypeQName = new QName(interfaceNS, name);

      EndpointResult result = new EndpointResult();
      result.serviceMetaData = new ServiceMetaData(wsMetaData, new QName(serviceNS, serviceName));
      result.sepMetaData = new ServerEndpointMetaData(result.serviceMetaData, ep, portQName, portTypeQName, EndpointMetaData.Type.JAXWS);
      result.epClass = (seiClass != null ? seiClass : sepClass);
      result.serviceMetaData.addEndpoint(result.sepMetaData);
      wsMetaData.addService(result.serviceMetaData);

      if (dep instanceof ArchiveDeployment)
         result.wsdlLocation = ((ArchiveDeployment)dep).getResourceResolver().resolve(wsdlLocation);

      return result;
   }

   private void message(String msg)
   {
      if (messageStream != null)
         messageStream.println(msg);
   }

   public void setMessageStream(PrintStream messageStream)
   {
      this.messageStream = messageStream;
   }
}
