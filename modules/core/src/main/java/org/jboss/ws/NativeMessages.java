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
import java.net.URL;
import java.util.Collection;

import javax.management.ObjectName;
import javax.wsdl.Part;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xs.XSElementDeclaration;
import org.jboss.logging.Cause;
import org.jboss.logging.Message;
import org.jboss.logging.MessageBundle;
import org.jboss.ws.metadata.wsdl.WSDLException;
import org.jboss.ws.metadata.wsdl.WSDLTypes;
import org.jboss.wsf.spi.deployment.Deployment;

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
    WSException cannotObtainEndpoint(ObjectName oname);
    
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
    
}
