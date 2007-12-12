package org.jboss.test.ws.jaxws.wsrm;

import java.util.concurrent.Future;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

import javax.xml.ws.addressing.Action;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

import org.jboss.ws.annotation.EndpointConfig;

@WebService(name = "ReqRes", targetNamespace = "http://org.jboss.ws/jaxws/wsrm")
@SOAPBinding(style = Style.RPC)
@EndpointConfig
(
   configName = "Standard WSRM Client",
   configFile = "META-INF/wsrm-jaxws-client-config.xml"
)
public interface ReqResServiceIface
{
   @WebMethod(operationName = "echo")
   @Action(input="http://wsrm.example/reqres/echo/input", output="http://wsrm.example/reqres/echo/output")
   public Response<String> echoAsync(@WebParam(name = "String_1") String string1);

   @WebMethod(operationName = "echo")
   @Action(input="http://wsrm.example/reqres/echo/input", output="http://wsrm.example/reqres/echo/output")
   public Future<?> echoAsync(@WebParam(name = "String_1") String string1, @WebParam(name = "asyncHandler") AsyncHandler<String> asyncHandler);

   @WebMethod
   @WebResult(name = "result")
   @Action(input="http://wsrm.example/reqres/echo/input", output="http://wsrm.example/reqres/echo/output")
   public String echo(@WebParam(name = "String_1") String string1);
}
