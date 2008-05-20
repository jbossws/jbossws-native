
package org.jboss.test.ws.jaxws.jbws1843.generated;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CountryCodeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="CountryCodeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="DE"/>
 *     &lt;enumeration value="CZ"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "CountryCodeType", namespace = "urn:BaseComponents")
@XmlEnum
public enum CountryCodeType {

    DE,
    CZ;

    public String value() {
        return name();
    }

    public static CountryCodeType fromValue(String v) {
        return valueOf(v);
    }

}
