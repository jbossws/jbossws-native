/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.test.ws.benchmark.jaxrpc;

import java.util.Calendar;


/**
 * A Customer.
 * 
 * @author <a href="anders.hedstrom@home.se">Anders Hedstrom</a>
 * @version $Revision: 1757 $
 */
public class Customer {
    protected int customerId;
    protected String contactFirstName;
    protected String contactLastName;
    protected String contactPhone;
    protected Calendar lastActivityDate;
    protected String creditCardNumber;
    protected String creditCardExpirationDate;
    protected Address billingAddress;
    protected Address shippingAddress;
    
    public Customer() {
    }
    
    public Customer(int customerId, String contactFirstName, String contactLastName, String contactPhone, Calendar lastActivityDate, String creditCardNumber, String creditCardExpirationDate, Address billingAddress, Address shippingAddress) {
        this.customerId = customerId;
        this.contactFirstName = contactFirstName;
        this.contactLastName = contactLastName;
        this.contactPhone = contactPhone;
        this.lastActivityDate = lastActivityDate;
        this.creditCardNumber = creditCardNumber;
        this.creditCardExpirationDate = creditCardExpirationDate;
        this.billingAddress = billingAddress;
        this.shippingAddress = shippingAddress;
    }
    
    public int getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }
    
    public String getContactFirstName() {
        return contactFirstName;
    }
    
    public void setContactFirstName(String contactFirstName) {
        this.contactFirstName = contactFirstName;
    }
    
    public String getContactLastName() {
        return contactLastName;
    }
    
    public void setContactLastName(String contactLastName) {
        this.contactLastName = contactLastName;
    }
    
    public String getContactPhone() {
        return contactPhone;
    }
    
    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }
    
    public Calendar getLastActivityDate() {
        return lastActivityDate;
    }
    
    public void setLastActivityDate(Calendar lastActivityDate) {
        this.lastActivityDate = lastActivityDate;
    }
    
    public String getCreditCardNumber() {
        return creditCardNumber;
    }
    
    public void setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }
    
    public String getCreditCardExpirationDate() {
        return creditCardExpirationDate;
    }
    
    public void setCreditCardExpirationDate(String creditCardExpirationDate) {
        this.creditCardExpirationDate = creditCardExpirationDate;
    }
    
    public Address getBillingAddress() {
        return billingAddress;
    }
    
    public void setBillingAddress(Address billingAddress) {
        this.billingAddress = billingAddress;
    }
    
    public Address getShippingAddress() {
        return shippingAddress;
    }
    
    public void setShippingAddress(Address shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
}
