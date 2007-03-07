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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.core.ConfigProvider;
import org.jboss.ws.metadata.j2ee.serviceref.UnifiedServiceRefMetaData;

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

   // The ServiceRefMetaData association
   private static ThreadLocal<UnifiedServiceRefMetaData> serviceRefAssociation = new ThreadLocal<UnifiedServiceRefMetaData>();

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

         // Get the target class name
         String targetClassName = (String)ref.get(ServiceReferenceable.TARGET_CLASS_NAME).getContent();

         // Unmarshall the UnifiedServiceRef
         UnifiedServiceRefMetaData serviceRef = unmarshallServiceRef(ref);
         String serviceRefName = serviceRef.getServiceRefName();
         QName serviceQName = serviceRef.getServiceQName();

         String serviceImplClass = serviceRef.getServiceImplClass();
         if (serviceImplClass == null)
            serviceImplClass = (String)ref.get(ServiceReferenceable.SERVICE_IMPL_CLASS).getContent();

         // If the target defaults to javax.xml.ws.Service, user the service as the target
         if (Service.class.getName().equals(targetClassName))
            targetClassName = serviceImplClass;
         
         if(log.isDebugEnabled()) log.debug("[name=" + serviceRefName + ",service=" + serviceImplClass + ",target=" + targetClassName + "]");

         // Load the service class
         ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
         Class serviceClass = ctxLoader.loadClass(serviceImplClass);
         Class targetClass = (targetClassName != null ? ctxLoader.loadClass(targetClassName) : null);

         if (Service.class.isAssignableFrom(serviceClass) == false)
            throw new IllegalArgumentException("WebServiceRef type '" + serviceClass + "' is not assignable to javax.xml.ws.Service");

         // Receives either a javax.xml.ws.Service or a dynamic proxy
         Object target;

         // Get the URL to the wsdl
         URL wsdlURL = serviceRef.getWsdlLocation();
         try
         {
            // Associate the ServiceRefMetaData with this thread
            serviceRefAssociation.set(serviceRef);

            // Generic javax.xml.ws.Service
            if (serviceClass == Service.class)
            {
               if (wsdlURL != null)
               {
                  target = Service.create(wsdlURL, serviceQName);
               }
               else
               {
                  throw new IllegalArgumentException("Cannot create generic javax.xml.ws.Service without wsdlLocation: " + serviceRefName);
               }
            }
            // Generated javax.xml.ws.Service subclass
            else
            {
               if (wsdlURL != null)
               {
                  Constructor ctor = serviceClass.getConstructor(new Class[] { URL.class, QName.class });
                  target = ctor.newInstance(new Object[] { wsdlURL, serviceQName });
               }
               else
               {
                  target = (Service)serviceClass.newInstance();
               }
            }
         }
         finally
         {
            serviceRefAssociation.set(null);
         }

         // Configure the service
         configureService((Service)target, serviceRef);

         if (targetClassName != null && targetClassName.equals(serviceImplClass) == false)
         {
            try
            {
               Object port = null;
               if (serviceClass != Service.class)
               {
                  for (Method method : serviceClass.getDeclaredMethods())
                  {
                     String methodName = method.getName();
                     Class retType = method.getReturnType();
                     if (methodName.startsWith("get") && targetClass.isAssignableFrom(retType))
                     {
                        port = method.invoke(target, new Object[0]);
                        target = port;
                        break;
                     }
                  }
               }

               if (port == null)
               {
                  Method method = serviceClass.getMethod("getPort", new Class[] { Class.class });
                  port = method.invoke(target, new Object[] { targetClass });
                  target = port;
               }
            }
            catch (InvocationTargetException ex)
            {
               throw ex.getTargetException();
            }
         }

         return target;
      }
      catch (Throwable ex)
      {
         WSException.rethrow("Cannot create service", ex);
         return null;
      }
   }

   public static UnifiedServiceRefMetaData getServiceRefAssociation()
   {
      // The ServiceDelegateImpl get the usRef at ctor time
      return (UnifiedServiceRefMetaData)serviceRefAssociation.get();
   }

   private void configureService(Service service, UnifiedServiceRefMetaData serviceRef)
   {
      String configFile = serviceRef.getConfigFile();
      String configName = serviceRef.getConfigName();
      if (service instanceof ConfigProvider)
      {
         if(log.isDebugEnabled()) log.debug("Configure Service: [configName=" + configName + ",configFile=" + configFile + "]");

         ConfigProvider cp = (ConfigProvider)service;
         if (configName != null || configFile != null)
            cp.setConfigName(configName, configFile);
      }
   }

   private UnifiedServiceRefMetaData unmarshallServiceRef(Reference ref) throws ClassNotFoundException, NamingException
   {
      UnifiedServiceRefMetaData sref;
      RefAddr refAddr = ref.get(ServiceReferenceable.SERVICE_REF_META_DATA);
      ByteArrayInputStream bais = new ByteArrayInputStream((byte[])refAddr.getContent());
      try
      {
         ObjectInputStream ois = new ObjectInputStream(bais);
         sref = (UnifiedServiceRefMetaData)ois.readObject();
         ois.close();
      }
      catch (IOException e)
      {
         throw new NamingException("Cannot unmarshall service ref meta data, cause: " + e.toString());
      }
      return sref;
   }
}
