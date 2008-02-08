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
package org.jboss.test.ws.jaxws.samples.news;

import java.net.URL;

import org.jboss.ws.core.StubExt;

public class SecureAgency extends Agency
{
   public SecureAgency(URL url)
   {
      super(url);
      ((StubExt)endpoint).setConfigName("Standard WSSecurity Client");
   }
   
   public static void main(String[] args)
   {
      try
      {
         if (args.length == 3)
         {
            Agency agency = new SecureAgency(new URL(args[0]));
            agency.run(args[1], args[2]);
            System.out.println("Press release sent.");
         }
         else
         {
            System.out.println("SecureAgency client usage:");
            System.out.println("./wsrunclient.sh -classpath agency.jar -Dorg.jboss.ws.wsse.trustStore=wsse10.truststore -Dorg.jboss.ws.wsse.keyStore=alice-sign_enc.jks " +
            		"-Dorg.jboss.ws.wsse.keyStorePassword=password -Dorg.jboss.ws.wsse.trustStorePassword=password org.jboss.test.ws.jaxws.samples.news.SecureAgency " +
            		"http://localhost.localdomain:8080/news/pressRelease?wsdl title body");
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
}
