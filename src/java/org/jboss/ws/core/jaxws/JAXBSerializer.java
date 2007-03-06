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
package org.jboss.ws.core.jaxws;

// $Id$

import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.ws.WebServiceException;

import org.jboss.logging.Logger;
import org.jboss.ws.core.jaxrpc.TypeMappingImpl;
import org.jboss.ws.core.jaxrpc.binding.BindingException;
import org.jboss.ws.core.jaxrpc.binding.BufferedStreamResult;
import org.jboss.ws.core.jaxrpc.binding.ComplexTypeSerializer;
import org.jboss.ws.core.jaxrpc.binding.SerializationContext;
import org.jboss.ws.core.utils.JavaUtils;
import org.jboss.ws.extensions.xop.jaxws.AttachmentMarshallerImpl;
import org.jboss.ws.metadata.umdm.ParameterMetaData;
import org.w3c.dom.NamedNodeMap;
import com.sun.org.apache.xalan.internal.xsltc.runtime.Parameter;

/**
 * A Serializer that can handle complex types by delegating to JAXB.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 04-Dec-2004
 */
public class JAXBSerializer extends ComplexTypeSerializer
{
   // provide logging
   private static final Logger log = Logger.getLogger(JAXBSerializer.class);

   public JAXBSerializer() throws BindingException
   {
   }

   @Override
   public Result serialize(QName xmlName, QName xmlType, Object value, SerializationContext serContext, NamedNodeMap attributes) throws BindingException
   {
      if(log.isDebugEnabled()) log.debug("serialize: [xmlName=" + xmlName + ",xmlType=" + xmlType + "]");

      Result result = null;
      try
      {
         // The PMD contains the base type, which is needed for JAXB to marshall xsi:type correctly.
         // This should be more efficient and accurate than searching the type mapping
         ParameterMetaData pmd = (ParameterMetaData)serContext.getProperty(ParameterMetaData.class.getName());
         Class expectedType = pmd.getJavaType();
         Class actualType = value.getClass();

         //JAXBContextCache contextCache = JAXBContextCache.getContextCache();
         //JAXBContext jaxbContext = contextCache.getInstance(expectedType, actualType);

         Class[] types = shouldFilter(actualType) ? new Class[]{expectedType} : new Class[]{expectedType, actualType};
         JAXBContext jaxbContext = JAXBContext.newInstance(types);
         
         Marshaller marshaller = jaxbContext.createMarshaller();

         marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
         marshaller.setAttachmentMarshaller(new AttachmentMarshallerImpl());

         // It's safe to pass a stream result, because the SCE will always be in XML_VALID state afterwards.
         // This state can safely be written to an outstream. See XMLFragment and XMLContent as well.
         result = new BufferedStreamResult();
         marshaller.marshal(new JAXBElement(xmlName, expectedType, value), result);

         if(log.isDebugEnabled()) log.debug("serialized: " + result);
      }
      catch (Exception ex)
      {
         handleMarshallException(ex);
      }

      return result;
   }

   // Remove this when we add a XMLGregorianCalendar Serializer
   private boolean shouldFilter(Class<?> actualType)
   {
      return XMLGregorianCalendar.class.isAssignableFrom(actualType);
   }

   // 4.21 Conformance (Marshalling failure): If an error occurs when using the supplied JAXBContext to marshall
   // a request or unmarshall a response, an implementation MUST throw a WebServiceException whose
   // cause is set to the original JAXBException.
   private void handleMarshallException(Exception ex)
   {
      if (ex instanceof WebServiceException)
         throw (WebServiceException)ex;

      throw new WebServiceException(ex);
   }
}
