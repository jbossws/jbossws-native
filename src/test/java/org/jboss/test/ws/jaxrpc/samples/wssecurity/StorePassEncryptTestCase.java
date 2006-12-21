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
package org.jboss.test.ws.jaxrpc.samples.wssecurity;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.Stub;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.rpc.handler.HandlerRegistry;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.ws.core.jaxrpc.ServiceFactoryImpl;
import org.jboss.ws.core.jaxrpc.ServiceImpl;
import org.jboss.ws.extensions.security.jaxrpc.WSSecurityHandlerOutbound;

/**
 * This test simulates simulates the usage of a jboss-ws-security keystore and truststore use cases
 *
 * @author magesh.bojan@jboss.com
 * @version $Revision$
 */
public class StorePassEncryptTestCase extends JBossWSTest
{
   private static Hello port;
   
   public static Test suite() throws Exception
   {
      return JBossWSTestSetup.newTestSetup(StorePassEncryptTestCase.class, "jaxrpc-samples-store-pass-encrypt.war, jaxrpc-samples-store-pass-encrypt-client.jar");
   }


   public void setUp() throws Exception
   {
      if (port == null)
      {
         if (isTargetServerJBoss())
         {
            InitialContext iniCtx = getInitialContext();
            Service service = (Service)iniCtx.lookup("java:comp/env/service/HelloService");
            port = (Hello)service.getPort(Hello.class);
         }
         else
         {
            ServiceFactoryImpl factory = new ServiceFactoryImpl();
            URL wsdlURL = new File("resources/jaxrpc/samples/wssecurity/WEB-INF/wsdl/HelloService.wsdl").toURL();
            URL mappingURL = new File("resources/jaxrpc/samples/wssecurity/WEB-INF/jaxrpc-mapping.xml").toURL();
            URL securityURL = new File("resources/jaxrpc/samples/wssecurity/store-pass-encrypt/META-INF/jboss-wsse-client.xml").toURL();

            QName serviceName = new QName("http://org.jboss.ws/samples/wssecurity", "HelloService");
            QName portName = new QName("http://org.jboss.ws/samples/wssecurity", "HelloPort");
            ServiceImpl service = (ServiceImpl)factory.createService(wsdlURL, serviceName, mappingURL, securityURL);

            HandlerRegistry registry = service.getDynamicHandlerRegistry();
            List infos = registry.getHandlerChain(portName);
            infos.add(new HandlerInfo(WSSecurityHandlerOutbound.class, new HashMap(), new QName[] {}));
            registry.setHandlerChain(portName, infos);

            port = (Hello)service.getPort(Hello.class);
            ((Stub)port)._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, "http://" + getServerHost() + ":8080/jaxrpc-samples-store-pass-encrypt");
         }
      }
   }
   
   public void testEndpoint() throws Exception
   {
      System.out.println("FIXME: [JBWS-1329] Fix StorePassEncryptTestCase for jbossas-5.0");
      if (true) return;
      
      UserType in0 = new UserType("Kermit");
      UserType retObj = port.echoUserType(in0);
      assertEquals(in0, retObj);
   }
}
