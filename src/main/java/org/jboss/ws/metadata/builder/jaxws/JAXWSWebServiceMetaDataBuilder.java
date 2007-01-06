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

import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.api.TypeReference;
import org.jboss.logging.Logger;
import org.jboss.ws.Constants;
import org.jboss.ws.WSException;
import org.jboss.ws.core.jaxrpc.Style;
import org.jboss.ws.core.jaxrpc.Use;
import org.jboss.ws.core.jaxws.DynamicWrapperGenerator;
import org.jboss.ws.core.jaxws.WrapperGenerator;
import org.jboss.ws.core.server.UnifiedDeploymentInfo;
import org.jboss.ws.core.utils.HolderUtils;
import org.jboss.ws.core.utils.IOUtils;
import org.jboss.ws.core.utils.JBossWSEntityResolver;
import org.jboss.ws.core.utils.JavaUtils;
import org.jboss.ws.extensions.addressing.AddressingPropertiesImpl;
import org.jboss.ws.extensions.addressing.metadata.AddressingOpMetaExt;
import org.jboss.ws.metadata.acessor.JAXBAccessor;
import org.jboss.ws.metadata.builder.MetaDataBuilder;
import org.jboss.ws.metadata.j2ee.UnifiedHandlerMetaData;
import org.jboss.ws.metadata.jsr181.HandlerChainFactory;
import org.jboss.ws.metadata.jsr181.HandlerChainMetaData;
import org.jboss.ws.metadata.jsr181.HandlerChainsMetaData;
import org.jboss.ws.metadata.umdm.*;
import org.jboss.ws.metadata.umdm.HandlerMetaData.HandlerType;
import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.metadata.wsdl.WSDLUtils;
import org.jboss.ws.metadata.wsdl.xmlschema.JBossXSModel;
import org.jboss.ws.metadata.wsse.WSSecurityConfigFactory;
import org.jboss.ws.metadata.wsse.WSSecurityConfiguration;
import org.jboss.ws.tools.jaxws.JAXBWSDLGenerator;
import org.jboss.ws.tools.jaxws.WSDLGenerator;
import org.jboss.ws.tools.wsdl.WSDLWriter;
import org.jboss.ws.tools.wsdl.WSDLWriterResolver;
import org.jboss.xb.binding.ObjectModelFactory;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;

import javax.jws.*;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPMessageHandlers;
import javax.management.ObjectName;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.WebFault;
import javax.xml.ws.addressing.AddressingProperties;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
public class JAXWSWebServiceMetaDataBuilder extends JAXWSEndpointMetaDataBuilder
{
   private static class EndpointResult
   {
      private Class<?> epClass;
      private ServerEndpointMetaData sepMetaData;
      private ServiceMetaData serviceMetaData;
      private URL wsdlLocation;
   }

   // provide logging
   private static final Logger log = Logger.getLogger(JAXWSWebServiceMetaDataBuilder.class);
   private List<Class> javaTypes = new ArrayList<Class>();
   private JAXBRIContext jaxbCtx;
   private List<TypeReference> typeRefs = new ArrayList<TypeReference>();

   private WrapperGenerator wrapperGenerator;

   public JAXWSWebServiceMetaDataBuilder()
   {
   }

   private void addFault(OperationMetaData omd, Class<?> exception)
   {
      if (omd.isOneWay())
         throw new IllegalStateException("JSR-181 4.3.1 - A JSR-181 processor is REQUIRED to report an error if an operation marked "
               + "@Oneway has a return value, declares any checked exceptions or has any INOUT or OUT parameters.");

      WebFault annotation = exception.getAnnotation(WebFault.class);

      String name;
      String namespace;
      String faultBeanName = null;

      // Only the element name is effected by @WebFault, the type uses the same convention
      QName xmlType = new QName(omd.getQName().getNamespaceURI(), exception.getSimpleName());

      /*
       * If @WebFault is present, and the exception contains getFaultInfo, the
       * return value should be used. Otherwise we need to generate the bean.
       */
      boolean generate = true;
      if (annotation != null)
      {
         name = annotation.name();
         namespace = annotation.targetNamespace();
         if (namespace.length() == 0)
            namespace = omd.getQName().getNamespaceURI();

         Class<?> faultBean = getFaultInfo(exception);
         if (faultBean != null)
         {
            generate = false;
            faultBeanName = faultBean.getName();
         }
      }
      else
      {
         name = xmlType.getLocalPart();
         namespace = xmlType.getNamespaceURI();
      }

      if (faultBeanName == null)
         faultBeanName = JavaUtils.getPackageName(omd.getEndpointMetaData().getServiceEndpointInterface()) + ".jaxws." + exception.getSimpleName() + "Bean";

      QName xmlName = new QName(namespace, name);

      FaultMetaData fmd = new FaultMetaData(omd, xmlName, xmlType, exception.getName());
      fmd.setFaultBeanName(faultBeanName);
      fmd.setAccessorFactoryCreator(JAXBAccessor.FACTORY_CREATOR);

      if (generate)
         wrapperGenerator.generate(fmd);

      javaTypes.add(fmd.getFaultBean());
      typeRefs.add(new TypeReference(fmd.getXmlName(), fmd.getFaultBean()));

      omd.addFault(fmd);
   }

   private String convertToVariable(String localName)
   {
      return JAXBRIContext.mangleNameToVariableName(localName);
   }

   private String[] convertTypeArguments(Class rawType, Type type)
   {
      if (!Collection.class.isAssignableFrom(rawType) && !Map.class.isAssignableFrom(rawType))
         return null;

      if (!(type instanceof ParameterizedType))
         return null;

      ParameterizedType paramType = (ParameterizedType)type;
      Type[] arguments = paramType.getActualTypeArguments();
      String[] ret = new String[arguments.length];
      for (int i = 0; i < arguments.length; i++)
         ret[i] = JavaUtils.erasure(arguments[i]).getName();

      return ret;
   }

   private ParameterMetaData createRequestWrapper(OperationMetaData operation, Method method)
   {
      String requestWrapperType = null;
      QName xmlName = operation.getQName();
      QName xmlType = xmlName;
      if (method.isAnnotationPresent(RequestWrapper.class))
      {
         RequestWrapper anReqWrapper = method.getAnnotation(RequestWrapper.class);

         String localName = anReqWrapper.localName().length() > 0 ? anReqWrapper.localName() : xmlName.getLocalPart();
         String targetNamespace = anReqWrapper.targetNamespace().length() > 0 ? anReqWrapper.targetNamespace() : xmlName.getNamespaceURI();
         xmlName = new QName(targetNamespace, localName);

         if (anReqWrapper.className().length() > 0)
            requestWrapperType = anReqWrapper.className();
      }

      // Conformance 3.18, the default value must be the same as the method name
      if (requestWrapperType == null)
      {
         String packageName = JavaUtils.getPackageName(method.getDeclaringClass()) + ".jaxws";
         requestWrapperType = packageName + "." + JavaUtils.capitalize(method.getName());
      }

      // JAX-WS p.37 pg.1, the annotation only affects the element name, not the type name
      ParameterMetaData wrapperParameter = new ParameterMetaData(operation, xmlName, xmlType, requestWrapperType);
      wrapperParameter.setAccessorFactoryCreator(JAXBAccessor.FACTORY_CREATOR);
      operation.addParameter(wrapperParameter);

      return wrapperParameter;
   }

   private ParameterMetaData createResponseWrapper(OperationMetaData operation, Method method)
   {
      QName operationQName = operation.getQName();
      QName xmlName = new QName(operationQName.getNamespaceURI(), operationQName.getLocalPart() + "Response");
      QName xmlType = xmlName;

      String responseWrapperType = null;
      if (method.isAnnotationPresent(ResponseWrapper.class))
      {
         ResponseWrapper anResWrapper = method.getAnnotation(ResponseWrapper.class);

         String localName = anResWrapper.localName().length() > 0 ? anResWrapper.localName() : xmlName.getLocalPart();
         String targetNamespace = anResWrapper.targetNamespace().length() > 0 ? anResWrapper.targetNamespace() : xmlName.getNamespaceURI();
         xmlName = new QName(targetNamespace, localName);

         if (anResWrapper.className().length() > 0)
            responseWrapperType = anResWrapper.className();
      }

      if (responseWrapperType == null)
      {
         String packageName = JavaUtils.getPackageName(method.getDeclaringClass()) + ".jaxws";
         responseWrapperType = packageName + "." + JavaUtils.capitalize(method.getName()) + "Response";
      }

      ParameterMetaData retMetaData = new ParameterMetaData(operation, xmlName, xmlType, responseWrapperType);
      retMetaData.setAccessorFactoryCreator(JAXBAccessor.FACTORY_CREATOR);
      operation.setReturnParameter(retMetaData);

      return retMetaData;
   }

   private Class<?> getFaultInfo(Class<?> exception)
   {
      try
      {
         Method method = exception.getMethod("getFaultInfo");
         Class<?> returnType = method.getReturnType();
         if (returnType == void.class)
            return null;

         return returnType;
      }
      catch (SecurityException e)
      {
         throw new WSException("Unexpected security exception: " + e.getMessage(), e);
      }
      catch (NoSuchMethodException e)
      {
         return null;
      }
   }

   private ParameterMode getParameterMode(WebParam anWebParam, Class javaType)
   {
      if (anWebParam != null)
      {
         if (anWebParam.mode() == WebParam.Mode.INOUT)
            return ParameterMode.INOUT;
         if (anWebParam.mode() == WebParam.Mode.OUT)
            return ParameterMode.OUT;
      }

      return HolderUtils.isHolderType(javaType) ? ParameterMode.INOUT : ParameterMode.IN;
   }

   private WebParam getWebParamAnnotation(Method method, int pos)
   {
      for (Annotation annotation : method.getParameterAnnotations()[pos])
         if (annotation instanceof WebParam)
            return (WebParam)annotation;

      return null;
   }

   private QName getWebParamName(OperationMetaData opMetaData, int index, WebParam webParam)
   {
      String namespace = null;
      String name = null;
      boolean header = false;

      if (webParam != null)
      {
         if (webParam.targetNamespace().length() > 0)
            namespace = webParam.targetNamespace();

         // RPC types use the partName for their XML name
         if (webParam.partName().length() > 0 && opMetaData.isRPCLiteral())
            name = webParam.partName();
         else if (webParam.name().length() > 0)
            name = webParam.name();

         header = webParam.header();
      }

      // Bare and headers must be qualified
      if (namespace == null && (opMetaData.isDocumentBare() || header))
         namespace = opMetaData.getQName().getNamespaceURI();

      // RPC body parts must have no namespace
      else if (opMetaData.isRPCLiteral() && !header)
         namespace = null;

      // Bare uses the operation name as the default, everything else is generated
      if (name == null)
         name = opMetaData.isDocumentBare() && !header ? opMetaData.getQName().getLocalPart() : "arg" + index;

      return (namespace != null) ? new QName(namespace, name) : new QName(name);
   }

   private QName getWebResultName(OperationMetaData opMetaData, WebResult anWebResult)
   {
      String name = null;
      String namespace = null;
      boolean header = false;

      if (anWebResult != null)
      {
         if (anWebResult.targetNamespace().length() > 0)
            namespace = anWebResult.targetNamespace();

         // RPC types use the partName for their XML name
         if (anWebResult.partName().length() > 0 && opMetaData.isRPCLiteral())
            name = anWebResult.partName();
         else if (anWebResult.name().length() > 0)
            name = anWebResult.name();

         header = anWebResult.header();
      }

      // Bare and headers must be qualified
      if (namespace == null && (opMetaData.isDocumentBare() || header))
         namespace = opMetaData.getQName().getNamespaceURI();

      // RPC body parts must have no namespace
      else if (opMetaData.isRPCLiteral() && !header)
         namespace = null;

      // Bare uses the operation name as the default, everything else is generated
      if (name == null)
         name = opMetaData.isDocumentBare() && !header ? opMetaData.getResponseName().getLocalPart() : "return";

      return (namespace != null) ? new QName(namespace, name) : new QName(name);
   }

   private void populateXmlType(FaultMetaData faultMetaData)
   {
      EndpointMetaData epMetaData = faultMetaData.getOperationMetaData().getEndpointMetaData();
      TypesMetaData types = epMetaData.getServiceMetaData().getTypesMetaData();

      QName xmlType = faultMetaData.getXmlType();
      String faultBeanName = faultMetaData.getFaultBeanName();

      types.addTypeMapping(new TypeMappingMetaData(types, xmlType, faultBeanName));
   }

   private void populateXmlType(ParameterMetaData paramMetaData)
   {
      EndpointMetaData epMetaData = paramMetaData.getOperationMetaData().getEndpointMetaData();
      TypesMetaData types = epMetaData.getServiceMetaData().getTypesMetaData();

      QName xmlName = paramMetaData.getXmlName();
      QName xmlType = paramMetaData.getXmlType();
      Class javaType = paramMetaData.getJavaType();
      String javaName = paramMetaData.getJavaTypeName();

      if (xmlType == null)
      {
         try
         {
            xmlType = jaxbCtx.getTypeName(new TypeReference(xmlName, javaType));
         }
         catch (IllegalArgumentException e)
         {
            throw new IllegalStateException("Cannot obtain xml type for: [xmlName=" + xmlName + ",javaName=" + javaName + "]");
         }

         /* Anonymous type.
          *
          * Currently the design of our stack is based on the
          * notion of their always being a unique type. In order to lookup the
          * appropriate (de)serializer you must have a type. So we use a fake
          * name. This is an illegal NCName, so it shouldn't collide.
          */
         if (xmlType == null)
            xmlType = new QName(xmlName.getNamespaceURI(), ">" + xmlName.getLocalPart());

         paramMetaData.setXmlType(xmlType);
      }

      types.addTypeMapping(new TypeMappingMetaData(types, xmlType, javaName));
   }

   /**
    * Process operation meta data extensions.
    */
   private void processMetaExtensions(EndpointMetaData epMetaData, OperationMetaData opMetaData)
   {
      // Until there is a addressing annotion we fallback to implicit action association
      // TODO: figure out a way to assign message name instead of IN and OUT
      String tns = epMetaData.getQName().getNamespaceURI();
      String portTypeName = epMetaData.getQName().getLocalPart();

      AddressingProperties ADDR = new AddressingPropertiesImpl();
      AddressingOpMetaExt addrExt = new AddressingOpMetaExt(ADDR.getNamespaceURI());
      addrExt.setInboundAction(tns + "/" + portTypeName + "/IN");

      if (!opMetaData.isOneWay())
         addrExt.setOutboundAction(tns + "/" + portTypeName + "/OUT");

      opMetaData.addExtension(addrExt);
   }

   private void processWebMethod(EndpointMetaData epMetaData, Method method)
   {
      String javaName = method.getName();

      // skip asnyc methods, they dont need meta data representation
      if (method.getName().endsWith(Constants.ASYNC_METHOD_SUFFIX))
         return;

      // reflection defaults
      String soapAction = "";
      String operationName = method.getName();

      // annotation values that override defaults
      if (method.isAnnotationPresent(WebMethod.class))
      {
         WebMethod anWebMethod = method.getAnnotation(WebMethod.class);
         soapAction = anWebMethod.action();
         if (anWebMethod.operationName().length() > 0)
         {
            operationName = anWebMethod.operationName();
         }
      }

      String targetNS = epMetaData.getInterfaceQName().getNamespaceURI();
      OperationMetaData opMetaData = new OperationMetaData(epMetaData, new QName(targetNS, operationName), javaName);
      opMetaData.setOneWay(method.isAnnotationPresent(Oneway.class));
      opMetaData.setSOAPAction(soapAction);

      if (method.isAnnotationPresent(SOAPBinding.class))
      {
         SOAPBinding anBinding = method.getAnnotation(SOAPBinding.class);
         if (anBinding.style() != SOAPBinding.Style.DOCUMENT || epMetaData.getStyle() != Style.DOCUMENT)
            throw new IllegalArgumentException("@SOAPBinding must be specified using DOCUMENT style when placed on a method");
         opMetaData.setParameterStyle(anBinding.parameterStyle());
      }

      epMetaData.addOperation(opMetaData);

      // Build parameter meta data
      Class[] parameterTypes = method.getParameterTypes();
      Type[] genericTypes = method.getGenericParameterTypes();
      Annotation[][] parameterAnnotations = method.getParameterAnnotations();
      ParameterMetaData wrapperParameter = null, wrapperOutputParameter = null;
      List<WrappedParameter> wrappedParameters = null, wrappedOutputParameters = null;

      // Force paramter style to wrapped
      if (method.isAnnotationPresent(RequestWrapper.class) || method.isAnnotationPresent(ResponseWrapper.class))
      {
         epMetaData.setParameterStyle(ParameterStyle.WRAPPED);
      }

      if (opMetaData.isDocumentWrapped())
      {
         wrapperParameter = createRequestWrapper(opMetaData, method);
         wrappedParameters = new ArrayList<WrappedParameter>(parameterTypes.length);
         wrapperParameter.setWrappedParameters(wrappedParameters);

         if (!opMetaData.isOneWay())
         {
            wrapperOutputParameter = createResponseWrapper(opMetaData, method);
            wrappedOutputParameters = new ArrayList<WrappedParameter>(parameterTypes.length + 1);
            wrapperOutputParameter.setWrappedParameters(wrappedOutputParameters);
         }
      }

      for (int i = 0; i < parameterTypes.length; i++)
      {
         Class javaType = parameterTypes[i];
         Type genericType = genericTypes[i];
         String javaTypeName = javaType.getName();
         WebParam anWebParam = getWebParamAnnotation(method, i);
         boolean isHeader = anWebParam != null && anWebParam.header();
         boolean isWrapped = opMetaData.isDocumentWrapped() && !isHeader;
         ParameterMode mode = getParameterMode(anWebParam, javaType);

         // Assert one-way
         if (opMetaData.isOneWay() && mode != ParameterMode.IN)
            throw new IllegalArgumentException("A one-way operation can not have output parameters [" + "method = " + method.getName() + ", parameter = " + i + "]");

         if (HolderUtils.isHolderType(javaType))
         {
            genericType = HolderUtils.getGenericValueType(genericType);
            javaType = JavaUtils.erasure(genericType);
            javaTypeName = javaType.getName();
         }

         if (isWrapped)
         {
            QName wrappedElementName = getWebParamName(opMetaData, i, anWebParam);
            String variable = convertToVariable(wrappedElementName.getLocalPart());

            WrappedParameter wrappedParameter = new WrappedParameter(wrappedElementName, javaTypeName, variable, i);
            wrappedParameter.setTypeArguments(convertTypeArguments(javaType, genericType));

            if (mode != ParameterMode.OUT)
               wrappedParameters.add(wrappedParameter);
            if (mode != ParameterMode.IN)
            {
               wrappedOutputParameters.add(wrappedParameter);
               wrappedParameter.setHolder(true);
            }
         }
         else
         {
            QName xmlName = getWebParamName(opMetaData, i, anWebParam);

            ParameterMetaData paramMetaData = new ParameterMetaData(opMetaData, xmlName, javaTypeName);
            paramMetaData.setInHeader(isHeader);
            paramMetaData.setIndex(i);
            paramMetaData.setMode(mode);

            if (anWebParam != null && anWebParam.partName().length() > 0)
               paramMetaData.setPartName(anWebParam.partName());

            opMetaData.addParameter(paramMetaData);
            javaTypes.add(javaType);
            typeRefs.add(new TypeReference(xmlName, genericType, parameterAnnotations[i]));
         }
      }

      // Build result meta data
      Class returnType = method.getReturnType();
      Type genericReturnType = method.getGenericReturnType();
      String returnTypeName = returnType.getName();
      if (!(returnType == void.class))
      {
         if (opMetaData.isOneWay())
            throw new IllegalArgumentException("[JSR-181 2.5.1] The method '" + method.getName() + "' can not have a return value if it is marked OneWay");

         WebResult anWebResult = method.getAnnotation(WebResult.class);
         boolean isHeader = anWebResult != null && anWebResult.header();
         boolean isWrapped = opMetaData.isDocumentWrapped() && !isHeader;
         QName xmlName = getWebResultName(opMetaData, anWebResult);

         if (isWrapped)
         {
            WrappedParameter wrapped = new WrappedParameter(xmlName, returnTypeName, convertToVariable(xmlName.getLocalPart()), -1);
            wrapped.setTypeArguments(convertTypeArguments(returnType, genericReturnType));

            // insert at the beginning just for prettiness
            wrappedOutputParameters.add(0, wrapped);
         }
         else
         {
            ParameterMetaData retMetaData = new ParameterMetaData(opMetaData, xmlName, returnTypeName);
            retMetaData.setInHeader(isHeader);
            if (anWebResult != null && anWebResult.partName().length() > 0)
               retMetaData.setPartName(anWebResult.partName());

            opMetaData.setReturnParameter(retMetaData);

            javaTypes.add(returnType);
            typeRefs.add(new TypeReference(xmlName, genericReturnType, method.getAnnotations()));
         }
      }

      // Generate wrapper beans
      if (opMetaData.isDocumentWrapped())
      {
         wrapperGenerator.generate(wrapperParameter);
         Class wrapperClass = wrapperParameter.getJavaType();
         javaTypes.add(wrapperClass);
         // In case there is no @XmlRootElement
         typeRefs.add(new TypeReference(wrapperParameter.getXmlName(), wrapperClass));
         if (!opMetaData.isOneWay())
         {
            wrapperGenerator.generate(wrapperOutputParameter);
            wrapperClass = wrapperOutputParameter.getJavaType();
            javaTypes.add(wrapperClass);
            // In case there is no @XmlRootElement
            typeRefs.add(new TypeReference(wrapperOutputParameter.getXmlName(), wrapperClass));
         }
      }

      // Add faults
      for (Class exClass : method.getExceptionTypes())
         if (!RemoteException.class.isAssignableFrom(exClass))
            addFault(opMetaData, exClass);

      // process op meta data extension
      processMetaExtensions(epMetaData, opMetaData);
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

         // @WebService[name] is allowed, but what should we do with it?
            
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

   protected void createJAXBContext(EndpointMetaData epMetaData)
   {
      try
      {
         String targetNS = epMetaData.getInterfaceQName().getNamespaceURI();
         log.debug("JAXBContext [types=" + javaTypes + ",tns=" + targetNS + "]");
         jaxbCtx = JAXBRIContext.newInstance(javaTypes.toArray(new Class[0]), typeRefs, targetNS, false);
      }
      catch (JAXBException ex)
      {
         throw new IllegalStateException("Cannot build JAXB context", ex);
      }
   }

   protected void populateXmlTypes(EndpointMetaData epMetaData)
   {
      for (OperationMetaData operation : epMetaData.getOperations())
      {
         // parameters
         for (ParameterMetaData paramMetaData : operation.getParameters())
         {
            populateXmlType(paramMetaData);
         }

         // return value
         ParameterMetaData returnParameter = operation.getReturnParameter();
         if (returnParameter != null)
            populateXmlType(returnParameter);

         // faults
         for (FaultMetaData faultMetaData : operation.getFaults())
         {
            populateXmlType(faultMetaData);
         }
      }
   }

   /**
    * Process an optional @HandlerChain annotation
    *
    * Location of the handler chain file. The location supports 2 formats.
    *
    *    1. An absolute java.net.URL in externalForm.
    *    (ex: http://myhandlers.foo.com/handlerfile1.xml)
    *
    *    2. A relative path from the source file or class file.
    *    (ex: bar/handlerfile1.xml)
    */
   protected void processHandlerChain(EndpointMetaData epMetaData, Class<?> wsClass)
   {
      if (wsClass.isAnnotationPresent(SOAPMessageHandlers.class))
         throw new WSException("Cannot combine @HandlerChain with @SOAPMessageHandlers");

      HandlerChain anHandlerChain = wsClass.getAnnotation(HandlerChain.class);

      URL fileURL = null;
      String filename = anHandlerChain.file();

      // Try the filename as URL
      try
      {
         fileURL = new URL(filename);
      }
      catch (MalformedURLException ex)
      {
         // ignore
      }

      // Try the filename as File
      if (fileURL == null)
      {
         try
         {
            File file = new File(filename);
            if (file.exists())
               fileURL = file.toURL();
         }
         catch (MalformedURLException e)
         {
            // ignore
         }
      }

      // Try the filename as Resource
      if (fileURL == null)
      {
         fileURL = epMetaData.getClassLoader().getResource(filename);
      }

      // Try the filename relative to class
      if (fileURL == null)
      {
         String packagePath = wsClass.getPackage().getName().replace('.', '/');
         fileURL = epMetaData.getClassLoader().getResource(packagePath + "/" + filename);
      }

      if (fileURL == null)
         throw new WSException("Cannot resolve handler file '" + filename + "' on " + wsClass.getName());

      try
      {
         HandlerChainsMetaData handlerChainsMetaData = null;
         InputStream is = fileURL.openStream();
         try
         {
            Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
            unmarshaller.setValidation(true);
            unmarshaller.setSchemaValidation(true);
            unmarshaller.setEntityResolver(new JBossWSEntityResolver());
            ObjectModelFactory factory = new HandlerChainFactory();
            handlerChainsMetaData = (HandlerChainsMetaData)unmarshaller.unmarshal(is, factory, null);
         }
         finally
         {
            is.close();
         }

         // Setup the endpoint handlers
         for (HandlerChainMetaData handlerChainMetaData : handlerChainsMetaData.getHandlerChains())
         {
            for (UnifiedHandlerMetaData uhmd : handlerChainMetaData.getHandlers())
            {
               epMetaData.addHandler(uhmd.getHandlerMetaDataJAXWS(epMetaData, HandlerType.ENDPOINT));
            }
         }
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         throw new WSException("Cannot process handler chain: " + filename, ex);
      }
   }

   protected void processOrGenerateWSDL(Class wsClass, ServiceMetaData serviceMetaData, URL wsdlLocation, EndpointMetaData epMetaData)
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
            wsdlDefinitions.getWsdlTypes().setNamespace(epMetaData.getInterfaceQName().getNamespaceURI());

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

   protected void processSOAPBinding(EndpointMetaData epMetaData, Class<?> wsClass)
   {

      if (!wsClass.isAnnotationPresent(SOAPBinding.class))
         return;

      SOAPBinding anSoapBinding = wsClass.getAnnotation(SOAPBinding.class);

      SOAPBinding.Style attrStyle = anSoapBinding.style();
      Style style = (attrStyle == SOAPBinding.Style.RPC ? Style.RPC : Style.DOCUMENT);
      epMetaData.setStyle(style);

      SOAPBinding.Use attrUse = anSoapBinding.use();
      if (attrUse == SOAPBinding.Use.ENCODED)
         throw new WSException("SOAP encoding is not supported for JSR-181 deployments");

      epMetaData.setEncodingStyle(Use.LITERAL);

      ParameterStyle paramStyle = anSoapBinding.parameterStyle();
      epMetaData.setParameterStyle(paramStyle);
   }

   //   If the implementation bean does not implement a service endpoint interface and
   //   there are no @WebMethod annotations in the implementation bean (excluding
   //   @WebMethod annotations used to exclude inherited @WebMethods), all public
   //   methods other than those inherited from java.lang.Object will be exposed as Web
   //   Service operations, subject to the inheritance rules specified in Common
   //   Annotations for the Java Platform [12], section 2.1.
   protected void processWebMethods(EndpointMetaData epMetaData, Class wsClass)
   {
      epMetaData.clearOperations();

      // Process @WebMethod annotations
      int webMethodCount = 0;
      for (Method method : wsClass.getMethods())
      {
         WebMethod annotation = method.getAnnotation(WebMethod.class);
         boolean exclude = annotation != null && annotation.exclude();
         if (!exclude && (annotation != null || wsClass.isInterface()))
         {
            processWebMethod(epMetaData, method);
            webMethodCount++;
         }
      }

      // @WebService should expose all inherited methods if @WebMethod is never specified
      if (webMethodCount == 0 && !wsClass.isInterface())
      {
         for (Method method : wsClass.getMethods())
         {
            WebMethod annotation = method.getAnnotation(WebMethod.class);
            boolean exclude = annotation != null && annotation.exclude();
            if (!exclude && method.getDeclaringClass() != Object.class)
            {
               processWebMethod(epMetaData, method);
               webMethodCount++;
            }
         }
      }

      if (webMethodCount == 0)
         throw new WSException("No exposable methods found");
   }

   protected void resetMetaDataBuilder(ClassLoader loader)
   {
      wrapperGenerator = new DynamicWrapperGenerator(loader);
      javaTypes.clear();
      typeRefs.clear();
      jaxbCtx = null;
   }

   public ServerEndpointMetaData buildEndpointMetaData(UnifiedMetaData wsMetaData, UnifiedDeploymentInfo udi, Class<?> sepClass, String linkName)
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

         // process config name and config file amongst others
         processPortComponent(udi, seiClass, linkName, sepMetaData);

         // setup handler chain from config
         sepMetaData.configure(sepMetaData);

         // Process an optional @HandlerChain annotation
         if (sepClass.isAnnotationPresent(HandlerChain.class))
         {
            processHandlerChain(sepMetaData, sepClass);
         }
         else if (seiClass.isAnnotationPresent(HandlerChain.class))
         {
            processHandlerChain(sepMetaData, seiClass);
         }

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
}
