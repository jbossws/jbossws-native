/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws2706;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-2706] Unable to unmarshall attachment parts where the type is 'application/octet-stream'.
 * 
 * @author darran.lofthouse@jboss.com
 * @since 22nd July 2009
 * @see https://jira.jboss.org/jira/browse/JBWS-2706
 */
public class JBWS2706TestCase extends JBossWSTest
{

   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-jbws2706/";

   public static Test suite() throws Exception
   {
      return new JBossWSTestSetup(JBWS2706TestCase.class, "jaxws-jbws2706.war");
   }

   public void testCall() throws Exception
   {
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
      QName serviceName = new QName("http://SwaTestService.org/wsdl", "WSIDLSwaTestService");

      Service service = Service.create(wsdlURL, serviceName);
      SwaTest port = service.getPort(SwaTest.class);
      
      String message = "Howdy";
      InputRequest request = new InputRequest();
      request.setMessage(message);

      Holder<String> holderOne = new Holder<String>("One");
      Holder<byte[]> holderTwo = new Holder<byte[]>("Two".getBytes());

      OutputResponse response = port.echoMultipleAttachments(request, holderOne, holderTwo);

      assertEquals("Response", request.getMessage(), response.getMessage());
      assertEquals("Holder One", "One", holderOne.value);
      assertEquals("Holder Two", "Two", new String(holderTwo.value, "UTF-8"));
   }

}
