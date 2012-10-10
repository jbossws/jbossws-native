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
    
}
