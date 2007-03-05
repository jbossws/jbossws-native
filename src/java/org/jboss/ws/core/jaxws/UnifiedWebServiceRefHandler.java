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
package org.jboss.ws.core.jaxws;

// $Id$

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.jws.HandlerChain;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.xml.transform.Source;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceRef;
import javax.xml.ws.WebServiceRefs;

import org.jboss.logging.Logger;
import org.jboss.util.naming.Util;
import org.jboss.ws.WSException;
import org.jboss.ws.core.UnifiedVirtualFile;
import org.jboss.ws.core.jaxws.client.ServiceReferenceable;
import org.jboss.ws.core.jaxws.client.UnifiedServiceRef;

/**
 * Binds a JAXWS Service object in the client's ENC
 *
 * @author Thomas.Diesler@jboss.org
 * @since 17-Jan-2007
 */
public class UnifiedWebServiceRefHandler
{
   // logging support
   private static Logger log = Logger.getLogger(UnifiedWebServiceRefHandler.class);

   protected void setupWebServiceRef(Context encCtx, String encName, AnnotatedElement anElement, UnifiedVirtualFile vfsRoot, Source metadata) throws NamingException
   {
      WebServiceRef wsref = null;

      // Build the list of @WebServiceRef relevant annotations 
      List<WebServiceRef> wsrefList = new ArrayList<WebServiceRef>();
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

      if (wsref == null)
         throw new IllegalArgumentException("@WebServiceRef must be present on: " + anElement);

      Class targetClass = null;
      if (anElement instanceof Field)
         targetClass = ((Field)anElement).getType();
      else if (anElement instanceof Method)
         targetClass = ((Method)anElement).getParameterTypes()[0];

      String externalName = encCtx.getNameInNamespace() + "/" + encName;
      String targetClassName = (targetClass != null ? targetClass.getName() : null);
      log.debug("setupWebServiceRef [jndi=" + externalName + ",target=" + targetClassName + "]");

      String serviceClassName = null;

      // #1 Use the explicit @WebServiceRef.value 
      if (wsref.value() != Object.class)
         serviceClassName = wsref.value().getName();

      // #2 Use the target ref type 
      if (serviceClassName == null && targetClass != null && Service.class.isAssignableFrom(targetClass))
         serviceClassName = targetClass.getName();

      // #3 Use javax.xml.ws.Service 
      if (serviceClassName == null)
         serviceClassName = Service.class.getName();

      // #1 Use the explicit @WebServiceRef.type 
      if (wsref.type() != Object.class)
         targetClassName = wsref.type().getName();

      // #2 Use the target ref type 
      if (targetClassName == null && targetClass != null && Service.class.isAssignableFrom(targetClass) == false)
         targetClassName = targetClass.getName();

      try
      {
         UnifiedServiceRefObjectFactory factory = UnifiedServiceRefObjectFactory.newInstance();
         UnifiedServiceRef usRef = factory.parse(metadata);
         usRef.setRootFile(vfsRoot);

         // Set the wsdlLocation if there is no override already
         if (usRef.getWsdlLocation() == null && wsref.wsdlLocation().length() > 0)
            usRef.setWsdlLocation(wsref.wsdlLocation());

         // Set the handlerChain from @HandlerChain on the annotated element
         String handlerChain = usRef.getHandlerChain();
         HandlerChain anHandlerChain = anElement.getAnnotation(HandlerChain.class);
         if (handlerChain == null && anHandlerChain != null && anHandlerChain.file().length() > 0)
            handlerChain = anHandlerChain.file();

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
            
            usRef.setHandlerChain(handlerChain);
         }

         // Do not use rebind, the binding should be unique
         // [JBWS-1499] - Revisit WebServiceRefHandler JNDI rebind
         Util.rebind(encCtx, encName, new ServiceReferenceable(serviceClassName, targetClassName, usRef));

         log.debug("<service-ref> bound to: java:comp/env/" + encName);
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         throw new WSException("Cannot bind web service ref: " + encName, ex);
      }
   }

   private boolean findHandlerChain(UnifiedVirtualFile vfsRoot, String handlerChain)
   {
      try
      {
         vfsRoot.findChild(handlerChain);
         return true;
      }
      catch (IOException io)
      {
         return false;
      }
   }
}
