/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.ws;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.URL;
import java.util.Collection;

import javax.management.ObjectName;
import javax.wsdl.Part;
import javax.xml.namespace.QName;
import javax.xml.rpc.JAXRPCException;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPException;
import javax.xml.transform.Source;

import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xs.XSElementDeclaration;
import org.jboss.logging.Cause;
import org.jboss.logging.Message;
import org.jboss.logging.MessageBundle;
import org.jboss.ws.core.WSTimeoutException;
import org.jboss.ws.core.binding.BindingException;
import org.jboss.ws.core.soap.utils.Style;
import org.jboss.ws.core.soap.utils.Use;
import org.jboss.ws.metadata.wsdl.WSDLException;
import org.jboss.ws.metadata.wsdl.WSDLTypes;
import org.jboss.wsf.spi.deployment.Deployment;
import org.w3c.dom.Node;

/**
 * JBossWS-CXF exception messages
 * 
 * @author alessio.soldano@jboss.com
 */
@MessageBundle(projectCode = "JBWS")
public interface NativeMessages {

    NativeMessages MESSAGES = org.jboss.logging.Messages.getBundle(NativeMessages.class);
    
    @Message(id = 25000, value = "server config cannot be null")
    IllegalArgumentException serverConfigCannotBeNull();
    
    @Message(id = 25001, value = "Unsupported method: %s")
    WSException unsupportedMethod(String method);
    
    @Message(id = 25002, value = "Deployment has no classloader associated: %s")
    IllegalStateException deploymentHasNoClassLoaderAssociated(Deployment dep);
    
    @Message(id = 25003, value = "Cannot obtain endpoint meta data for endpoint %s")
    IllegalStateException cannotObtainEndpointMetaData(Object epName);
    
    @Message(id = 25004, value = "Cannot obtain SOAPPart from response message")
    SOAPException cannotObtainSoapPart();
    
    @Message(id = 25005, value = "Cannot obtain ServiceEndpointInvoker for endpoint %s")
    IllegalStateException cannotObtainServiceEndpointInvoker(ObjectName epName);
    
    @Message(id = 25007, value = "Failed to post process response message")
    String failedToPostProcessResponseMessage();
    
    @Message(id = 25008, value = "Invalid endpoint address: %s")
    IllegalArgumentException invalidEndpointAddress(String epAddress);
    
    @Message(id = 25011, value = "WSDL 2.0 not supported")
    UnsupportedOperationException wsdl20NotSupported();
    
    @Message(id = 25012, value = "Cannot publish wsdl to: %s")
    WSException cannotPublishWSDLTo(File file, @Cause Throwable cause);
    
    @Message(id = 25013, value = "Invalid publish location: %s")
    IllegalArgumentException invalidPublishLocation(String publishLocation, @Cause Throwable cause);
    
    @Message(id = 25014, value = "Invalid wsdlFile %s, expected in: %s")
    RuntimeException invalidWsdlFile(String wsdlLocation, String expLocation);
    
    @Message(id = 25018, value = "Cannot obtain unified meta data for deployment: %s")
    IllegalStateException cannotObtainUnifiedMetaData(Deployment dep);
    
    @Message(id = 25019, value = "Missing runtime classloader for deployment: %s")
    IllegalStateException missingRuntimeClassLoader(Deployment dep);
    
    @Message(id = 25020, value = "Cannot load class: %s")
    WSException cannotLoadClass(String className, @Cause Throwable cause);
    
    @Message(id = 25021, value = "Cannot obtain endpoint for: %s")
    WSException cannotObtainEndpoint(Object oname);
    
    @Message(id = 25023, value = "Schema mapping, %s argument is null")
    IllegalArgumentException illegalNullArgumentInSchemaMapping(String argument);
    
    @Message(id = 25024, value = "Schema mapping, unsupported type %s")
    IllegalArgumentException unsupportedTypeInSchemaMapping(QName type);
    
    @Message(id = 25025, value = "Schema mapping, problem in introspection of the Java Type during type generation")
    WSException introspectionProblemInSchemaMapping(@Cause Throwable cause);
    
    @Message(id = 25026, value = "Schema mapping, JAX-RPC Enumeration type did not conform to expectations")
    WSException notConformToExpectationInSchemaMapping(@Cause Throwable cause);
    
    @Message(id = 25029, value = "Schema mapping, class %s has a public field & property %s")
    WSException classHasPublicFieldPropertyInSchemaMapping(String clazz, String field);
    
    @Message(id = 25030, value = "Schema mapping, cannot determine namespace, class %s has no package")
    WSException cannotDeterminNamespaceInSchemaMapping(Class<?> clazz);
    
    @Message(id = 25031, value = "Schema mapping, type %s is not an exception")
    IllegalArgumentException typeIsNotAnExceptionInSchemaMapping(Class<?> clazz);
    
    @Message(id = 25032, value = "Schema mapping, JAXRPC violation, exception %s cannot extend RuntimeException")
    IllegalArgumentException jaxrpcExceptionExtendingRuntimeExcViolationInSchemaMapping(Class<?> clazz);
    
    @Message(id = 25033, value = "Schema mapping, could not locate a constructor with the following types: %s %s")
    IllegalArgumentException couldNotLocateConstructorInSchemaMapping(Class<?> clazz, Collection<Class<?>> types);
    
    @Message(id = 25034, value = "Cannot load schema: %s")
    WSException cannotLoadSchema(URL url);
    
    @Message(id = 25035, value = "Cannot load grammar: %s")
    IllegalStateException cannotLoadGrammar(URL url);
    
    @Message(id = 25036, value = "Java to XSD, missing schema location map")
    IllegalArgumentException javaToXSDMissingSchemaLocationMap();
    
    @Message(id = 25037, value = "WSDL definitions is null")
    IllegalArgumentException wsdlDefinitionIsNull();
    
    @Message(id = 25038, value = "WSDL file argument cannot be null")
    IllegalArgumentException wsdlFileArgumentCannotBeNull();
    
    @Message(id = 25039, value = "Cannot obtain wsdl from %s")
    IllegalArgumentException cannotObtainWsdlFrom(URL location);
    
    @Message(id = 25040, value = "Cannot access wsdl from %s")
    RuntimeException cannotAccessWsdlFrom(URL location, @Cause Throwable cause);
    
    @Message(id = 25041, value = "Cannot resolve imported resource: %s")
    IllegalArgumentException cannotResolveImportedResource(String res);
    
    @Message(id = 25042, value = "Cannot access imported wsdl: %s")
    WSException cannotAccessImportedWsdl(String wsdl, @Cause Throwable cause);
    
    @Message(id = 25044, value = "Cannot parse WSDL with null url")
    IllegalArgumentException cannotParsedWsdlWithNullURL();
    
    @Message(id = 25045, value = "Cannot parse wsdl from %s")
    WSDLException cannotParseWsdlFrom(URL location, @Cause Throwable cause);
    
    @Message(id = 25046, value = "Invalid default WSDL namespace: %s")
    WSDLException invalidDefaultWSDLNamespace(String ns);
    
    @Message(id = 25047, value = "Prefix not bound for namespace: %s")
    WSException prefixNotBound(String namespaceURI);
    
    @Message(id = 25048, value = "WSDL 1.1 only supports In-Only, and In-Out MEPS, more than one reference input found")
    IllegalArgumentException wsd11MultipleRefInput();
    
    @Message(id = 25049, value = "WSDL Style is null (should be rpc or document)")
    IllegalArgumentException wsdlStyleNull();
    
    @Message(id = 25051, value = "WSDL interface is null")
    IllegalArgumentException wsdlInterfaceNull();
    
    @Message(id = 25052, value = "WSDl 1.1 only supports In-Only, and In-Out MEPS")
    WSException wsd11UnsupportedMEP();
    
    @Message(id = 25053, value = "Unsuported schema element in %s: %s")
    IllegalArgumentException unsupportedSchemaElement(URL location, String el);
    
    @Message(id = 25054, value = "Cannot process wsdl import, parent location not set")
    IllegalArgumentException cannotProcessImportParentLocationSetToNull();
    
    @Message(id = 25055, value = "schemaLocation is null for xsd:import")
    IllegalArgumentException xsdImportNullSchemaLocation();
    
    @Message(id = 25056, value = "Cannot process include, parent location not set")
    IllegalArgumentException cannotProcessIncludeParentLocationSetToNull();
    
    @Message(id = 25057, value = "schemaLocation is null for xsd:include")
    IllegalArgumentException xsdIncludeNullSchemaLocation();
    
    @Message(id = 25058, value = "Cannot name for wsdl part: %s")
    IllegalStateException cannotFindNameForWSDLPart(Part part);
    
    @Message(id = 25064, value = "Feature name must not be null")
    IllegalArgumentException featureNameMustNotBeNull();
    
    @Message(id = 25065, value = "Feature name not recognized: %s")
    IllegalArgumentException featureNameNotRecognized(String name);
    
    @Message(id = 25066, value = "Java to XSD, %s is null")
    IllegalArgumentException javaToXSDIsNull(String name);
    
    @Message(id = 25067, value = "Unsupported WSDL version: %s")
    WSException unsupportedWSDLVersion(String version);
    
    @Message(id = 25068, value = "Cannot obtain XSTypeDefinition for: %s")
    WSException cannotObtainXSTypeDef(QName q);
    
    @Message(id = 25069, value = "Illegal null argument: %s")
    IllegalArgumentException illegalNullArgument(Object argument);
    
    @Message(id = 25070, value = "String passed is null")
    WSException stringPassedIsNull();
    
    @Message(id = 25071, value = "Illegal null or array arg: %s")
    IllegalArgumentException illegalNullOrArrayArgument(Class<?> clazz);
    
    @Message(id = 25072, value = "QName passed is null")
    IllegalArgumentException qNamePassedIsNull();
    
    @Message(id = 25073, value = "Formatted String is not of format prefix:localpart: %s")
    IllegalArgumentException formattedStringNotInFormatPrefixLocalPart(String s);
    
    @Message(id = 25074, value = "WSDLTypes is not an XSModelTypes: %s")
    WSException wsdlTypesNotAnXSModelTypes(WSDLTypes wt);
    
    @Message(id = 25075, value = "Only Request-Only and Request-Response MEPs are allowed, WSDLInterfaceOperation = %s")
    WSException reqOnlyAndReqResMEPsOnlySupported(QName opName);
    
    @Message(id = 25076, value = "Illegal property URI: %s")
    IllegalArgumentException illegalPropertyURI(String uri);
    
    @Message(id = 25077, value = "Attempt to map multiple operation inputs to: %s")
    WSException attempToMapMultipleInputs(QName q);
    
    @Message(id = 25078, value = "Attempt to map multiple operation outputs to: %s")
    WSException attempToMapMultipleOutputs(QName q);
    
    @Message(id = 25079, value = "Cannot obtain xmlType for element: %s")
    WSException cannotObtainXmlType(QName q);
    
    @Message(id = 25080, value = "Illegal feature URI: %s")
    IllegalArgumentException illegalFeatureURI(String uri);
    
    @Message(id = 25081, value = "Cannot obtain the binding: %s")
    WSException cannotObtainBinding(QName q);
    
    @Message(id = 25082, value = "Cannot obtain the interface associated with this endpoint: %s")
    WSException cannotObtainInterface(QName q);
    
    @Message(id = 25083, value = "The prefix %s cannot be bound to any namespace other than its usual namespace, trying to bind to %s")
    IllegalArgumentException prefixCannotBeBoundTryingBindingTo(String prefix, String to);
    
    @Message(id = 25087, value = "Cannot get interface for name: %s")
    WSException cannotGetInterfaceForName(QName q);
    
    @Message(id = 25088, value = "Empty union type not expected")
    WSException emptyUnionTypeNotExpected();
    
    @Message(id = 25089, value = "Absent variety is not supported in simple types")
    WSException absentVariety();
    
    @Message(id = 25090, value = "Expected base type to be a simple type")
    WSException baseTypeNotSimple();
    
    @Message(id = 25091, value = "xmlName has a null %s")
    IllegalArgumentException xmlNameHasNull(String prop);
    
    @Message(id = 25092, value = "Cannot parse schema: %s")
    WSException cannotParseSchema(String location);
    
    @Message(id = 25093, value = "Target Namespace of xsmodel is null")
    WSException nullTargetNsXSModel();
    
    @Message(id = 25094, value = "%s is not a global element")
    IllegalArgumentException notAGlobalElement(XSElementDeclaration el);
    
    @Message(id = 25095, value = "Type unidentified")
    WSException typeUnidentified();
    
    @Message(id = 25096, value = "objectType should be simple type or complex type")
    IllegalArgumentException objectTypeShouldBeSimpleOrComplex();
    
    @Message(id = 25098, value = "Parser should stop: %s")
    XNIException parserShouldStop(String traceMex, @Cause Exception cause);
    
    @Message(id = 25099, value = "Parser should stop, the key value is %s")
    XNIException parserShouldStopKeyValueIs(String keyValue, @Cause Exception cause);
    
    @Message(id = 25100, value = "Classloader not available")
    IllegalStateException classloaderNotAvailable();
    
    @Message(id = 25102, value = "Ambiguous type mappping for: %s")
    WSException ambiguousTypeMapping(QName qname);
    
    @Message(id = 25103, value = "Invalid qname scope: %s")
    IllegalArgumentException invalidQNameScope(String scope);
    
    @Message(id = 25105, value = "EndpointMetaData name must be unique: %s")
    WSException endpointMetadataMustBeUnique(QName qname);
    
    @Message(id = 25106, value = "No type mapping for encoding style: %s")
    WSException noTypeMapping(Use use);
    
    @Message(id = 25107, value = "Conflicting encoding styles not supported: %s, %s")
    WSException conflictingEncodingStyles(Object obj1, Object obj2);
    
    @Message(id = 25108, value = "Requested namespace is not WSDL target namespace: %s")
    WSException notWsdlTargetNamespace(String ns);
    
    @Message(id = 25109, value = "context root should start with '/': %s")
    IllegalArgumentException contextRootShouldStartsWith(String ctxRoot);
    
    @Message(id = 25110, value = "URL pattern should start with '/': %s")
    IllegalArgumentException urlPatternShouldStartsWith(String urlPattern);
    
    @Message(id = 25111, value = "Endpoint not available")
    IllegalStateException endpointNotAvailable();
    
    @Message(id = 25113, value = "Invalid mode: %s")
    IllegalArgumentException invalidMode(String mode);
    
    @Message(id = 25114, value = "Invalid mime type: %s")
    IllegalArgumentException invalidMimeType(QName xmlType);
    
    @Message(id = 25115, value = "Autogeneration of wrapper beans not supported with JAXRPC")
    WSException jaxrpcWrapperBeanAutogenNotSupported();
    
    @Message(id = 25116, value = "Cannot load: %s")
    WSException cannotLoad(String className);
    
    @Message(id = 25117, value = "Cannot find java method: %s")
    WSException cannotFindJavaMethod(String method);

    @Message(id = 25118, value = "OneWay operations cannot have a return parameter")
    WSException onewayOperationCannotHaveReturn();

    @Message(id = 25119, value = "OneWay operations cannot have checked exceptions")
    WSException onewayOperationCannotHaveCheckedExc();

    @Message(id = 25120, value = "OneWay operations cannot have INOUT or OUT parameters")
    WSException onewayOperationCannotHaveInOutPars();
    
    @Message(id = 25121, value = "Handler class name cannot be null")
    IllegalStateException handlerClassNameCannotBeNull();
    
    @Message(id = 25123, value = "%s is not assignable to Exception")
    IllegalStateException notAssignableToException(Class<?> clazz);
    
    @Message(id = 25124, value = "%s is not a java.lang.Exception")
    WSException notAnException(String clazz);
    
    @Message(id = 25125, value = "Could not instantiate service exception (%s), since neither a faultInfo nor sorted constructor is present: %s")
    WSException couldNotInstanciateServiceException(String exc, String s);
    
    @Message(id = 25126, value = "Unsupported binding: %s")
    WSException unsupportedBinding(String bindingID);
    
    @Message(id = 25127, value = "Mixed SOAP parameter styles not supported: %s, %s")
    WSException conflictingSOAPParameterStyles(Object obj1, Object obj2);
    
    @Message(id = 25128, value = "Cannot uniquely indentify operation: %s")
    WSException cannotUniquelyIdentifyOp(QName op);
    
    @Message(id = 25132, value = "Invalid parameter mode: %s")
    IllegalArgumentException invalidParameterMode(String mode);
    
    @Message(id = 25133, value = "Cannot parse: %s")
    IOException cannotParse(URL url);
    
    @Message(id = 25134, value = "Invalid anonymous qname: %s")
    IllegalArgumentException invalidAnonymousQName(String value);
    
    @Message(id = 25136, value = "Service path cannot be null")
    WSException servicePathCannotBeNull();
    
    @Message(id = 25137, value = "Cannot find <url-pattern> for servlet-name: %s")
    WSException cannotFindURLPatternForServletName(String s);
    
    @Message(id = 25139, value = "Cannot find port in wsdl: %s")
    IllegalArgumentException cannotFindPortInWsdl2(QName port);
    
    @Message(id = 25142, value = "jaxrpc-mapping-file not configured from webservices.xml")
    WSException mappingFileNotConfigured();
    
    @Message(id = 25143, value = "Cannot obtain UnifiedBeanMetaData for: %s")
    WSException cannotObtainUnifiedBeanMetaData(String name);
    
    @Message(id = 25145, value = "Cannot find port in wsdl: %s")
    WSException cannotFindPortInWsdl(QName port);
    
    @Message(id = 25146, value = "Cannot obtain java type mapping for: %s")
    WSException cannotObtainJavaTypeMappingFor(QName qname);
    
    @Message(id = 25147, value = "Cannot obtain java/xml type mapping for: %s")
    WSException cannotObtainJavaXmlTypeMappingFor(QName qname);
    
    @Message(id = 25148, value = "Cannot locate binding operation for: %s")
    WSException cannotLocateBindingOperationFor(QName q);
    
    @Message(id = 25149, value = "Cannot obtain method mapping for: %s")
    WSException cannotObtainMethodMappingFor(QName qname);
    
    @Message(id = 25150, value = "Cannot obtain method parameter mapping for message part '%s' in wsdl operation %s")
    WSException cannotObtainMethodParameterMappingFor(String part, String wsdlOp);
    
    @Message(id = 25151, value = "RPC style was missing an output, and was not an IN-ONLY MEP.")
    WSException rpcStyleMissingOutputAndNotAInOnlyMEP();
    
    @Message(id = 25152, value = "Cannot wrap parameters without SEI method mapping")
    IllegalArgumentException cannotWrapParametersWithoutSEIMethodMapping();
    
    @Message(id = 25153, value = "Could not determine variable name for element: %s")
    IllegalArgumentException couldNotDetermineVariableNameForElement(String elem);
    
    @Message(id = 25154, value = "Invalid parameter mode for element %s")
    IllegalArgumentException invalidParameterModeForElement(String elem);
    
    @Message(id = 25155, value = "Could not update IN parameter to be INOUT, as indicated in the mapping: %s")
    WSException couldNotUpdateInParameterAsIndicated(String part);
    
    @Message(id = 25156, value = "Cannot obtain wsdl service: %s")
    IllegalArgumentException cannotObtainWSDLService(QName service);
    
    @Message(id = 25157, value = "Field cannot be static: %s")
    WSException fieldCannotBeStatic(String field);
    
    @Message(id = 25158, value = "Unsupported message type: %s")
    String unsupportedMessageType(Object obj);
    
    @Message(id = 25159, value = "Cannot trace SOAP Message")
    String cannotTraceSOAPMessage();
    
    @Message(id = 25160, value = "Operation %s does not have a return value")
    WSException operationDoesNotHaveReturnValue(QName op);
    
    @Message(id = 25162, value = "Mime type %s not allowed for parameter %s allowed types are: %s")
    SOAPException mimeTypeNotAllowed(String type, QName param, Collection<String> allowed);
    
    @Message(id = 25163, value = "javaType [%s] is not assignable from attachment content: %s")
    SOAPException javaTypeIsNotAssignable(String javaType, String contentType);
    
    @Message(id = 25164, value = "Parameter %s not assignable from %s")
    WSException parameterNotAssignable(Object clazz1, Object clazz2);
    
    @Message(id = 25165, value = "javaType %s is not assignable from: %s")
    BindingException javaTypeIsNotAssignableFrom(String s1, String s2);
    
    @Message(id = 25166, value = "Cannot find RPC element in %s")
    SOAPException cannotFindRPCElement(Object parent);
    
    @Message(id = 25167, value = "Invalid number of payload elements: %s")
    WSException invalidNumberOfPayloadElements(int n);
    
    @Message(id = 25168, value = "Cannot unbind response message with empty soap body")
    WSException emptySOAPBody();
    
    @Message(id = 25170, value = "Could not determine mime type for attachment parameter: %s")
    BindingException couldNotDetermineMimeType(String par);
    
    @Message(id = 25171, value = "Could not locate attachment for parameter: %s")
    BindingException couldNotLocateAttachment(QName qname);
    
    @Message(id = 25172, value = "Cannot find child element: %s")
    WSException cannotFindChildElement(Name name);
    
    @Message(id = 25173, value = "No SOAPMessage available. Current message context carries: %s")
    UnsupportedOperationException noSoapMessageAvailable(Class<?> clazz);
    
    @Message(id = 25175, value = "Cannot obtain operation meta data for %s")
    WSException cannotObtainOperationMetaData(QName opName);
    
    @Message(id = 25176, value = "Target endpoint address not set")
    WSException targetEndpointAddressNotSet();
    
    @Message(id = 25178, value = "No ByteArrayConverter for %s")
    WSException noByteArrayConverterFor(String c);
    
    @Message(id = 25179, value = "Failed to convert %s")
    WSException failedToConvert(Object o);
    
    @Message(id = 25180, value = "%s is already a javax.xml.rpc.holders.Holder")
    IllegalArgumentException alreadyAHolder(String className);
    
    @Message(id = 25181, value = "%s is not a javax.xml.rpc.holders.Holder")
    IllegalArgumentException notAHolder(Object holder);
    
    @Message(id = 25182, value = "Cannot find or access public 'value' field in %s")
    IllegalArgumentException cannotFindOrAccessPublicFieldValue(Object holder);
    
    @Message(id = 25183, value = "Holder [%s] value not assignable: %s")
    IllegalArgumentException holderValueNotAssignable(Object holder, Object value);
    
    @Message(id = 25184, value = "Object value not available")
    IllegalStateException objectValueNotAvailable();
    
    @Message(id = 25185, value = "Content root name does not match element name: %s != %s")
    WSException doesNotMatchElementName(QName contentRootName, QName elementName);
    
    @Message(id = 25186, value = "javaType %s is not assignable from: %s")
    WSException javaTypeIsNotAssignableFrom2(String s1, String s2);
    
    @Message(id = 25187, value = "The parent element of a soap part is not defined")
    SOAPException parentElemOfSOAPPartIsNotDefined();
    
    @Message(id = 25188, value = "Setting value of a soap part is not defined")
    IllegalStateException settingValueOfSOAPPartIsNotDefined();
    
    @Message(id = 25189, value = "Unsupported DOMSource node: %s")
    SOAPException unsupportedDOMSourceNode(Node node);
    
    @Message(id = 25190, value = "Unsupported source parameter: %s")
    SOAPException unsupportedSourceParameter(Source s);
    
    @Message(id = 25191, value = "Access to '%s' resource is not allowed")
    IOException accessIsNotAllowed(String path);
    
    @Message(id = 25194, value = "Cannot resolve port-component-link: %s")
    WSException cannotResolvePortComponentLink(String pcl);
    
    @Message(id = 25195, value = "Cannot obtain remote connetion for %s")
    IllegalArgumentException cannotObtainRemoteConnectionFor(Object obj);
    
    @Message(id = 25196, value = "Cannot obtain target address from %s")
    IllegalArgumentException cannotObtainTargetAddressFrom(Object obj);
    
    @Message(id = 25197, value = "Connection is already closed")
    IOException connectionAlreadyClosed();
    
    @Message(id = 25198, value = "Invalid chunk size (must be greater than 0): %s")
    IllegalArgumentException invalidChunkSize(int size);
    
    @Message(id = 25199, value = "Cannot get channel future before closing the stream")
    IllegalStateException cannotGetChannelFuture();
    
    @Message(id = 25200, value = "Could not connect to %s")
    ConnectException couldNotConnectTo(String host);
    
    @Message(id = 25202, value = "Timeout after: %s ms")
    WSTimeoutException timeout(Long timeout);
    
    @Message(id = 25203, value = "Receive timeout")
    WSTimeoutException receiveTimeout();
    
    @Message(id = 25206, value = "Could not transmit message")
    IOException couldNotTransmitMessage();
    
    @Message(id = 25207, value = "Connection timeout %s")
    WSTimeoutException connectionTimeout(Long timeout);
    
    @Message(id = 25208, value = "Can not set remoting socket factory with null protocol")
    IllegalArgumentException cannotSetRemotingSocketFactory();
    
    @Message(id = 25209, value = "Error creating server socket factory SSL context")
    IOException errorCreatingServerSocketFactorySSLContext(@Cause Exception cause);
    
    @Message(id = 25210, value = "Error creating socket factory SSL context")
    IOException errorCreatingSocketFactorySSLContext(@Cause Exception cause);
    
    @Message(id = 25211, value = "Can not find keystore url.")
    IOException cannotFindKeystoreUrl(@Cause Exception cause);
    
    @Message(id = 25212, value = "Error initializing server socket factory SSL context")
    IOException errorInitializingServerSocketFactorySSLContext(@Cause Exception cause);
    
    @Message(id = 25213, value = "Can not find truststore url.")
    IOException cannotFindTruststoreUrl(@Cause Exception cause);
    
    @Message(id = 25214, value = "Error initializing socket factory SSL context")
    IOException errorInitializingSocketFactorySSLContext(@Cause Exception cause);
    
    @Message(id = 25215, value = "Can not find key entry for key store (%s) with given alias (%s)")
    IOException cannotFindKeyEntry(URL ksUrl, String alias);
    
    @Message(id = 25216, value = "Can not find store file for url because store url is null")
    String nullStoreURL();
    
    @Message(id = 25219, value = "Cannot compare IQName to %s")
    IllegalArgumentException cannotCompareIQNameTo(Object obj);
    
    @Message(id = 25220, value = "Only element nodes are supported")
    UnsupportedOperationException onlyElementNotesSupported();
    
    @Message(id = 25221, value = "Only DOMSource is supported")
    UnsupportedOperationException onlyDOMSourceSupported();
    
    @Message(id = 25222, value = "Unsupported encoding style: %s")
    JAXRPCException unsupportedEncodingStyle(String s);
    
    @Message(id = 25223, value = "Cannot obtain deserializer factory for: %s")
    JAXRPCException cannotObtainDeserializerFactory(QName qname);
    
    @Message(id = 25224, value = "Cannot obtain serializer factory for: %s")
    JAXRPCException cannotObtainSerializerFactory(QName qname);
    
    @Message(id = 25225, value = "Invalid deserialization result: %s")
    WSException invalidDeserializationResult(Object res);
    
    @Message(id = 25228, value = "Cannot create SOAPFault message")
    JAXRPCException cannotCreateSoapFaultMessage(@Cause Throwable cause);
    
    @Message(id = 25230, value = "Illegal faultcode '%s', allowed values are: %s")
    IllegalArgumentException illegalFaultCode(QName fc, Collection<QName> allowed);
    
    @Message(id = 25232, value = "RoleSource was not available")
    IllegalStateException roleSourceNotAvailable();
    
    @Message(id = 25233, value = "Cannot generate xsd schema for: %s")
    JAXRPCException cannotGenerateXsdSchemaFor(QName qname, @Cause Throwable cause);
    
    @Message(id = 25234, value = "Cannot generate XSModel")
    WSException cannotGenerateXsdModel();
    
    @Message(id = 25235, value = "Unexpected style: %s")
    WSException unexpectedStyle(Style style);
    
    @Message(id = 25236, value = "Cannot generate XSModel")
    WSException unexpectedParameterStyle();
    
    @Message(id = 25237, value = "Operation is not document/literal (wrapped)")
    WSException operationIsNotDocLitWrapped();
    
    @Message(id = 25238, value = "Cannot generate a type when there is no wrapped parameter")
    WSException cannotGenerateTypeWithNoWrappedParams();
    
    @Message(id = 25239, value = "Could not generate wrapper type: %s")
    WSException cannotGenerateWrapperType(String type, @Cause Throwable cause);
    
    @Message(id = 25248, value = "No handler at position: %s")
    IllegalArgumentException noHandlerAtPosition(int p);
    
    @Message(id = 25249, value = "Invalid handler entry")
    IllegalStateException invalidHandlerEntry();
    
    @Message(id = 25251, value = "Don't know how to invoke method %s")
    JAXRPCException dontKnowHowToInvoke(Method method);
    
}
