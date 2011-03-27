/*
* JBoss, Home of Professional Open Source.
* Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws3070;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test case to test JBWS-3070 for the correct publising of imported schemas.
 * 
 * @author darran.lofthouse@jboss.com
 * @since 24th June 2010
 */
public class JBWS3070TestCase extends JBossWSTest
{

   public static Test suite() throws Exception
   {
      return new JBossWSTestSetup(JBWS3070TestCase.class, "jaxws-jbws3070.war");
   }

   private TestEndpoint getPort() throws Exception
   {

      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws3070?wsdl");
      QName serviceName = new QName("http://org.jboss.test.ws/jbws3070", "TestEndpointService");

      Service service = Service.create(wsdlURL, serviceName);

      return service.getPort(TestEndpoint.class);
   }

   public void testCall() throws Exception
   {
      String message = "Hi";
      String response = getPort().echo(message);
      assertEquals(message, response);
   }

}
