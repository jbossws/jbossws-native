package org.jboss.test.ws.jaxws.samples.asynchronous;

import java.util.concurrent.Future;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

/**
 * This class was generated by the JAXWS SI.
 * JAX-WS RI 2.0_01-b59-fcs
 * Generated source version: 2.0
 * 
 */
@WebService(name = "TestEndpoint", targetNamespace = "http://org.jboss.ws/jaxws/asynchronous")
@SOAPBinding(style = Style.RPC)
public interface TestEndpoint
{
   @WebMethod(operationName = "echo")
   public Response<String> echoAsync(@WebParam(name = "String_1", partName = "String_1")
   String string1);

   @WebMethod(operationName = "echo")
   public Future<?> echoAsync(@WebParam(name = "String_1", partName = "String_1")
   String string1, @WebParam(name = "asyncHandler", partName = "asyncHandler")
   AsyncHandler<String> asyncHandler);

   @WebMethod
   @WebResult(name = "result", partName = "result")
   public String echo(@WebParam(name = "String_1", partName = "String_1")
   String string1);

}
