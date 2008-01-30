package org.jboss.test.ws.jaxws.wsrm.services;

import javax.jws.Oneway;
import javax.jws.WebService;

@WebService
public interface OneWayServiceIface
{
   @Oneway
   void method1();
   
   @Oneway
   void method2(String s);
   
   @Oneway
   void method3(String[] sa);
}
