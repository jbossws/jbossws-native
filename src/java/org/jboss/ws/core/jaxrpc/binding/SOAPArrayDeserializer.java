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
package org.jboss.ws.core.jaxrpc.binding;

// $Id$

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.jboss.logging.Logger;
import org.jboss.ws.Constants;
import org.jboss.ws.WSException;
import org.jboss.ws.core.jaxrpc.TypeMappingImpl;
import org.jboss.ws.core.utils.DOMUtils;
import org.jboss.ws.core.utils.JavaUtils;
import org.jboss.ws.metadata.umdm.ParameterMetaData;
import org.w3c.dom.Element;

/**
 * A Deserializer that can handle SOAP encoded arrays.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 31-Oct-2005
 */
public class SOAPArrayDeserializer extends DeserializerSupport
{
   // provide logging
   private static final Logger log = Logger.getLogger(SOAPArrayDeserializer.class);

   private DeserializerSupport compDeserializer;

   public SOAPArrayDeserializer() throws BindingException
   {
   }

   public Object deserialize(QName xmlName, QName xmlType, Source xmlFragment, SerializationContext serContext) throws BindingException {
      return deserialize(xmlName, xmlType, sourceToString(xmlFragment), serContext);
   }

   /**
    */
   private Object deserialize(QName xmlName, QName xmlType, String xmlFragment, SerializationContext serContext) throws BindingException
   {
      if(log.isDebugEnabled()) log.debug("deserialize: [xmlName=" + xmlName + ",xmlType=" + xmlType + "]");
      try
      {
         ParameterMetaData paramMetaData = (ParameterMetaData)serContext.getProperty(ParameterMetaData.class.getName());
         QName compXmlType = paramMetaData.getSOAPArrayCompType();
         QName compXmlName = paramMetaData.getXmlName();

         Element arrayElement = DOMUtils.parse(xmlFragment);
         int[] arrDims = getDimensionsFromAttribute(arrayElement);
         Class compJavaType = getComponentTypeFromAttribute(arrayElement, serContext);
         Object[] retArray = (Object[])Array.newInstance(compJavaType, arrDims);

         TypeMappingImpl typeMapping = serContext.getTypeMapping();
         if (compXmlType == null)
         {
            compXmlType = typeMapping.getXMLType(compJavaType);
            paramMetaData.setSOAPArrayCompType(compXmlType);
         }

         if (compXmlType == null)
            throw new WSException("Cannot obtain component xmlType for: " + compJavaType);

         // Get the component type deserializer factory
         if(log.isDebugEnabled()) log.debug("Get component deserializer for: [javaType=" + compJavaType.getName() + ",xmlType=" + compXmlType + "]");
         DeserializerFactoryBase compDeserializerFactory = (DeserializerFactoryBase)typeMapping.getDeserializer(compJavaType, compXmlType);
         if (compDeserializerFactory == null)
         {
            log.warn("Cannot obtain component deserializer for: [javaType=" + compJavaType.getName() + ",xmlType=" + compXmlType + "]");
            compDeserializerFactory = (DeserializerFactoryBase)typeMapping.getDeserializer(null, compXmlType);
         }
         if (compDeserializerFactory == null)
            throw new WSException("Cannot obtain component deserializer for: " + compXmlType);

         // Get the component type deserializer
         compDeserializer = (DeserializerSupport)compDeserializerFactory.getDeserializer();

         if (arrDims.length < 1 || 2 < arrDims.length)
            throw new WSException("Unsupported array dimensions: " + Arrays.asList(arrDims));

         Iterator it = DOMUtils.getChildElements(arrayElement);
         if (arrDims.length == 1)
         {
            Object[] subArr = retArray;
            deserializeMemberValues(compXmlName, compXmlType, serContext, it, subArr);
         }
         if (arrDims.length == 2)
         {
            for (int i = 0; i < arrDims[0]; i++)
            {
               Object[] subArr = (Object[])retArray[i];
               deserializeMemberValues(compXmlName, compXmlType, serContext, it, subArr);
            }
         }

         if(log.isDebugEnabled()) log.debug("deserialized: " + retArray.getClass().getName());
         return retArray;
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new BindingException(e);
      }
   }

   private void deserializeMemberValues(QName compXmlName, QName compXmlType, SerializationContext serContext, Iterator it, Object[] subArr) throws BindingException
   {
      int dim = subArr.length;
      for (int i = 0; i < dim; i++)
      {
         Object compValue = null;
         if (it.hasNext())
         {
            Element childElement = (Element)it.next();
            Source compXMLFragment = new DOMSource(childElement);
            compValue = compDeserializer.deserialize(compXmlName, compXmlType, compXMLFragment, serContext);
            compValue = JavaUtils.getWrapperValueArray(compValue);
         }
         subArr[i] = compValue;
      }
   }

   private int[] getDimensionsFromAttribute(Element arrayElement)
   {
      QName attrQName = new QName(Constants.URI_SOAP11_ENC, "arrayType");
      QName arrayType = DOMUtils.getAttributeValueAsQName(arrayElement, attrQName);
      if (arrayType == null)
         throw new WSException("Cannot obtain attribute: " + attrQName);

      String localPart = arrayType.getLocalPart();
      int dimIndex = localPart.indexOf("[");

      String dimStr = localPart.substring(dimIndex);
      StringTokenizer st = new StringTokenizer(dimStr, "[,]");
      int[] arrDims = new int[st.countTokens()];
      for (int i = 0; st.hasMoreTokens(); i++)
         arrDims[i] = new Integer(st.nextToken()).intValue();

      return arrDims;
   }

   private Class getComponentTypeFromAttribute(Element arrayElement, SerializationContext serContext)
   {
      QName attrQName = new QName(Constants.URI_SOAP11_ENC, "arrayType");
      QName arrayType = DOMUtils.getAttributeValueAsQName(arrayElement, attrQName);
      if (arrayType == null)
         throw new WSException("Cannot obtain attribute: " + attrQName);

      String nsURI = arrayType.getNamespaceURI();
      String localPart = arrayType.getLocalPart();
      int dimIndex = localPart.indexOf("[");
      QName compXmlType = new QName(nsURI, localPart.substring(0, dimIndex));

      TypeMappingImpl typeMapping = serContext.getTypeMapping();
      Class javaType = typeMapping.getJavaType(compXmlType);
      if (javaType == null)
         throw new WSException("Cannot obtain javaType for: " + compXmlType);

      return JavaUtils.getWrapperType(javaType);
   }
}
