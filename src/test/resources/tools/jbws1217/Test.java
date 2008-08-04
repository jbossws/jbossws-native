/*
 * JBossWS WS-Tools Generated Source
 *
 * Generation Date: Wed Mar 12 14:18:42 IST 2008
 *
 * This generated source code represents a derivative work of the input to
 * the generator that produced it. Consult the input for the copyright and
 * terms of use that apply to this source code.
 */
package org.jboss.test.ws.jbws1217;
public interface  Test extends java.rmi.Remote
{

  public org.jboss.test.ws.jbws1217.base.BaseException  getException() throws org.jboss.test.ws.jbws1217.exception.TestException, java.rmi.RemoteException;
  public org.jboss.test.ws.jbws1217.base.BaseException[]  getExceptions() throws org.jboss.test.ws.jbws1217.exception.TestException, java.rmi.RemoteException;
  public void  setException(org.jboss.test.ws.jbws1217.base.BaseException baseException_1) throws org.jboss.test.ws.jbws1217.exception.TestException, java.rmi.RemoteException;
  public void  setExceptions(org.jboss.test.ws.jbws1217.base.BaseException[] arrayOfBaseException_1) throws org.jboss.test.ws.jbws1217.exception.TestException, java.rmi.RemoteException;
  public void  testException() throws org.jboss.test.ws.jbws1217.exception.TestException, java.rmi.RemoteException;
}
