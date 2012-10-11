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
import javax.xml.rpc.handler.HandlerInfo;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Cause;
import org.jboss.logging.LogMessage;
import org.jboss.logging.Message;
import org.jboss.logging.MessageLogger;

/**
 * JBossWS-CXF log messages
 * 
 * @author alessio.soldano@jboss.com
 */
@MessageLogger(projectCode = "JBWS")
public interface NativeLoggers extends BasicLogger
{
    NativeLoggers ROOT_LOGGER = org.jboss.logging.Logger.getMessageLogger(NativeLoggers.class, "org.jboss.ws.native");
    
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
    
}
