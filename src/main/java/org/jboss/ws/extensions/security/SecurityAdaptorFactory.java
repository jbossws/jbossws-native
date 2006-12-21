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
package org.jboss.ws.extensions.security;

//$Id$

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;

/**
 * A container idependent security adaptor factory
 *
 * @author Thomas.Diesler@jboss.org
 * @since 10-May-2005
 */
public class SecurityAdaptorFactory
{
   // provide logging
   private static final Logger log = Logger.getLogger(SecurityAdaptorFactory.class);

   public static final String DEFAULT_FACTORY = "org.jboss.ws.integration.SecurityAdaptorFactoryImpl";

   private static SecurityAdaptorFactory delegate;

   public static SecurityAdaptorFactory getInstance()
   {
      if (delegate == null)
      {
         ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
         try
         {
            Class factoryClass = ctxLoader.loadClass(DEFAULT_FACTORY);
            delegate = (SecurityAdaptorFactory)factoryClass.newInstance();
         }
         catch (RuntimeException rte)
         {
            throw rte;
         }
         catch (Exception e)
         {
            throw new WSException("Cannot load: " + DEFAULT_FACTORY, e);
         }
      }
      return delegate;
   }

   public SecurityAdaptor getSecurityAdaptor()
   {
      return delegate.getSecurityAdaptor();
   }
}
