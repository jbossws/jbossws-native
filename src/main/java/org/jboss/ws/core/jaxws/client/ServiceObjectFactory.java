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
package org.jboss.ws.core.jaxws.client;

// $Id$

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import org.jboss.logging.Logger;
import org.jboss.ws.core.jaxws.ServiceDecorator;

/**
 * This ServiceObjectFactory reconstructs a javax.xml.ws.Service
 * for a given WSDL when the webservice client does a JNDI lookup
 *
 * @author Thomas.Diesler@jboss.org
 * @since 24-Oct-2004
 */
public class ServiceObjectFactory implements ObjectFactory
{
   // provide logging
   private static final Logger log = Logger.getLogger(ServiceObjectFactory.class);

   /**
    * Creates an object using the location or reference information specified.
    * <p/>
    *
    * @param obj         The possibly null object containing location or reference
    *                    information that can be used in creating an object.
    * @param name        The name of this object relative to <code>nameCtx</code>,
    *                    or null if no name is specified.
    * @param nameCtx     The context relative to which the <code>name</code>
    *                    parameter is specified, or null if <code>name</code> is
    *                    relative to the default initial context.
    * @param environment The possibly null environment that is used in
    *                    creating the object.
    * @return The object created; null if an object cannot be created.
    * @throws Exception if this object factory encountered an exception
    *                   while attempting to create an object, and no other object factories are
    *                   to be tried.
    * @see javax.naming.spi.NamingManager#getObjectInstance
    * @see javax.naming.spi.NamingManager#getURLContext
    */
   public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable environment) throws Exception
   {
      try
      {
         Reference ref = (Reference)obj;

         String serviceTypeName = (String)ref.get(ServiceReferenceable.SERVICE_TYPE).getContent();
         String portTypeName = (String)ref.get(ServiceReferenceable.PORT_TYPE).getContent();
         UnifiedServiceRef serviceRef = unmarshallServiceRef(ref);

         ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
         Class serviceType = ctxLoader.loadClass(serviceTypeName);
         Class portType = (portTypeName != null ? ctxLoader.loadClass(portTypeName) : null);

         if (Service.class.isAssignableFrom(serviceType) == false)
            throw new IllegalArgumentException("WebServiceRef type '" + serviceType + "' is not assignable to javax.xml.ws.Service");

         Object target;

         URL wsdlURL = getWsdlLocationURL(serviceType, serviceRef.getWsdlLocation());
         
         // Generic javax.xml.ws.Service
         if (serviceType == Service.class)
         {
            if (wsdlURL != null)
            {
               target = ServiceDecorator.create(wsdlURL, null);
            }
            else
            {
               throw new IllegalArgumentException("Cannot create generic javax.xml.ws.Service without wsdlLocation");
            }
         }
         // Generated javax.xml.ws.Service subclass
         else
         {
            if (wsdlURL != null)
            {
               Constructor ctor = serviceType.getConstructor(new Class[] { URL.class, QName.class });
               target = (Service)ctor.newInstance(new Object[] { wsdlURL, null });
            }
            else
            {
               target = (Service)serviceType.newInstance();
            }
         }

         if (portTypeName != null && portTypeName.equals(serviceTypeName) == false)
         {
            Object port = null;
            for (Method method : serviceType.getMethods())
            {
               String methodName = method.getName();
               Class retType = method.getReturnType();
               if (methodName.startsWith("get") && portType.isAssignableFrom(retType))
               {
                  port = method.invoke(target, new Object[0]);
                  target = port;
                  break;
               }
            }

            if (port == null)
               throw new WebServiceException("Cannot find getter for port type: " + portTypeName);
         }

         // process config-name and config-file
         processConfig(target, serviceRef);

         return target;
      }
      catch (Exception ex)
      {
         log.error("Cannot create service", ex);
         throw ex;
      }
   }

   private void processConfig(Object target, UnifiedServiceRef serviceRef) {
      if(target instanceof ServiceDecorator)
      {
         ServiceDecorator service = (ServiceDecorator)target;
         service.setProperty(ServiceDecorator.CLIENT_CONF_NAME, serviceRef.getConfigName());
         service.setProperty(ServiceDecorator.CLIENT_CONF_FILE, serviceRef.getConfigFile());
      }
      else
      {
         log.warn("Configuration ignored for " + target.getClass().getName());
      }
   }

   private UnifiedServiceRef unmarshallServiceRef(Reference ref) throws ClassNotFoundException, NamingException
   {
      UnifiedServiceRef sref;
      RefAddr refAddr = ref.get(ServiceReferenceable.SERVICE_REF);
      ByteArrayInputStream bais = new ByteArrayInputStream((byte[])refAddr.getContent());
      try
      {
         ObjectInputStream ois = new ObjectInputStream(bais);
         sref = (UnifiedServiceRef)ois.readObject();
         ois.close();
      }
      catch (IOException e)
      {
         throw new NamingException("Cannot unmarshall service ref meta data, cause: " + e.toString());
      }
      return sref;
   }

   private URL getWsdlLocationURL(Class type, String wsdlLocation)
   {
      URL wsdlURL = null;
      if (wsdlLocation != null && wsdlLocation.length() > 0)
      {
         // Try the wsdlLocation as URL
         try
         {
            wsdlURL = new URL(wsdlLocation);
         }
         catch (MalformedURLException ex)
         {
            // ignore
         }

         // Try the filename as File
         if (wsdlURL == null)
         {
            try
            {
               File file = new File(wsdlLocation);
               if (file.exists())
                  wsdlURL = file.toURL();
            }
            catch (MalformedURLException e)
            {
               // ignore
            }
         }

         // Try the filename as Resource
         if (wsdlURL == null)
         {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            wsdlURL = loader.getResource(wsdlLocation);
         }

         // Try the filename relative to class
         if (wsdlURL == null)
         {
            String packagePath = type.getPackage().getName().replace('.', '/');
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            wsdlURL = loader.getResource(packagePath + "/" + wsdlLocation);
         }
      }
      return wsdlURL;
   }
}
