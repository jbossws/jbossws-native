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
package org.jboss.test.ws.common.config;

import java.io.File;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.ws.metadata.config.JBossWSConfigFactory;
import org.jboss.ws.metadata.config.EndpointProperty;
import org.jboss.ws.metadata.config.jaxws.ConfigRootJAXWS;
import org.jboss.ws.metadata.config.jaxws.EndpointConfigJAXWS;
import org.jboss.ws.metadata.config.jaxrpc.ConfigRootJAXRPC;
import org.jboss.ws.metadata.config.jaxrpc.EndpointConfigJAXRPC;
import org.jboss.ws.metadata.j2ee.UnifiedHandlerMetaData;
import org.jboss.ws.metadata.jsr181.HandlerChainMetaData;

/**
 * Test parsing of the JBossWS config
 *
 * @author Thomas.Diesler@jboss.org
 * @since 21-Dec-2004
 */
public class ConfigFactoryTestCase extends JBossWSTest
{
   
   public void testJAXRPCObjectModelFactory() throws Exception
   {
      File confFile = new File("resources/common/config/jaxrpc-endpoint-config.xml");
      assertTrue(confFile.exists());

      JBossWSConfigFactory factory = JBossWSConfigFactory.newInstance();
      ConfigRootJAXRPC config = (ConfigRootJAXRPC)factory.parse(confFile.toURL());

      assertNotNull("Null config", config);

      assertEquals(2, config.getEndpointConfig().size());
      EndpointConfigJAXRPC epc1 = (EndpointConfigJAXRPC)config.getEndpointConfig().get(0);
      EndpointConfigJAXRPC epc2 = (EndpointConfigJAXRPC)config.getEndpointConfig().get(1);

      assertEquals("Standard Endpoint", epc1.getConfigName());
      assertNull(epc1.getPreHandlerChain());
      assertNull(epc1.getPostHandlerChain());

      assertEquals("Standard WSSecurity Endpoint", epc2.getConfigName());
      HandlerChainMetaData preChain = epc2.getPreHandlerChain();
      assertEquals(1, preChain.getHandlers().size());
      UnifiedHandlerMetaData h1 = (UnifiedHandlerMetaData)preChain.getHandlers().get(0);
      assertEquals("WSSecurityHandlerInbound", h1.getHandlerName());
      assertEquals("org.jboss.ws.extensions.security.jaxrpc.WSSecurityHandlerInbound", h1.getHandlerClass());
   }

   public void testFeatures() throws Exception
   {
      File confFile = new File("resources/common/config/jaxws-endpoint-config.xml");
      assertTrue(confFile.exists());

      JBossWSConfigFactory factory = JBossWSConfigFactory.newInstance();
      ConfigRootJAXWS config = (ConfigRootJAXWS)factory.parse(confFile.toURL());
      assertNotNull("Null config", config);

      EndpointConfigJAXWS epConfig = (EndpointConfigJAXWS)config.getConfigByName("Standard MTOM Endpoint");
      assertTrue("Feature not set" , epConfig.hasFeature("http://org.jboss.ws/mtom"));

      // disable feature
      epConfig.setFeature("http://org.jboss.ws/mtom", false);
      assertFalse("Feature still set", epConfig.hasFeature("http://org.jboss.ws/mtom"));

   }

   public void testProperties() throws Exception
   {
      File confFile = new File("resources/common/config/jaxws-endpoint-config.xml");
      assertTrue(confFile.exists());

      JBossWSConfigFactory factory = JBossWSConfigFactory.newInstance();
      ConfigRootJAXWS config = (ConfigRootJAXWS)factory.parse(confFile.toURL());
      assertNotNull("Null config", config);

      EndpointConfigJAXWS epConfig = (EndpointConfigJAXWS)config.getConfigByName("Standard WSSecurity Endpoint");
      String value = epConfig.getProperty(EndpointProperty.MTOM_THRESHOLD);
      assertNotNull("Property does not exist", value);
      assertEquals("Wrong property valule", value, "5000");
   }
}
