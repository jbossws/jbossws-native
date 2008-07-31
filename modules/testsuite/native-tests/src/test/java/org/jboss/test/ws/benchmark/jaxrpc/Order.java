/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.test.ws.benchmark.jaxrpc;

import java.io.Serializable;
import java.util.Calendar;

/**
 * A Order.
 * 
 * @author <a href="anders.hedstrom@home.se">Anders Hedstrom</a>
 */
public class Order implements Serializable
{

   protected int orderId;
   protected int orderStatus;
   protected Calendar orderDate;
   protected float orderTotalAmount;
   protected Customer customer;
   protected LineItem[] lineItems;
   
   public Order()
   {
   }

   
   /**
    * @param orderId
    * @param orderStatus
    * @param orderDate
    * @param orderTotalAmount
    * @param customer
    * @param lineItems
    */
   public Order(int orderId, int orderStatus, Calendar orderDate,
         float orderTotalAmount, Customer customer, LineItem[] lineItems)
   {
      super();
      this.orderId = orderId;
      this.orderStatus = orderStatus;
      this.orderDate = orderDate;
      this.orderTotalAmount = orderTotalAmount;
      this.customer = customer;
      this.lineItems = lineItems;
   }
   
   
   public Customer getCustomer()
   {
      return customer;
   }
   public void setCustomer(Customer customer)
   {
      this.customer = customer;
   }
   public LineItem[] getLineItems()
   {
      return lineItems;
   }
   public void setLineItems(LineItem[] lineItems)
   {
      this.lineItems = lineItems;
   }
   public Calendar getOrderDate()
   {
      return orderDate;
   }
   public void setOrderDate(Calendar orderDate)
   {
      this.orderDate = orderDate;
   }
   public int getOrderId()
   {
      return orderId;
   }
   public void setOrderId(int orderId)
   {
      this.orderId = orderId;
   }
   public int getOrderStatus()
   {
      return orderStatus;
   }
   public void setOrderStatus(int orderStatus)
   {
      this.orderStatus = orderStatus;
   }
   public float getOrderTotalAmount()
   {
      return orderTotalAmount;
   }
   public void setOrderTotalAmount(float orderTotalAmount)
   {
      this.orderTotalAmount = orderTotalAmount;
   }
}
