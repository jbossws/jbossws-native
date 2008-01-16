package org.jboss.test.ws.jaxws.jbws1841;

import junit.framework.Test;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.naming.InitialContext;
import java.net.URL;

/**
 * Serviceref thorugh ejb3 deployment descriptor.
 *
 * http://jira.jboss.org/jira/browse/JBWS-1841
 *
 * @author Heiko.Braun@jboss.com
 * @since 09-Oct-2007
 */
public class JBWS1841TestCase extends JBossWSTest
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-jbws1841/EJB3Bean";

   private static EndpointInterface port;
   private static StatelessRemote remote;

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS1841TestCase.class, "jaxws-jbws1841.jar");
   }

   protected void setUp() throws Exception
   {
      if (port == null)
      {
         URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
         QName serviceName = new QName("http://www.openuri.org/2004/04/HelloWorld", "TestService");
         port = Service.create(wsdlURL, serviceName).getPort(EndpointInterface.class);


         InitialContext ctx = new InitialContext();
         remote = (StatelessRemote)ctx.lookup("/StatelessBean/remote");
      }
   }

   /**
    * Check if the servce was deploed correctly
    * @throws Exception
    */
   public void testDirectWSInvocation() throws Exception
   {
      String result = port.echo("DirectWSInvocation");
      assertEquals("DirectWSInvocation", result);

   }

   public void testEJBRelay1() throws Exception
   {
      String result = remote.echo1("Relay1");
      assertEquals("Relay1", result);
   }

   public void testEJBRelay2() throws Exception
   {
      String result = remote.echo2("Relay2");
      assertEquals("Relay2", result);
   }


   public void testEJBRelay3() throws Exception
   {
      String result = remote.echo3("Relay3");
      assertEquals("Relay3", result);
   }

   public void testEJBRelay4() throws Exception
   {
      String result = remote.echo4("Relay4");
      assertEquals("Relay4", result);
   }


}
