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

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

import org.jboss.logging.Logger;
import org.jboss.ws.Constants;
import org.jboss.ws.WSException;
import org.jboss.ws.core.jaxrpc.ParameterWrapping;
import org.jboss.ws.core.jaxws.DynamicWrapperGenerator;
import org.jboss.ws.core.utils.HolderUtils;
import org.jboss.ws.core.utils.JavaUtils;
import org.jboss.ws.extensions.xop.jaxws.ReflectiveXOPScanner;
import org.jboss.ws.metadata.acessor.ReflectiveMethodAccessor;

/**
 * A request/response parameter that a given operation supports.
 *
 * @author Thomas.Diesler@jboss.org
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @since 12-May-2005
 */
public class ParameterMetaData
{
   // provide logging
   private final Logger log = Logger.getLogger(ParameterMetaData.class);

   // The parent operation
   private OperationMetaData opMetaData;

   private QName xmlName;
   private String partName;
   private QName xmlType;
   private String javaTypeName;
   private Class javaType;
   private ParameterMode mode;
   private Set<String> mimeTypes;
   private boolean inHeader;
   private boolean isSwA;
   private boolean isXOP;
   private List<WrappedParameter> wrappedParameters;
   private int index;

   // SOAP-ENC:Array
   private boolean soapArrayParam;
   private QName soapArrayCompType;
   private AccessorFactoryCreator accessorFactoryCreator = ReflectiveMethodAccessor.FACTORY_CREATOR;

   private static final List<String> messageTypes = new ArrayList<String>();
   static
   {
      messageTypes.add("javax.xml.soap.SOAPElement");
      messageTypes.add("org.w3c.dom.Element");
   }

   public ParameterMetaData(OperationMetaData opMetaData, QName xmlName, QName xmlType, String javaTypeName)
   {
      this(opMetaData, xmlName, javaTypeName);
      setXmlType(xmlType);
   }

   public ParameterMetaData(OperationMetaData opMetaData, QName xmlName, String javaTypeName)
   {
      if (xmlName == null)
         throw new IllegalArgumentException("Invalid null xmlName argument");

      // Remove the prefixes
      if (xmlName.getNamespaceURI().length() > 0)
         xmlName = new QName(xmlName.getNamespaceURI(), xmlName.getLocalPart());

      this.xmlName = xmlName;
      this.opMetaData = opMetaData;
      this.mode = ParameterMode.IN;
      this.partName = xmlName.getLocalPart();
      this.javaTypeName = javaTypeName;
   }

   private static boolean matchParameter(Method method, int index, Class expectedType, Set<Integer> matches, boolean exact, boolean holder)
   {
      Class returnType = method.getReturnType();
      Type[] genericParameters = method.getGenericParameterTypes();
      Class[] classParameters = method.getParameterTypes();

      if (index == -1 && matchTypes(returnType, expectedType, exact, false))
         return true;

      boolean indexInBounds = -1 < index && index < classParameters.length;
      boolean matchTypes;
      
      if (JavaUtils.isRetro14())
         matchTypes = matchTypes(classParameters[index], expectedType, exact, holder);
      else 
         matchTypes = matchTypes(genericParameters[index], expectedType, exact, holder);
      
      if (indexInBounds && matchTypes)
      {
         matches.add(index);
         return true;
      }

      return false;
   }
   
   private static boolean matchTypes(Type actualType, Class expectedType, boolean exact, boolean holder)
   {
      if (holder && HolderUtils.isHolderType(actualType) == false)
         return false;

      Type valueType = (holder ? HolderUtils.getValueType(actualType) : actualType);
      Class valueClass = JavaUtils.erasure(valueType);

      return matchTypesInternal(valueClass, expectedType, exact);
   }

   // This duplication is needed because Class does not implement Type in 1.4, 
   // which makes retrotranslation not possible. This takes advantage of overloading to
   // prevent the problem.
   private static boolean matchTypes(Class actualType, Class expectedType, boolean exact, boolean holder)
   {
      if (holder && HolderUtils.isHolderType(actualType) == false)
         return false;

      Class valueClass = (holder ? HolderUtils.getValueType(actualType) : actualType);

      return matchTypesInternal(valueClass, expectedType, exact);
   }
   
   private static boolean matchTypesInternal(Class valueClass, Class expectedType, boolean exact)
   {
      // FIXME - Why do we need this hack? It shouldn't be needed. The method
      // signature should _ALWAYS_ match, else we will get ambiguous or
      // incorrect results
      List<Class> anyTypes = new ArrayList<Class>();
      anyTypes.add(javax.xml.soap.SOAPElement.class);
      anyTypes.add(org.w3c.dom.Element.class);

      boolean matched;
      if (exact)
      {
         matched = valueClass.getName().equals(expectedType.getName());
         if (matched == false && anyTypes.contains(valueClass))
            matched = anyTypes.contains(expectedType);
      }
      else
      {
         matched = JavaUtils.isAssignableFrom(valueClass, expectedType);
      }
      return matched;
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
         throw new IllegalArgumentException("Invalid null xmlType");

      // Remove potential prefix
      if (xmlType.getNamespaceURI().length() > 0)
         this.xmlType = new QName(xmlType.getNamespaceURI(), xmlType.getLocalPart());
      else this.xmlType = xmlType;

      // Special case to identify attachments
      if (Constants.NS_ATTACHMENT_MIME_TYPE.equals(xmlType.getNamespaceURI()))
      {
         String mimeType = convertXmlTypeToMimeType(xmlType);
         setMimeTypes(mimeType);
         this.isSwA = true;
      }
   }

   public String getJavaTypeName()
   {
      return javaTypeName;
   }

   public void setJavaTypeName(String typeName)
   {
      // Warn if this is called after eager initialization
      UnifiedMetaData wsMetaData = opMetaData.getEndpointMetaData().getServiceMetaData().getUnifiedMetaData();
      if (wsMetaData.isEagerInitialized() && UnifiedMetaData.isFinalRelease() == false)
         log.warn("Set java type name after eager initialization", new IllegalStateException());

      javaTypeName = typeName;
      javaType = null;
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

            UnifiedMetaData wsMetaData = opMetaData.getEndpointMetaData().getServiceMetaData().getUnifiedMetaData();
            if (wsMetaData.isEagerInitialized())
            {
               // This should not happen, see the warning in setJavaTypeName
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

   public ParameterMode getMode()
   {
      return mode;
   }

   public void setMode(String mode)
   {
      if ("IN".equals(mode))
         setMode(ParameterMode.IN);
      else if ("INOUT".equals(mode))
         setMode(ParameterMode.INOUT);
      else if ("OUT".equals(mode))
         setMode(ParameterMode.OUT);
      else throw new IllegalArgumentException("Invalid mode: " + mode);
   }

   public void setMode(ParameterMode mode)
   {
      this.mode = mode;
   }

   public Set<String> getMimeTypes()
   {
      return mimeTypes;
   }

   public void setMimeTypes(String mimeStr)
   {
      mimeTypes = new HashSet<String>();
      StringTokenizer st = new StringTokenizer(mimeStr, ",");
      while (st.hasMoreTokens())
         mimeTypes.add(st.nextToken().trim());
   }

   public boolean isInHeader()
   {
      return inHeader;
   }

   public void setInHeader(boolean inHeader)
   {
      this.inHeader = inHeader;
   }

   public boolean isSwA()
   {
      return isSwA;
   }

   public void setSwA(boolean isSwA)
   {
      this.isSwA = isSwA;
   }

   public boolean isXOP()
   {
      return isXOP;
   }

   public void setXOP(boolean isXOP)
   {
      this.isXOP = isXOP;
   }

   public boolean isSOAPArrayParam()
   {
      return soapArrayParam;
   }

   public void setSOAPArrayParam(boolean soapArrayParam)
   {
      this.soapArrayParam = soapArrayParam;
   }

   public QName getSOAPArrayCompType()
   {
      return soapArrayCompType;
   }

   public void setSOAPArrayCompType(QName xmlType)
   {
      this.soapArrayCompType = xmlType;
   }


   @Deprecated
   // FIXME This hack should be removed
   public boolean isMessageType()
   {
      return messageTypes.contains(javaTypeName);
   }

   @Deprecated
   public static boolean isMessageType(String javaTypeName)
   {
      return messageTypes.contains(javaTypeName);
   }

   /** Converts a proprietary JBossWS attachment xml type to the MIME type that it represents.
    */
   private String convertXmlTypeToMimeType(QName xmlType)
   {
      StringBuilder mimeName = new StringBuilder(xmlType.getLocalPart());
      int pos = mimeName.indexOf("_");
      if (pos == -1)
         throw new IllegalArgumentException("Invalid mime type: " + xmlType);

      mimeName.setCharAt(pos, '/');
      return mimeName.toString();
   }

   public int getIndex()
   {
      return index;
   }

   /**
    * Sets the method parameter index of the parameter this meta data corresponds to. A value of -1 indicates
    * that this parameter is mapped to the return value.
    *
    * @param index the method parameter offset, or -1 for a return value
    */
   public void setIndex(int index)
   {
      this.index = index;
   }

   public List<WrappedParameter> getWrappedParameters()
   {
      return wrappedParameters;
   }

   public void setWrappedParameters(List<WrappedParameter> wrappedParameters)
   {
      this.wrappedParameters = wrappedParameters;
   }

   public String getPartName()
   {
      return partName;
   }

   public void setPartName(String partName)
   {
      this.partName = partName;
   }

   public void validate()
   {
      // nothing to do
   }

   /**
    * @see UnifiedMetaData#eagerInitialize()
    */
   public void eagerInitialize()
   {
      // reset java type
      javaType = null;

      // FIXME - Remove messageType hack
      if (getOperationMetaData().isDocumentWrapped() && !isInHeader() && !isSwA() && !isMessageType())
      {
         new DynamicWrapperGenerator(getClassLoader()).generate(this);

         // Initialize accessors
         AccessorFactory factory = accessorFactoryCreator.create(this);
         for (WrappedParameter wrapped : wrappedParameters)
            wrapped.setAccessor(factory.create(wrapped));
      }

      javaType = getJavaType();
      if (javaType == null)
         throw new WSException("Cannot load java type: " + javaTypeName);

      // check if the JavaType is an mtom parameter
      // TODO: this should only apply to JAX-WS and needs to happen outside UMD
      ReflectiveXOPScanner scanner = new ReflectiveXOPScanner();
      String mimeType = scanner.scan(javaType);
      if (mimeType != null)
      {
         log.debug("MTOM parameter found: " + xmlName);
         setXOP(true);
      }
   }

   private ClassLoader getClassLoader()
   {
      ClassLoader loader = opMetaData.getEndpointMetaData().getServiceMetaData().getUnifiedMetaData().getClassLoader();
      if (loader == null)
         throw new WSException("ClassLoader not available");
      return loader;
   }

   public boolean matchParameter(Method method, Set<Integer> matches, boolean exact)
   {
      ClassLoader loader = getOperationMetaData().getEndpointMetaData().getClassLoader();
      List<WrappedParameter> wrappedParameters = getWrappedParameters();
      Class wrapperType = getJavaType();

      // Standard type
      if (wrappedParameters == null)
         return matchParameter(method, getIndex(), getJavaType(), matches, exact, mode != ParameterMode.IN);

      // Wrapped type
      for (WrappedParameter wrapped : wrappedParameters)
      {
         String typeName = wrapped.getType();

         try
         {
            Class type = (typeName != null) ? JavaUtils.loadJavaType(typeName, loader) : ParameterWrapping.getWrappedType(wrapped.getVariable(), wrapperType);
            if (type == null)
               return false;
            if (!matchParameter(method, wrapped.getIndex(), type, matches, exact, wrapped.isHolder()))
               return false;
         }
         catch (Exception ex)
         {
            log.debug("Invalid wrapper type:" + typeName, ex);
            return false;
         }
      }

      return true;
   }

   public void setAccessorFactoryCreator(AccessorFactoryCreator accessorFactoryCreator)
   {
      this.accessorFactoryCreator = accessorFactoryCreator;
   }

   public String toString()
   {
      boolean isReturn = (opMetaData.getReturnParameter() == this);
      StringBuilder buffer = new StringBuilder("\n" + (isReturn ? "ReturnMetaData:" : "ParameterMetaData:"));
      buffer.append("\n xmlName=" + getXmlName());
      buffer.append("\n partName=" + getPartName());
      buffer.append("\n xmlType=" + getXmlType());
      buffer.append("\n javaType=" + getJavaTypeName());
      buffer.append("\n mode=" + getMode());
      buffer.append("\n inHeader=" + isInHeader());
      buffer.append("\n index=" + index);

      if (soapArrayParam)
         buffer.append("\n soapArrayCompType=" + soapArrayCompType);

      if (isSwA())
      {
         buffer.append("\n isSwA=" + isSwA());
         buffer.append("\n mimeTypes=" + getMimeTypes());
      }

      if (wrappedParameters != null)
         buffer.append("\n wrappedParameters=" + wrappedParameters);

      if (isXOP())
      {
         buffer.append("\n isXOP=" + isXOP());
         buffer.append("\n mimeTypes=" + getMimeTypes());
      }

      return buffer.toString();
   }
}
