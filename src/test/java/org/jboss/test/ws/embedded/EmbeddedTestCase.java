/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.test.ws.embedded;

import junit.framework.TestCase;
import org.jboss.wsf.common.ResourceLoaderAdapter;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.DeploymentModelFactory;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.stack.jbws.embedded.EmbeddableWSFRuntime;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Heiko.Braun <heiko.braun@jboss.com>
 */
public class EmbeddedTestCase extends TestCase
{

   /**
    * During instantation the the standalone container
    * goes through the SPIProvider to get all required  {@link org.jboss.wsf.spi.SPIView}'s
    * to properly staff itself.
    * However this requires a successfully bootstrapped container.
    * @throws Exception
    */
   public void testEmbeddedContainer() throws Exception
   {
      SPIProvider spi = SPIProviderResolver.getInstance().getProvider();
      DeploymentModelFactory modelFactory = spi.getSPI(DeploymentModelFactory.class);

      // Deployment
      ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
      Deployment dep = modelFactory.newDeployment("HelloWorldDeployment", contextClassLoader);
      dep.setRuntimeClassLoader(contextClassLoader);

      // TODO: Hack, should this become another DeploymentAspect?
      ((ArchiveDeployment)dep).setRootFile(new ResourceLoaderAdapter());
      
      dep.setType(Deployment.DeploymentType.JAXWS_JSE);
      dep.setService(modelFactory.newService());

      // Service
      dep.getService().setContextRoot("/hello");

      // Endpoint
      Endpoint endpoint = modelFactory.newEndpoint("org.jboss.test.ws.embedded.HelloWorldEndpoint");
      endpoint.setShortName("hello");
      endpoint.setURLPattern("/endpoint");      
      dep.getService().addEndpoint(endpoint);

      // Publish
      EmbeddableWSFRuntime container = EmbeddableWSFRuntime.bootstrap( EmbeddableWSFRuntime.DEFAULT_CONFIG_URL);
      container.create(dep);
      container.start(dep);

      // Invoke it
      String wsdl = GETRequest("/hello/endpoint?wsdl");
      assertNotNull("Unable to retrieve WSDL", wsdl);      

      Service service = Service.create(
        new URL("http://localhost:20000/hello/endpoint?wsdl"),
        new QName("http://embedded.ws.test.jboss.org/", "HelloWorldEndpointService")
        );

      HelloWorldSEI port = service.getPort(HelloWorldSEI.class);
      String response = port.hello("StandaloneContainer");
      assertEquals("Hello StandaloneContainer", response);

      // Remove 
      container.stop(dep);
      container.destroy(dep);
      try
      {
         wsdl = GETRequest("/hello/endpoint?wsdl");
      } catch (IOException e)
      {
         //
      }

      // The root context accepts the request
      assertEquals("JBossWS HttpDeamon", wsdl);
   }

   public static String GETRequest(String context)
     throws IOException
   {
      if(context.startsWith("/"))
         context = context.substring(1);

      URLConnection con = new URL("http://localhost:20000/"+context).openConnection();
      con.connect();

      BufferedReader in = new BufferedReader(
        new InputStreamReader(
          con.getInputStream()
        )
      );

      String inputLine;
      StringBuffer sb = new StringBuffer();
      while ((inputLine = in.readLine()) != null)
         sb.append(inputLine);
      in.close();
      return sb.toString();
   }   
}
