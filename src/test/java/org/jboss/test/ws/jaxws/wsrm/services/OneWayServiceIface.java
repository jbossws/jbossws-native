package org.jboss.test.ws.jaxws.wsrm.services;

import javax.jws.Oneway;
import javax.jws.WebService;

@WebService
(
   targetNamespace = "http://www.jboss.org/jbossws/ws-extensions/wsrm"
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
