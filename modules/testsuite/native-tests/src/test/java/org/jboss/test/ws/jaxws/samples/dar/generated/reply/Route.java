
package org.jboss.test.ws.jaxws.samples.dar.generated.reply;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for route complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="route">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="busId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="stops" type="{http://org.jboss.ws/samples/dar}stop" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "route", propOrder = {
    "busId",
    "stops"
})
public class Route {

    protected String busId;
    @XmlElement(nillable = true)
    protected List<Stop> stops;

    /**
     * Gets the value of the busId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBusId() {
        return busId;
    }

    /**
     * Sets the value of the busId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBusId(String value) {
        this.busId = value;
    }

    /**
     * Gets the value of the stops property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the stops property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStops().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Stop }
     * 
     * 
     */
    public List<Stop> getStops() {
        if (stops == null) {
            stops = new ArrayList<Stop>();
        }
        return this.stops;
    }

}
