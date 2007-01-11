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
package org.jboss.ws.core.jaxrpc;

// $Id$

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.rpc.Call;
import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.Stub;
import javax.xml.rpc.encoding.SerializerFactory;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.soap.SOAPException;

import org.jboss.logging.Logger;
import org.jboss.ws.Constants;
import org.jboss.ws.core.CommonClient;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.jaxrpc.binding.JBossXBDeserializerFactory;
import org.jboss.ws.core.jaxrpc.binding.JBossXBSerializerFactory;
import org.jboss.ws.core.jaxrpc.handler.HandlerChainBaseImpl;
import org.jboss.ws.core.jaxrpc.handler.SOAPMessageContextJAXRPC;
import org.jboss.ws.core.soap.MessageContextAssociation;
import org.jboss.ws.core.soap.UnboundHeader;
import org.jboss.ws.core.utils.JavaUtils;
import org.jboss.ws.extensions.xop.XOPContext;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.OperationMetaData;
import org.jboss.ws.metadata.umdm.ParameterMetaData;
import org.jboss.ws.metadata.umdm.ServiceMetaData;
import org.jboss.ws.metadata.umdm.TypesMetaData;
import org.jboss.ws.metadata.umdm.HandlerMetaData.HandlerType;
import org.jboss.ws.metadata.wsdl.xmlschema.JBossXSModel;

/** Provides support for the dynamic invocation of a service endpoint.
 * The javax.xml.rpc.Service interface acts as a factory for the creation of Call instances.
 *
 * Once a Call instance is created, various setter and getter methods may be used to configure this Call instance.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 10-Oct-2004
 */
public class CallImpl extends CommonClient implements Call
{
   // provide logging
   private static Logger log = Logger.getLogger(CallImpl.class);

   // The service that created this call
   private ServiceImpl jaxrpcService;
   // The port type name
   private QName portType;
   // A Map<QName,UnboundHeader> of header entries
   private Map<QName, UnboundHeader> unboundHeaders = new LinkedHashMap<QName, UnboundHeader>();
   // A Map<String,Object> of Call properties
   private Map<String, Object> properties = new HashMap<String, Object>();

   // The set of supported properties
   private static final Set<String> standardProperties = new HashSet<String>();
   static
   {
      standardProperties.add(Call.ENCODINGSTYLE_URI_PROPERTY);
      standardProperties.add(Call.OPERATION_STYLE_PROPERTY);
      standardProperties.add(Call.SESSION_MAINTAIN_PROPERTY);
      standardProperties.add(Call.SOAPACTION_URI_PROPERTY);
      standardProperties.add(Call.SOAPACTION_USE_PROPERTY);
      standardProperties.add(Call.USERNAME_PROPERTY);
      standardProperties.add(Call.PASSWORD_PROPERTY);

      standardProperties.add(Stub.ENDPOINT_ADDRESS_PROPERTY);
      standardProperties.add(Stub.SESSION_MAINTAIN_PROPERTY);
      standardProperties.add(Stub.USERNAME_PROPERTY);
      standardProperties.add(Stub.PASSWORD_PROPERTY);
   }

   /** Create a call that needs to be configured manually
    */
   CallImpl(ServiceImpl service)
   {
      super(service.getServiceMetaData());
      this.jaxrpcService = service;

      if (epMetaData != null)
         setTargetEndpointAddress(epMetaData.getEndpointAddress());
   }

   /** Create a call for a known WSDL endpoint.
    *
    * @param epMetaData A WSDLEndpoint
    */
   CallImpl(ServiceImpl service, EndpointMetaData epMetaData)
   {
      super(epMetaData);
      this.jaxrpcService = service;

      setTargetEndpointAddress(epMetaData.getEndpointAddress());
   }

   /** Create a call for a known WSDL endpoint.
    *
    * @param portName Qualified name for the target service endpoint
    * @throws ServiceException
    */
   CallImpl(ServiceImpl service, QName portName, QName opName) throws ServiceException
   {
      super(service.getServiceMetaData(), portName, opName);
      this.jaxrpcService = service;

      if (epMetaData != null)
         setTargetEndpointAddress(epMetaData.getEndpointAddress());
   }

   public ServiceImpl getServiceImpl()
   {
      return jaxrpcService;
   }

   @Override
   protected Map<String, Object> getRequestContext()
   {
      return properties;
   }

   /**
    * Add a header that is not bound to an input parameter.
    * A propriatory extension, that is not part of JAXRPC.
    *
    * @param xmlName The XML name of the header element
    * @param xmlType The XML type of the header element
    */
   public void addUnboundHeader(QName xmlName, QName xmlType, Class javaType, ParameterMode mode)
   {
      UnboundHeader unboundHeader = new UnboundHeader(xmlName, xmlType, javaType, mode);
      unboundHeaders.put(xmlName, unboundHeader);
   }

   /**
    * Get the header value for the given XML name.
    * A propriatory extension, that is not part of JAXRPC.
    *
    * @param xmlName The XML name of the header element
    * @return The header value, or null
    */
   public Object getUnboundHeaderValue(QName xmlName)
   {
      UnboundHeader unboundHeader = unboundHeaders.get(xmlName);
      return (unboundHeader != null ? unboundHeader.getHeaderValue() : null);
   }

   /**
    * Set the header value for the given XML name.
    * A propriatory extension, that is not part of JAXRPC.
    *
    * @param xmlName The XML name of the header element
    */
   public void setUnboundHeaderValue(QName xmlName, Object value)
   {
      UnboundHeader unboundHeader = unboundHeaders.get(xmlName);
      if (unboundHeader == null)
         throw new IllegalArgumentException("Cannot find unbound header: " + xmlName);

      unboundHeader.setHeaderValue(value);
   }

   /**
    * Clear all registered headers.
    * A propriatory extension, that is not part of JAXRPC.
    */
   public void clearUnboundHeaders()
   {
      unboundHeaders.clear();
   }

   /**
    * Remove the header for the given XML name.
    * A propriatory extension, that is not part of JAXRPC.
    */
   public void removeUnboundHeader(QName xmlName)
   {
      unboundHeaders.remove(xmlName);
   }

   /**
    * Get an Iterator over the registered header XML names.
    * A propriatory extension, that is not part of JAXRPC.
    */
   public Iterator getUnboundHeaders()
   {
      return unboundHeaders.keySet().iterator();
   }

   /** Gets the address of a target service endpoint.
    */
   public String getTargetEndpointAddress()
   {
      return (String)properties.get(Stub.ENDPOINT_ADDRESS_PROPERTY);
   }

   /** Sets the address of the target service endpoint. This address must correspond to the transport
    * specified in the binding for this Call instance.
    *
    * @param address Address of the target service endpoint; specified as an URI
    */
   public void setTargetEndpointAddress(String address)
   {
      this.properties.put(Stub.ENDPOINT_ADDRESS_PROPERTY, address);
   }

   /** Adds a parameter type and mode for a specific operation.
    */
   public void addParameter(String paramName, QName xmlType, ParameterMode parameterMode)
   {
      TypeMappingImpl typeMapping = getEndpointMetaData().getServiceMetaData().getTypeMapping();
      Class javaType = typeMapping.getJavaType(xmlType);

      // CTS com/sun/ts/tests/jaxrpc/api/javax_xml_rpc/Call/AddGetRemoveAllParametersTest1
      // tests addParameter/getParameter without giving the javaType for a custom parameter
      // IMHO, this flavour of addParameter should only be used for standard types, where
      // the javaType can be derived from the xmlType
      if (javaType == null)
      {
         log.warn("Register unqualified call parameter for: " + xmlType);
         javaType = new UnqualifiedCallParameter(xmlType).getClass();
         typeMapping.register(javaType, xmlType, null, null);
      }

      addParameter(paramName, xmlType, javaType, parameterMode);
   }

   /** Adds a parameter type and mode for a specific operation.
    */
   public void addParameter(String paramName, QName xmlType, Class javaType, ParameterMode mode)
   {
      QName xmlName = new QName(paramName);
      addParameter(xmlName, xmlType, javaType, mode, false);
   }

   /** Add a parameter to the current operation description.
    * This is a propriatary extension that gives full control over the parameter configuration.
    */
   public void addParameter(QName xmlName, QName xmlType, Class javaType, ParameterMode mode, boolean inHeader)
   {
      if (xmlType == null || javaType == null)
         throw new IllegalArgumentException("Invalid null parameter");

      OperationMetaData opMetaData = getOperationMetaData();
      ParameterMetaData paramMetaData = new ParameterMetaData(opMetaData, xmlName, xmlType, javaType.getName());
      paramMetaData.setMode(mode);
      paramMetaData.setInHeader(inHeader);
      paramMetaData.setIndex(opMetaData.getParameters().size());
      opMetaData.addParameter(paramMetaData);

      registerParameterType(xmlType, javaType);
   }

   /** Removes all specified parameters from this Call instance. Note that this method removes only the parameters and
    * not the return type. The setReturnType(null) is used to remove the return type.
    *
    * @throws javax.xml.rpc.JAXRPCException This exception may be thrown If this method is called when the method isParameterAndReturnSpecRequired returns false for this Call's operation.
    */
   public void removeAllParameters()
   {
      OperationMetaData opMetaData = getOperationMetaData();
      opMetaData.removeAllParameters();
   }

   /** Sets the return type for a specific operation. Invoking setReturnType(null) removes the return type for this Call object.
    */
   public void setReturnType(QName xmlType)
   {
      Class javaType = getEndpointMetaData().getServiceMetaData().getTypeMapping().getJavaType(xmlType);
      setReturnType(xmlType, javaType);
   }

   /** Sets the return type for a specific operation.
    */
   public void setReturnType(QName xmlType, Class javaType)
   {
      if (xmlType == null || javaType == null)
         throw new IllegalArgumentException("Invalid null parameter");

      OperationMetaData opMetaData = getOperationMetaData();
      QName xmlName = new QName(Constants.DEFAULT_RPC_RETURN_NAME);
      String javaTypeName = javaType.getName();
      ParameterMetaData retMetaData = new ParameterMetaData(opMetaData, xmlName, xmlType, javaTypeName);
      opMetaData.setReturnParameter(retMetaData);

      registerParameterType(xmlType, javaType);
   }

   /** Invokes a remote method using the one-way interaction mode.
    */
   public void invokeOneWay(Object[] inputParams)
   {
      try
      {
         invokeInternal(operationName, inputParams, unboundHeaders, true);
      }
      catch (RemoteException ex)
      {
         throw new JAXRPCException(ex);
      }
   }

   /** Invokes a specific operation using a synchronous request-response interaction mode.
    */
   public Object invoke(Object[] inputParams) throws RemoteException
   {
      return invokeInternal(operationName, inputParams, unboundHeaders, false);
   }

   /** Invokes a specific operation using a synchronous request-response interaction mode.
    */
   public Object invoke(QName operationName, Object[] inputParams) throws RemoteException
   {
      return invokeInternal(operationName, inputParams, unboundHeaders, false);
   }

   protected CommonMessageContext processPivot(CommonMessageContext requestContext) {
      log.debug("Begin response processing");
      return requestContext;
   }

   /** Returns a List values for the output parameters of the last invoked operation.
    *
    * @return java.util.List Values for the output parameters. An empty List is returned if there are no output values.
    * @throws JAXRPCException If this method is invoked for a one-way operation or is invoked before any invoke method has been called.
    */
   public List getOutputValues()
   {
      if (epInv == null)
         throw new JAXRPCException("Output params not available");

      try
      {
         OperationMetaData opMetaData = getOperationMetaData();

         List<Object> objPayload = new ArrayList<Object>();
         for (QName xmlName : epInv.getResponseParamNames())
         {
            Object paramValue = epInv.getResponseParamValue(xmlName);
            if (opMetaData.isDocumentWrapped())
            {
               objPayload = Arrays.asList((Object[])paramValue);
               break;
            }
            else
            {
               objPayload.add(paramValue);
            }
         }
         return objPayload;
      }
      catch (SOAPException ex)
      {
         throw new JAXRPCException("Cannot obtain response payload", ex);
      }
   }

   /** Returns a Map of {name, value} for the output parameters of the last invoked operation.
    *  The parameter names in the returned Map are of type java.lang.String.
    *
    * @return Map Output parameters for the last Call.invoke(). Empty Map is returned if there are no output parameters.
    * @throws JAXRPCException If this method is invoked for a one-way operation or is invoked before any invoke method has been called.
    */
   public Map getOutputParams()
   {
      if (epInv == null)
         throw new JAXRPCException("Output params not available");

      try
      {
         Map<String, Object> outMap = new LinkedHashMap<String, Object>();
         for (QName xmlName : epInv.getResponseParamNames())
         {
            Object value = epInv.getResponseParamValue(xmlName);
            outMap.put(xmlName.getLocalPart(), value);
         }
         return outMap;
      }
      catch (SOAPException ex)
      {
         throw new JAXRPCException("Cannot obtain response payload", ex);
      }
   }

   /**
    * Gets the qualified name of the port type.
    *
    * @return Qualified name of the port type
    */
   public QName getPortTypeName()
   {
      if (portType != null)
      {
         return portType;
      }

      /* This code could be used to derive the portType from the endpoint meta data.
       * However, it breaks CTS com/sun/ts/tests/jaxrpc/api/javax_xml_rpc/Call/Client.java#SetGetPortTypeNameTest2
       if (epMetaData != null)
       {
       ServiceMetaData serviceMetaData = epMetaData.getServiceMetaData();
       WSDLDefinitions wsdlDefinitions = serviceMetaData.getWsdlDefinitions();
       WSDLService wsdlService = wsdlDefinitions.getService(new NCName(serviceMetaData.getName().getLocalPart()));
       WSDLEndpoint wsdlEndpoint = wsdlService.getEndpoint(epMetaData.getName());
       WSDLInterface wsdlInterface = wsdlEndpoint.getInterface();
       return wsdlInterface.getQName();
       }
       */

      // CTS com/sun/ts/tests/jaxrpc/api/javax_xml_rpc/Call/Client.java#SetGetPortTypeNameTest2
      return new QName("");
   }

   /**
    * Gets the return type for a specific operation
    *
    * @return Returns the XML type for the return value
    */
   public QName getReturnType()
   {
      QName retType = null;
      if (operationName != null)
      {
         OperationMetaData opDesc = getOperationMetaData();
         ParameterMetaData retMetaData = opDesc.getReturnParameter();
         if (retMetaData != null)
            retType = retMetaData.getXmlType();
      }
      return retType;
   }

   /**
    * Sets the qualified name of the interface.
    *
    * @param portType - Qualified name of the port type
    */
   public void setPortTypeName(QName portType)
   {
      this.portType = portType;
   }

   /**
    * Indicates whether addParameter and setReturnType methods are to be invoked to specify the parameter and return
    * type specification for a specific operation.
    *
    * @param opName Qualified name of the operation
    * @return Returns true if the Call implementation class requires addParameter and setReturnType to be invoked in the client code for the specified operation. This method returns false otherwise.
    * @throws IllegalArgumentException If invalid operation name is specified
    */
   public boolean isParameterAndReturnSpecRequired(QName opName)
   {
      setOperationName(opName);
      OperationMetaData opMetaData = getOperationMetaData();
      return opMetaData.getParameters().size() == 0 && opMetaData.getReturnParameter() == null;
   }

   /** Gets the names of configurable properties supported by this Call object.
    * @return Iterator for the property names
    */
   public Iterator getPropertyNames()
   {
      return standardProperties.iterator();
   }

   /** Gets the value of a named property.
    */
   public Object getProperty(String name)
   {
      if (null == name)
         throw new JAXRPCException("Unsupported property: " + name);
      // CTS: com/sun/ts/tests/jaxrpc/api/javax_xml_rpc/Call/Client.java#SetGetPropertyTest2
      if (name.startsWith("javax.xml.rpc") && standardProperties.contains(name) == false)
         throw new JAXRPCException("Unsupported property: " + name);

      return properties.get(name);
   }

   /** Sets the value for a named property.
    */
   public void setProperty(String name, Object value)
   {
      if (null == name)
         throw new JAXRPCException("Unsupported property: " + name);
      // CTS: com/sun/ts/tests/jaxrpc/api/javax_xml_rpc/Call/Client.java#SetGetPropertyTest2
      if (name.startsWith("javax.xml.rpc") && standardProperties.contains(name) == false)
         throw new JAXRPCException("Unsupported property: " + name);

      properties.put(name, value);
   }

   /** Removes a named property.
    */
   public void removeProperty(String name)
   {
      properties.remove(name);
   }

   /** Gets the XML type of a parameter by name.
    */
   public QName getParameterTypeByName(String paramName)
   {
      OperationMetaData opMetaData = getOperationMetaData();
      ParameterMetaData paramMetaData = opMetaData.getParameter(new QName(paramName));
      if (paramMetaData != null)
         return paramMetaData.getXmlType();
      else return null;
   }

   @Override
   protected void setInboundContextProperties()
   {
   }

   @Override
   protected void setOutboundContextProperties()
   {
   }

   private Object invokeInternal(QName opName, Object[] inputParams, Map<QName, UnboundHeader> unboundHeaders, boolean forceOneway) throws RemoteException
   {
      if (opName.equals(operationName) == false)
         setOperationName(opName);

      OperationMetaData opMetaData = getOperationMetaData();

      // Check or generate the the schema if this call is unconfigured
      generateOrUpdateSchemas(opMetaData);
      
      // Associate a message context with the current thread
      SOAPMessageContextJAXRPC msgContext = new SOAPMessageContextJAXRPC();
      MessageContextAssociation.pushMessageContext(msgContext);
      Object retObj = null;
      try
      {
         retObj = super.invoke(opName, inputParams, unboundHeaders, properties, forceOneway);
         return retObj;
      }
      catch (SOAPFaultException ex)
      {
         log.error("Call invocation failed", ex);
         String faultCode = ex.getFaultCode().getLocalPart();
         throw new RemoteException("Call invocation failed with code [" + faultCode + "] because of: " + ex.getFaultString(), ex);
      }
      catch (RemoteException rex)
      {
         throw rex;
      }
      catch (Exception ex)
      {
         throw new RemoteException("Call invocation failed", ex);
      }
      finally
      {
         // Reset the message context association
         MessageContextAssociation.popMessageContext();
      }
   }

   @Override
   protected boolean callRequestHandlerChain(QName portName, HandlerType type)
   {
      SOAPMessageContextJAXRPC msgContext = (SOAPMessageContextJAXRPC)MessageContextAssociation.peekMessageContext();
      HandlerChainBaseImpl handlerChain = (HandlerChainBaseImpl)jaxrpcService.getHandlerChain(portName);
      boolean status = (handlerChain != null ? handlerChain.handleRequest(msgContext, type) : true);

      if (type == HandlerType.ENDPOINT)
         XOPContext.visitAndRestoreXOPData();
      
      return status;
   }

   @Override
   protected boolean callResponseHandlerChain(QName portName, HandlerType type)
   {
      SOAPMessageContextJAXRPC msgContext = (SOAPMessageContextJAXRPC)MessageContextAssociation.peekMessageContext();
      HandlerChainBaseImpl handlerChain = (HandlerChainBaseImpl)jaxrpcService.getHandlerChain(portName);

      boolean status = true;
      if (handlerChain != null)
      {
         status = handlerChain.handleResponse(msgContext, type);
      }
      return status;
   }

   /** Generate or update the XSD schema for all parameters and the return.
    *  This should only be done when the Call is unconfigured, hence there is no WSDL
    */
   private void generateOrUpdateSchemas(OperationMetaData opMetaData)
   {
      ServiceMetaData serviceMetaData = opMetaData.getEndpointMetaData().getServiceMetaData();
      if (serviceMetaData.getWsdlLocation() == null)
      {
         TypesMetaData typesMetaData = serviceMetaData.getTypesMetaData();
         for (ParameterMetaData paramMetaData : opMetaData.getParameters())
         {
            generateOrUpdateParameterSchema(typesMetaData, paramMetaData);
         }

         ParameterMetaData retMetaData = opMetaData.getReturnParameter();
         if (retMetaData != null)
         {
            generateOrUpdateParameterSchema(typesMetaData, retMetaData);
         }
      }
   }

   /** Generate or update the XSD schema for a given parameter
    *  This should only be done if the parameter is not an attachment
    */
   private void generateOrUpdateParameterSchema(TypesMetaData typesMetaData, ParameterMetaData paramMetaData)
   {
      if (paramMetaData.isSwA() == false)
      {
         QName xmlType = paramMetaData.getXmlType();
         Class javaType = paramMetaData.getJavaType();

         ServiceMetaData serviceMetaData = getEndpointMetaData().getServiceMetaData();
         TypeMappingImpl typeMapping = serviceMetaData.getTypeMapping();
         SerializerFactory serFactory = typeMapping.getSerializer(javaType, xmlType);
         if (serFactory instanceof JBossXBSerializerFactory)
         {
            SchemaGenerator xsdGenerator = new SchemaGenerator();
            JBossXSModel model = xsdGenerator.generateXSDSchema(xmlType, javaType);
            typesMetaData.addSchemaModel(model);
         }
      }
   }

   private void registerParameterType(QName xmlType, Class javaType)
   {
      ServiceMetaData serviceMetaData = getEndpointMetaData().getServiceMetaData();

      String nsURI = xmlType.getNamespaceURI();
      if (Constants.NS_ATTACHMENT_MIME_TYPE.equals(nsURI) == false)
      {
         TypeMappingImpl typeMapping = serviceMetaData.getTypeMapping();
         Class regJavaType = typeMapping.getJavaType(xmlType);
         if (regJavaType == null)
         {
            typeMapping.register(javaType, xmlType, new JBossXBSerializerFactory(), new JBossXBDeserializerFactory());
         }
         else if (regJavaType != null && JavaUtils.isAssignableFrom(regJavaType, javaType) == false)
         {
            throw new IllegalArgumentException("Different java type already registered: " + regJavaType.getName());
         }
      }
   }
}
