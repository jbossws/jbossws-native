
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
 *         &lt;element ref="{http://xmlsoap.org/Ping}PingResponse" minOccurs="0"/>
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
    "pingResponse"
})
@XmlRootElement(name = "PingResponse")
public class PingResponse {

    @XmlElement(name = "PingResponse", namespace = "http://xmlsoap.org/Ping")
    protected PingResponseBody pingResponse;

    /**
     * Gets the value of the pingResponse property.
     * 
     * @return
     *     possible object is
     *     {@link PingResponseBody }
     *     
     */
    public PingResponseBody getPingResponse() {
        return pingResponse;
    }

    /**
     * Sets the value of the pingResponse property.
     * 
     * @param value
     *     allowed object is
     *     {@link PingResponseBody }
     *     
     */
    public void setPingResponse(PingResponseBody value) {
        this.pingResponse = value;
    }

}
