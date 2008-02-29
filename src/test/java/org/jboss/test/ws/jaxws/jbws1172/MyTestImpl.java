package org.jboss.test.ws.jaxws.jbws1172;

import javax.jws.WebService;

import org.jboss.logging.Logger;
import org.jboss.ws.annotation.EndpointConfig;

@WebService(serviceName="MyTestService", targetNamespace="http://www.my-company.it/ws/my-test", endpointInterface = "org.jboss.test.ws.jaxws.jbws1172.types.MyTest")
@EndpointConfig
public class MyTestImpl
{
   // provide logging
   private static Logger log = Logger.getLogger(MyTestImpl.class);
   
   public void performTest(Long code) 
   {
      log.info(code);
   }
}
