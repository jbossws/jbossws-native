
package org.jboss.test.ws.jaxws.client;

import javax.xml.bind.annotation.*;
import java.awt.*;


/**
 * <p>Java class for DataType3 complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DataType3">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="doc" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DataType3", propOrder = {
    "doc"
})
public class DataType3 {

    @XmlElement(namespace = "http://mtomtestservice.org/xsd")
    @XmlMimeType("image/jpeg")
    protected Image doc;

    /**
     * Gets the value of the doc property.
     * 
     * @return
     *     possible object is
     *     {@link Image }
     *     
     */
    public Image getDoc() {
        return doc;
    }

    /**
     * Sets the value of the doc property.
     * 
     * @param value
     *     allowed object is
     *     {@link Image }
     *     
     */
    public void setDoc(Image value) {
        this.doc = value;
    }

}
