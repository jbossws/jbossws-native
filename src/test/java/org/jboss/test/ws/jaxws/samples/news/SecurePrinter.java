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

import javax.xml.ws.BindingProvider;

public class SecurePrinter extends Printer
{
   public SecurePrinter(URL url, boolean mtom)
   {
      super(url,mtom);
      BindingProvider bp = mtom ? (BindingProvider)mtomEndpoint : (BindingProvider)swaEndpoint;
      bp.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "kermit");
      bp.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "thefrog");
      System.setProperty("javax.net.ssl.trustStore", "/home/alessio/dati/jboss-4.2.2.GA/server/default/truststore_ale");
      System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
      System.setProperty("javax.net.ssl.trustStoreType", "jks");
      System.setProperty("org.jboss.security.ignoreHttpsHost", "true");
   }
   
   public static void main(String[] args)
   {
      try
      {
         if (args.length == 1)
         {
            SecurePrinter printer = new SecurePrinter(new URL(args[0]), args[0].endsWith("mtom?wsdl"));
            printer.run();
         }
         else
         {
            System.out.println("SecurePrinter client usage:");
            System.out.println("wsrunclient.sh -classpath agency.jar org.jboss.test.ws.jaxws.samples.news.SecurePrinter http://host:port/news/newspaper/mtom?wsdl");
            System.out.println("or");
            System.out.println("wsrunclient.sh -classpath agency.jar org.jboss.test.ws.jaxws.samples.news.SecurePrinter http://host:port/news/newspaper/swa?wsdl");
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
}
