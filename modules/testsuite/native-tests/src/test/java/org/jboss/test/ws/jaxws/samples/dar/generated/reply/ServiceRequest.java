
package org.jboss.test.ws.jaxws.samples.dar.generated.reply;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for serviceRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="serviceRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="from" type="{http://org.jboss.ws/samples/dar}stop" minOccurs="0"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="people" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="to" type="{http://org.jboss.ws/samples/dar}stop" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "serviceRequest", propOrder = {
    "from",
    "id",
    "people",
    "to"
})
public class ServiceRequest {

    protected Stop from;
    protected String id;
    protected int people;
    protected Stop to;

    /**
     * Gets the value of the from property.
     * 
     * @return
     *     possible object is
     *     {@link Stop }
     *     
     */
    public Stop getFrom() {
        return from;
    }

    /**
     * Sets the value of the from property.
     * 
     * @param value
     *     allowed object is
     *     {@link Stop }
     *     
     */
    public void setFrom(Stop value) {
        this.from = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the people property.
     * 
     */
    public int getPeople() {
        return people;
    }

    /**
     * Sets the value of the people property.
     * 
     */
    public void setPeople(int value) {
        this.people = value;
    }

    /**
     * Gets the value of the to property.
     * 
     * @return
     *     possible object is
     *     {@link Stop }
     *     
     */
    public Stop getTo() {
        return to;
    }

    /**
     * Sets the value of the to property.
     * 
     * @param value
     *     allowed object is
     *     {@link Stop }
     *     
     */
    public void setTo(Stop value) {
        this.to = value;
    }

}
