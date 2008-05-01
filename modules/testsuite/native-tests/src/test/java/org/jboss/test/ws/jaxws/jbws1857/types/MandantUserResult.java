
package org.jboss.test.ws.jaxws.jbws1857.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for mandantUserResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="mandantUserResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="mandantUser" type="{http://example.com}mandantUser" minOccurs="0"/>
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
@XmlType(name = "mandantUserResult", propOrder = {
    "mandantUser",
    "serviceStatus"
})
public class MandantUserResult {

    protected MandantUser mandantUser;
    protected ServiceStatus serviceStatus;

    /**
     * Gets the value of the mandantUser property.
     * 
     * @return
     *     possible object is
     *     {@link MandantUser }
     *     
     */
    public MandantUser getMandantUser() {
        return mandantUser;
    }

    /**
     * Sets the value of the mandantUser property.
     * 
     * @param value
     *     allowed object is
     *     {@link MandantUser }
     *     
     */
    public void setMandantUser(MandantUser value) {
        this.mandantUser = value;
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
