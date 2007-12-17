package org.jboss.test.ws.jaxws.wsrm.services;

import javax.jws.Oneway;
import javax.jws.WebService;

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
   targetNamespace = "http://org.jboss.ws/jaxws/wsrm",
   endpointInterface = "org.jboss.test.ws.jaxws.wsrm.services.OneWayServiceIface"
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
