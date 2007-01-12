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

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.soap.AttachmentPart;

import org.jboss.ws.metadata.umdm.EndpointMetaData;

// $Id$

/**
 * An instance of a stub class represents a client side proxy or stub instance for the target service endpoint.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 27-Jan-2005
 */
public interface StubExt extends javax.xml.rpc.Stub
{
   /** ClientTimeout property: org.jboss.ws.timeout */
   static final String PROPERTY_CLIENT_TIMEOUT = "org.jboss.ws.timeout";
   /** KeyStore property: org.jboss.ws.keyStore */
   static final String PROPERTY_KEY_STORE = "org.jboss.ws.keyStore";
   /** KeyStorePassword property: org.jboss.ws.keyStorePassword */
   static final String PROPERTY_KEY_STORE_PASSWORD = "org.jboss.ws.keyStorePassword";
   /** KeyStoreType property: org.jboss.ws.keyStoreType */
   static final String PROPERTY_KEY_STORE_TYPE = "org.jboss.ws.keyStoreType";
   /** TrustStore property: org.jboss.ws.trustStore */
   static final String PROPERTY_TRUST_STORE = "org.jboss.ws.trustStore";
   /** TrustStorePassword property: org.jboss.ws.trustStorePassword */
   static final String PROPERTY_TRUST_STORE_PASSWORD = "org.jboss.ws.trustStorePassword";
   /** TrustStoreType property: org.jboss.ws.trustStoreType */
   static final String PROPERTY_TRUST_STORE_TYPE = "org.jboss.ws.trustStoreType";
   /** Authentication type, used to specify basic, etc) */
   static final String PROPERTY_AUTH_TYPE = "org.jboss.ws.authType";
   /** Authentication type, BASIC */
   static final String PROPERTY_AUTH_TYPE_BASIC = "org.jboss.ws.authType.basic";
   /** Authentication type, WSEE */
   static final String PROPERTY_AUTH_TYPE_WSSE = "org.jboss.ws.authType.wsse";
   /** Enable MTOM on the stub */
   static final String PROPERTY_MTOM_ENABLED= "org.jboss.ws.mtom.enabled";

   /**
    * Get the endpoint meta data for this stub
    */
   EndpointMetaData getEndpointMetaData();
   
   /**
    * Add a header that is not bound to an input parameter.
    * A propriatory extension, that is not part of JAXRPC.
    *
    * @param xmlName The XML name of the header element
    * @param xmlType The XML type of the header element
    */
   void addUnboundHeader(QName xmlName, QName xmlType, Class javaType, ParameterMode mode);

   /**
    * Get the header value for the given XML name.
    * A propriatory extension, that is not part of JAXRPC.
    *
    * @param xmlName The XML name of the header element
    * @return The header value, or null
    */
   Object getUnboundHeaderValue(QName xmlName);

   /**
    * Set the header value for the given XML name.
    * A propriatory extension, that is not part of JAXRPC.
    *
    * @param xmlName The XML name of the header element
    */
   void setUnboundHeaderValue(QName xmlName, Object value);

   /**
    * Clear all registered headers.
    * A propriatory extension, that is not part of JAXRPC.
    */
   void clearUnboundHeaders();

   /**
    * Remove the header for the given XML name.
    * A propriatory extension, that is not part of JAXRPC.
    */
   void removeUnboundHeader(QName xmlName);

   /**
    * Get an Iterator over the registered header XML names.
    * A propriatory extension, that is not part of JAXRPC.
    */
   Iterator getUnboundHeaders();
   
   /**
    * Adds the given AttachmentPart object to the outgoing SOAPMessage.
    * An AttachmentPart object must be created before it can be added to a message.
    */
   void addAttachmentPart(AttachmentPart attachmentpart);

   /**
    * Clears the list of attachment parts.
    */
   void clearAttachmentParts();
   
   /**
    * Creates a new empty AttachmentPart object.
    */
   AttachmentPart createAttachmentPart();

   /** 
    * Get the current port configuration file 
    * A propriatory extension, that is not part of JAXRPC.
    */
   String getConfigFile();

   /** 
    * Set the current port configuration file 
    * A propriatory extension, that is not part of JAXRPC.
    */
   void setConfigFile(String configFile);

   /** 
    * Get the current port configuration name 
    * A propriatory extension, that is not part of JAXRPC.
    */
   String getConfigName();

   /** 
    * Set the current port configuration name 
    * A propriatory extension, that is not part of JAXRPC.
    */
   void setConfigName(String configName);
}
