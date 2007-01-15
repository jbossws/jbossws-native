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
package org.jboss.ws.annotation;

// $Id$

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides web context specific meta data to EJB based web service endpoints.
 *
 * @author thomas.diesler@jboss.org
 * @since 26-Apr-2005
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = { ElementType.TYPE })
public @interface PortComponent {

   /** 
    * The contextRoot element specifies the context root that the web service endpoint is deployed to.
    * If it is not specified it will be derived from the deployment short name.
    * 
    * Applies to server side port components only. 
    */
   String contextRoot() default "";
   
   /** 
    * The virtual hosts that the web service endpoint is deployed to.
    * 
    * Applies to server side port components only.
    */
   String[] virtualHosts() default {};
   
   /** 
    * Relative path that is appended to the contextRoot to form fully qualified
    * endpoint address for the web service endpoint.
    * 
    * Applies to server side port components only. 
    */
   String urlPattern() default "";

   /**
    * The authMethod is used to configure the authentication mechanism for the web service. 
    * As a prerequisite to gaining access to any web service which are protected by an authorization
    * constraint, a user must have authenticated using the configured mechanism.
    *
    * Legal values for this element are "BASIC", or "CLIENT-CERT".
    */
   String authMethod() default "";

   /**
    * The transportGuarantee specifies that the communication
    * between client and server should be NONE, INTEGRAL, or
    * CONFIDENTIAL. NONE means that the application does not require any
    * transport guarantees. A value of INTEGRAL means that the application
    * requires that the data sent between the client and server be sent in
    * such a way that it can't be changed in transit. CONFIDENTIAL means
    * that the application requires that the data be transmitted in a
    * fashion that prevents other entities from observing the contents of
    * the transmission. In most cases, the presence of the INTEGRAL or
    * CONFIDENTIAL flag will indicate that the use of SSL is required.
    */
   String transportGuarantee() default "";

   /**
    * A secure endpoint does not by default publish it's wsdl on an unsecure transport.
    * You can override this behaviour by explicitly setting the secureWSDLAccess flag to false.
    * 
    * Protect access to WSDL
    * http://jira.jboss.org/jira/browse/JBWS-723   
    */
   boolean secureWSDLAccess() default true;
   
   /** 
    * The optional config-name element gives the client configuration name that must be present in
    * the configuration given by element config-file.
    *  
    * Server side default: Standard Endpoint 
    * Client side default: Standard Client 
    */
   String configName() default "";

   /** 
    * The optional config-file element gives the to a URL or resource name for the configuration.
    * 
    * Server side default: standard-jaxrpc-endpoint-config.xml, standard-jaxws-endpoint-config.xml 
    * Client side default: standard-jaxrpc-client-config.xml, standard-jaxws-client-config.xml 
    */
   String configFile() default "";
}
