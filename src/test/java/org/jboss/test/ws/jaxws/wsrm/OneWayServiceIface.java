package org.jboss.test.ws.jaxws.wsrm;

import javax.jws.Oneway;
import javax.jws.WebService;
import javax.xml.ws.addressing.Action;

import org.jboss.ws.annotation.EndpointConfig;

@WebService
@EndpointConfig(configName = "Standard WSRM Client", configFile = "META-INF/wsrm-jaxws-client-config.xml")
public interface OneWayServiceIface
{
   @Oneway
   @Action(input="http://wsrm.example/oneway/method1")
   void method1();
   
   @Action(input="http://wsrm.example/oneway/method2")
   @Oneway
   void method2(String s);
   
   @Action(input="http://wsrm.example/oneway/method3")
   @Oneway
   void method3(String[] sa);
}
