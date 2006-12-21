
package org.jboss.test.ws.jaxws.samples.retail.cc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for verificationResponse complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="verificationResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="verified" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "verificationResponse", propOrder = {
    "verified"
})
public class VerificationResponse {

    protected Boolean verified;

    /**
     * Gets the value of the verified property.
     *
     */
    public Boolean isVerified() {
        return verified;
    }

    /**
     * Sets the value of the verified property.
     *
     */
    public void setVerified(Boolean value) {
        this.verified = value;
    }

}
