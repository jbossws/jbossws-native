package org.jboss.test.ws.jaxws.jbws1665;

public interface TrackingServiceInterface {
  public TracePollData pollTraces(String customer, String mark) throws NullPointerException;
  
  public void requestTrace(String customer, String[] terminals) throws NullPointerException;
}
