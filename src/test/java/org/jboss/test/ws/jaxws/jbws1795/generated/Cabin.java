
package org.jboss.test.ws.jaxws.jbws1795.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for cabin complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="cabin">
 *   &lt;complexContent>
 *     &lt;extension base="{http://service.jbws1795.jaxws.ws.test.jboss.org/}locationImpl">
 *       &lt;sequence>
 *         &lt;element name="bedCount" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="deckLevel" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="shipId" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cabin", propOrder = {
    "bedCount",
    "deckLevel",
    "name",
    "shipId"
})
public class Cabin
    extends LocationImpl
{

    protected int bedCount;
    protected int deckLevel;
    protected String name;
    protected int shipId;

    /**
     * Gets the value of the bedCount property.
     * 
     */
    public int getBedCount() {
        return bedCount;
    }

    /**
     * Sets the value of the bedCount property.
     * 
     */
    public void setBedCount(int value) {
        this.bedCount = value;
    }

    /**
     * Gets the value of the deckLevel property.
     * 
     */
    public int getDeckLevel() {
        return deckLevel;
    }

    /**
     * Sets the value of the deckLevel property.
     * 
     */
    public void setDeckLevel(int value) {
        this.deckLevel = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the shipId property.
     * 
     */
    public int getShipId() {
        return shipId;
    }

    /**
     * Sets the value of the shipId property.
     * 
     */
    public void setShipId(int value) {
        this.shipId = value;
    }

}
