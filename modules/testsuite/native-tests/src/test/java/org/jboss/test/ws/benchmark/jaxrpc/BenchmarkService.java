/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.test.ws.benchmark.jaxrpc;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * BenchmarkService.
 * 
 * @author <a href="anders.hedstrom@home.se">Anders Hedstrom</a>
 */
public interface BenchmarkService extends Remote {
   
	public SimpleUserType echoSimpleType(SimpleUserType simpleUserType) throws RemoteException;
	
	public SimpleUserType[] echoArrayOfSimpleUserType(SimpleUserType[] array) throws RemoteException;
	
	public Synthetic echoSynthetic(Synthetic synthetic) throws RemoteException;
	
	public Order getOrder(int orderId, int customerId) throws RemoteException;
}
