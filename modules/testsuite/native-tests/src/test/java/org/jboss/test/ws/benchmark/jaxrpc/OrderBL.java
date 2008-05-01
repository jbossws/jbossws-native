/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.test.ws.benchmark.jaxrpc;

import java.util.ArrayList;
import java.util.GregorianCalendar;

/**
 * A OrderBL.
 * 
 * @author <a href="anders.hedstrom@home.se">Anders Hedstrom</a>
 * @version $Revision: 1757 $
 */
public class OrderBL {
   
    public OrderBL() {
	}

	public Order getOrder(int orderId, int customerId)
	{
		int id = 1;
		Address ship = new Address("Ship FirstName " + id, "Ship LastName " + id, "Ship StreetAddres " + id, "Street Address Line 2 " + id, "City " + id, "State " + id, "12345");
		Address bill = new Address("Bill FirstName " + id, "Bil1 LastName " + id, "Bill StreetAddres " + id, "Street Address Line 2 " + id, "City " + id, "State " + id, "12345");

		Customer customer = new Customer(customerId, "FirstName " + id, "LastName " + id, Integer.toString(id), new GregorianCalendar(), Integer.toString(id), Integer.toString(id), bill, ship);

		int numberLineItems = orderId;

		ArrayList lines = new ArrayList();

		for(int i = 0; i < numberLineItems; i++)
		{
			LineItem line = new LineItem(orderId, i+1, i, "Test Product " +i, 1, (float) 1.00);

			lines.add(line);
		}

		
		Order order = new Order(orderId, 1, new GregorianCalendar(), (float) 50, customer, (LineItem[])lines.toArray(new LineItem[0]) );

		return order;
	}
}
