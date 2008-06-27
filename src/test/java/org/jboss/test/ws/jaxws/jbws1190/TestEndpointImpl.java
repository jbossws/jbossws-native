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
package org.jboss.test.ws.jaxws.jbws1190;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;

import javax.jws.WebService;

import org.jboss.ws.metadata.wsdl.WSDLDefinitions;
import org.jboss.ws.metadata.wsdl.WSDLEndpoint;
import org.jboss.ws.metadata.wsdl.WSDLService;
import org.jboss.ws.tools.wsdl.WSDLDefinitionsFactory;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.management.ServerConfigFactory;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;

/**
 * [JBWS-1190] - WSDL generated for JSR-181 POJO does not take 'transport-guarantee' in web.xml into account
 * 
 * http://jira.jboss.org/jira/browse/JBWS-1190
 * 
 * @author darran.lofthouse@jboss.com
 * @since 19-October-2006
 */
@WebService(name = "Test", serviceName = "TestService", targetNamespace = "http://org.jboss/test/ws/jbws1190", endpointInterface = "org.jboss.test.ws.jaxws.jbws1190.TestEndpoint")
public class TestEndpointImpl implements TestEndpoint
{

   public void testAddress(final String archive, final String service, final String scheme, final String port)
   {
      SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
      ServerConfig serverConfig = spiProvider.getSPI(ServerConfigFactory.class).getServerConfig();File tmpDir = serverConfig.getServerTempDir();
      
      File dataDir = serverConfig.getServerDataDir();
      File wsdlDir = new File(dataDir.getAbsolutePath() + File.separator + "wsdl" + File.separator + archive);

      if (wsdlDir.exists() == false)
      {
         throw new JBWS1190Exception(wsdlDir.getAbsolutePath() + " does not exist.");
      }

      File[] wsdls = wsdlDir.listFiles(new FilenameFilter() {
         public boolean accept(File dir, String name)
         {
            return name.startsWith(service);
         }
      });

      File wsdlFile = null;
      for (int i = 0; i < wsdls.length; i++)
      {
         if (wsdlFile == null || wsdls[i].compareTo(wsdlFile) > 0)
         {
            wsdlFile = wsdls[i];
         }
      }
      assertTrue("No WSDL files found", wsdlFile != null);

      WSDLDefinitionsFactory factory = WSDLDefinitionsFactory.newInstance();
      WSDLDefinitions wsdl;
      try
      {
         wsdl = factory.parse(wsdlFile.toURL());
      }
      catch (MalformedURLException e)
      {
         throw new JBWS1190Exception("Error readin WSDL", e);
      }

      WSDLService[] services = wsdl.getServices();
      assertEquals("No of services", 1, services.length);

      WSDLEndpoint[] endpoints = services[0].getEndpoints();
      assertEquals("No of endpoints", 1, endpoints.length);

      String address = endpoints[0].getAddress();
      assertTrue("Expected Scheme '" + scheme + "' from address '" + address + "'", address.startsWith(scheme + "://"));
      assertTrue("Expected Port '" + port + "' from address '" + address + "'", address.indexOf(":" + port + "/") > -1);
   }

   private void assertEquals(final String message, final int expected, final int actual)
   {
      if (expected != actual)
      {
         throw new JBWS1190Exception(message + " expected=" + expected + " actual=" + actual);
      }
   }

   private void assertTrue(final String message, final boolean value)
   {
      if (value == false)
      {
         throw new JBWS1190Exception(message);
      }
   }
}