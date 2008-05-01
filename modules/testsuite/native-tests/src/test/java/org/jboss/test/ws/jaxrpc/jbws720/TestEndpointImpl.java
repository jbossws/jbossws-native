package org.jboss.test.ws.jaxrpc.jbws720;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.jboss.logging.Logger;

public class TestEndpointImpl implements TestEndpoint, Remote
{
   private static Logger log = Logger.getLogger(TestEndpointImpl.class);

   public GetPropertyResponse getProperty(GetProperty getp) throws RemoteException
   {
      log.info("getProperty: " + getp);
      return new GetPropertyResponse(getp.toString());
   }
}
