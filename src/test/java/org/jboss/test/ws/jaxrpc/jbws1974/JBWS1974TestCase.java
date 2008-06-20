package org.jboss.test.ws.jaxrpc.jbws1974;

import java.io.File;
import java.net.URL;

import javax.naming.InitialContext;
import javax.xml.rpc.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

public class JBWS1974TestCase extends JBossWSTest
{

   private static TestEndpoint port;

   public static Test suite() throws Exception
   {
      return new JBossWSTestSetup(JBWS1974TestCase.class, "jaxrpc-jbws1974.war, jaxrpc-jbws1974-client.jar");
   }

   public void setUp() throws Exception
   {
      super.setUp();
      if (port == null)
      {
         InitialContext iniCtx = getInitialContext();
         Service service = (Service)iniCtx.lookup("java:comp/env/service/TestService");
         port = (TestEndpoint)service.getPort(TestEndpoint.class);
      }
   }

   public void testCall() throws Exception
   {
      // It is the null value that is required to trigger the failure.
      EchoType toEcho = new EchoType("A", "b", null);

      EchoType response = port.echo(toEcho);

      assertEquals(toEcho.getMessage_1(), response.getMessage_1());
      assertEquals(toEcho.getMessage_2(), response.getMessage_2());
      assertEquals(toEcho.getMessage_3(), response.getMessage_3());
   }

}
