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

import org.jboss.logging.Logger;
import org.jboss.util.naming.Util;
import org.jboss.ws.metadata.j2ee.serviceref.UnifiedServiceRefMetaData;

import javax.jws.HandlerChain;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceRef;
import javax.xml.ws.WebServiceRefs;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Binds a JAXWS Service object in the client's ENC
 *
 * @author Thomas.Diesler@jboss.org
 * @since 17-Jan-2007
 */
public class ServiceRefHandlerJAXWS
{
   // logging support
   private static Logger log = Logger.getLogger(ServiceRefHandlerJAXWS.class);

   public void setupServiceRef(Context encCtx, String encName, AnnotatedElement anElement, UnifiedServiceRefMetaData serviceRef) throws NamingException
   {
      WebServiceRef wsref = null;

      // Build the list of @WebServiceRef relevant annotations 
      List<WebServiceRef> wsrefList = new ArrayList<WebServiceRef>();
      if (anElement != null)
      {
         for (Annotation an : anElement.getAnnotations())
         {
            if (an instanceof WebServiceRef)
               wsrefList.add((WebServiceRef)an);

            if (an instanceof WebServiceRefs)
            {
               WebServiceRefs wsrefs = (WebServiceRefs)an;
               for (WebServiceRef aux : wsrefs.value())
                  wsrefList.add(aux);
            }
         }
      }

      // Use the single @WebServiceRef
      if (wsrefList.size() == 1)
      {
         wsref = wsrefList.get(0);
      }
      else
      {
         for (WebServiceRef aux : wsrefList)
         {
            if (encName.endsWith("/" + aux.name()))
            {
               wsref = aux;
               break;
            }
         }
      }

      Class targetClass = null;
      if (anElement instanceof Field)
      {
         targetClass = ((Field)anElement).getType();
      }
      else if (anElement instanceof Method)
      {
         targetClass = ((Method)anElement).getParameterTypes()[0];
      }
      else
      {
         if( wsref!=null && (wsref.type() != Object.class) )
            targetClass = wsref.type();
      }

      String targetClassName = (targetClass != null ? targetClass.getName() : null);
      String externalName = encCtx.getNameInNamespace() + "/" + encName;
      log.info("setupServiceRef [jndi=" + externalName + ",target=" + targetClassName + "]");

      String serviceImplClass = null;

      // #1 Use the explicit @WebServiceRef.value 
      if (wsref != null && wsref.value() != Object.class)
         serviceImplClass = wsref.value().getName();

      // #2 Use the target ref type 
      if (serviceImplClass == null && targetClass != null && Service.class.isAssignableFrom(targetClass))
         serviceImplClass = targetClass.getName();

      // #3 Use <service-interface> 
      if (serviceImplClass == null && serviceRef.getServiceInterface() != null)
         serviceImplClass = serviceRef.getServiceInterface();

      // #4 Use javax.xml.ws.Service 
      if (serviceImplClass == null)
         serviceImplClass = Service.class.getName();

      // #1 Use the explicit @WebServiceRef.type 
      if (wsref != null && wsref.type() != Object.class)
         targetClassName = wsref.type().getName();


      // #2 Use the target ref type 
      if (targetClassName == null && targetClass != null && Service.class.isAssignableFrom(targetClass) == false)
         targetClassName = targetClass.getName();

      // Set the wsdlLocation if there is no override already
      if (serviceRef.getWsdlOverride() == null && wsref != null && wsref.wsdlLocation().length() > 0)
         serviceRef.setWsdlOverride(wsref.wsdlLocation());

      // Set the handlerChain from @HandlerChain on the annotated element
      String handlerChain = serviceRef.getHandlerChain();
      if (anElement != null)
      {
         HandlerChain anHandlerChain = anElement.getAnnotation(HandlerChain.class);
         if (handlerChain == null && anHandlerChain != null && anHandlerChain.file().length() > 0)
            handlerChain = anHandlerChain.file();
      }

      // Resolve path to handler chain
      if (handlerChain != null)
      {
         try
         {
            new URL(handlerChain);
         }
         catch (MalformedURLException ex)
         {
            Class declaringClass = null;
            if (anElement instanceof Field)
               declaringClass = ((Field)anElement).getDeclaringClass();
            else if (anElement instanceof Method)
               declaringClass = ((Method)anElement).getDeclaringClass();
            else if (anElement instanceof Class)
               declaringClass = (Class)anElement;

            handlerChain = declaringClass.getPackage().getName().replace('.', '/') + "/" + handlerChain;
         }

         serviceRef.setHandlerChain(handlerChain);
      }

      // Do not use rebind, the binding should be unique
      // [JBWS-1499] - Revisit WebServiceRefHandler JNDI rebind
      Util.bind(encCtx, encName, new ServiceReferenceable(serviceImplClass, targetClassName, serviceRef));

   }
}
