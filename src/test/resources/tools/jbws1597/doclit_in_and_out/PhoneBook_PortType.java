/*
 * JBossWS WS-Tools Generated Source
 *
 * Generation Date: Tue May 29 17:26:46 CEST 2007
 *
 * This generated source code represents a derivative work of the input to
 * the generator that produced it. Consult the input for the copyright and
 * terms of use that apply to this source code.
 */
package org.jboss.test.ws.jbws1597;
public interface  PhoneBook_PortType extends java.rmi.Remote
{

  public org.jboss.test.ws.jbws1597.TelephoneNumber  lookup(org.jboss.test.ws.jbws1597.Person lookup, org.jboss.test.ws.jbws1597.NickName inHeader, org.jboss.test.ws.jbws1597.BillingAccountHolder outHeader) throws  java.rmi.RemoteException;
}