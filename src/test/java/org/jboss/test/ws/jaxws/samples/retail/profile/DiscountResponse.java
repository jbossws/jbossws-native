
package org.jboss.test.ws.jaxws.samples.retail.profile;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.jboss.test.ws.jaxws.samples.retail.Customer;


/**
 * <p>Java class for discountResponse complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="discountResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="customer" type="{http://org.jboss.ws/samples/retail}customer" minOccurs="0"/>
 *         &lt;element name="discount" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "discountResponse", propOrder = {
    "customer",
    "discount"
    })
public class DiscountResponse {

   protected Customer customer;
   protected double discount;

   public DiscountResponse() {
   }

   public DiscountResponse(Customer customer, double discount) {
      this.customer = customer;
      this.discount = discount;
   }

   /**
    * Gets the value of the customer property.
    *
    * @return
    *     possible object is
    *     {@link Customer }
    *
    */
   public Customer getCustomer() {
      return customer;
   }

   /**
    * Sets the value of the customer property.
    *
    * @param value
    *     allowed object is
    *     {@link Customer }
    *
    */
   public void setCustomer(Customer value) {
      this.customer = value;
   }

   /**
    * Gets the value of the discount property.
    *
    */
   public double getDiscount() {
      return discount;
   }

   /**
    * Sets the value of the discount property.
    *
    */
   public void setDiscount(double value) {
      this.discount = value;
   }

}
