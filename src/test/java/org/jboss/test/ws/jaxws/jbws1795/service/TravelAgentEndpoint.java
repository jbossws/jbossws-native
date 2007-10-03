package org.jboss.test.ws.jaxws.jbws1795.service;

import java.rmi.RemoteException;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.jboss.test.ws.jaxws.jbws1795.handcoded.ListLocation;

@WebService
public interface TravelAgentEndpoint
{

   @WebMethod
   public ListLocation passLocation(@WebParam (name="ListLocation") ListLocation listLocation)
   throws RemoteException;

}
