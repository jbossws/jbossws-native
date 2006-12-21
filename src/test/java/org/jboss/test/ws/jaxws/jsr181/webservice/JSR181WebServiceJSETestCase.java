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
package org.jboss.test.ws.jaxws.jsr181.webservice;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTestSetup;

/**
 * Test the JSR-181 annotation: javax.jws.WebService
 *
 * @author Thomas.Diesler@jboss.org
 * @author <a href="jason.greene@jboss.com">Jason T. Greene</a>
 * @since 29-Apr-2005
 */
public class JSR181WebServiceJSETestCase extends JSR181WebServiceBase
{
   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(JSR181WebServiceJSETestCase.class, "");
   }

   public void testWebServiceTest() throws Exception
   {
      deploy("jaxws-jsr181-webservice01-jse.war");
      try
      {
         webServiceTest();
      }
      finally
      {
         undeploy("jaxws-jsr181-webservice01-jse.war");
      }
   }

   public void testWebServiceWsdlLocation() throws Exception
   {
      deploy("jaxws-jsr181-webservice02-jse.war");
      try
      {
         webServiceWsdlLocationTest();
      }
      finally
      {
         undeploy("jaxws-jsr181-webservice02-jse.war");
      }
   }

   public void testWebServiceEndpointInterface() throws Exception
   {
      deploy("jaxws-jsr181-webservice03-jse.war");
      try
      {
         webServiceEndpointInterfaceTest();
      }
      finally
      {
         undeploy("jaxws-jsr181-webservice03-jse.war");
      }
   }
}
