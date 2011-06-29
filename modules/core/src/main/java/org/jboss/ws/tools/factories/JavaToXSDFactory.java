/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.ws.tools.factories;

import java.util.ResourceBundle;

import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.tools.JavaToXSD;
import org.jboss.ws.tools.exceptions.JBossWSToolsException;
import org.jboss.ws.tools.interfaces.JavaToXSDIntf;
 
 
/**
 * Factory that provides a Java To Schema Converter
 *  @author <mailto:Anil.Saldhana@jboss.org>Anil Saldhana
 *  @since   Jul 23, 2005 
 */

public class JavaToXSDFactory
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(JavaToXSDFactory.class);
   /**
    * Create a JavaToXSDFactory
    * @return
    * @throws JBossWSToolsException
    */
   public static JavaToXSDFactory newInstance()  throws JBossWSToolsException
   {
      String factoryName = null;
      JavaToXSDFactory factory = null;
      try
      {
         String defaultName = "org.jboss.ws.tools.factories.JavaToXSDFactory";
         factoryName = System.getProperty("org.jboss.ws.tools.JavaToXSDFactory", defaultName);
         ClassLoader loader = SecurityActions.getContextClassLoader();
         Class factoryClass = SecurityActions.loadClass(loader, factoryName);
         factory = (JavaToXSDFactory) factoryClass.newInstance();
      }
      catch(Throwable e)
      {
         throw new JBossWSToolsException(BundleUtils.getMessage(bundle, "CANNOT_CREATE_JAVATOXSDFACTORY"), e);
      }
      return factory;
   }

   public JavaToXSDFactory()
   {
   }
   
   public JavaToXSDIntf getJavaToXSD(String targetNamespace,String typeNamespace)
   { 
      return  new JavaToXSD();
   }
}

