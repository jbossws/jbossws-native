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

import javax.xml.namespace.QName;
import javax.xml.transform.Result;

import org.jboss.logging.Logger;
import org.jboss.ws.Constants;
import org.jboss.ws.WSException;
import org.jboss.ws.core.jaxrpc.TypeMappingImpl;
import org.jboss.ws.core.soap.XMLFragment;
import org.jboss.ws.core.utils.JavaUtils;
import org.jboss.ws.metadata.umdm.ParameterMetaData;
import org.w3c.dom.NamedNodeMap;

/**
 * A Serializer that can handle SOAP encoded arrays.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 31-Oct-2005
 */
public class SOAPArraySerializer extends SerializerSupport
{
   // provide logging
   private static final Logger log = Logger.getLogger(SOAPArraySerializer.class);

   private SerializerSupport compSerializer;
   private NullValueSerializer nullSerializer;
   private boolean isArrayComponentType;
   private boolean xsiNamespaceInserted;
   private StringBuilder buffer;

   public SOAPArraySerializer() throws BindingException
   {
      nullSerializer = new NullValueSerializer();
   }

   /**
    */
   public Result serialize(QName xmlName, QName xmlType, Object value, SerializationContext serContext, NamedNodeMap attributes) throws BindingException
   {
      if(log.isDebugEnabled()) log.debug("serialize: [xmlName=" + xmlName + ",xmlType=" + xmlType + ",valueType=" + value.getClass().getName() + "]");
      try
      {
         ParameterMetaData paramMetaData = (ParameterMetaData)serContext.getProperty(ParameterMetaData.class.getName());
         QName compXmlType = paramMetaData.getSOAPArrayCompType();
         QName compXmlName = paramMetaData.getXmlName();
         Class javaType = paramMetaData.getJavaType();

         Class compJavaType = javaType.getComponentType();
         isArrayComponentType = isArrayJavaType(compJavaType) && isArrayXmlType(compXmlType);
         while (compJavaType.getComponentType() != null && isArrayComponentType == false)
         {
            compJavaType = compJavaType.getComponentType();
            isArrayComponentType = isArrayJavaType(compJavaType) && isArrayXmlType(compXmlType);
         }

         TypeMappingImpl typeMapping = serContext.getTypeMapping();
         if (compXmlType == null)
         {
            compXmlType = typeMapping.getXMLType(compJavaType);
            paramMetaData.setSOAPArrayCompType(compXmlType);
         }

         if (compXmlType == null)
            throw new WSException("Cannot obtain component xmlType for: " + compJavaType);

         // Get the component type serializer factory
         if(log.isDebugEnabled()) log.debug("Get component serializer for: [javaType=" + compJavaType.getName() + ",xmlType=" + compXmlType + "]");
         SerializerFactoryBase compSerializerFactory = (SerializerFactoryBase)typeMapping.getSerializer(compJavaType, compXmlType);
         if (compSerializerFactory == null)
         {
            log.warn("Cannot obtain component serializer for: [javaType=" + compJavaType.getName() + ",xmlType=" + compXmlType + "]");
            compSerializerFactory = (SerializerFactoryBase)typeMapping.getSerializer(null, compXmlType);
         }
         if (compSerializerFactory == null)
            throw new WSException("Cannot obtain component serializer for: " + compXmlType);

         // Get the component type serializer
         compSerializer = (SerializerSupport)compSerializerFactory.getSerializer();

         // Get the corresponding wrapper type
         if (JavaUtils.isPrimitive(value.getClass()))
            value = JavaUtils.getWrapperValueArray(value);

         buffer = new StringBuilder("<" + Constants.PREFIX_SOAP11_ENC + ":Array "+
            "xmlns:"+Constants.PREFIX_SOAP11_ENC+"='http://schemas.xmlsoap.org/soap/encoding/' ");

         if (value instanceof Object[])
         {
            Object[] objArr = (Object[])value;
            String arrayDim = "" + objArr.length;

            // Get multiple array dimension
            Object[] subArr = (Object[])value;
            while (isArrayComponentType == false && subArr.length > 0 && subArr[0] instanceof Object[])
            {
               subArr = (Object[])subArr[0];
               arrayDim += "," + subArr.length;
            }

            compXmlType = serContext.getNamespaceRegistry().registerQName(compXmlType);
            String arrayType = Constants.PREFIX_SOAP11_ENC + ":arrayType='" + compXmlType.getPrefix() + ":" + compXmlType.getLocalPart() + "[" + arrayDim + "]'";
            String compns = " xmlns:" + compXmlType.getPrefix() + "='" + compXmlType.getNamespaceURI() + "'";
            buffer.append(arrayType + compns + ">");

            serializeArrayComponents(compXmlName, compXmlType, serContext, objArr);
         }
         else
         {
            throw new WSException("Unsupported array type: " + javaType);
         }
         buffer.append("</" + Constants.PREFIX_SOAP11_ENC + ":Array>");

         if(log.isDebugEnabled()) log.debug("serialized: " + buffer);
         return stringToResult(buffer.toString());
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

   private void serializeArrayComponents(QName compXmlName, QName compXmlType, SerializationContext serContext, Object[] objArr) throws BindingException
   {
      for (Object compValue : objArr)
      {
         if (isArrayComponentType == false && compValue instanceof Object[])
         {
            serializeArrayComponents(compXmlName, compXmlType, serContext, (Object[])compValue);
         }
         else
         {
            SerializerSupport ser = compSerializer;

            // Null component value
            if (compValue == null)
            {
               ser = nullSerializer;
               if (xsiNamespaceInserted == false)
               {
                  xsiNamespaceInserted = true;
                  int insIndex = ("<" + Constants.PREFIX_SOAP11_ENC + ":Array ").length();
                  buffer.insert(insIndex, "xmlns:" + Constants.PREFIX_XSI + "='" + Constants.NS_SCHEMA_XSI + "' ");
               }
            }

            Result result = ser.serialize(compXmlName, compXmlType, compValue, serContext, null);
            XMLFragment fragment = new XMLFragment(result);
            buffer.append(fragment.toStringFragment());
         }
      }
   }

   /** True for all array xmlTypes, i.e. nmtokens, base64Binary, hexBinary
    * 
    *  FIXME: This method should be removed as soon as we can reliably get the SOAP
    *  arrayType from wsdl + schema. 
    */
   private boolean isArrayXmlType(QName xmlType)
   {
      boolean isArrayType = Constants.TYPE_SOAP11_BASE64.equals(xmlType);
      isArrayType = isArrayType || Constants.TYPE_SOAP11_BASE64.equals(xmlType);
      isArrayType = isArrayType || Constants.TYPE_SOAP11_BASE64BINARY.equals(xmlType);
      isArrayType = isArrayType || Constants.TYPE_SOAP11_HEXBINARY.equals(xmlType);
      isArrayType = isArrayType || Constants.TYPE_SOAP11_NMTOKENS.equals(xmlType);
      isArrayType = isArrayType || Constants.TYPE_LITERAL_BASE64BINARY.equals(xmlType);
      isArrayType = isArrayType || Constants.TYPE_LITERAL_HEXBINARY.equals(xmlType);
      isArrayType = isArrayType || Constants.TYPE_LITERAL_NMTOKENS.equals(xmlType);
      return isArrayType;
   }

   /** True for all array javaTypes, i.e. String[], Byte[], byte[] 
    * 
    *  FIXME: This method should be removed as soon as we can reliably get the SOAP
    *  arrayType from wsdl + schema. 
    */
   private boolean isArrayJavaType(Class javaType)
   {
      boolean isBinaryType = String[].class.equals(javaType) || Byte[].class.equals(javaType) || byte[].class.equals(javaType);
      return isBinaryType;
   }

}
