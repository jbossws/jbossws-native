package org.jboss.test.ws.jaxws.wsrm.services;

import java.util.concurrent.Future;

import javax.jws.WebMethod;
import javax.jws.WebService;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

@WebService
public interface ReqResServiceIface
{
   @WebMethod(operationName = "echo")
   public Response<String> echoAsync(String s);

   @WebMethod(operationName = "echo")
   public Future<?> echoAsync(String s, AsyncHandler<String> h);

   @WebMethod
   public String echo(String s);
}
