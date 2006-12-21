package org.jboss.test.ws.jaxws.client;

import junit.framework.TestCase;

import javax.xml.ws.Service;
import javax.xml.namespace.QName;
import java.io.File;
import java.net.URL;

public class ServiceDelegateTestCase extends TestCase {

   String WSDL_LOC = "resources/jaxws/client/META-INF/MTOMTestService.wsdl";

   public void testPortBySEI() throws Exception
   {
      File f = new File(WSDL_LOC);
      assertTrue("WSDL file not found", f.exists());

      URL wsdlLocation = new URL("file:" +WSDL_LOC);
      QName serviceName = new QName("http://mtomtestservice.org/wsdl", "MTOMTestService");
      Service service = Service.create( wsdlLocation, serviceName );

      MTOMTest port = service.getPort(MTOMTest.class);
      assertNotNull("Failed to create port.", port);

   }


}
