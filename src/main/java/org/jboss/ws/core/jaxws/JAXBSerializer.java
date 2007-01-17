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

import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;

import org.jboss.logging.Logger;
import org.jboss.ws.core.jaxrpc.TypeMappingImpl;
import org.jboss.ws.core.jaxrpc.binding.BindingException;
import org.jboss.ws.core.jaxrpc.binding.ComplexTypeSerializer;
import org.jboss.ws.core.jaxrpc.binding.SerializationContext;
import org.jboss.ws.core.utils.JavaUtils;
import org.jboss.ws.extensions.xop.jaxws.AttachmentMarshallerImpl;
import org.w3c.dom.NamedNodeMap;

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
   public String serialize(QName xmlName, QName xmlType, Object value, SerializationContext serContext, NamedNodeMap attributes) throws BindingException
   {
      log.debug("serialize: [xmlName=" + xmlName + ",xmlType=" + xmlType + "]");

      String xmlFragment = null;
      try
      {
         TypeMappingImpl typeMapping = serContext.getTypeMapping();

         Class javaType = null;
         List<Class> possibleJavaTypes = typeMapping.getJavaTypes(xmlType);
         if(possibleJavaTypes.size()>1)
         {
             // resolve java type by assignability
            for(Class type : possibleJavaTypes)
            {
               if(JavaUtils.isAssignableFrom(type, value.getClass()))
               {
                  javaType = type;
                  break;
               }
            }
         }
         else
         {
            javaType = typeMapping.getJavaType(xmlType);
         }

         if(null == javaType)
            throw new Exception("Unable to resolve target java type");

         JAXBContext jaxbContext = JAXBContext.newInstance(javaType);
         Marshaller marshaller = jaxbContext.createMarshaller();

         marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
         marshaller.setAttachmentMarshaller(new AttachmentMarshallerImpl());

         StringWriter strwr = new StringWriter();
         marshaller.marshal(new JAXBElement(xmlName, javaType, value), strwr);
         xmlFragment = strwr.toString();

         log.debug("serialized: " + xmlFragment);
      }
      catch (Exception ex)
      {
         handleMarshallException(ex);
      }
      return xmlFragment;
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
