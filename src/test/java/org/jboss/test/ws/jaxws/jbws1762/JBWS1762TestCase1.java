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
package org.jboss.test.ws.jaxws.jbws1762;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-1762] web.xml modified to web.xml.org - subsequent runs fail
 *
 * @author Richard.Opalka@jboss.com
 * @since 13-Aug-2007
 */
public class JBWS1762TestCase1 extends JBossWSTest
{

   private String targetNS = "http://jbws1762.jaxws.ws.test.jboss.org/";
   private JBWS1762 proxy;

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS1762TestCase1.class, "jaxws-jbws1762.war");
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      QName serviceName = new QName(targetNS, "JBWS1762Service");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws1762/JBWS1762Service?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      proxy = (JBWS1762)service.getPort(JBWS1762.class);
   }

   public void testIssue() throws Exception
   {
      assertEquals(proxy.echo("Hello!"), "Hello!");
   }

}
