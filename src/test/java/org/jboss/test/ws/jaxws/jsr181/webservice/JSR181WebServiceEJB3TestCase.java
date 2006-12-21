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

import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTestSetup;

/**
 * Test the JSR-181 annotation: javax.jws.WebService
 *
 * @author Thomas.Diesler@jboss.org
 * @author <a href="jason.greene@jboss.com">Jason T. Greene</a>
 * @since 29-Apr-2005
 */
public class JSR181WebServiceEJB3TestCase extends JSR181WebServiceBase
{
   public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(JSR181WebServiceEJB3TestCase.class, "");
   }

   public void testRemoteAccess() throws Exception
   {
      deploy("jaxws-jsr181-webservice01-ejb3.jar");
      try
      {
         InitialContext iniCtx = getInitialContext();
         EJB3RemoteInterface ejb3Remote = (EJB3RemoteInterface)iniCtx.lookup("/ejb3/EJB3EndpointInterface");

         String helloWorld = "Hello world!";
         Object retObj = ejb3Remote.echo(helloWorld);
         assertEquals(helloWorld, retObj);
      }
      finally
      {
         undeploy("jaxws-jsr181-webservice01-ejb3.jar");
      }
   }

   public void testWebServiceTest() throws Exception
   {
      deploy("jaxws-jsr181-webservice01-ejb3.jar");
      try
      {
         webServiceTest();
      }
      finally
      {
         undeploy("jaxws-jsr181-webservice01-ejb3.jar");
      }
   }

   public void testWebServiceWsdlLocationTest() throws Exception
   {
      deploy("jaxws-jsr181-webservice02-ejb3.jar");
      try
      {
         webServiceWsdlLocationTest();
      }
      finally
      {
         undeploy("jaxws-jsr181-webservice02-ejb3.jar");
      }
   }

   public void WebServiceEndpointInterfaceTest() throws Exception
   {
      deploy("jaxws-jsr181-webservice03-ejb3.jar");
      try
      {
         webServiceEndpointInterfaceTest();
      }
      finally
      {
         undeploy("jaxws-jsr181-webservice03-ejb3.jar");
      }
   }
}
