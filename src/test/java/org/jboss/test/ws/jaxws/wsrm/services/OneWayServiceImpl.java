package org.jboss.test.ws.jaxws.wsrm.services;

import javax.jws.Oneway;
import javax.jws.WebService;

import org.jboss.logging.Logger;
import org.jboss.ws.annotation.EndpointConfig;

import java.util.Arrays;

@WebService
(
   name = "OneWay",
   serviceName = "OneWayService",
   wsdlLocation = "WEB-INF/wsdl/OneWayService.wsdl",
   targetNamespace = "http://www.jboss.org/jbossws/ws-extensions/wsrm",
   endpointInterface = "org.jboss.test.ws.jaxws.wsrm.services.OneWayServiceIface"
)
@EndpointConfig
(
   configName = "Standard WSRM Endpoint",
   configFile = "WEB-INF/wsrm-jaxws-endpoint-config.xml"
)
public class OneWayServiceImpl implements OneWayServiceIface
{
   private Logger log = Logger.getLogger(OneWayServiceImpl.class);

   @Oneway
   public void method1()
   {
      log.info("method1()");
   }

   @Oneway
   public void method2(String s)
   {
      log.info("method2(" + s + ")");
   }

   @Oneway
   public void method3(String[] sa)
   {
      log.info("method3(" + Arrays.asList(sa) + ")");
   }
}
