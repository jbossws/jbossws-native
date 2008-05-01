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
package org.jboss.test.ws.jaxws.samples.dar;

//$Id$

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Invokes the DAR JMS client
 * (this is actually a weak test since we don't check if the 
 * response queue actually receives the response)
 *
 * @author alessio.soldano@jboss.org
 * @since 01-May-2008
 */
public class JMSClientTestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSTestSetup(JMSClientTestCase.class, "jaxws-samples-dar-jms-client.sar,jaxws-samples-dar-jms.jar");
   }
   
   public void test() throws Exception
   {
      String url = "http://" + getServerHost() + ":8080/dar-jms-client/JMSClient";
      Date start = new Date();
      HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
      int responseCode = connection.getResponseCode();
      if (responseCode != HttpURLConnection.HTTP_OK)
      {
         fail("Cannot access JMSClient servlet, responseCode == " + responseCode);
      }
      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      StringBuffer buffer = new StringBuffer();
      String line;
      while ((line = in.readLine()) != null) {
        buffer.append(line + "\n");
      }
      assertTrue(buffer.toString().contains("Request message sent, doing something interesting in the mean time... ;-) "));
      Date stop = new Date();
      assertTrue(stop.getTime() - start.getTime() < 3000);
   }
}
