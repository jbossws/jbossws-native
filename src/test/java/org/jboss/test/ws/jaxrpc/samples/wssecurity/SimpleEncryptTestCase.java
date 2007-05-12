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

import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.Stub;

import junit.framework.Test;

import org.jboss.test.ws.JBossWSTest;
import org.jboss.test.ws.JBossWSTestSetup;
import org.jboss.ws.core.StubExt;
import org.jboss.ws.core.jaxrpc.client.ServiceFactoryImpl;
import org.jboss.ws.core.jaxrpc.client.ServiceImpl;

/**
 * Test WS-Security with RPC/Literal
 *
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @version $Revision$
 */
public class SimpleEncryptTestCase extends JBossWSTest
{
   /** Construct the test case with a given name
    */

   /** Deploy the test */
   public static Test suite() throws Exception
   {
      return JBossWSTestSetup.newTestSetup(SimpleEncryptTestCase.class, "jaxrpc-samples-wssecurity-encrypt.war, jaxrpc-samples-wssecurity-encrypt-client.jar");
   }

   /**
    * Test JSE endpoint
    */
   public void testEndpoint() throws Exception
   {
      Hello hello = getPort();

      UserType in0 = new UserType("Kermit");
      UserType retObj = hello.echoUserType(in0);
      assertEquals(in0, retObj);
   }

   private Hello getPort() throws Exception
   {
      if (isTargetJBoss())
      {
         InitialContext iniCtx = getInitialContext();
         Service service = (Service)iniCtx.lookup("java:comp/env/service/HelloService");
         Hello port = (Hello)service.getPort(Hello.class);
         return port;
      }
      else
      {
         ServiceFactoryImpl factory = new ServiceFactoryImpl();
         URL wsdlURL = new File("resources/jaxrpc/samples/wssecurity/WEB-INF/wsdl/HelloService.wsdl").toURL();
         URL mappingURL = new File("resources/jaxrpc/samples/wssecurity/WEB-INF/jaxrpc-mapping.xml").toURL();
         URL securityURL = new File("resources/jaxrpc/samples/wssecurity/simple-encrypt/META-INF/jboss-wsse-client.xml").toURL();

         QName serviceName = new QName("http://org.jboss.ws/samples/wssecurity", "HelloService");
         ServiceImpl service = (ServiceImpl)factory.createService(wsdlURL, serviceName, mappingURL, securityURL);
         
         Hello port = (Hello)service.getPort(Hello.class);
         
         ((Stub)port)._setProperty(Stub.ENDPOINT_ADDRESS_PROPERTY, "http://" + getServerHost() + ":8080/jaxrpc-samples-wssecurity-encrypt");
         ((StubExt)port).setConfigName("Standard WSSecurity Client");
         
         return port;
      }
   }
}
