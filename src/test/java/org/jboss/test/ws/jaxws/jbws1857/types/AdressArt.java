
package org.jboss.test.ws.jaxws.jbws1857.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for adressArt.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="adressArt">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="KORRESPONDENZADRESSE"/>
 *     &lt;enumeration value="WOHNADRESSE"/>
 *     &lt;enumeration value="UNBEKANNT"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "adressArt")
@XmlEnum
public enum AdressArt {

    KORRESPONDENZADRESSE,
    WOHNADRESSE,
    UNBEKANNT;

    public String value() {
        return name();
    }

    public static AdressArt fromValue(String v) {
        return valueOf(v);
    }

}
