/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.Service;

import org.jboss.logging.Logger;
import org.jboss.ws.api.util.BundleUtils;

/**
 * This is the proxy that implements the service interface .
 * <p/>
 * Additionally it handles some ws4ee functionality that is not part of jaxrpc behaviour.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 15-May-2004
 */
public class ServiceProxy implements InvocationHandler, Serializable, Externalizable
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(ServiceProxy.class);
   // provide logging
   private static final Logger log = Logger.getLogger(ServiceProxy.class);

   // The underlying jaxrpc service
   private ServiceImpl jaxrpcService;
   private Class siClass;

   // The methods from java.lang.Object
   private transient List objectMethods = new ArrayList();
   // The methods from javax.xml.rpc.Service
   private transient List jaxrpcServiceMethods = new ArrayList();
   // The methods from the service interface, in case of javax.xml.rpc.Service it is empty
   private transient List serviceInterfaceMethods = new ArrayList();

   // The cached getPort method
   private transient Method getPortMethod;

   public ServiceProxy() {
       // for deserialization only
   }

   /**
    * Construct a client side service proxy.
    * <p/>
    * This proxy implements the (generated) service interface.
    *
    * @param service The underlying {@link javax.xml.rpc.Service}
    * @param siClass The service interface, a subclass of {@link javax.xml.rpc.Service}
    */
   public ServiceProxy(ServiceImpl service, Class siClass)
   {
      this.jaxrpcService = service;
      this.siClass = siClass;
      init();
   }

   private void init() {
       // initialize java.lang.Object methods
       objectMethods.addAll(Arrays.asList(Object.class.getMethods()));

       // initialize javax.xml.rpc.Service methods
       jaxrpcServiceMethods.addAll(Arrays.asList(ServiceExt.class.getMethods()));

       // initialize SI methods
       if (siClass.getName().equals("javax.xml.rpc.Service") == false)
          serviceInterfaceMethods.addAll(Arrays.asList(siClass.getDeclaredMethods()));

       // initialize special ws4ee methods
       try
       {
          getPortMethod = Service.class.getMethod("getPort", new Class[]{Class.class});
       }
       catch (NoSuchMethodException e)
       {
          throw new JAXRPCException(e.toString());
       }
   }

   /**
    * Processes a method invocation on a proxy instance and returns
    * the result.
    */
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      String methodName = method.getName();

      try
      {
         Object retObj = null;
         if (jaxrpcServiceMethods.contains(method))
         {
            if(log.isDebugEnabled()) log.debug("Invoke on jaxrpc service: " + methodName);

            if (method.getName().equals("getPort"))
            {
               Remote port = (Remote)method.invoke(jaxrpcService, args);
               return port;
            }
            else
            {
               retObj = method.invoke(jaxrpcService, args);
               return retObj;
            }
         }
         if (serviceInterfaceMethods.contains(method))
         {
            if(log.isDebugEnabled()) log.debug("Invoke on service interface: " + methodName);

            Class seiClass = method.getReturnType();
            retObj = getPortMethod.invoke(jaxrpcService, new Object[]{seiClass});
            return retObj;
         }
         if (objectMethods.contains(method))
         {
            if(log.isDebugEnabled()) log.debug("Invoke on object: " + methodName);

            retObj = method.invoke(jaxrpcService, args);
            return retObj;
         }

         throw new JAXRPCException(BundleUtils.getMessage(bundle, "NOT_KNOW_HOW_TO_INVOKE",  method));
      }
      catch (Exception e)
      {
         handleException(e);
         return null;
      }
   }

   /**
    * Log the client side exception
    */
   private void handleException(Exception ex) throws Throwable
   {
      Throwable th = ex;
      if (ex instanceof InvocationTargetException)
         th = ((InvocationTargetException)ex).getTargetException();

      log.error(BundleUtils.getMessage(bundle, "SERVICE_ERROR"),  th);
      throw th;
   }

   public void writeExternal(final ObjectOutput out) throws IOException {
       out.writeObject(jaxrpcService);
       out.writeObject(siClass);
   }

   public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
       jaxrpcService = (ServiceImpl)in.readObject();
       siClass = (Class)in.readObject();
       init();
   }

}
