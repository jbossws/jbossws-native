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
package org.jboss.ws.core.jaxrpc;

// $Id$

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.rpc.encoding.DeserializerFactory;
import javax.xml.rpc.encoding.SerializerFactory;
import javax.xml.rpc.encoding.TypeMapping;

import org.jboss.logging.Logger;
import org.jboss.ws.Constants;
import org.jboss.ws.core.jaxrpc.binding.Base64DeserializerFactory;
import org.jboss.ws.core.jaxrpc.binding.Base64SerializerFactory;
import org.jboss.ws.core.jaxrpc.binding.CalendarDeserializerFactory;
import org.jboss.ws.core.jaxrpc.binding.CalendarSerializerFactory;
import org.jboss.ws.core.jaxrpc.binding.DateDeserializerFactory;
import org.jboss.ws.core.jaxrpc.binding.DateSerializerFactory;
import org.jboss.ws.core.jaxrpc.binding.HexDeserializerFactory;
import org.jboss.ws.core.jaxrpc.binding.HexSerializerFactory;
import org.jboss.ws.core.jaxrpc.binding.QNameDeserializerFactory;
import org.jboss.ws.core.jaxrpc.binding.QNameSerializerFactory;
import org.jboss.ws.core.jaxrpc.binding.SimpleDeserializerFactory;
import org.jboss.ws.core.jaxrpc.binding.SimpleSerializerFactory;
import org.jboss.ws.core.utils.JavaUtils;

/**
 * This is the representation of a type mapping.
 * This TypeMapping implementation supports the literal encoding style.
 *
 * The TypeMapping instance maintains a tuple of the type
 * {XML typeQName, Java Class, SerializerFactory, DeserializerFactory}.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 10-Oct-2004
 */
public abstract class TypeMappingImpl implements TypeMapping
{
   // provide logging
   private static final Logger log = Logger.getLogger(TypeMappingImpl.class);

   // Map<KeyPair,FactoryPair>
   private Map<KeyPair, FactoryPair> tupleMap = new LinkedHashMap<KeyPair, FactoryPair>();

   /**
    * Gets the DeserializerFactory registered for the specified pair of Java type and XML data type.
    * @param javaType Class of the Java type
    * @param xmlType QName of the XML type
    * @return Registered DeserializerFactory or null if there is no registered factory
    */
   public DeserializerFactory getDeserializer(Class javaType, QName xmlType)
   {
      FactoryPair fPair = getFactoryPair(xmlType, javaType);
      return (fPair != null ? fPair.getDeserializerFactory() : null);
   }

   /**
    * Gets the SerializerFactory registered for the specified pair of Java type and XML data type.
    * @param javaType Class of the Java type
    * @param xmlType QName of the XML type
    * @return Registered SerializerFactory or null if there is no registered factory
    */
   public SerializerFactory getSerializer(Class javaType, QName xmlType)
   {
      FactoryPair fPair = getFactoryPair(xmlType, javaType);
      return (fPair != null ? fPair.getSerializerFactory() : null);
   }

   /**
    * Returns the encodingStyle URIs (as String[]) supported by this TypeMapping instance.
    * A TypeMapping that contains only encoding style independent serializers and deserializers
    * returns null from this method.
    *
    * @return Array of encodingStyle URIs for the supported encoding styles
    */
   public abstract String[] getSupportedEncodings();

   /**
    * Sets the encodingStyle URIs supported by this TypeMapping instance. A TypeMapping that contains only encoding
    * independent serializers and deserializers requires null as the parameter for this method.
    *
    * @param encodingStyleURIs Array of encodingStyle URIs for the supported encoding styles
    */
   public abstract void setSupportedEncodings(String[] encodingStyleURIs);

   /**
    * Checks whether or not type mapping between specified XML type and Java type is registered.
    * @param javaType Class of the Java type
    * @param xmlType QName of the XML type
    * @return boolean; true if type mapping between the specified XML type and Java type is registered; otherwise false
    */
   public boolean isRegistered(Class javaType, QName xmlType)
   {
      return getFactoryPair(xmlType, javaType) != null;
   }

   /**
    * Registers SerializerFactory and DeserializerFactory for a specific type mapping between an XML type and Java type.
    * This method replaces any existing registered SerializerFactory DeserializerFactory instances.
    * @param javaType Class of the Java type
    * @param xmlType QName of the XML type
    * @param sf SerializerFactory
    * @param df DeserializerFactory
    * @throws javax.xml.rpc.JAXRPCException If any error during the registration
    */
   public void register(Class javaType, QName xmlType, SerializerFactory sf, DeserializerFactory df)
   {
      log.debug("register: TypeMappingImpl@"  + hashCode() + " [xmlType=" + xmlType + ",javaType=" + javaType.getName() + ",sf=" + sf + ",df=" + df + "]");
      registerInternal(javaType, xmlType, sf, df);
   }

   void registerInternal(Class javaType, QName xmlType, SerializerFactory sf, DeserializerFactory df)
   {
      if (javaType == null)
         throw new IllegalArgumentException("javaType cannot be null for: " + xmlType);
      if (xmlType == null)
         throw new IllegalArgumentException("xmlType cannot be null for: " + javaType);

      KeyPair kPair = new KeyPair(xmlType, javaType);
      FactoryPair fPair = new FactoryPair(sf, df);
      tupleMap.put(kPair, fPair);
   }

   /**
    * Removes the DeserializerFactory registered for the specified pair of Java type and XML data type.
    * @param javaType Class of the Java type
    * @param xmlType QName of the XML type
    * @throws javax.xml.rpc.JAXRPCException If there is error in removing the registered DeserializerFactory
    */
   public void removeDeserializer(Class javaType, QName xmlType)
   {
      FactoryPair fPair = getFactoryPair(xmlType, javaType);
      if (fPair != null)
         fPair.setDeserializerFactory(null);
   }

   /**
    * Removes the SerializerFactory registered for the specified pair of Java type and XML data type.
    * @param javaType Class of the Java type
    * @param xmlType QName of the XML type
    * @throws javax.xml.rpc.JAXRPCException If there is error in removing the registered SerializerFactory
    */
   public void removeSerializer(Class javaType, QName xmlType)
   {
      FactoryPair fPair = getFactoryPair(xmlType, javaType);
      if (fPair != null)
         fPair.setSerializerFactory(null);
   }

   /** Get the list of registered XML types */
   public List<QName> getRegisteredXmlTypes()
   {
      List<QName> types = new ArrayList<QName>();
      for (KeyPair keyPair : getKeyPairs(null, null))
      {
         types.add(keyPair.getXmlType());
      }
      return types;
   }

   /** Get the list of registered Java types */
   public List<Class> getRegisteredJavaTypes()
   {
      List<Class> types = new ArrayList<Class>();
      for (KeyPair keyPair : getKeyPairs(null, null))
      {
         types.add(keyPair.getJavaType());
      }
      return types;
   }

   /** Get the Class that was registered last for this xmlType */
   public Class getJavaType(QName xmlType)
   {
      Class javaType = null;

      List keyPairList = getKeyPairs(xmlType, null);
      int size = keyPairList.size();
      if (size > 0)
      {
         KeyPair kPair = (KeyPair)keyPairList.get(size - 1);
         javaType = kPair.getJavaType();
      }

      return javaType;
   }

   /**
    * Get all of the Classes registered for this xmlType.
    */
   public List<Class> getJavaTypes(QName xmlType)
   {
      List<KeyPair> keyPairList = getKeyPairs(xmlType, null);
      List<Class> classes = new ArrayList<Class>(keyPairList.size());
      
      for (KeyPair current : keyPairList)
      {
         classes.add(current.getJavaType());
      }
      
      return classes;
   }
   
   /**
    * Get the Class that was registered last for this xmlType
    * If there are two Java Types registered for the xmlType
    * return the primitive type rather than the wrapper,
    * if available
    */
   public Class getJavaType(QName xmlType,boolean getPrimitive)
   {
      //Lets get the primitive type if available
      Class javaType = null;

      List keyPairList = getKeyPairs(xmlType, null);
      int size = keyPairList.size();
      if (size == 2 && getPrimitive)
      {
         KeyPair kPair1 = (KeyPair)keyPairList.get(0);
         Class javaType1 = kPair1.getJavaType();
         KeyPair kPair2 = (KeyPair)keyPairList.get(1);
         Class javaType2 = kPair2.getJavaType();
         if(javaType2.isPrimitive() && !javaType1.isPrimitive())
            javaType =  javaType2;
         else
            if(javaType1.isPrimitive() && !javaType2.isPrimitive())
               javaType =  javaType1;
         else
               javaType = javaType2; //Fallback on the most latest
      }
      else
         return getJavaType(xmlType);

      return javaType;
   }

   /** Get the Class name that was registered last for this xmlType */
   public String getJavaTypeName(QName xmlType)
   {
      Class javaType = getJavaType(xmlType);
      return (javaType != null ? javaType.getName() : null);
   }

   /** Get the QName that was registered last for this javaType */
   public QName getXMLType(Class javaType)
   {
      QName xmlType = null;

      List keyPairList = getKeyPairs(null, javaType);
      int size = keyPairList.size();
      if (size > 0)
      {
         KeyPair kPair = (KeyPair)keyPairList.get(size - 1);
         xmlType = kPair.getXmlType();
      }

      return xmlType;
   }

   /**
    * Get the QName that was registered last for this javaType
    * @param javaType class for which XML Type is needed
    * @param tryAssignable  If the xmlType is not registered for javaType
    *                                           should a base class type be checked?
    *
    */
   public QName getXMLType(Class javaType, boolean tryAssignable)
   {
      if(tryAssignable) return getXMLType(javaType);

      QName xmlType = null;

      List keyPairList = getKeyPairs(null, javaType, tryAssignable);
      int size = keyPairList.size();
      if (size > 0)
      {
         KeyPair kPair = (KeyPair)keyPairList.get(size - 1);
         xmlType = kPair.getXmlType();
      }

      return xmlType;
   }

   /**
    * Get the serializer/deserializer factory pair for the given xmlType, javaType
    * Both xmlType, javaType may be null. In that case, this implementation still
    * returns a FactoryPair if there is only one possible match.
    */
   List<KeyPair> getKeyPairs(QName xmlType, Class javaType)
   {
      List<KeyPair> keyPairList = new ArrayList<KeyPair>();

      // Getting the exact matching pair
      if (xmlType != null && javaType != null)
      {
         for (KeyPair entry : tupleMap.keySet())
         {
            if (xmlType.equals(entry.getXmlType()) && entry.getJavaType() == javaType)
            {
               keyPairList.add(entry);
            }
         }
         // No exact match, try assignable
         if (keyPairList.size() == 0)
         {
            for (KeyPair entry : tupleMap.keySet())
            {
               if (xmlType.equals(entry.getXmlType()) && JavaUtils.isAssignableFrom(entry.getJavaType(), javaType))
               {
                  keyPairList.add(entry);
               }
            }
         }
      }

      // Getting the pair for a given xmlType
      else if (xmlType != null && javaType == null)
      {
         for (KeyPair entry : tupleMap.keySet())
         {
            if (xmlType.equals(entry.getXmlType()))
            {
               keyPairList.add(entry);
            }
         }
      }

      // Getting the pair for a given javaType
      else if (xmlType == null && javaType != null)
      {
         for (KeyPair entry : tupleMap.keySet())
         {
            if (entry.getJavaType() == javaType)
            {
               keyPairList.add(entry);
            }
         }
         // No exact match, try assignable
         if (keyPairList.size() == 0)
         {
            for (KeyPair entry : tupleMap.keySet())
            {
               if (JavaUtils.isAssignableFrom(entry.getJavaType(), javaType))
               {
                  keyPairList.add(entry);
               }
            }
         }
      }

      // Getting the all pairs
      else if (xmlType == null && javaType == null)
      {
         keyPairList.addAll(tupleMap.keySet());
      }

      return keyPairList;
   }

   /**
    * Get the serializer/deserializer factory pair for the given xmlType, javaType
    * Both xmlType, javaType may be null. In that case, this implementation still
    * returns a FactoryPair if there is only one possible match.
    * <br>Note: This method does not try for the base class, if no keypair exists for the
    * javaType in question.
    */
   List<KeyPair> getKeyPairs(QName xmlType, Class javaType, boolean tryAssignable)
   {
      if(tryAssignable) return getKeyPairs(  xmlType,   javaType);

      List<KeyPair> keyPairList = new ArrayList<KeyPair>();

      // Getting the exact matching pair
      if (xmlType != null && javaType != null)
      {
         for (KeyPair entry : tupleMap.keySet())
         {
            if (xmlType.equals(entry.getXmlType()) && entry.getJavaType() == javaType)
            {
               keyPairList.add(entry);
            }
         }
      }

      // Getting the pair for a given xmlType
      else if (xmlType != null && javaType == null)
      {
         for (KeyPair entry : tupleMap.keySet())
         {
            if (xmlType.equals(entry.getXmlType()))
            {
               keyPairList.add(entry);
            }
         }
      }

      // Getting the pair for a given javaType
      else if (xmlType == null && javaType != null)
      {
         for (KeyPair entry : tupleMap.keySet())
         {
            if (entry.getJavaType() == javaType)
            {
               keyPairList.add(entry);
            }
         }
      }

      // Getting the all pairs
      else if (xmlType == null && javaType == null)
      {
         keyPairList.addAll(tupleMap.keySet());
      }

      return keyPairList;
   }

   /**
    * Get the serializer/deserializer factory pair for the given xmlType, javaType
    * Both xmlType, javaType may be null. In that case, this implementation still
    * returns a FactoryPair that was last registered
    */
   FactoryPair getFactoryPair(QName xmlType, Class javaType)
   {
      FactoryPair fPair = null;

      List<KeyPair> keyPairList = getKeyPairs(xmlType, javaType);
      int size = keyPairList.size();
      if (size > 0)
      {
         KeyPair kPair = keyPairList.get(size - 1);
         fPair = (FactoryPair)tupleMap.get(kPair);
      }

      return fPair;
   }

   protected void registerStandardLiteralTypes()
   {
      registerInternal(BigDecimal.class, Constants.TYPE_LITERAL_DECIMAL, new SimpleSerializerFactory(), new SimpleDeserializerFactory());

      registerInternal(BigInteger.class, Constants.TYPE_LITERAL_POSITIVEINTEGER, new SimpleSerializerFactory(), new SimpleDeserializerFactory());
      registerInternal(BigInteger.class, Constants.TYPE_LITERAL_NEGATIVEINTEGER, new SimpleSerializerFactory(), new SimpleDeserializerFactory());
      registerInternal(BigInteger.class, Constants.TYPE_LITERAL_NONPOSITIVEINTEGER, new SimpleSerializerFactory(), new SimpleDeserializerFactory());
      registerInternal(BigInteger.class, Constants.TYPE_LITERAL_NONNEGATIVEINTEGER, new SimpleSerializerFactory(), new SimpleDeserializerFactory());
      registerInternal(BigInteger.class, Constants.TYPE_LITERAL_UNSIGNEDLONG, new SimpleSerializerFactory(), new SimpleDeserializerFactory());
      registerInternal(BigInteger.class, Constants.TYPE_LITERAL_INTEGER, new SimpleSerializerFactory(), new SimpleDeserializerFactory());

      registerInternal(Date.class, Constants.TYPE_LITERAL_DATETIME, new DateSerializerFactory(), new DateDeserializerFactory());

      registerInternal(Calendar.class, Constants.TYPE_LITERAL_DATE, new CalendarSerializerFactory(), new CalendarDeserializerFactory());
      registerInternal(Calendar.class, Constants.TYPE_LITERAL_TIME, new CalendarSerializerFactory(), new CalendarDeserializerFactory());
      registerInternal(Calendar.class, Constants.TYPE_LITERAL_DATETIME, new CalendarSerializerFactory(), new CalendarDeserializerFactory());

      registerInternal(QName.class, Constants.TYPE_LITERAL_QNAME, new QNameSerializerFactory(), new QNameDeserializerFactory());

      registerInternal(String.class, Constants.TYPE_LITERAL_ANYSIMPLETYPE, new SimpleSerializerFactory(), new SimpleDeserializerFactory());
      registerInternal(String.class, Constants.TYPE_LITERAL_DURATION, new SimpleSerializerFactory(), new SimpleDeserializerFactory());
      registerInternal(String.class, Constants.TYPE_LITERAL_GDAY, new SimpleSerializerFactory(), new SimpleDeserializerFactory());
      registerInternal(String.class, Constants.TYPE_LITERAL_GMONTH, new SimpleSerializerFactory(), new SimpleDeserializerFactory());
      registerInternal(String.class, Constants.TYPE_LITERAL_GMONTHDAY, new SimpleSerializerFactory(), new SimpleDeserializerFactory());
      registerInternal(String.class, Constants.TYPE_LITERAL_GYEAR, new SimpleSerializerFactory(), new SimpleDeserializerFactory());
      registerInternal(String.class, Constants.TYPE_LITERAL_GYEARMONTH, new SimpleSerializerFactory(), new SimpleDeserializerFactory());
      registerInternal(String.class, Constants.TYPE_LITERAL_ID, new SimpleSerializerFactory(), new SimpleDeserializerFactory());
      registerInternal(String.class, Constants.TYPE_LITERAL_LANGUAGE, new SimpleSerializerFactory(), new SimpleDeserializerFactory());
      registerInternal(String.class, Constants.TYPE_LITERAL_NAME, new SimpleSerializerFactory(), new SimpleDeserializerFactory());
      registerInternal(String.class, Constants.TYPE_LITERAL_NCNAME, new SimpleSerializerFactory(), new SimpleDeserializerFactory());
      registerInternal(String.class, Constants.TYPE_LITERAL_NMTOKEN, new SimpleSerializerFactory(), new SimpleDeserializerFactory());
      registerInternal(String.class, Constants.TYPE_LITERAL_NORMALIZEDSTRING, new SimpleSerializerFactory(), new SimpleDeserializerFactory());
      registerInternal(String.class, Constants.TYPE_LITERAL_TOKEN, new SimpleSerializerFactory(), new SimpleDeserializerFactory());
      registerInternal(String.class, Constants.TYPE_LITERAL_STRING, new SimpleSerializerFactory(), new SimpleDeserializerFactory());

      registerInternal(String[].class, Constants.TYPE_LITERAL_NMTOKENS, new SimpleSerializerFactory(), new SimpleDeserializerFactory());

      registerInternal(URI.class, Constants.TYPE_LITERAL_ANYURI, new SimpleSerializerFactory(), new SimpleDeserializerFactory());

      registerInternal(boolean.class, Constants.TYPE_LITERAL_BOOLEAN, new SimpleSerializerFactory(), new SimpleDeserializerFactory());
      registerInternal(Boolean.class, Constants.TYPE_LITERAL_BOOLEAN, new SimpleSerializerFactory(), new SimpleDeserializerFactory());

      registerInternal(byte.class, Constants.TYPE_LITERAL_BYTE, new SimpleSerializerFactory(), new SimpleDeserializerFactory());
      registerInternal(Byte.class, Constants.TYPE_LITERAL_BYTE, new SimpleSerializerFactory(), new SimpleDeserializerFactory());

      registerInternal(Byte[].class, Constants.TYPE_LITERAL_HEXBINARY, new HexSerializerFactory(), new HexDeserializerFactory());
      registerInternal(byte[].class, Constants.TYPE_LITERAL_HEXBINARY, new HexSerializerFactory(), new HexDeserializerFactory());

      registerInternal(Byte[].class, Constants.TYPE_LITERAL_BASE64BINARY, new Base64SerializerFactory(), new Base64DeserializerFactory());
      registerInternal(byte[].class, Constants.TYPE_LITERAL_BASE64BINARY, new Base64SerializerFactory(), new Base64DeserializerFactory());

      registerInternal(double.class, Constants.TYPE_LITERAL_DOUBLE, new SimpleSerializerFactory(), new SimpleDeserializerFactory());
      registerInternal(Double.class, Constants.TYPE_LITERAL_DOUBLE, new SimpleSerializerFactory(), new SimpleDeserializerFactory());

      registerInternal(float.class, Constants.TYPE_LITERAL_FLOAT, new SimpleSerializerFactory(), new SimpleDeserializerFactory());
      registerInternal(Float.class, Constants.TYPE_LITERAL_FLOAT, new SimpleSerializerFactory(), new SimpleDeserializerFactory());

      registerInternal(int.class, Constants.TYPE_LITERAL_UNSIGNEDSHORT, new SimpleSerializerFactory(), new SimpleDeserializerFactory());
      registerInternal(Integer.class, Constants.TYPE_LITERAL_UNSIGNEDSHORT, new SimpleSerializerFactory(), new SimpleDeserializerFactory());
      registerInternal(int.class, Constants.TYPE_LITERAL_INT, new SimpleSerializerFactory(), new SimpleDeserializerFactory());
      registerInternal(Integer.class, Constants.TYPE_LITERAL_INT, new SimpleSerializerFactory(), new SimpleDeserializerFactory());

      registerInternal(long.class, Constants.TYPE_LITERAL_UNSIGNEDINT, new SimpleSerializerFactory(), new SimpleDeserializerFactory());
      registerInternal(Long.class, Constants.TYPE_LITERAL_UNSIGNEDINT, new SimpleSerializerFactory(), new SimpleDeserializerFactory());
      registerInternal(long.class, Constants.TYPE_LITERAL_LONG, new SimpleSerializerFactory(), new SimpleDeserializerFactory());
      registerInternal(Long.class, Constants.TYPE_LITERAL_LONG, new SimpleSerializerFactory(), new SimpleDeserializerFactory());

      registerInternal(short.class, Constants.TYPE_LITERAL_UNSIGNEDBYTE, new SimpleSerializerFactory(), new SimpleDeserializerFactory());
      registerInternal(Short.class, Constants.TYPE_LITERAL_UNSIGNEDBYTE, new SimpleSerializerFactory(), new SimpleDeserializerFactory());
      registerInternal(short.class, Constants.TYPE_LITERAL_SHORT, new SimpleSerializerFactory(), new SimpleDeserializerFactory());
      registerInternal(Short.class, Constants.TYPE_LITERAL_SHORT, new SimpleSerializerFactory(), new SimpleDeserializerFactory());
   }

   /** A tuple of the type {XML typeQName, Java Class, SerializerFactory, DeserializerFactory}.
    */
   public static class KeyPair
   {
      private QName xmlType;
      private Class javaType;

      public KeyPair(QName xmlType, Class javaType)
      {
         this.javaType = javaType;
         this.xmlType = xmlType;
      }

      public Class getJavaType()
      {
         return javaType;
      }

      public QName getXmlType()
      {
         return xmlType;
      }

      public boolean equals(Object o)
      {
         if (this == o) return true;
         if (!(o instanceof KeyPair)) return false;

         final KeyPair keyPair = (KeyPair)o;

         if (!javaType.equals(keyPair.javaType)) return false;
         if (!xmlType.equals(keyPair.xmlType)) return false;

         return true;
      }

      public int hashCode()
      {
         int result;
         result = xmlType.hashCode();
         result = 29 * result + javaType.hashCode();
         return result;
      }

      public String toString()
      {
         return "[xmlType=" + xmlType + ",javaType=" + javaType.getName() + "]";
      }
   }

   /** A tuple of the type {XML typeQName, Java Class, SerializerFactory, DeserializerFactory}.
    */
   public static class FactoryPair
   {
      private SerializerFactory serializerFactory;
      private DeserializerFactory deserializerFactory;

      FactoryPair(SerializerFactory sf, DeserializerFactory df)
      {
         this.deserializerFactory = df;
         this.serializerFactory = sf;
      }

      public DeserializerFactory getDeserializerFactory()
      {
         return deserializerFactory;
      }

      public SerializerFactory getSerializerFactory()
      {
         return serializerFactory;
      }

      public void setDeserializerFactory(DeserializerFactory df)
      {
         this.deserializerFactory = df;
      }

      public void setSerializerFactory(SerializerFactory sf)
      {
         this.serializerFactory = sf;
      }
   }
}
