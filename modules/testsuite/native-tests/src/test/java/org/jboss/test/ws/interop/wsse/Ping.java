
package org.jboss.test.ws.interop.wsse;

import javax.xml.bind.annotation.*;


/**
 * <p>Java class for ping complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ping">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="scenario" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="origin" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="text" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ping", propOrder = {
    "scenario",
    "origin",
    "text"
})
public class Ping {

    @XmlElement(namespace = "http://xmlsoap.org/Ping", nillable = true)
    protected String scenario;
    @XmlElement(namespace = "http://xmlsoap.org/Ping", nillable = true)
    protected String origin;
    @XmlElement(namespace = "http://xmlsoap.org/Ping", nillable = true)
    protected String text;

    /**
     * Gets the value of the scenario property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getScenario() {
        return scenario;
    }

    /**
     * Sets the value of the scenario property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setScenario(String value) {
        this.scenario = value;
    }

    /**
     * Gets the value of the origin property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * Sets the value of the origin property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrigin(String value) {
        this.origin = value;
    }

    /**
     * Gets the value of the text property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the value of the text property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setText(String value) {
        this.text = value;
    }

}
