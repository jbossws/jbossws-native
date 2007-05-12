
package org.jboss.test.ws.jaxws.samples.logicalhandler;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for echo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="echo">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="String_1" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "echo", propOrder = {
    "string1"
})
public class Echo {

    @XmlElement(name = "String_1", namespace = "http://org.jboss.ws/jaxws/samples/logicalhandler", required = true, nillable = true)
    protected String string1;

    /**
     * Gets the value of the string1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getString1() {
        return string1;
    }

    /**
     * Sets the value of the string1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setString1(String value) {
        this.string1 = value;
    }

}
