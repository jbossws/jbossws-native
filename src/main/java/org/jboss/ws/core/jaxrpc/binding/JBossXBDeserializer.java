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

import java.io.ByteArrayInputStream;

import javax.xml.namespace.QName;

import org.apache.xerces.xs.XSModel;
import org.jboss.logging.Logger;
import org.jboss.ws.core.jaxrpc.SerializationContextJAXRPC;
import org.jboss.ws.core.jaxrpc.binding.jbossxb.JBossXBConstants;
import org.jboss.ws.core.jaxrpc.binding.jbossxb.JBossXBUnmarshaller;
import org.jboss.ws.core.jaxrpc.binding.jbossxb.JBossXBUnmarshallerImpl;
import org.jboss.ws.metadata.jaxrpcmapping.JavaWsdlMapping;

/**
 * A Deserializer that can handle complex types by delegating to JAXB.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 04-Dec-2004
 */
public class JBossXBDeserializer extends ComplexTypeDeserializer
{
   // provide logging
   private static final Logger log = Logger.getLogger(JBossXBDeserializer.class);

   private JBossXBUnmarshaller unmarshaller;

   public JBossXBDeserializer() throws BindingException
   {
      // Get the JAXB marshaller for complex objects
      unmarshaller = new JBossXBUnmarshallerImpl();
   }

   /**
    * For unmarshalling the WS layer passes to the JAXB layer
    *
    *    - required self contained xml content
    *    - required map of packaged or generated XSDSchema
    *    - optional QName of the root complex type
    *    - optional instance of JavaWsdlMapping
    *
    * The xmlType is redundant if the root element name corresponds to a global element definition in schema.
    * If the java mapping is null, default mapping rules apply.
    *
    * The result is an object instance or null.
    * In case of an unmarshalling problem a descriptive exception is thrown.
    */
   public Object deserialize(QName xmlName, QName xmlType, String val, SerializationContext serContext) throws BindingException
   {
      log.debug("deserialize: [xmlName=" + xmlName + ",xmlType=" + xmlType + "]");

      // Expect the specific JAXRPC serialization context
      SerializationContextJAXRPC jaxrpcContext = (SerializationContextJAXRPC)serContext;

      Object value = null;
      String typeName = xmlType.getLocalPart();

      try
      {
         // Get the parsed model
         XSModel model = jaxrpcContext.getXsModel();

         // Get the jaxrpc-mapping.xml meta data
         JavaWsdlMapping jaxrpcMapping = jaxrpcContext.getJavaWsdlMapping();

         unmarshaller.setProperty(JBossXBConstants.JBXB_XS_MODEL, model);
         unmarshaller.setProperty(JBossXBConstants.JBXB_ROOT_QNAME, xmlName);
         unmarshaller.setProperty(JBossXBConstants.JBXB_TYPE_QNAME, xmlType);
         unmarshaller.setProperty(JBossXBConstants.JBXB_JAVA_MAPPING, jaxrpcMapping);

         ByteArrayInputStream ins = new ByteArrayInputStream(val.getBytes("UTF-8"));
         value = unmarshaller.unmarshal(ins);
      }
      catch (RuntimeException rte)
      {
         throw rte;
      }
      catch (Exception ex)
      {
         throw new BindingException(ex);
      }

      log.debug("deserialized: " + (value != null ? value.getClass().getName() : null));
      return value;

   }
}
