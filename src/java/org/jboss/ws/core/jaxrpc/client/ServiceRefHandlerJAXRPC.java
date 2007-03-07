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
package org.jboss.ws.core.jaxrpc.client;

// $Id$

import javax.naming.Context;
import javax.naming.NamingException;

import org.jboss.logging.Logger;
import org.jboss.util.naming.Util;
import org.jboss.ws.metadata.j2ee.serviceref.UnifiedServiceRefMetaData;

/**
 * Binds a JAXRPC Service object in the client's ENC for every service-ref element in the
 * deployment descriptor.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 04-Nov-2006
 */
public class ServiceRefHandlerJAXRPC
{
   // logging support
   private static Logger log = Logger.getLogger(ServiceRefHandlerJAXRPC.class);

   /**
    * Binds a Service into the callers ENC for every service-ref element
    */
   public void setupServiceRef(Context encCtx, String encName, UnifiedServiceRefMetaData serviceRef) throws NamingException
   {
      String serviceRefName = serviceRef.getServiceRefName();
      ServiceReferenceable ref = new ServiceReferenceable(serviceRef);

      // Do not use rebind, the binding should be unique
      Util.bind(encCtx, encName, ref);

      log.debug("<service-ref> bound to: java:comp/env/" + serviceRefName);
   }
}
