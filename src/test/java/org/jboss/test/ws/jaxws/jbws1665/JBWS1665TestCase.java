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
package org.jboss.test.ws.jaxws.jbws1665;

// $Id: $

import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.Test;

import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.tools.wsdl.WSDLDefinitionsFactory;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-1665] incorrect wsdl generation
 * 
 * http://jira.jboss.org/jira/browse/JBWS-1665
 */
public class JBWS1665TestCase extends JBossWSTest
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-jbws1655/TrackingService";

//   public static Test suite()
//   {
//      return JBossWSTestSetup.newTestSetup(JBWS1665TestCase.class, "jaxws-jbws1665.jar");
//   }

   public void testWebService() throws Exception
   {
      System.out.println("FIXME: [JBWS-1665] incorrect wsdl generation");
      
      //assertWSDLAccess();

      // Need to validate the WSDL is property populated.  Several fields such as
      // element names are being left blank.  This is related to case 16130.  The
      // entire generated wsdl is attached to the case.

      /*
      QName serviceName = new QName("http://org.jboss.ws/jbws1665", "EJB3BeanService");
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      TrackingServiceInterface port = (TrackingServiceInterface)service.getPort(TrackingServiceInterface.class);
      */
   }

   private void assertWSDLAccess() throws MalformedURLException
   {
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
      WSDLDefinitionsFactory factory = WSDLDefinitionsFactory.newInstance();
      WSDLDefinitions wsdlDefinitions = factory.parse(wsdlURL);
      assertNotNull(wsdlDefinitions);
   }
}
