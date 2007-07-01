package org.jboss.test.ws.jaxws.jbws1665;

import java.rmi.RemoteException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.ejb.FinderException;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@Stateless
@WebService(name = "TrackingService", targetNamespace = "http://fleetworks.acunia.com/fleet/service")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class TrackingServiceBean implements TrackingServiceInterface {  
  @WebMethod
  public TracePollData pollTraces(
     @WebParam(name="customer") String customer, 
     @WebParam(name="mark") String mark) throws NullPointerException
  {
     return null;
  }
  
  @WebMethod
  public void requestTrace(
     @WebParam(name="customer") String customer, 
     @WebParam(name="terminals") String[] terminals) 
  throws NullPointerException
  {
  }
}
