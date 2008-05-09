
package org.jboss.test.ws.jaxws.jbws1857.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for glaeubigerResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="glaeubigerResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="glaeubiger" type="{http://example.com}glaeubiger" minOccurs="0"/>
 *         &lt;element name="serviceStatus" type="{http://example.com}serviceStatus" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "glaeubigerResult", propOrder = {
    "glaeubiger",
    "serviceStatus"
})
public class GlaeubigerResult {

    protected Glaeubiger glaeubiger;
    protected ServiceStatus serviceStatus;

    /**
     * Gets the value of the glaeubiger property.
     * 
     * @return
     *     possible object is
     *     {@link Glaeubiger }
     *     
     */
    public Glaeubiger getGlaeubiger() {
        return glaeubiger;
    }

    /**
     * Sets the value of the glaeubiger property.
     * 
     * @param value
     *     allowed object is
     *     {@link Glaeubiger }
     *     
     */
    public void setGlaeubiger(Glaeubiger value) {
        this.glaeubiger = value;
    }

    /**
     * Gets the value of the serviceStatus property.
     * 
     * @return
     *     possible object is
     *     {@link ServiceStatus }
     *     
     */
    public ServiceStatus getServiceStatus() {
        return serviceStatus;
    }

    /**
     * Sets the value of the serviceStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link ServiceStatus }
     *     
     */
    public void setServiceStatus(ServiceStatus value) {
        this.serviceStatus = value;
    }

}
