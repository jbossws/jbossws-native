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

import java.util.List;
import java.util.SortedMap;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.ConstPool;

import javax.xml.bind.annotation.*;
import javax.xml.namespace.QName;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.core.utils.JavassistUtils;
import org.jboss.ws.metadata.umdm.FaultMetaData;
import org.jboss.ws.metadata.umdm.OperationMetaData;
import org.jboss.ws.metadata.umdm.ParameterMetaData;
import org.jboss.ws.metadata.umdm.TypeMappingMetaData;
import org.jboss.ws.metadata.umdm.TypesMetaData;
import org.jboss.ws.metadata.umdm.WrappedParameter;
import org.jboss.wsf.common.JavaUtils;

public class DynamicWrapperGenerator extends AbstractWrapperGenerator
{
   private static Logger log = Logger.getLogger(DynamicWrapperGenerator.class);

   protected ClassPool pool;
   protected boolean prune = true;

   public DynamicWrapperGenerator(ClassLoader loader)
   {
      super(loader);
      init(loader);
   }

   private void init(ClassLoader loader)
   {
      pool = new ClassPool(true);
      pool.appendClassPath(new LoaderClassPath(loader));
   }

   @Override
   public void reset(ClassLoader loader)
   {
      super.reset(loader);
      init(loader);
   }

   /**
    * Generates a wrapper type and assigns it to the passed ParameterMetaData
    * object. This routine requires the pmd to contain completed wrappedTypes
    * and wrappedVariables properties of the passed ParameterMetaData object.
    *
    * @param pmd a document/literal wrapped parameter
    */
   public void generate(ParameterMetaData pmd)
   {
      String wrapperName = pmd.getJavaTypeName();

      List<WrappedParameter> wrappedParameters = pmd.getWrappedParameters();
      OperationMetaData opMetaData = pmd.getOperationMetaData();

      if (opMetaData.isDocumentWrapped() == false)
         throw new WSException("Operation is not document/literal (wrapped)");

      if (wrappedParameters == null)
         throw new WSException("Cannot generate a type when their is no wrapper parameters");

      if(log.isDebugEnabled()) log.debug("Generating wrapper: " + wrapperName);

      QName xmlName = pmd.getXmlName();
      QName xmlType = pmd.getXmlType();

      try
      {
         CtClass clazz = pool.makeClass(wrapperName);
         clazz.getClassFile().setVersionToJava5();
         addClassAnnotations(clazz, xmlName, xmlType, null);

         for (WrappedParameter parameter : wrappedParameters)
         {
            addProperty(
                  clazz, parameter.getType(),
                  parameter.getName(), parameter.getVariable(),
                  parameter.getTypeArguments(),
                  new boolean[] {parameter.isSwaRef(), parameter.isXop()}
            );
         }
         clazz.stopPruning(!prune);
         pool.toClass(clazz, loader);
         JavaUtils.clearBlacklists(loader);
      }
      catch (Exception e)
      {
         throw new WSException("Could not generate wrapper type: " + wrapperName, e);
      }

      // Add the generated type to the types meta data
      TypesMetaData types = opMetaData.getEndpointMetaData().getServiceMetaData().getTypesMetaData();
      types.addTypeMapping(new TypeMappingMetaData(types, xmlType, wrapperName));
   }

   public void generate(FaultMetaData fmd)
   {
      String faultBeanName = fmd.getFaultBeanName();
      if(log.isDebugEnabled()) log.debug("Generating fault bean: " + faultBeanName);

      QName xmlType = fmd.getXmlType();

      Class exception = fmd.getJavaType();
      try
      {
         SortedMap<String, Class<?>> properties = getExceptionProperties(exception);
         String[] propertyOrder = properties.keySet().toArray(new String[0]);

         CtClass clazz = pool.makeClass(faultBeanName);
         clazz.getClassFile().setVersionToJava5();
         addClassAnnotations(clazz, fmd.getXmlName(), fmd.getXmlType(), propertyOrder);

         for (String property : propertyOrder)
            addProperty(
                  clazz, properties.get(property).getName(),
                  new QName(property), property, null,
                  new boolean[] {false, false}
            );

         clazz.stopPruning(!prune);
         pool.toClass(clazz, loader);
         JavaUtils.clearBlacklists(loader);
      }
      catch (Exception e)
      {
         throw new WSException("Could not generate fault wrapper bean: " + faultBeanName, e);
      }

      // Add the generated type to the types meta data
      TypesMetaData types = fmd.getOperationMetaData().getEndpointMetaData().getServiceMetaData().getTypesMetaData();
      types.addTypeMapping(new TypeMappingMetaData(types, xmlType, faultBeanName));
   }

   private static String getterPrefix(CtClass type)
   {
      return type == CtClass.booleanType || "java.lang.Boolean".equals(type.getName()) ? "is" : "get";
   }

   private String typeSignature(String type, String[] arguments)
   {
      StringBuilder ret = new StringBuilder(JavaUtils.toSignature(type));
      ret.deleteCharAt(ret.length() - 1).append('<');

      for (String arg : arguments)
         ret.append(JavaUtils.toSignature(arg));

      return ret.append(">;").toString();
   }

   private String getterSignature(String type)
   {
      return "()" + type;
   }

   private String setterSignature(String type)
   {
      return "(" + type + ")V";
   }

   private void addProperty(CtClass clazz, String typeName,
                            QName name, String variable, String[] typeArguments,
                            boolean[] attachments)
         throws CannotCompileException, NotFoundException
   {
      ConstPool constPool = clazz.getClassFile().getConstPool();
      String fieldName = JavaUtils.isReservedKeyword(variable) ?  "_" + variable : variable;
      CtField field = new CtField(pool.get(typeName), fieldName, clazz);
      field.setModifiers(Modifier.PRIVATE);

      // Add generics attributes
      String typeSignature = null;
      if (typeArguments != null)
      {
         typeSignature = typeSignature(typeName, typeArguments);
         JavassistUtils.addSignature(field, typeSignature);
      }

      // Add @XmlElement
      JavassistUtils.Annotation annotation = JavassistUtils.createAnnotation(XmlElement.class, constPool);
      if (name.getNamespaceURI() != null)
         annotation.addParameter("namespace", name.getNamespaceURI());
      annotation.addParameter("name", name.getLocalPart());
      annotation.markField(field);
      // @XmlAttachmentRef
      if(attachments[0])
      {
         annotation = JavassistUtils.createAnnotation(XmlAttachmentRef.class, constPool);
         annotation.markField(field);
      }
      // @XmlMimeType
      if(attachments[1])
      {
         annotation = JavassistUtils.createAnnotation(XmlMimeType.class, constPool);
         annotation.addParameter("value", "application/octet-stream"); // TODO: default mime 
         annotation.markField(field);
      }
      clazz.addField(field);

      // Add accessor methods
      CtMethod getter = CtNewMethod.getter(getterPrefix(field.getType()) + JavaUtils.capitalize(variable), field);
      CtMethod setter = CtNewMethod.setter("set" + JavaUtils.capitalize(variable), field);
      if (typeSignature != null)
      {
         JavassistUtils.addSignature(getter, getterSignature(typeSignature));
         JavassistUtils.addSignature(setter, setterSignature(typeSignature));
      }
      clazz.addMethod(getter);
      clazz.addMethod(setter);
   }

   private static void addClassAnnotations(CtClass clazz, QName xmlName, QName xmlType, String[] propertyOrder)
   {
      ConstPool constPool = clazz.getClassFile().getConstPool();

      // Add @XmlRootElement
      JavassistUtils.Annotation annotation = JavassistUtils.createAnnotation(XmlRootElement.class, constPool);
      if (xmlName.getNamespaceURI() != null && xmlName.getNamespaceURI().length() > 0)
         annotation.addParameter("namespace", xmlName.getNamespaceURI());
      annotation.addParameter("name", xmlName.getLocalPart());
      annotation.markClass(clazz);

      // Add @XmlType;
      annotation = JavassistUtils.createAnnotation(XmlType.class, constPool);
      if (xmlType.getNamespaceURI() != null & xmlType.getNamespaceURI().length() > 0)
         annotation.addParameter("namespace", xmlType.getNamespaceURI());
      annotation.addParameter("name", xmlType.getLocalPart());
      if (propertyOrder != null)
         annotation.addParameter("propOrder", propertyOrder);
      annotation.markClass(clazz);

      // Add @XmlAccessorType
      annotation = JavassistUtils.createAnnotation(XmlAccessorType.class, constPool);
      annotation.addParameter("value",  XmlAccessType.FIELD);
      annotation.markClass(clazz);
   }
}
