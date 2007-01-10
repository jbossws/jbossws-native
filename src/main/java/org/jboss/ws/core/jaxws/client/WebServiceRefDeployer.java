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
package org.jboss.ws.core.jaxws.client;

// $Id$

import javax.naming.Context;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceRef;

import org.jboss.logging.Logger;
import org.jboss.util.naming.Util;

/**
 * Binds a JAXWS Service object into JNDI
 *
 * @author Thomas.Diesler@jboss.org
 * @since 24-Oct-2006
 */
public class WebServiceRefDeployer
{
   // provide logging
   private static Logger log = Logger.getLogger(WebServiceRefDeployer.class);

   private WebServiceRefDeployer()
   {
      // Hide ctor
   }

   public static void setupWebServiceRef(Context ctx, String jndiName, Class refType, WebServiceRef wsref, UnifiedServiceRef sref) throws Exception
   {
      String externalName = ctx.getNameInNamespace() + "/" + jndiName;
      String refTypeName = (refType != null ? refType.getName() : null);
      String wsrefString = "[name=" + wsref.name() + ",value=" + wsref.value() + ",type=" + wsref.type() + ",wsdl=" + wsref.wsdlLocation() + ",mapped=" + wsref.mappedName() + "]";
      log.debug("setupWebServiceRef [jndi=" + externalName + ",refType=" + refTypeName + ",wsref=" + wsrefString + ",sref=" + sref + "]");
      
      String serviceTypeName = null;
      String portTypeName = null;

      // #1 Use the explicit @WebServiceRef.value 
      if (wsref.value() != Object.class)
         serviceTypeName = wsref.value().getName();

      // #2 Use the target ref type 
      if (serviceTypeName == null && refType != null && Service.class.isAssignableFrom(refType))
         serviceTypeName = refType.getName();

      // #3 Use javax.xml.ws.Service 
      if (serviceTypeName == null)
         serviceTypeName = Service.class.getName();

      // #1 Use the explicit @WebServiceRef.type 
      if (wsref.type() != Object.class)
         portTypeName = wsref.type().getName();

      // #2 Use the target ref type 
      if (portTypeName == null && refType != null && Service.class.isAssignableFrom(refType) == false)
         portTypeName = refType.getName();
      
      // Set the wsdlLocation if there is no override already
      if (sref.getWsdlLocation() == null && wsref.wsdlLocation().length() > 0)
         sref.setWsdlLocation(wsref.wsdlLocation());
      
      Util.rebind(ctx, jndiName, new ServiceReferenceable(serviceTypeName, portTypeName, sref));
   }
}
