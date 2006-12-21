
package org.jboss.test.ws.jaxws.samples.retail.profile;

import org.jboss.test.ws.jaxws.samples.retail.Customer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for discountRequest complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="discountRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="customer" type="{http://org.jboss.ws/samples/retail}customer" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "discountRequest", propOrder = {
    "customer"
    })
public class DiscountRequest {

   protected Customer customer;

   public DiscountRequest() {
   }

   public DiscountRequest(Customer customer) {
      this.customer = customer;
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

}
