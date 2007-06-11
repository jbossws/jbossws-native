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
package org.jboss.test.ws.jaxws.jbws1422;

import junit.framework.Test;
import org.jboss.ws.WSException;
import org.jboss.wsf.spi.test.JBossWSTest;
import org.jboss.wsf.spi.test.JBossWSTestSetup;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.URL;

/**
 * If @WebParam.name starts with one lower-case character followed
 * by an upper-case character a NPE is thrown on deployment.
 *
 * http://jira.jboss.org/jira/browse/JBWS-1422
 *
 * @version $Revision:1370 $
 */
public class JBWS1422TestCase extends JBossWSTest
{
   private String targetNS = "http://org.jboss.test.ws/jbws1422";
   private IWebsvc port;
	private URL wsdlURL;

	public static Test suite()
   {
      return JBossWSTestSetup.newTestSetup(JBWS1422TestCase.class, "jaxws-jbws1422.jar");
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      QName serviceName = new QName(targetNS, "JBWS1422Service");
      wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws1422/IWebsvcImpl?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      port = service.getPort(IWebsvc.class);
   }

	/**	 
	 *
	 * @throws Exception
	 */
	public void testDeployment() throws Exception
	{
		try
      {
         String result = port.cancel("myFooBar");
			assertNotNull(result);
			assertEquals("Cancelled", result);
		}
      catch (Exception ex)
      {
         WSException.rethrow(ex);
      }
	}

}
