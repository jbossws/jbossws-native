
package org.jboss.test.ws.jaxws.jbws1857.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for personAdresse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="personAdresse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="adresse" type="{http://example.com}adresse" minOccurs="0"/>
 *         &lt;element name="person" type="{http://example.com}person" minOccurs="0"/>
 *         &lt;element name="verbindungsTyp" type="{http://example.com}verbindungsTyp" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "personAdresse", propOrder = {
    "adresse",
    "person",
    "verbindungsTyp"
})
public class PersonAdresse {

    protected Adresse adresse;
    protected Person person;
    protected VerbindungsTyp verbindungsTyp;

    /**
     * Gets the value of the adresse property.
     * 
     * @return
     *     possible object is
     *     {@link Adresse }
     *     
     */
    public Adresse getAdresse() {
        return adresse;
    }

    /**
     * Sets the value of the adresse property.
     * 
     * @param value
     *     allowed object is
     *     {@link Adresse }
     *     
     */
    public void setAdresse(Adresse value) {
        this.adresse = value;
    }

    /**
     * Gets the value of the person property.
     * 
     * @return
     *     possible object is
     *     {@link Person }
     *     
     */
    public Person getPerson() {
        return person;
    }

    /**
     * Sets the value of the person property.
     * 
     * @param value
     *     allowed object is
     *     {@link Person }
     *     
     */
    public void setPerson(Person value) {
        this.person = value;
    }

    /**
     * Gets the value of the verbindungsTyp property.
     * 
     * @return
     *     possible object is
     *     {@link VerbindungsTyp }
     *     
     */
    public VerbindungsTyp getVerbindungsTyp() {
        return verbindungsTyp;
    }

    /**
     * Sets the value of the verbindungsTyp property.
     * 
     * @param value
     *     allowed object is
     *     {@link VerbindungsTyp }
     *     
     */
    public void setVerbindungsTyp(VerbindungsTyp value) {
        this.verbindungsTyp = value;
    }

}
