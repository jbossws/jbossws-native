/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.test.ws.benchmark.jaxrpc;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import java.rmi.RemoteException;

/**
 * A BenchmarkEJBEndpoint.
 * 
 * @author <a href="anders.hedstrom@home.se">Anders Hedstrom</a>
 */
public class BenchmarkEJBEndpoint implements SessionBean, BenchmarkService
{

   public SimpleUserType echoSimpleType(SimpleUserType simpleUserType) throws RemoteException
   {
      return simpleUserType;
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
      return (new OrderBL()).getOrder(orderId, customerId);
   }

   
   // ---- lifecycle methods -------
   public void setSessionContext(SessionContext ctx) throws EJBException, RemoteException
   {
   }

   public void ejbRemove() throws EJBException, RemoteException
   {
   }

   public void ejbActivate() throws EJBException, RemoteException
   {

   }

   public void ejbPassivate() throws EJBException, RemoteException
   {
   }
   
   public void ejbCreate() throws CreateException 
   {
      
   }

}
