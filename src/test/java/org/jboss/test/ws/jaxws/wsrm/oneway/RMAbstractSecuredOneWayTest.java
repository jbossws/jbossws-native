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
package org.jboss.test.ws.jaxws.wsrm.oneway;

import static org.jboss.test.ws.jaxws.wsrm.Helper.setAddrProps;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.jboss.test.ws.jaxws.wsrm.services.SecuredOneWayServiceIface;
import org.jboss.ws.extensions.wsrm.api.RMProvider;
import org.jboss.ws.extensions.wsrm.api.RMSequence;
import org.jboss.wsf.test.JBossWSTest;

/**
 * Secure Reliable JBoss WebService client invoking one way methods
 *
 * @author richard.opalka@jboss.com
 *
 * @since Dec 17, 2007
 */
public abstract class RMAbstractSecuredOneWayTest extends JBossWSTest
{
   private final String serviceURL = "http://" + getServerHost() + ":8080/jaxws-secured-wsrm/SecuredOneWayService";
   private SecuredOneWayServiceIface proxy;
   
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      
      QName serviceName = new QName("http://org.jboss.ws/jaxws/wsrm", "SecuredOneWayService");
      URL wsdlURL = new URL(serviceURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      proxy = (SecuredOneWayServiceIface)service.getPort(SecuredOneWayServiceIface.class);
   }
   
   public void testOneWayMethods() throws Exception
   {
      RMSequence sequence = ((RMProvider)proxy).createSequence(isClientAddressable());
      setAddrProps(proxy, "http://useless/action1", serviceURL);
      proxy.method1();
      setAddrProps(proxy, "http://useless/action2", serviceURL);
      proxy.method2("Hello World");
      setAddrProps(proxy, "http://useless/action3", serviceURL);
      proxy.method3(new String[] {"Hello","World"});
      sequence.close();
   }

   public static String getClasspath()
   {
      return "jaxws-secured-wsrm.war, jaxws-secured-wsrm-client.jar";
   }
   
   protected abstract boolean isClientAddressable();
}
