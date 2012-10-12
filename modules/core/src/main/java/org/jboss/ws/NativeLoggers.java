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

import static org.jboss.logging.Logger.Level.DEBUG;
import static org.jboss.logging.Logger.Level.ERROR;
import static org.jboss.logging.Logger.Level.INFO;
import static org.jboss.logging.Logger.Level.TRACE;
import static org.jboss.logging.Logger.Level.WARN;

import java.net.URL;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.rpc.handler.Handler;
import javax.xml.rpc.handler.HandlerInfo;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Cause;
import org.jboss.logging.LogMessage;
import org.jboss.logging.Message;
import org.jboss.logging.MessageLogger;
import org.jboss.ws.metadata.umdm.HandlerMetaDataJAXRPC;

/**
 * JBossWS-CXF log messages
 * 
 * @author alessio.soldano@jboss.com
 */
@MessageLogger(projectCode = "JBWS")
public interface NativeLoggers extends BasicLogger
{
    NativeLoggers ROOT_LOGGER = org.jboss.logging.Logger.getMessageLogger(NativeLoggers.class, "org.jboss.ws.native");
    NativeLoggers CLIENT_LOGGER = org.jboss.logging.Logger.getMessageLogger(NativeLoggers.class, "org.jboss.ws.native.client");
    NativeLoggers JAXRPC_LOGGER = org.jboss.logging.Logger.getMessageLogger(NativeLoggers.class, "org.jboss.ws.native.jaxrpc");
    
    @LogMessage(level = ERROR)
    @Message(id = 25006, value = "Cannot process metrics")
    void cannotProcessMetrics(@Cause Throwable cause);

    @LogMessage(level = ERROR)
    @Message(id = 25009, value = "Error processing web service request")
    void errorProcessingWebServiceRequest(@Cause Throwable cause);

    @LogMessage(level = INFO)
    @Message(id = 25010, value = "WSDL published to: %s")
    void wsdlFilePublished(URL url);
    
    @LogMessage(level = WARN)
    @Message(id = 25015, value = "Cannot get wsdl publish location for null wsdl location")
    void cannotGetWsdlPublishLocation();
    
    @LogMessage(level = DEBUG)
    @Message(id = 25016, value = "Adding server side handler to service '%s': %s")
    void addingServerSideHandler(QName serviceName, HandlerInfo handlerInfo);
    
    @LogMessage(level = ERROR)
    @Message(id = 25017, value = "SOAP request exception")
    void soapRequestException(@Cause Throwable cause);
    
    @LogMessage(level = WARN)
    @Message(id = 25022, value = "Failed to create inputstream from systemId: %s")
    void failedToCreateInputStreamFromSystemID(String systemId);
    
    @LogMessage(level = WARN)
    @Message(id = 25027, value = "JAX-RPC schema mapping does not allow collection types, skipping field: %s.%s")
    void jaxrpcNotAllowCollectionSkippingFieldInSchemaMapping(String type, String field);
    
    @LogMessage(level = WARN)
    @Message(id = 25028, value = "Indexed properties without non-indexed accessors are not supported, skipping: %s.%s")
    void indexedPropNotSupportedSkippingInSchemaMapping(String type, String field);
    
    @LogMessage(level = ERROR)
    @Message(id = 25043, value = "%s is not a valid url")
    void notAValidUrl(String s);
    
    @LogMessage(level = WARN)
    @Message(id = 25059, value = "WSDL parsing, unsupported fault message part in message: %s")
    void unsupportedFaultMessagePartInMessage(QName qname);
    
    @LogMessage(level = WARN)
    @Message(id = 25060, value = "WSDL parsing, unsupported binding: %s")
    void unsupportedBinding(QName qname);
    
    @LogMessage(level = WARN)
    @Message(id = 25061, value = "WSDL parsing, encoding style %s not supported for: %s")
    void encodingStyleNotSupported(String s, QName q);
    
    @LogMessage(level = WARN)
    @Message(id = 25062, value = "WSDL parsing, multiple encoding styles not supported: %s")
    void multipleEncodingStyleNotSupported(Collection<?> list);
    
    @LogMessage(level = WARN)
    @Message(id = 25063, value = "WSDL parsing, unprocessed extension element: %s")
    void unprocessedWSDLExtensionElement(QName el);
    
    @LogMessage(level = WARN)
    @Message(id = 25084, value = "Multiple WSDL bindings reference the same interface: %s")
    void multipleWSDLBindingRefs(QName qname);
    
    @LogMessage(level = WARN)
    @Message(id = 25085, value = "Multiple binding operations reference: %s")
    void multipleBindingOperationRefs(QName qname);
    
    @LogMessage(level = WARN)
    @Message(id = 25086, value = "Cannot obtain binding operation for ref: %s")
    void cannotObtainBindingOperationForRef(QName qname);
    
    @LogMessage(level = ERROR)
    @Message(id = 25097, value = "Cannot parse XSModel string: %s")
    void cannotParseXSModelString(String s, @Cause Throwable cause);

    @LogMessage(level = WARN)
    @Message(id = 25101, value = "Cannot obtain javaTypeName for xmlType: %s")
    void cannotObtainJavaTypeName(QName qname);
    
    @LogMessage(level = WARN)
    @Message(id = 25104, value = "Multiple possible endpoints implementing SEI: %s")
    void multiplePossibleEndpointImplementingSEI(String seiName);
    
    @LogMessage(level = WARN)
    @Message(id = 25112, value = "Set java type name after eager initialization: %s")
    void setJavaTypeAfterEagerInit(String typeName);
    
    @LogMessage(level = WARN)
    @Message(id = 25122, value = "Loading java type after eager initialization")
    void loadingJavaTypeAfterEagerInit();
    
    @LogMessage(level = WARN)
    @Message(id = 25129, value = "Setting name of or loading SEI after eager initialization")
    void loadingSettingSEIAfterEagerInit();
    
    @LogMessage(level = WARN)
    @Message(id = 25130, value = "Cannot load class for type %s %s")
    void cannotLoadClassForType(QName q, String s, @Cause Throwable cause);
    
    @LogMessage(level = WARN)
    @Message(id = 25131, value = "Loading java method after eager initialization")
    void loadingJavaMethodAfterEagerInit();
    
    @LogMessage(level = WARN)
    @Message(id = 25135, value = "Cannot find jaxrpc-mapping for type: %s")
    void cannotFindJAXRPCMappingForType(QName type);
    
    @LogMessage(level = WARN)
    @Message(id = 25138, value = "Malformed URL: %s")
    void malformedURL(String url);
    
    @LogMessage(level = WARN)
    @Message(id = 25140, value = "Cannot obtain fault type for element: %s")
    void cannotObtainFaultTypeForElement(QName qname);
    
    @LogMessage(level = WARN)
    @Message(id = 25141, value = "Cannot obtain java type mapping for: %s")
    void cannotObtainJavaTypeMappingFor(QName qname);
    
    @LogMessage(level = WARN)
    @Message(id = 25144, value = "Cannot obtain SEI mapping for: %s")
    void cannotObtainSEIMappingFor(String name);
    
    @LogMessage(level = WARN)
    @Message(id = 25161, value = "Mime type %s not allowed for parameter %s allowed types are: %s")
    void mimeTypeNotAllowed(String type, QName param, Collection<String> allowed);
    
    @LogMessage(level = WARN)
    @Message(id = 25169, value = "Expected SOAP %s envelope, but got: %s")
    void unexpectedSoapEnvelopeVersion(String expectedEnvelopeVersion, String envelopeNS);
    
    @LogMessage(level = WARN)
    @Message(id = 25174, value = "Failed to cleanup attachment part")
    void failedToCleanupAttachmentPart(@Cause Throwable cause);
    
    @LogMessage(level = ERROR)
    @Message(id = 25177, value = "Exception caught while (preparing for) performing invocation")
    void exceptionWhilePreparingForInvocation(@Cause Throwable cause);
    
    @LogMessage(level = WARN)
    @Message(id = 25192, value = "Exception while processing handleFault")
    void exceptionProcessingHandleFault(@Cause Throwable cause);
    
    @LogMessage(level = WARN)
    @Message(id = 25193, value = "Multiple service endoints found for: %s")
    void multipleServiceEndpointFoundFor(String s);
    
    @LogMessage(level = ERROR)
    @Message(id = 25201, value = "Error while parsing headers for configuring keep-alive, closing connection.")
    void errorParsingHeadersForConfiguringKeepAlive(@Cause Throwable cause);
    
    @LogMessage(level = ERROR)
    @Message(id = 25204, value = "Channel closed by remote peer while sending message.")
    void channelClosed();
    
    @LogMessage(level = WARN)
    @Message(id = 25205, value = "Can't set chunk size from call properties, illegal value provided.")
    void cannotSetChunkSize();
    
    @LogMessage(level = DEBUG)
    @Message(id = 25217, value = "Could not find keytore url.")
    void couldNotFindKeystore(@Cause Throwable cause);
    
    @LogMessage(level = DEBUG)
    @Message(id = 25218, value = "Could not find truststore url.")
    void couldNotFindTruststore(@Cause Throwable cause);
    
    @LogMessage(level = ERROR)
    @Message(id = 25226, value = "Cannot deserialize fault detail")
    void cannotDeserializeFaultDetail(@Cause Throwable cause);
    
    @LogMessage(level = ERROR)
    @Message(id = 25227, value = "Error creating SOAPFault message")
    void errorCreatingSoapFaultMessage(@Cause Throwable cause);
    
    @LogMessage(level = WARN)
    @Message(id = 25229, value = "Empty namespace URI with fault code %s, assuming: %s")
    void emptyNamespaceURI(QName q, String s);
    
    @LogMessage(level = INFO)
    @Message(id = 25231, value = "Ignoring Call.SOAPACTION_USE_PROPERTY because of BP-1.0 R2745, R2745")
    void ignoreCallSoapActionUseProperty();
    
    @LogMessage(level = WARN)
    @Message(id = 25240, value = "Handler is in state DOES_NOT_EXIST, skipping Handler.handleRequest for: %s")
    void handlerDoesNotExistSkippingHandleRequest(Handler h);
    
    @LogMessage(level = WARN)
    @Message(id = 25241, value = "Handler is in state DOES_NOT_EXIST, skipping Handler.handleResponse for: %s")
    void handlerDoesNotExistSkippingHandleResponse(Handler h);
    
    @LogMessage(level = WARN)
    @Message(id = 25242, value = "Handler is in state DOES_NOT_EXIST, skipping Handler.handleFault for: %s")
    void handlerDoesNotExistSkippingHandleFault(Handler h);
    
    @LogMessage(level = WARN)
    @Message(id = 25243, value = "RuntimeException in handler method, transition to does not exist")
    void handlerTransitionToDoesNotExist();
    
    @LogMessage(level = ERROR)
    @Message(id = 25244, value = "RuntimeException in request handler")
    void runtimeExceptionInRequestHandler(@Cause Throwable cause);
    
    @LogMessage(level = ERROR)
    @Message(id = 25245, value = "RuntimeException in response handler")
    void runtimeExceptionInResponseHandler(@Cause Throwable cause);
    
    @LogMessage(level = ERROR)
    @Message(id = 25246, value = "Cannot trace SOAP message")
    void cannotTraceJAXRPCSoapMessage(@Cause Throwable cause);
    
    @LogMessage(level = ERROR)
    @Message(id = 25247, value = "Cannot create handler instance for: %s")
    void cannotCreateHandlerInstance(Object handlerInfo, @Cause Throwable cause);
    
    @LogMessage(level = ERROR)
    @Message(id = 25250, value = "JAX-RPC Service error")
    void jaxRpcServiceError(@Cause Throwable cause);
    
    @LogMessage(level = INFO)
    @Message(id = 25257, value = "Deprecated use of <call-properties> on JAXRPC Stub. Use <stub-properties>")
    void deprecatedUseOfCallPropsOnJAXRPCStub();
    
    @LogMessage(level = DEBUG)
    @Message(id = 25258, value = "Adding client side handler to endpoint '%s': %s")
    void addingClientSideHandlerToEndpoint(QName portName, Object handlerInfo);
    
    @LogMessage(level = INFO)
    @Message(id = 25259, value = "Using jaxrpc-mapping from: %s")
    void useJaxRpcMappingFrom(URL mappingURL);
    
    @LogMessage(level = DEBUG)
    @Message(id = 25260, value = "Add handler to: %s%s")
    void addHandlerTo(QName portName, HandlerMetaDataJAXRPC handler);
    
    @LogMessage(level = WARN)
    @Message(id = 25269, value = "Cannot set endpoint address for port-component-link, unsuported number of endpoints.")
    void cannotSetEndpointAddressForPCL();
    
    @LogMessage(level = ERROR)
    @Message(id = 25271, value = "Cannot create Service")
    void cannotCreateService(@Cause Throwable cause);
    
    @LogMessage(level = WARN)
    @Message(id = 25274, value = "Unable to narrow port selection for %s")
    void unableToNarrowPortSelection(Object obj);
    
    @LogMessage(level = WARN)
    @Message(id = 25284, value = "Cannot obtain TypeBinding for: %s")
    void cannotObtainTypeBindingFor(QName type);
    
    @LogMessage(level = WARN)
    @Message(id = 25285, value = "Ambiguous binding for attribute: %s")
    void ambiguosBinding(String attr);
    
    @LogMessage(level = WARN)
    @Message(id = 25288, value = "Type definition not found in schema: %s")
    void typeDefinitionNotInSchema(QName qname);
    
    @LogMessage(level = WARN)
    @Message(id = 25289, value = "Global element not found in schema: %s")
    void globalElementNotInSchema(QName qname);
    
}