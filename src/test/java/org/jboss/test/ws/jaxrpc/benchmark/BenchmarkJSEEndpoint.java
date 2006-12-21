/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.test.ws.jaxrpc.benchmark;

import java.rmi.RemoteException;

/**
 * A BenchmarkJSEEndpoint.
 * 
 * @author <a href="anders.hedstrom@home.se">Anders Hedstrom</a>
 * @version $Revision$
 */
public class BenchmarkJSEEndpoint implements BenchmarkService {


   public SimpleUserType echoSimpleType(SimpleUserType simpleType) throws RemoteException
   {
      return simpleType;
   }

   public SimpleUserType[] echoArrayOfSimpleUserType(SimpleUserType[] array) throws RemoteException
   {
      return array;
   }

   public Synthetic echoSynthetic(Synthetic synthetic) throws RemoteException
   {
      return synthetic;
   }

   public Order getOrder(int orderId, int customerId) throws RemoteException
   {
      return (new OrderBL()).getOrder(orderId,customerId);
   }

}
