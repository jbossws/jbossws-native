
package org.jboss.test.ws.jaxws.samples.dar.generated;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for darResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="darResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="routes" type="{http://org.jboss.ws/samples/dar}route" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="unservedRequests" type="{http://org.jboss.ws/samples/dar}serviceRequest" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "darResponse", propOrder = {
    "routes",
    "unservedRequests"
})
public class DarResponse {

    @XmlElement(nillable = true)
    protected List<Route> routes;
    @XmlElement(nillable = true)
    protected List<ServiceRequest> unservedRequests;

    /**
     * Gets the value of the routes property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the routes property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRoutes().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Route }
     * 
     * 
     */
    public List<Route> getRoutes() {
        if (routes == null) {
            routes = new ArrayList<Route>();
        }
        return this.routes;
    }

    /**
     * Gets the value of the unservedRequests property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the unservedRequests property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUnservedRequests().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ServiceRequest }
     * 
     * 
     */
    public List<ServiceRequest> getUnservedRequests() {
        if (unservedRequests == null) {
            unservedRequests = new ArrayList<ServiceRequest>();
        }
        return this.unservedRequests;
    }

}
