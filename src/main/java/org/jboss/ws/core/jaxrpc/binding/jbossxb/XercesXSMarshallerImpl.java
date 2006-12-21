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
package org.jboss.ws.core.jaxrpc.binding.jbossxb;

// $Id$

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.xerces.xs.XSModel;
import org.jboss.logging.Logger;
import org.jboss.util.NotImplementedException;
import org.jboss.ws.WSException;
import org.jboss.ws.core.utils.JavaUtils;
import org.jboss.ws.metadata.jaxrpcmapping.JavaWsdlMapping;
import org.jboss.ws.metadata.jaxrpcmapping.JavaXmlTypeMapping;
import org.jboss.ws.metadata.jaxrpcmapping.VariableMapping;
import org.jboss.xb.binding.Constants;
import org.jboss.xb.binding.MappingObjectModelProvider;
import org.jboss.xb.binding.XercesXsMarshaller;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

/**
 * An implementation of a JAXB Marshaller that user XercesXSMarshaller impl.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 18-Oct-2004
 */
public class XercesXSMarshallerImpl implements JBossXBMarshaller
{

   // provide logging
   private static final Logger log = Logger.getLogger(XercesXSMarshallerImpl.class);

   // The marshaller properties
   private HashMap properties = new HashMap();

   private XercesXsMarshaller delegate;

   public XercesXSMarshallerImpl()
   {
      delegate = new XercesXsMarshaller();
      delegate.setProperty(XercesXsMarshaller.PROP_OUTPUT_XML_VERSION, "false");
      delegate.setProperty(XercesXsMarshaller.PROP_OUTPUT_INDENTATION, "false");
      delegate.declareNamespace("xsi", Constants.NS_XML_SCHEMA_INSTANCE);
      delegate.setSupportNil(true);
      delegate.setSimpleContentProperty("_value");
   }

   /**
    * Marshal the content tree rooted at obj into a Writer.
    */
   public void marshal(Object obj, Writer writer) throws MarshalException
   {
      assertRequiredProperties();

      try
      {
         QName xmlName = (QName)getProperty(JBossXBConstants.JBXB_ROOT_QNAME);
         delegate.addRootElement(xmlName);

         QName xmlType = (QName)getProperty(JBossXBConstants.JBXB_TYPE_QNAME);
         if (xmlType != null)
         {
            delegate.setRootTypeQName(xmlType);
         }

         if (xmlName.getNamespaceURI().length() > 0)
         {
            String prefix = xmlName.getPrefix();
            String nsURI = xmlName.getNamespaceURI();
            delegate.declareNamespace(prefix, nsURI);
         }

         MappingObjectModelProvider provider = new MappingObjectModelProvider();
         provider.setIgnoreLowLine(false);
         provider.setIgnoreNotFoundField(false);

         // todo complete wsdl mapping merge
         JavaWsdlMapping wsdlMapping = (JavaWsdlMapping)getProperty(JBossXBConstants.JBXB_JAVA_MAPPING);
         if (wsdlMapping != null)
         {
            JavaXmlTypeMapping[] javaXmlMappings = wsdlMapping.getJavaXmlTypeMappings();
            if (javaXmlMappings != null)
            {
               for (int i = 0; i < javaXmlMappings.length; ++i)
               {
                  JavaXmlTypeMapping javaXmlMapping = javaXmlMappings[i];
                  VariableMapping[] variableMappings = javaXmlMapping.getVariableMappings();
                  if (variableMappings != null)
                  {
                     String clsName = javaXmlMapping.getJavaType();
                     Class cls = JavaUtils.loadJavaType(clsName, Thread.currentThread().getContextClassLoader());
                     QName clsQName = javaXmlMapping.getRootTypeQName();

                     if (clsQName != null)
                     {
                        if ("element".equals(javaXmlMapping.getQnameScope()))
                        {
                           delegate.mapClassToGlobalElement(cls, clsQName.getLocalPart(), clsQName.getNamespaceURI(), null, provider);
                        }
                        else
                        {
                           delegate.mapClassToGlobalType(cls, clsQName.getLocalPart(), clsQName.getNamespaceURI(), null, provider);
                           delegate.mapClassToXsiType(cls, clsQName.getNamespaceURI(), clsQName.getLocalPart());
                        }
                     }

                     for (int j = 0; j < variableMappings.length; ++j)
                     {
                        VariableMapping variableMapping = variableMappings[j];
                        String javaName = variableMapping.getJavaVariableName();
                        if (variableMapping.getXmlElementName() != null)
                        {
                           String xmlElementName = variableMapping.getXmlElementName();
                           provider.mapFieldToElement(cls, javaName, "", xmlElementName, null);
                        }
                        else if (variableMapping.getXmlAttributeName() != null)
                        {
                           log.trace("Unmapped attribute: " + javaName);
                        }
                        else if (variableMapping.getXmlWildcard())
                        {
                           delegate.mapFieldToWildcard(cls, "_any", JBossXBSupport.getWildcardMarshaller());
                        }
                        else
                        {
                           log.warn("Unmapped variable: " + javaName);
                        }
                     }
                  }
               }
            }
         }

         if (getProperty(JBossXBConstants.JBXB_XS_MODEL) != null)
         {
            XSModel model = (XSModel)getProperty(JBossXBConstants.JBXB_XS_MODEL);
            delegate.marshal(model, provider, obj, writer);
         }
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new MarshalException(e);
      }
   }

   /**
    * Marshal the content tree rooted at obj into SAX2 events.
    */
   public void marshal(Object obj, ContentHandler handler)
   {
      throw new NotImplementedException();
   }

   /**
    * Marshal the content tree rooted at obj into a DOM tree.
    */
   public void marshal(Object obj, Node node)
   {
      throw new NotImplementedException();
   }

   /**
    * Marshal the content tree rooted at obj into an output stream.
    */
   public void marshal(Object obj, OutputStream os) throws MarshalException
   {
      marshal(obj, new OutputStreamWriter(os));
   }

   /**
    * Get the particular property in the underlying implementation of
    * Marshaller.
    */
   public Object getProperty(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("name parameter is null");

      return properties.get(name);
   }

   /**
    * Set the particular property in the underlying implementation of
    * Marshaller.
    *
    */
   public void setProperty(String name, Object value)
   {
      if (name == null)
         throw new IllegalArgumentException("name parameter is null");

      properties.put(name, value);
   }

   /**
    * Get a DOM tree view of the content tree(Optional).
    */
   public Node getNode(Object contentTree)
   {
      throw new NotImplementedException();
   }

   /**
    * Assert the required properties
    */
   private void assertRequiredProperties()
   {
      if (getProperty(JBossXBConstants.JBXB_SCHEMA_READER) == null && getProperty(JBossXBConstants.JBXB_XS_MODEL) == null)
         throw new WSException("Cannot find required property: " + JBossXBConstants.JBXB_XS_MODEL);

      if (getProperty(JBossXBConstants.JBXB_JAVA_MAPPING) == null)
         throw new WSException("Cannot find required property: " + JBossXBConstants.JBXB_JAVA_MAPPING);

      QName xmlName = (QName)getProperty(JBossXBConstants.JBXB_ROOT_QNAME);
      if (xmlName == null)
         throw new WSException("Cannot find required property: " + JBossXBConstants.JBXB_ROOT_QNAME);

      if (xmlName.getNamespaceURI().length() > 0 && xmlName.getPrefix().length() == 0)
         throw new IllegalArgumentException("The given root element name must be prefix qualified: " + xmlName);
   }
}
