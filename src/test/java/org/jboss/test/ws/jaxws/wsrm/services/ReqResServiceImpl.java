package org.jboss.test.ws.jaxws.wsrm.services;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.jboss.logging.Logger;
import org.jboss.ws.annotation.EndpointConfig;

@WebService
(
   name = "ReqRes",
   serviceName = "ReqResService",
   wsdlLocation = "WEB-INF/wsdl/ReqResService.wsdl",
   targetNamespace = "http://www.jboss.org/jbossws/ws-extensions/wsrm",
   endpointInterface = "org.jboss.test.ws.jaxws.wsrm.services.ReqResServiceIface"
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
