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
package org.jboss.ws.metadata.umdm;

// $Id$

import javax.xml.namespace.QName;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.core.jaxws.DynamicWrapperGenerator;
import org.jboss.ws.core.utils.JavaUtils;

/**
 * A Fault component describes a fault that a given operation supports.
 *
 * @author Thomas.Diesler@jboss.org
 * @author jason.greene@jboss.com
 * @since 12-May-2005
 */
public class FaultMetaData
{
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

   public FaultMetaData(OperationMetaData operation, QName xmlName, QName xmlType, String javaTypeName)
   {
      this(operation, xmlName, javaTypeName);
      setXmlType(xmlType);
   }

   public FaultMetaData(OperationMetaData operation, QName xmlName, String javaTypeName)
   {
      if (xmlName == null)
         throw new IllegalArgumentException("Invalid null xmlName argument");
      if (javaTypeName == null)
         throw new IllegalArgumentException("Invalid null javaTypeName argument, for: " + xmlName);

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
         throw new IllegalArgumentException("Invalid null xmlType argument, for: " + xmlName);

      this.xmlType = xmlType;
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
      Class tmpJavaType = javaType;
      if (tmpJavaType == null && javaTypeName != null)
      {
         try
         {
            ClassLoader loader = opMetaData.getEndpointMetaData().getClassLoader();
            tmpJavaType = JavaUtils.loadJavaType(javaTypeName, loader);
            
            if (opMetaData.getEndpointMetaData().getServiceMetaData().getUnifiedMetaData().isEagerInitialized())
            {
               log.warn("Loading java type after eager initialization");
               javaType = tmpJavaType;
            }
         }
         catch (ClassNotFoundException ex)
         {
            throw new WSException("Cannot load java type: " + javaTypeName, ex);
         }
      }
      return tmpJavaType;
   }

   public String getFaultBeanName()
   {
      return faultBeanName;
   }

   public void setFaultBeanName(String faultBeanName)
   {
      this.faultBeanName = faultBeanName;
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
            throw new WSException("Cannot load fault bean: " + faultBeanName, ex);
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
      ClassLoader loader = opMetaData.getEndpointMetaData().getClassLoader();
      new DynamicWrapperGenerator(loader).generate(this);
      
      // Initialize the cache
      javaType = getJavaType();
      if (javaType == null)
         throw new WSException("Cannot load java type: " + javaTypeName);
      
      faultBean = getFaultBean();
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