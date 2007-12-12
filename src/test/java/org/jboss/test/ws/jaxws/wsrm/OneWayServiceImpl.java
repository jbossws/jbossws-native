package org.jboss.test.ws.jaxws.wsrm;

import javax.jws.Oneway;
import javax.jws.WebService;
import javax.xml.ws.addressing.Action;

import org.jboss.logging.Logger;
import org.jboss.ws.annotation.EndpointConfig;
import org.jboss.ws.extensions.policy.PolicyScopeLevel;
import org.jboss.ws.extensions.policy.annotation.Policy;
import org.jboss.ws.extensions.policy.annotation.PolicyAttachment;

import java.util.Arrays;

@WebService
(
   name = "OneWay",
   serviceName = "OneWayService",
   endpointInterface = "org.jboss.test.ws.jaxws.wsrm.OneWayServiceIface"
)
@PolicyAttachment
(
   @Policy
   (
      policyFileLocation = "WEB-INF/wsrm-exactly-once-in-order-policy.xml",
      scope = PolicyScopeLevel.WSDL_BINDING
   )
)
@EndpointConfig
(
   configName = "Standard WSRM Endpoint",
   configFile = "WEB-INF/wsrm-jaxws-endpoint-config.xml"
)
public class OneWayServiceImpl implements OneWayServiceIface
{
   private Logger log = Logger.getLogger(OneWayServiceImpl.class);

   @Action(input="http://wsrm.example/oneway/method1")
   @Oneway
   public void method1()
   {
      log.info("method1()");
   }

   @Action(input="http://wsrm.example/oneway/method2")
   @Oneway
   public void method2(String s)
   {
      log.info("method2(" + s + ")");
   }

   @Action(input="http://wsrm.example/oneway/method3")
   @Oneway
   public void method3(String[] sa)
   {
      log.info("method3(" + Arrays.asList(sa) + ")");
   }
}
