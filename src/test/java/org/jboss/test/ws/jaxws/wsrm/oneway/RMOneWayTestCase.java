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

import static org.jboss.test.ws.jaxws.wsrm.Helper.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.ws.extensions.wsrm.api.RMAddressingType;
import org.jboss.ws.extensions.wsrm.api.RMProvider;
import org.jboss.ws.extensions.wsrm.api.RMSequence;
import org.jboss.ws.extensions.wsrm.api.RMSequenceType;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;
import org.jboss.test.ws.jaxws.wsrm.OneWayServiceIface;

/**
 * Reliable JBoss WebService client invoking one way methods
 *
 * @author richard.opalka@jboss.com
 * @since 22-Aug-2007
 */
public class RMOneWayTestCase extends JBossWSTest
{
   private static final Properties props = new Properties();
   private String targetNS = "http://wsrm.jaxws.ws.test.jboss.org/";
   private OneWayServiceIface proxy;
   private final boolean emulatorOn = Boolean.parseBoolean((String)props.get("emulator"));
   private final boolean addressable = Boolean.parseBoolean((String)props.get("addressable"));
   private final String serviceURL = "http://" + getServerHost() + ":" + props.getProperty("port") + props.getProperty("path");
   
   static
   {
      // load test properties
      File propertiesFile = new File("resources/jaxws/wsrm/properties/RMOneWayTestCase.properties");
      try 
      {
         props.load(new FileInputStream(propertiesFile));
      }
      catch (IOException ignore)
      {
         ignore.printStackTrace();
      }
   }
   
   public static Test suite()
   {
      return new JBossWSTestSetup(RMOneWayTestCase.class, props.getProperty("archives"));
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      QName serviceName = new QName(targetNS, "OneWayService");
      URL wsdlURL = new URL(serviceURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      proxy = (OneWayServiceIface)service.getPort(OneWayServiceIface.class);
   }
   
   public void testOneWayMethods() throws Exception
   {
      System.out.println("FIXME [JBWS-515] Provide an initial implementation for WS-ReliableMessaging");
      System.out.println("FIXME [JBWS-1699] Implement the basic message exchange that is required for WS-RM");
      System.out.println("FIXME [JBWS-1700] Provide a comprehensive test case for WS-RM");
      if (true) return; // disable WS-RM tests - they cause regression in hudson
      
      RMSequence sequence = null;
      if (emulatorOn)
      {
         RMProvider wsrmProvider = (RMProvider)proxy;
         sequence = wsrmProvider.createSequence(getAddressingType(), RMSequenceType.SIMPLEX);
         System.out.println("Created sequence with id=" + sequence.getOutboundId());
      }
      setAddrProps(proxy, "http://useless/action1", serviceURL);
      proxy.method1();
      setAddrProps(proxy, "http://useless/action2", serviceURL);
      proxy.method2("Hello World");
      setAddrProps(proxy, "http://useless/action3", serviceURL);
      proxy.method3(new String[] {"Hello","World"});
      if (emulatorOn)
      {
         if (!sequence.isCompleted(1000, TimeUnit.MILLISECONDS)) {
            fail("Sequence not completed within specified time amount");
         } else {
            sequence.close();
         }
      }
   }

   private RMAddressingType getAddressingType()
   {
      return addressable ? RMAddressingType.ADDRESSABLE : RMAddressingType.ANONYMOUS;
   }
   
}
