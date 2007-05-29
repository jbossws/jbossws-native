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
package org.jboss.ws.tools.jaxws.impl;

import com.sun.codemodel.*;
import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.core.jaxws.AbstractWrapperGenerator;
import org.jboss.ws.metadata.umdm.FaultMetaData;
import org.jboss.ws.metadata.umdm.OperationMetaData;
import org.jboss.ws.metadata.umdm.ParameterMetaData;
import org.jboss.ws.metadata.umdm.WrappedParameter;
import org.jboss.wsf.spi.utils.JavaUtils;

import javax.xml.bind.annotation.*;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.SortedMap;

/**
 * Generates source for wrapper beans
 *
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @version $Revision$
 */
public class SourceWrapperGenerator extends AbstractWrapperGenerator implements WritableWrapperGenerator
{
   private static Logger log = Logger.getLogger(SourceWrapperGenerator.class);
   private PrintStream stream;
   private JCodeModel codeModel;

   
   public SourceWrapperGenerator(ClassLoader loader, PrintStream stream)
   {
      super(loader);
      this.stream = stream;
      codeModel = new JCodeModel();
   }
   
   @Override
   public void reset(ClassLoader loader)
   {
      super.reset(loader);
      codeModel = new JCodeModel();
   }
   
   public void write(File directory) throws IOException
   {
      stream.println("Writing Source:");
      codeModel.build(directory, stream);
   }

   public void generate(ParameterMetaData pmd)
   {
      List<WrappedParameter> wrappedParameters = pmd.getWrappedParameters();
      OperationMetaData operationMetaData = pmd.getOperationMetaData();

      if (operationMetaData.isDocumentWrapped() == false)
         throw new WSException("Operation is not document/literal (wrapped)");

      if (wrappedParameters == null)
         throw new WSException("Cannot generate a type when their is no type information");

      String wrapperName = pmd.getJavaTypeName();
      if (log.isDebugEnabled())
         if(log.isDebugEnabled()) log.debug("Generating wrapper: " + wrapperName);

      try
      {

         JDefinedClass clazz = codeModel._class(wrapperName);
         addClassAnnotations(clazz, pmd.getXmlName(), pmd.getXmlType(), null);
         for (WrappedParameter wrapped : wrappedParameters)
         {
            addProperty(clazz, wrapped.getType(), wrapped.getName(), wrapped.getVariable());
         }
      }
      catch (Exception e)
      {
         throw new WSException("Could not generate wrapper type: " + wrapperName, e);
      }
   }
   public void generate(FaultMetaData fmd)
   {
      String faultBeanName = fmd.getFaultBeanName();
      Class exception = fmd.getJavaType();

      try
      {
         SortedMap<String, Class<?>> properties = getExceptionProperties(exception);
         String[] propertyOrder = properties.keySet().toArray(new String[0]);

         JDefinedClass clazz = codeModel._class(faultBeanName);
         addClassAnnotations(clazz, fmd.getXmlName(), fmd.getXmlType(), propertyOrder);

         for (String property : propertyOrder)
            addProperty(clazz, properties.get(property).getName(), new QName(property), property);
      }
      catch (Exception e)
      {
         throw new WSException("Could not generate wrapper type: " + faultBeanName, e);
      }
   }

   private static String getterPrefix(Class type)
   {
      return Boolean.TYPE == type || Boolean.class == type ? "is" : "get";
   }

   private void addProperty(JDefinedClass clazz, String typeName, QName name, String variable)
         throws ClassNotFoundException
   {
      Class type = JavaUtils.loadJavaType(typeName, loader);
      JFieldVar field = clazz.field(JMod.PRIVATE, type, variable);
      JAnnotationUse annotation = field.annotate(XmlElement.class);
      if (name.getNamespaceURI() != null)
         annotation.param("namespace", name.getNamespaceURI());
      annotation.param("name", name.getLocalPart());

      // Add acessor methods
      JMethod method = clazz.method(JMod.PUBLIC, type, getterPrefix(type) + JavaUtils.capitalize(variable));
      method.body()._return(JExpr._this().ref(variable));

      method = clazz.method(JMod.PUBLIC, type, "set" + JavaUtils.capitalize(variable));
      method.body().assign(JExpr._this().ref(variable), method.param(type, variable));
   }

   private static void addClassAnnotations(JDefinedClass clazz, QName xmlName, QName xmlType, String[] propertyOrder)
   {
      JAnnotationUse annotation = clazz.annotate(XmlRootElement.class);
      if (xmlName.getNamespaceURI() != null && xmlName.getNamespaceURI().length() > 0)
         annotation.param("namespace", xmlName.getNamespaceURI());
      annotation.param("name", xmlName.getLocalPart());

      annotation = clazz.annotate(XmlType.class);
      if (xmlType.getNamespaceURI() != null & xmlType.getNamespaceURI().length() > 0)
         annotation.param("namespace", xmlType.getNamespaceURI());
      annotation.param("name", xmlType.getLocalPart());
      if (propertyOrder != null)
      {
         JAnnotationArrayMember paramArray = annotation.paramArray("propOrder");
         for (String property : propertyOrder)
            paramArray.param(property);
      }

      annotation = clazz.annotate(XmlAccessorType.class);
      annotation.param("value", XmlAccessType.FIELD);
   }
}