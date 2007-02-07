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

import org.jboss.logging.Logger;
import org.jboss.ws.Constants;
import org.jboss.ws.core.CommonMessageContext;
import org.jboss.ws.core.jaxrpc.SOAPFaultHelperJAXRPC;
import org.jboss.ws.core.jaxrpc.binding.*;
import org.jboss.ws.core.soap.MessageContextAssociation;
import org.jboss.ws.core.soap.NameImpl;
import org.jboss.ws.core.soap.SOAPFactoryImpl;
import org.jboss.ws.core.soap.XMLFragment;
import org.jboss.ws.core.utils.DOMUtils;
import org.jboss.ws.metadata.umdm.FaultMetaData;
import org.jboss.ws.metadata.umdm.OperationMetaData;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.rpc.encoding.TypeMapping;
import javax.xml.soap.*;
import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.IOException;
import java.util.Iterator;

/**
 * Helper methods to translate between SOAPFault and SOAPFaultException
 * as well as between Exception and SOAPMessage containing a fault.
 * @author <a href="mailto:alex.guizar@jboss.com">Alejandro Guizar</a>
 * @version $Revision$
 */
public class SOAPFaultHelperJAXWS
{
   // provide logging
   private static Logger log = Logger.getLogger(SOAPFaultHelperJAXWS.class);

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
               if(log.isDebugEnabled()) log.debug("Deserialize fault: " + faultMetaData);
               QName xmlType = faultMetaData.getXmlType();
               Class<?> faultBeanClass = faultMetaData.getFaultBean();

               // Get the deserializer from the type mapping
               DeserializerFactoryBase desFactory = (DeserializerFactoryBase)typeMapping.getDeserializer(faultBeanClass, xmlType);
               if (desFactory == null)
                  throw new WebServiceException("Cannot obtain deserializer factory: xmlType=" + xmlType + ", javaType=" + faultBeanClass);

               // http://jira.jboss.org/jira/browse/JBWS-955
               // Cannot deserialize fault detail
               String prefix = deElement.getPrefix();
               if (prefix != null && prefix.length() > 0)
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
                  Class[] types = opMetaData.getEndpointMetaData().getRegisteredTypes().toArray(new Class[0]);
                  serContext.setProperty(SerializationContext.CONTEXT_TYPES, types);

                  Source source = new DOMSource(deElement);
                  DeserializerSupport des = (DeserializerSupport)desFactory.getDeserializer();
                  Object faultBean = des.deserialize(xmlName, xmlType, source, serContext);

                  Exception serviceEx = faultMetaData.toServiceException(faultBean, soapFault.getFaultString());
                  faultEx.initCause(serviceEx);
               }
               catch (BindingException e)
               {
                  throw new WebServiceException(e);
               }
            }
            else
            {
               if(log.isDebugEnabled()) log.debug("Cannot find fault meta data for: " + xmlName);
            }
         }
      }

      return faultEx;
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
         Object faultBean = faultMetaData.toFaultBean(ex);

         Detail detail = soapFault.addDetail();
         SOAPElement detailEntry = toDetailEntry(faultBean, serContext, faultMetaData);
         detail.addChildElement(detailEntry);
      }
      else
      {
         if(log.isDebugEnabled()) log.debug("Cannot obtain fault meta data for: " + exClass);
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
         Result result = ser.serialize(xmlName, xmlType, faultObject, serContext, null);
         XMLFragment fragment = new XMLFragment(result);

         SOAPFactoryImpl soapFactory = new SOAPFactoryImpl();
         Element domElement = DOMUtils.parse(fragment.toStringFragment());
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
