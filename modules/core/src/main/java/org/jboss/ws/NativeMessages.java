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

import javax.management.ObjectName;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.jboss.logging.Cause;
import org.jboss.logging.Message;
import org.jboss.logging.MessageBundle;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;

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
    IllegalStateException cannotObtainEndpointMetaData(ObjectName epName);
    
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
    
}
