package org.jboss.test.ws.jaxws.wsrm.services;

import javax.jws.Oneway;
import javax.jws.WebService;

import org.jboss.ws.annotation.EndpointConfig;

@WebService
@EndpointConfig
(
   configName = "Standard WSRM Client",
   configFile = "META-INF/wsrm-jaxws-client-config.xml"
)
public interface OneWayServiceIface
{
   @Oneway
   void method1();
   
   @Oneway
   void method2(String s);
   
   @Oneway
   void method3(String[] sa);
}
