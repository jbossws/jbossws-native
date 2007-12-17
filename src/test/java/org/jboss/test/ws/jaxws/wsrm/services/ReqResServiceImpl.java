package org.jboss.test.ws.jaxws.wsrm.services;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.jboss.logging.Logger;
import org.jboss.ws.annotation.EndpointConfig;
import org.jboss.ws.extensions.policy.PolicyScopeLevel;
import org.jboss.ws.extensions.policy.annotation.Policy;
import org.jboss.ws.extensions.policy.annotation.PolicyAttachment;

@WebService
(
   name = "ReqRes",
   serviceName = "ReqResService",
   targetNamespace = "http://org.jboss.ws/jaxws/wsrm",
   endpointInterface = "org.jboss.test.ws.jaxws.wsrm.services.ReqResServiceIface"
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
public class ReqResServiceImpl
{
   private static Logger log = Logger.getLogger(ReqResServiceImpl.class);

   @WebMethod
   public String echo(String s)
   {
      log.info("echo(" + s + ")");
      return s;
   }
}
