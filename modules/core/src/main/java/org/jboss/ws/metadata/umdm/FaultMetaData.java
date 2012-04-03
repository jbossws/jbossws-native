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
package org.jboss.ws.metadata.umdm;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.ResourceBundle;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.common.JavaUtils;

/**
 * A Fault component describes a fault that a given operation supports.
 *
 * @author Thomas.Diesler@jboss.org
 * @author jason.greene@jboss.com
 * @since 12-May-2005
 */
public class FaultMetaData implements InitalizableMetaData
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(FaultMetaData.class);
   // provide logging
   private final Logger log = Logger.getLogger(FaultMetaData.class);

   // The parent operation
   private OperationMetaData opMetaData;

   private QName xmlName;
   private QName xmlType;
   private String javaTypeName;
   private String faultBeanName;
   private Class javaType;
   private Class faultBean;

   private Method faultInfoMethod;
   private Constructor serviceExceptionConstructor;
   private Method[] serviceExceptionGetters;

   private WrappedParameter[] faultBeanProperties;

   private Class[] propertyTypes;

   public FaultMetaData(OperationMetaData operation, QName xmlName, QName xmlType, String javaTypeName)
   {
      this(operation, xmlName, javaTypeName);
      setXmlType(xmlType);
   }

   public FaultMetaData(OperationMetaData operation, QName xmlName, String javaTypeName)
   {
      if (xmlName == null)
         throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "INVALID_NULL_XMLNAME_ARGUMENT"));
      if (javaTypeName == null)
         throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "INVALID_NULL_JAVATYPENAME_ARGUMENT",  xmlName));

      this.opMetaData = operation;
      this.xmlName = xmlName;
      this.javaTypeName = javaTypeName;
   }

   public OperationMetaData getOperationMetaData()
   {
      return opMetaData;
   }

   public QName getXmlName()
   {
      return xmlName;
   }

   public QName getXmlType()
   {
      return xmlType;
   }

   public void setXmlType(QName xmlType)
   {
      if (xmlType == null)
         throw new IllegalArgumentException(BundleUtils.getMessage(bundle, "INVALID_NULL_XMLTYPE_ARGUMENT",  xmlName));

      this.xmlType = xmlType;
   }
   
   public void setJavaTypeName(String javaTypeName)
   {
      this.javaTypeName = javaTypeName;
   }

   public String getJavaTypeName()
   {
      return javaTypeName;
   }

   /** Load the java type.
    *  It should only be cached during eager initialization.
    */
   public Class getJavaType()
   {
      if (javaType != null)
         return javaType;

      if (javaTypeName == null)
         return null;

      try
      {
         ClassLoader loader = opMetaData.getEndpointMetaData().getClassLoader();
         Class exceptionType = JavaUtils.loadJavaType(javaTypeName, loader);
         if (Exception.class.isAssignableFrom(exceptionType) == false)
            throw new IllegalStateException(BundleUtils.getMessage(bundle, "NOT_ASSIGNABLE_TO_EXCEPTION",  exceptionType));

         if (opMetaData.getEndpointMetaData().getServiceMetaData().getUnifiedMetaData().isEagerInitialized())
         {
            log.warn(BundleUtils.getMessage(bundle, "LOADING_JAVA_TYPE"));
            javaType = exceptionType;
         }
         return exceptionType;
      }
      catch (ClassNotFoundException ex)
      {
         throw new WSException(BundleUtils.getMessage(bundle, "CANNOT_LOAD_JAVA_TYPE",  javaTypeName),  ex);
      }
   }

   public String getFaultBeanName()
   {
      return faultBeanName;
   }

   public void setFaultBeanName(String faultBeanName)
   {
      this.faultBeanName = faultBeanName;
   }

   public Class loadFaultBean()
   {
      Class faultBean = null;
      try
      {
         ClassLoader loader = getOperationMetaData().getEndpointMetaData().getClassLoader();
         faultBean = JavaUtils.loadJavaType(faultBeanName, loader);
      }
      catch (ClassNotFoundException ex)
      {
         // ignore
      }
      return faultBean;
   }

   public Class getFaultBean()
   {
      Class tmpFaultBean = faultBean;
      if (tmpFaultBean == null && faultBeanName != null)
      {
         try
         {
            ClassLoader loader = opMetaData.getEndpointMetaData().getClassLoader();
            tmpFaultBean = JavaUtils.loadJavaType(faultBeanName, loader);
         }
         catch (ClassNotFoundException ex)
         {
            throw new WSException(BundleUtils.getMessage(bundle, "CANNOT_LOAD_FAULT_BEAN",  faultBeanName),  ex);
         }
      }
      return tmpFaultBean;
   }

   public void validate()
   {
      // nothing to do
   }

   public void eagerInitialize()
   {
      // Initialize the cache
      javaType = getJavaType();
      if (javaType == null)
         throw new WSException(BundleUtils.getMessage(bundle, "CANNOT_LOAD_JAVA_TYPE",  javaTypeName));

      if (JavaUtils.isAssignableFrom(Exception.class, javaType) == false)
         throw new WSException(BundleUtils.getMessage(bundle, "FAULT_JAVA_TYPE_NOT_EXCEPTION",  javaTypeName));
   }

   public Object toFaultBean(Exception serviceException)
   {
      Object faultBeanInstance;
      try
      {
         /* is the service exception a wrapper
          * (i.e. does it match the pattern in JAX-WS 2.5)? */
         if (faultInfoMethod != null)
         {
            // extract the fault bean from the wrapper exception
            faultBeanInstance = faultInfoMethod.invoke(serviceException);
         }
         else
         {
            // instantiate the fault bean
            try
            {
               faultBeanInstance = faultBean.newInstance();
            }
            catch (InstantiationException e)
            {
               throw new WebServiceException(BundleUtils.getMessage(bundle, "FAULT_BEAN_CLASS_IS_NOT_INSTANTIABLE"),  e);
            }

            // copy the properties from the service exception to the fault bean
            for (int i = 0; i < serviceExceptionGetters.length; i++)
            {
               Object propertyValue = serviceExceptionGetters[i].invoke(serviceException);

               WrappedParameter faultBeanProperty = faultBeanProperties[i];
               if (log.isTraceEnabled())
                  log.trace("copying from " + javaType.getSimpleName() + '.' + serviceExceptionGetters[i].getName()
                     + " to " + faultBean.getSimpleName() + '.' + faultBeanProperty.getVariable() + "<->" + faultBeanProperty.getName()
                     + ": " + propertyValue);
               faultBeanProperty.accessor().set(faultBeanInstance, propertyValue);
            }
         }
      }
      catch (IllegalAccessException e)
      {
         throw new WebServiceException(e);
      }
      catch (InvocationTargetException e)
      {
         throw new WebServiceException(e.getTargetException());
      }
      return faultBeanInstance;
   }

   public Exception toServiceException(Object faultBean, String message)
   {
      Exception serviceException;

      try
      {
         /* is the service exception a wrapper
          * (i.e. does it match the pattern in JAX-WS 2.5)? */
         if (faultInfoMethod != null)
         {
            serviceException = (Exception)serviceExceptionConstructor.newInstance(message, faultBean);
         }
         else
         {
            if (serviceExceptionConstructor == null)
               throw new WSException(BundleUtils.getMessage(bundle, "COULD_NOT_INSTANTIATE_SERVICE_EXCEPTION", 
                     new Object[]{ javaType.getSimpleName(),  Arrays.toString(propertyTypes)}));

            // extract the properties from the fault bean
            int propertyCount = faultBeanProperties.length;
            Object[] propertyValues = new Object[propertyCount];

            for (int i = 0; i < propertyCount; i++)
               propertyValues[i] = faultBeanProperties[i].accessor().get(faultBean);

            if (log.isDebugEnabled())
               log.debug("constructing " + javaType.getSimpleName() + ": " + Arrays.toString(propertyValues));
            serviceException = (Exception)serviceExceptionConstructor.newInstance(propertyValues);
         }
      }
      catch (InstantiationException e)
      {
         throw new WebServiceException(BundleUtils.getMessage(bundle, "EXCEPTION_IS_NOT_INSTANTIABLE"),  e);
      }
      catch (IllegalAccessException e)
      {
         throw new WebServiceException(e);
      }
      catch (InvocationTargetException e)
      {
         throw new WebServiceException(e.getTargetException());
      }
      return serviceException;
   }

   public String toString()
   {
      StringBuilder buffer = new StringBuilder("\nFaultMetaData");
      buffer.append("\n xmlName=" + xmlName);
      buffer.append("\n xmlType=" + xmlType);
      buffer.append("\n javaType=" + javaTypeName);
      buffer.append("\n faultBean=" + faultBeanName);
      return buffer.toString();
   }
}
