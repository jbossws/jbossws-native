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
// $Id$
package org.jboss.ws.metadata.builder.jaxws;

import javax.jws.HandlerChain;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.BindingType;

import org.jboss.logging.Logger;
import org.jboss.ws.metadata.config.jaxws.ClientConfigJAXWS;
import org.jboss.ws.metadata.umdm.EndpointMetaData;
import org.jboss.ws.metadata.umdm.HandlerMetaData.HandlerType;

/**
 * A client side meta data builder that is based on JSR-181 annotations
 *
 * @author Thomas.Diesler@jboss.org
 * @since 9-Aug-2006
 */
public class JAXWSClientEndpointMetaDataBuilder extends JAXWSWebServiceMetaDataBuilder
{
   // provide logging
   private final Logger log = Logger.getLogger(JAXWSClientEndpointMetaDataBuilder.class);

   public void rebuildEndpointMetaData(EndpointMetaData epMetaData, Class<?> wsClass)
   {
      log.debug("START: rebuildMetaData");

      // Clear the java types, etc.
      resetMetaDataBuilder(epMetaData.getClassLoader());

      // Nuke parameterStyle
      epMetaData.setParameterStyle(null);

      // Process an optional @BindingType annotation
      if (wsClass.isAnnotationPresent(BindingType.class))
         processBindingType(epMetaData, wsClass);

      // Process @SOAPBinding
      if (wsClass.isAnnotationPresent(SOAPBinding.class))
         processSOAPBinding(epMetaData, wsClass);

      // process config, this will as well setup the handler
      epMetaData.configure(epMetaData);

      // Process an optional @HandlerChain annotation
      if (wsClass.isAnnotationPresent(HandlerChain.class))
         processHandlerChain(epMetaData, wsClass);

      // Process @WebMethod
      processWebMethods(epMetaData, wsClass);

      // Initialize types
      createJAXBContext(epMetaData);
      populateXmlTypes(epMetaData);

      // Eager initialization
      epMetaData.eagerInitialize();

      log.debug("END: rebuildMetaData\n" + epMetaData.getServiceMetaData());
   }
}
