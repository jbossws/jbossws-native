/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the JBPM BPEL PUBLIC LICENSE AGREEMENT as
 * published by JBoss Inc.; either version 1.0 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package org.jboss.ws.core.jaxws;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import javax.xml.rpc.encoding.TypeMapping;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;

import org.jboss.logging.Logger;
import org.jboss.ws.Constants;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.jaxrpc.SOAPFaultHelperJAXRPC;
import org.jboss.ws.core.jaxrpc.binding.BindingException;
import org.jboss.ws.core.jaxrpc.binding.DeserializerFactoryBase;
import org.jboss.ws.core.jaxrpc.binding.DeserializerSupport;
import org.jboss.ws.core.jaxrpc.binding.SerializationContext;
import org.jboss.ws.core.jaxrpc.binding.SerializerFactoryBase;
import org.jboss.ws.core.jaxrpc.binding.SerializerSupport;
import org.jboss.ws.core.soap.MessageContextAssociation;
import org.jboss.ws.core.soap.NameImpl;
import org.jboss.ws.core.soap.SOAPFactoryImpl;
import org.jboss.ws.core.utils.DOMUtils;
import org.jboss.ws.core.utils.DOMWriter;
import org.jboss.ws.metadata.umdm.FaultMetaData;
import org.jboss.ws.metadata.umdm.OperationMetaData;
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:alex.guizar@jboss.com">Alejandro Guizar</a>
 * @version $Revision$
 */
public class SOAPFaultHelperJAXWS
{
   // provide logging
   private static Logger log = Logger.getLogger(SOAPFaultHelperJAXWS.class);

   private static final Collection<String> excludedGetters = Arrays.asList(new String[] { "getCause", "getLocalizedMessage", "getStackTrace", "getClass" });

   /** Factory method for FaultException for a given SOAPFault */
   public static SOAPFaultException getSOAPFaultException(SOAPFault soapFault)
   {
      SOAPFaultException faultEx = new SOAPFaultException(soapFault);
      Detail detail = soapFault.getDetail();

      CommonMessageContext msgContext = MessageContextAssociation.peekMessageContext();
      if (detail != null && msgContext != null)
      {
         SerializationContext serContext = msgContext.getSerializationContext();
         TypeMapping typeMapping = serContext.getTypeMapping();

         Iterator it = detail.getDetailEntries();
         while (it.hasNext())
         {
            DetailEntry deElement = (DetailEntry)it.next();
            QName xmlName = new QName(deElement.getNamespaceURI(), deElement.getLocalName());

            OperationMetaData opMetaData = msgContext.getOperationMetaData();
            FaultMetaData faultMetaData = opMetaData.getFault(xmlName);
            if (faultMetaData != null)
            {
               log.debug("Deserialize fault: " + faultMetaData);
               QName xmlType = faultMetaData.getXmlType();
               Class<?> faultBeanClass = faultMetaData.getFaultBean();

               // Get the deserializer from the type mapping
               DeserializerFactoryBase desFactory = (DeserializerFactoryBase)typeMapping.getDeserializer(faultBeanClass, xmlType);
               if (desFactory == null)
                  throw new WebServiceException("Cannot obtain deserializer factory: xmlType=" + xmlType + ", javaType=" + faultBeanClass);

               // http://jira.jboss.org/jira/browse/JBWS-955
               // Cannot deserialize fault detail
               String prefix = deElement.getPrefix();
               if (prefix.length() > 0)
               {
                  String nsURI = deElement.getNamespaceURI();
                  String attrValue = deElement.getAttribute("xmlns:" + prefix);
                  if (nsURI.length() > 0 && attrValue.length() == 0)
                  {
                     try
                     {
                        deElement.addNamespaceDeclaration(prefix, nsURI);
                     }
                     catch (SOAPException e)
                     {
                        log.warn("Declaration of detail entry namespace failed", e);
                     }
                  }
               }               
               
               // Try jaxb deserialization
               try
               {
                  String xmlFragment = DOMWriter.printNode(deElement, false);
                  DeserializerSupport des = (DeserializerSupport)desFactory.getDeserializer();
                  Object faultBean = des.deserialize(xmlName, xmlType, xmlFragment, serContext);

                  /* JAX-WS 2.5: A wsdl:fault element refers to a wsdl:message that contains
                   * a single part. The global element declaration referred to by that part
                   * is mapped to a Java bean. A wrapper exception class contains the 
                   * following methods: 
                   * - WrapperException(String message, FaultBean faultInfo)
                   * - WrapperException(String message, FaultBean faultInfo, Throwable cause)
                   * - FaultBean getFaultInfo() */
                  Class<?> serviceExGenericClass = faultMetaData.getJavaType();
                  Class<? extends Exception> serviceExClass = serviceExGenericClass.asSubclass(Exception.class);
                  
                  Exception serviceEx;
                  try
                  {
                     Constructor<? extends Exception> serviceExCtor = serviceExClass.getConstructor(String.class, faultBeanClass);
                     serviceEx = serviceExCtor.newInstance(soapFault.getFaultString(), faultBean);
                  }
                  catch (NoSuchMethodException e)
                  {
                     serviceEx = toServiceException(faultBean, serviceExClass);
                  }
                  catch (InstantiationException e)
                  {
                     throw new WebServiceException("Service specific exception class is not instantiable", e);
                  }

                  faultEx.initCause(serviceEx);
               }
               catch (BindingException e)
               {
                  throw new WebServiceException(e);
               }
               catch (IllegalAccessException e)
               {
                  throw new WebServiceException(e);
               }
               catch (InvocationTargetException e)
               {
                  throw new WebServiceException(e.getTargetException());
               }
            }
            else
            {
               log.debug("Cannot find fault meta data for: " + xmlName);
            }
         }
      }

      return faultEx;
   }

   private static Exception toServiceException(Object faultBean, Class<? extends Exception> serviceExClass) throws IllegalAccessException, InvocationTargetException
   {
      Class<?> faultBeanClass = faultBean.getClass();
      XmlType xmlType = faultBeanClass.getAnnotation(XmlType.class);

      if (xmlType == null)
         throw new WebServiceException("@XmlType annotation missing from fault bean class: " + faultBeanClass.getName());

      String[] propertyNames = xmlType.propOrder();
      Class<?>[] propertyTypes = new Class<?>[propertyNames.length];
      Object[] propertyValues = new Object[propertyNames.length];

      for (int i = 0; i < propertyNames.length; i++)
      {
         String propertyName = propertyNames[i];
         propertyName = Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);

         Method propertyGetter;
         try
         {
            propertyGetter = faultBeanClass.getMethod("get" + propertyName);
         }
         catch (NoSuchMethodException e)
         {
            try
            {
               propertyGetter = faultBeanClass.getMethod("is" + propertyName);
            }
            catch (NoSuchMethodException ee)
            {
               throw new WebServiceException("Fault bean has no getter for property: " + propertyName, ee);
            }
         }
         propertyValues[i] = propertyGetter.invoke(faultBean);
         propertyTypes[i] = propertyGetter.getReturnType();
      }

      try
      {
         Constructor<? extends Exception> serviceExCtor = serviceExClass.getConstructor(propertyTypes);
         return serviceExCtor.newInstance(propertyValues);
      }
      catch (NoSuchMethodException e)
      {
         throw new WebServiceException("Service exception has no constructor for parameter types: " + Arrays.toString(propertyTypes));
      }
      catch (InstantiationException e)
      {
         throw new WebServiceException("Service exception is not instantiable", e);
      }
   }

   /** Translate the request exception into a SOAPFault message. */
   public static SOAPMessage exceptionToFaultMessage(Exception reqEx)
   {
      log.error("SOAP request exception", reqEx);

      try
      {
         SOAPMessage faultMessage;
         if (reqEx instanceof SOAPFaultException)
         {
            faultMessage = toSOAPMessage((SOAPFaultException)reqEx);
         }
         else if (reqEx instanceof javax.xml.rpc.soap.SOAPFaultException)
         {
            /* this exception should not occur in JAX-WS endpoints, but JBossWS
             * throws it to signal internal error conditions */
            faultMessage = SOAPFaultHelperJAXRPC.exceptionToFaultMessage(reqEx);
         }
         else
         {
            faultMessage = toSOAPMessage(reqEx);
         }

         return faultMessage;
      }
      catch (SOAPException ex)
      {
         log.error("Error creating SOAPFault message", ex);
         throw new WebServiceException("Cannot create SOAPFault message for: " + reqEx);
      }
   }

   private static SOAPMessage toSOAPMessage(SOAPFaultException faultEx) throws SOAPException
   {
      MessageFactory factory = MessageFactory.newInstance();
      SOAPMessage soapMessage = factory.createMessage();

      SOAPBody soapBody = soapMessage.getSOAPBody();
      populateSOAPFault(soapBody, faultEx);

      /* detail
       * X. Serialized service specific exception
       * 2. SOAPFaultException.getFault().getDetail() */
      Detail detail = faultEx.getFault().getDetail();
      if (detail != null)
         soapBody.getFault().addChildElement(detail);

      return soapMessage;
   }

   private static void populateSOAPFault(SOAPBody soapBody, SOAPFaultException faultEx) throws SOAPException
   {
      SOAPFault sourceFault = faultEx.getFault();

      /* JAX-WS 10.2.2.3: the fields of the fault message are populated according to the
       * following rules of precedence: */

      /* faultcode
       * 1. SOAPFaultException.getFault().getFaultCodeAsQName()
       * X. env:Server (Subcode omitted for SOAP 1.2) */
      Name faultCode = sourceFault.getFaultCodeAsName();
      if (faultCode != null)
      {
         faultCode = new NameImpl(faultCode.getLocalName(), "codeNS", faultCode.getURI());
      }
      else
      {
         faultCode = getFallbackFaultCode();
      }

      /* faultstring
       * 1. SOAPFaultException.getFault().getFaultString()
       * X. Exception.getMessage()
       * X. Exception.toString() */
      String faultString = sourceFault.getFaultString();
      if (faultString == null)
         faultString = getFallbackFaultString(faultEx);

      SOAPFault targetFault = soapBody.addFault(faultCode, faultString);

      /* faultactor
       * 1. SOAPFaultException.getFault().getFaultActor()
       * 2. Empty */
      String faultActor = sourceFault.getFaultActor();
      if (faultActor != null)
         targetFault.setFaultActor(faultActor);
   }

   private static SOAPMessage toSOAPMessage(Exception ex) throws SOAPException
   {
      MessageFactory factory = MessageFactory.newInstance();
      SOAPMessage soapMessage = factory.createMessage();

      SOAPBody soapBody = soapMessage.getSOAPBody();
      SOAPFault soapFault;

      /* JAX-WS 6.4.1: When an implementation catches an exception thrown by a
       * service endpoint implementation and the cause of that exception is an
       * instance of the appropriate ProtocolException subclass for the protocol
       * in use, an implementation MUST reflect the information contained in the
       * ProtocolException subclass within the generated protocol level fault. */
      Throwable cause = ex.getCause();
      if (cause instanceof SOAPFaultException)
      {
         populateSOAPFault(soapBody, (SOAPFaultException)cause);
         soapFault = soapBody.getFault();
      }
      else
      {
         soapFault = soapBody.addFault(getFallbackFaultCode(), getFallbackFaultString(ex));
      }

      CommonMessageContext msgContext = MessageContextAssociation.peekMessageContext();
      SerializationContext serContext = msgContext.getSerializationContext();

      NameImpl faultCode = (NameImpl)soapFault.getFaultCodeAsName();
      if (faultCode.getURI().length() > 0)
         serContext.getNamespaceRegistry().registerQName(faultCode.toQName());

      OperationMetaData opMetaData = msgContext.getOperationMetaData();
      Class<? extends Exception> exClass = ex.getClass();
      if (opMetaData != null && opMetaData.getFault(exClass) != null)
      {
         FaultMetaData faultMetaData = opMetaData.getFault(exClass);
         Class faultBeanClass = faultMetaData.getFaultBean();
         Object faultBean;
         try
         {
            try
            {
               /* JAX-WS 3.7: For exceptions that match the pattern described in section
                * 2.5 (i.e. exceptions that have a getFaultInfo method), the FaultBean
                * is used as input to JAXB */
               Method getFaultInfo = exClass.getMethod("getFaultInfo");
               faultBean = getFaultInfo.invoke(ex);
            }
            catch (NoSuchMethodException e)
            {
               /* JAX-WS 3.7: For exceptions that do not match the pattern described in
                * section 2.5, JAX-WS maps those exceptions to Java beans and then uses
                * those Java beans as input to the JAXB mapping. */
               faultBean = toFaultBean(ex, faultBeanClass);
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

         Detail detail = soapFault.addDetail();
         SOAPElement detailEntry = toDetailEntry(faultBean, serContext, faultMetaData);
         detail.addChildElement(detailEntry);
      }
      else
      {
         log.debug("Cannot obtain fault meta data for: " + exClass);
      }

      return soapMessage;
   }

   private static Name getFallbackFaultCode()
   {
      /* faultcode
       * X. SOAPFaultException.getFault().getFaultCodeAsQName()
       * 2. env:Server (Subcode omitted for SOAP 1.2) */
      return new NameImpl(Constants.SOAP11_FAULT_CODE_SERVER);
   }

   private static String getFallbackFaultString(Exception ex)
   {
      /* faultstring
       * X. SOAPFaultException.getFault().getFaultString()
       * 2. Exception.getMessage()
       * 3. Exception.toString() */
      String faultString = ex.getMessage();

      if (faultString == null)
         faultString = ex.toString();

      return faultString;
   }

   private static Object toFaultBean(Exception userEx, Class faultBeanClass) throws IllegalAccessException, InvocationTargetException
   {
      Object faultBean;
      try
      {
         faultBean = faultBeanClass.newInstance();
      }
      catch (InstantiationException e)
      {
         throw new WebServiceException("Fault bean class is not instantiable", e);
      }

      /* For each getter in the exception and its superclasses, a property of
       * the same type and name is added to the bean. The getCause, getLocalizedMessage
       * and getStackTrace getters from java.lang.Throwable and the getClass getter
       * from java.lang.Object are excluded from the list of getters to be mapped. */
      for (Method exMethod : userEx.getClass().getMethods())
      {
         if (exMethod.getParameterTypes().length > 0)
            continue;

         String exMethodName = exMethod.getName();

         if (excludedGetters.contains(exMethodName))
            continue;

         String propertyName;
         if (exMethodName.startsWith("get"))
         {
            propertyName = exMethodName.substring(3);
         }
         else if (exMethodName.startsWith("is"))
         {
            propertyName = exMethodName.substring(2);
         }
         else continue;

         // get the property value from the exception
         Object propertyValue = exMethod.invoke(userEx);
         Class propertyClass = exMethod.getReturnType();

         try
         {
            // set the value to the bean
            Method beanSetter = faultBeanClass.getMethod("set" + propertyName, propertyClass);
            beanSetter.invoke(faultBean, propertyValue);
         }
         catch (NoSuchMethodException e)
         {
            throw new WebServiceException("Fault bean has no setter for property: " + propertyName, e);
         }
      }

      return faultBean;
   }

   private static SOAPElement toDetailEntry(Object faultObject, SerializationContext serContext, FaultMetaData faultMetaData) throws SOAPException
   {
      QName xmlName = faultMetaData.getXmlName();
      xmlName = serContext.getNamespaceRegistry().registerQName(xmlName);

      // Get the serializer from the type mapping
      QName xmlType = faultMetaData.getXmlType();
      Class javaType = faultMetaData.getFaultBean() != null ? faultMetaData.getFaultBean() : faultMetaData.getJavaType();
      SerializerFactoryBase serFactory = (SerializerFactoryBase)serContext.getTypeMapping().getSerializer(javaType, xmlType);
      if (serFactory == null)
         throw new WebServiceException("Cannot obtain serializer factory: xmlType=" + xmlType + ", javaType=" + javaType);

      try
      {
         SerializerSupport ser = serFactory.getSerializer();
         String xmlFragment = ser.serialize(xmlName, xmlType, faultObject, serContext, null);

         SOAPFactoryImpl soapFactory = new SOAPFactoryImpl();
         Element domElement = DOMUtils.parse(xmlFragment);
         return soapFactory.createElement(domElement);
      }
      catch (BindingException e)
      {
         throw new WebServiceException(e);
      }
      catch (IOException e)
      {
         throw new WebServiceException(e);
      }
   }
}
