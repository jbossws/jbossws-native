// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation (1.1.3, build R1)
// Generated source version: 1.1.3

package org.jboss.test.ws.jaxws.jsr181.complex.client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Calendar;

public interface RegistrationService extends Remote
{
   public long[] bulkRegister(Customer[] customers, Calendar when) throws AlreadyRegisteredException, ValidationException, RemoteException;

   public Statistics getStatistics(Customer customer) throws RemoteException;

   public long register(Customer customer, Calendar when) throws ValidationException, AlreadyRegisteredException, RemoteException;

   public boolean registerForInvoice(InvoiceCustomer invoiceCustomer) throws ValidationException, AlreadyRegisteredException, RemoteException;
}
