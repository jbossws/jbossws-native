
package org.jboss.test.ws.jaxws.jbws1795.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for passLocation complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="passLocation">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ListLocation" type="{http://service.jbws1795.jaxws.ws.test.jboss.org/}listLocation" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "passLocation", propOrder = {
    "listLocation"
})
public class PassLocation {

    @XmlElement(name = "ListLocation")
    protected ListLocation listLocation;

    /**
     * Gets the value of the listLocation property.
     * 
     * @return
     *     possible object is
     *     {@link ListLocation }
     *     
     */
    public ListLocation getListLocation() {
        return listLocation;
    }

    /**
     * Sets the value of the listLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link ListLocation }
     *     
     */
    public void setListLocation(ListLocation value) {
        this.listLocation = value;
    }

}
