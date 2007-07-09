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
package org.jboss.test.ws.jaxws.binding;

import junit.framework.TestCase;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.BasicEndpoint;
import org.jboss.ws.core.jaxws.JAXBBindingCustomization;

import static org.jboss.wsf.spi.deployment.Endpoint.EndpointState;
import org.jboss.wsf.spi.binding.BindingCustomization;

import java.util.List;
import java.util.Iterator;

/**
 * @author Heiko.Braun@jboss.com
 *         Created: Jun 28, 2007
 */
public class BindingCustomizationTestCase extends TestCase {

   public void testCustomizationWriteAccess() throws Exception
   {
      Endpoint endpoint = new BasicEndpoint();
      JAXBBindingCustomization jaxbCustomization = new JAXBBindingCustomization();
      jaxbCustomization.put(JAXBBindingCustomization.DEFAULT_NAMESPACE_REMAP, "http://org.jboss.bindingCustomization");
      endpoint.addBindingCustomization(jaxbCustomization);

      // a started endpoint should deny customizations
      try
      {
         endpoint.setState(EndpointState.STARTED);
         endpoint.addBindingCustomization(jaxbCustomization);

         fail("It should not be possible to change bindinig customizations on a started endpoint");
      }
      catch (IllegalAccessError e)
      {
         // all fine, this should happen
      }
   }

   public void testCustomizationReadAccess() throws Exception
   {
      Endpoint endpoint = new BasicEndpoint();
      JAXBBindingCustomization jaxbCustomization = new JAXBBindingCustomization();
      jaxbCustomization.put(JAXBBindingCustomization.DEFAULT_NAMESPACE_REMAP, "http://org.jboss.bindingCustomization");
      endpoint.addBindingCustomization(jaxbCustomization);
      endpoint.setState(EndpointState.STARTED);

      // read a single customization
      List<BindingCustomization> customizations = endpoint.getBindingCustomizations();

      BindingCustomization knownCustomization = null;
      Iterator<BindingCustomization> it = customizations.iterator();
      while(it.hasNext())
      {
         knownCustomization = it.next();
         break;
      }

      assertNotNull(knownCustomization);

      // however the iteratoion should be unmodifiable
      try
      {
         customizations.add( new JAXBBindingCustomization() );
         fail("Started Endpoints should only axpose read acccess to their binding customizations");
      }
      catch (Exception e)
      {
         // all fine, we'd expect this
      }


   }
}
