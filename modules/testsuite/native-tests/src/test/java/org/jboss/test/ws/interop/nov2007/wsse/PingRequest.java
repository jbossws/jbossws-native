
package org.jboss.test.ws.interop.nov2007.wsse;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://xmlsoap.org/Ping}Ping" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "ping"
})
@XmlRootElement(name = "PingRequest")
public class PingRequest {

    @XmlElement(name = "Ping", namespace = "http://xmlsoap.org/Ping")
    protected Ping ping;

    /**
     * Gets the value of the ping property.
     * 
     * @return
     *     possible object is
     *     {@link Ping }
     *     
     */
    public Ping getPing() {
        return ping;
    }

    /**
     * Sets the value of the ping property.
     * 
     * @param value
     *     allowed object is
     *     {@link Ping }
     *     
     */
    public void setPing(Ping value) {
        this.ping = value;
    }

}
