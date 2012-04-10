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
package org.jboss.ws.core.jaxws;

import java.util.Collection;
import java.util.ResourceBundle;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.jboss.logging.Logger;
import org.jboss.ws.WSException;
import org.jboss.ws.api.util.BundleUtils;

import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.api.TypeReference;

/**
 * The default factory checks if a {@link JAXBBindingCustomization} exists
 * and uses it to customize the JAXBContext that will be created.
 * <p>
 * It uses the {@link org.jboss.wsf.spi.invocation.EndpointAssociation} to access customizations
 * if they are not passed explicitly.
 *
 * @see org.jboss.wsf.spi.deployment.Endpoint
 * @see org.jboss.wsf.spi.binding.BindingCustomization
 * @see JAXBBindingCustomization
 *
 * @see JAXBContext#newInstance(Class...)
 * @see JAXBContext#newInstance(String, ClassLoader, java.util.Map<java.lang.String,?>)
 *
 * @author Heiko.Braun@jboss.com
 *         Created: Jun 26, 2007
 */
public class CustomizableJAXBContextFactory extends JAXBContextFactory
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(CustomizableJAXBContextFactory.class);
   protected Logger log = Logger.getLogger(CustomizableJAXBContextFactory.class);

   public JAXBContext createContext(Class clazz) throws WSException
   {
      return createContext(new Class[] { clazz });
   }

   public JAXBContext createContext(Class[] clazzes) throws WSException
   {
      try
      {
         JAXBContext jaxbCtx = JAXBContext.newInstance(clazzes);
         jaxbCtx = createContext(clazzes);

         incrementContextCount();
         return jaxbCtx;
      }
      catch (JAXBException e)
      {
         throw new WSException(BundleUtils.getMessage(bundle, "FAILED_TO_CREATE_JAXBCONTEXT"),  e);
      }
   }

   public JAXBRIContext createContext(Class[] classes, Collection<TypeReference> refs, String defaultNS, boolean c14n)
   {
      try
      {
         JAXBRIContext jaxbCtx = JAXBRIContext.newInstance(classes, refs, null, defaultNS, c14n, null);
         incrementContextCount();
         return jaxbCtx;
      }
      catch (JAXBException e)
      {
         throw new WSException(BundleUtils.getMessage(bundle, "FAILED_TO_CREATE_JAXBCONTEXT"),  e);
      }
   }
}
