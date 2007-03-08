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
package org.jboss.ws.core.client;

// $Id$

import java.lang.reflect.AnnotatedElement;
import java.util.Collection;

import javax.naming.Context;
import javax.naming.NamingException;

import org.jboss.logging.Logger;
import org.jboss.ws.core.jaxrpc.client.ServiceRefHandlerJAXRPC;
import org.jboss.ws.core.jaxws.client.ServiceRefHandlerJAXWS;
import org.jboss.ws.integration.ServiceRefElement;
import org.jboss.ws.integration.ServiceRefHandler;
import org.jboss.ws.integration.ServiceRefMetaData;
import org.jboss.ws.integration.UnifiedVirtualFile;
import org.jboss.ws.metadata.j2ee.serviceref.UnifiedServiceRefMetaData;
import org.jboss.xb.binding.UnmarshallingContext;
import org.xml.sax.Attributes;

/**
 * Bind service refs in the client's ENC for every service-ref element in the
 * deployment descriptor.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 04-Nov-2006
 */
public class ServiceRefHandlerImpl implements ServiceRefHandler
{
   // logging support
   private static Logger log = Logger.getLogger(ServiceRefHandlerImpl.class);

   private ServiceRefObjectFactory objectFactory = new ServiceRefObjectFactory();
   
   public ServiceRefMetaData newMetaData()
   {
      return new UnifiedServiceRefMetaData();
   }

   public void setupServiceRefs(Context envCtx, UnifiedVirtualFile vfsRoot, Collection<ServiceRefMetaData> serviceRefs) throws NamingException
   {
      for (ServiceRefMetaData sref : serviceRefs)
      {
         String encName = sref.getServiceRefName();
         setupServiceRef(envCtx, encName, vfsRoot, sref);
      }
   }
   
   public void setupServiceRef(Context encCtx, String encName, UnifiedVirtualFile vfsRoot, ServiceRefMetaData sref) throws NamingException
   {
      if (sref.isProcessed())
      {
         log.warn("Attempt to rebind service-ref: " + sref);
         return;
      }
      
      UnifiedServiceRefMetaData serviceRef = (UnifiedServiceRefMetaData)sref;
      serviceRef.setVfsRoot(vfsRoot);
      try
      {
         if (isServiceRefJaxRpc(serviceRef))
         {
            ServiceRefHandlerJAXRPC handler = new ServiceRefHandlerJAXRPC();
            handler.setupServiceRef(encCtx, encName, serviceRef);
         }
         else
         {
            AnnotatedElement anElement = sref.getAnnotatedElement();
            ServiceRefHandlerJAXWS handler = new ServiceRefHandlerJAXWS();
            handler.setupServiceRef(encCtx, encName, anElement, serviceRef);
         }
      }
      finally
      {
         sref.setProcessed(true);
      }
   }
   
   public Object newChild(ServiceRefElement ref, UnmarshallingContext navigator, String namespaceURI, String localName, Attributes attrs)
   {
      return objectFactory.newChild(ref, navigator, namespaceURI, localName, attrs);
   }

   public void setValue(ServiceRefElement ref, UnmarshallingContext navigator, String namespaceURI, String localName, String value)
   {
      objectFactory.setValue(ref, navigator, namespaceURI, localName, value);
   }

   private boolean isServiceRefJaxRpc(UnifiedServiceRefMetaData serviceRef)
   {
      // The <service-interface> is a required element 
      // for JAXRPC and not defined for JAXWS
      return serviceRef.getServiceInterface() != null;
   }
}
