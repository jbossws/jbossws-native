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

// $Id$

import org.jboss.ws.Constants;
import org.jboss.ws.WSException;
import org.jboss.ws.core.server.UnifiedDeploymentInfo;
import org.jboss.ws.core.utils.IOUtils;
import org.jboss.ws.metadata.builder.MetaDataBuilder;
import org.jboss.ws.metadata.umdm.*;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.metadata.wsdl.WSDLUtils;
import org.jboss.ws.metadata.wsdl.xmlschema.JBossXSModel;
import org.jboss.ws.metadata.wsse.WSSecurityConfigFactory;
import org.jboss.ws.metadata.wsse.WSSecurityConfiguration;
import org.jboss.ws.tools.jaxws.JAXBWSDLGenerator;
import org.jboss.ws.tools.wsdl.WSDLGenerator;
import org.jboss.ws.tools.wsdl.WSDLWriter;
import org.jboss.ws.tools.wsdl.WSDLWriterResolver;

import javax.jws.*;
import javax.jws.soap.SOAPMessageHandlers;
import javax.management.ObjectName;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;

/**
 * An abstract annotation meta data builder.
 *
 * @author Thomas.Diesler@jboss.org
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @author Heiko.Braun@jboss.org
 *
 * @since 15-Oct-2005
 */
@SuppressWarnings("deprecation")
public class JAXWSWebServiceMetaDataBuilder extends JAXWSServerMetaDataBuilder
{
   private static class EndpointResult
   {
      private Class<?> epClass;
      private ServerEndpointMetaData sepMetaData;
      private ServiceMetaData serviceMetaData;
      private URL wsdlLocation;
   }


   public ServerEndpointMetaData buildWebServiceMetaData(UnifiedMetaData wsMetaData, UnifiedDeploymentInfo udi, Class<?> sepClass, String linkName)
   {
      try
      {
         EndpointResult result = processWebService(wsMetaData, sepClass, udi);

         // Clear the java types, etc.
         resetMetaDataBuilder(udi.classLoader);

         ServerEndpointMetaData sepMetaData = result.sepMetaData;
         ServiceMetaData serviceMetaData = result.serviceMetaData;
         Class<?> seiClass = result.epClass;

         sepMetaData.setLinkName(linkName);
         sepMetaData.setServiceEndpointImplName(sepClass.getName());
         sepMetaData.setServiceEndpointInterfaceName(seiClass.getName());

         // Assign the WS-Security configuration,
         WSSecurityConfigFactory wsseConfFactory = WSSecurityConfigFactory.newInstance();
         WSSecurityConfiguration securityConfiguration = wsseConfFactory.createConfiguration(udi);
         serviceMetaData.setSecurityConfiguration(securityConfiguration);

         // Process an optional @SOAPBinding annotation
         processSOAPBinding(sepMetaData, seiClass);

         // Process an optional @BindingType annotation
         processBindingType(sepMetaData, seiClass);

         // Process web methods
         processWebMethods(sepMetaData, seiClass);

         // Init the transport guarantee
         initTransportGuaranteeJSE(udi, sepMetaData, linkName);

         // Initialize types
         createJAXBContext(sepMetaData);
         populateXmlTypes(sepMetaData);

         // Process or generate WSDL
         processOrGenerateWSDL(seiClass, serviceMetaData, result.wsdlLocation, sepMetaData);

         // process config
         processEndpointConfig(udi, sepClass, linkName, sepMetaData);

         // Note, that @WebContext needs to be defined on the endpoint not the SEI
         processWebContext(udi, sepClass, linkName, sepMetaData);

         // setup handler chain from config
         sepMetaData.configure(sepMetaData);

         // Process an optional @HandlerChain annotation
         if (sepClass.isAnnotationPresent(HandlerChain.class))
            processHandlerChain(sepMetaData, sepClass);
         else if (seiClass.isAnnotationPresent(HandlerChain.class))
            processHandlerChain(sepMetaData, seiClass);

         // Sanity check: read the generated WSDL and initialize the schema model
         WSDLDefinitions wsdlDefinitions = serviceMetaData.getWsdlDefinitions();
         JBossXSModel schemaModel = WSDLUtils.getSchemaModel(wsdlDefinitions.getWsdlTypes());
         serviceMetaData.getTypesMetaData().setSchemaModel(schemaModel);

         // Init the endpoint address
         MetaDataBuilder.initEndpointAddress(udi, sepMetaData, linkName);

         // replace the SOAP address
         MetaDataBuilder.replaceAddressLocation(sepMetaData);

         // Process an optional @SOAPMessageHandlers annotation
         if (sepClass.isAnnotationPresent(SOAPMessageHandlers.class) || seiClass.isAnnotationPresent(SOAPMessageHandlers.class))
            log.warn("@SOAPMessageHandlers is deprecated as of JAX-WS 2.0 with no replacement.");

         // process endpoint meta data extensions
         processEndpointMetaDataExtensions(sepMetaData, wsdlDefinitions);

         // init service endpoint id
         ObjectName sepID = MetaDataBuilder.createServiceEndpointID(udi, sepMetaData);
         sepMetaData.setServiceEndpointID(sepID);

         return sepMetaData;
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
   
   private EndpointResult processWebService(UnifiedMetaData wsMetaData, Class<?> sepClass, UnifiedDeploymentInfo udi) throws ClassNotFoundException, IOException
   {
      WebService anWebService = sepClass.getAnnotation(WebService.class);
      if (anWebService == null)
         throw new WSException("Cannot obtain @WebService annotation from: " + sepClass.getName());

      Class<?> seiClass = null;
      String seiName;
      WSDLUtils wsdlUtils = WSDLUtils.getInstance();

      String name = anWebService.name();
      if (name.length() == 0)
         name = WSDLUtils.getJustClassName(sepClass);

      String serviceName = anWebService.serviceName();
      if (serviceName.length() == 0)
         serviceName = name + "Service";

      String serviceNS = anWebService.targetNamespace();
      if (serviceNS.length() == 0)
         serviceNS = wsdlUtils.getTypeNamespace(sepClass);

      String portName = anWebService.portName();
      if (portName.length() == 0)
         portName = name + "Port";

      String wsdlLocation = anWebService.wsdlLocation();
      String interfaceNS = serviceNS;

      if (anWebService.endpointInterface().length() > 0)
      {
         seiName = anWebService.endpointInterface();
         seiClass = udi.classLoader.loadClass(seiName);
         anWebService = seiClass.getAnnotation(WebService.class);

         if (anWebService == null)
            throw new WSException("Interface does not have a @WebService annotation: " + seiName);

         if (anWebService.portName().length() > 0 || anWebService.serviceName().length() > 0 || anWebService.endpointInterface().length() > 0)
            throw new WSException("@WebService[portName,serviceName,endpointInterface] MUST NOT be defined on: " + seiName);

         // Redfine the interface or "PortType" name
         name = anWebService.name();
         if (name.length() == 0)
            name = WSDLUtils.getJustClassName(seiClass);

         interfaceNS = anWebService.targetNamespace();
         if (interfaceNS.length() == 0)
            interfaceNS = wsdlUtils.getTypeNamespace(seiClass);

         // The spec states that WSDL location should be allowed on an SEI, although it
         // makes far more sense on the implementation bean, so we ALWAYS override the SEI
         // when wsdlLocation is defined on the bean

         if (wsdlLocation.length() == 0)
            wsdlLocation = anWebService.wsdlLocation();
      }

      // Setup the ServerEndpointMetaData
      QName portQName = new QName(serviceNS, portName);
      QName portTypeQName = new QName(interfaceNS, name);

      EndpointResult result = new EndpointResult();
      result.serviceMetaData = new ServiceMetaData(wsMetaData, new QName(serviceNS, serviceName));
      result.sepMetaData = new ServerEndpointMetaData(result.serviceMetaData, portQName, portTypeQName, EndpointMetaData.Type.JAXWS);
      result.epClass = (seiClass != null ? seiClass : sepClass);
      result.wsdlLocation = udi.getMetaDataFileURL(wsdlLocation);
      result.serviceMetaData.addEndpoint(result.sepMetaData);
      wsMetaData.addService(result.serviceMetaData);

      return result;
   }

   private void processOrGenerateWSDL(Class wsClass, ServiceMetaData serviceMetaData, URL wsdlLocation, EndpointMetaData epMetaData)
   {
      if (wsdlLocation != null)
      {
         serviceMetaData.setWsdlLocation(wsdlLocation);
      }
      else
      {
         try
         {
            String serviceName = serviceMetaData.getServiceName().getLocalPart();

            WSDLGenerator generator = new JAXBWSDLGenerator(jaxbCtx);
            WSDLDefinitions wsdlDefinitions = generator.generate(serviceMetaData);

            // Ensure that types are only in the interface qname
            wsdlDefinitions.getWsdlTypes().setNamespace(epMetaData.getPortTypeName().getNamespaceURI());

            final File tmpdir = IOUtils.createTempDirectory();
            File wsdlTmpFile = File.createTempFile(serviceName, ".wsdl", tmpdir);
            wsdlTmpFile.deleteOnExit();

            Writer writer = IOUtils.getCharsetFileWriter(wsdlTmpFile, Constants.DEFAULT_XML_CHARSET);
            new WSDLWriter(wsdlDefinitions).write(writer, Constants.DEFAULT_XML_CHARSET, new WSDLWriterResolver() {
               public WSDLWriterResolver resolve(String suggestedFile) throws IOException
               {
                  File newTmpFile = File.createTempFile(suggestedFile, ".wsdl", tmpdir);
                  newTmpFile.deleteOnExit();
                  actualFile = newTmpFile.getName();
                  charset = Constants.DEFAULT_XML_CHARSET;
                  writer = IOUtils.getCharsetFileWriter(newTmpFile, Constants.DEFAULT_XML_CHARSET);
                  return this;
               }
            });
            writer.close();

            serviceMetaData.setWsdlLocation(wsdlTmpFile.toURL());
         }
         catch (RuntimeException rte)
         {
            throw rte;
         }
         catch (IOException e)
         {
            throw new WSException("Cannot write generated wsdl", e);
         }
      }
   }
}
