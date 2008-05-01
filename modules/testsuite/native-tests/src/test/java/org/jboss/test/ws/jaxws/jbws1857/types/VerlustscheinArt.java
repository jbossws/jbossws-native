
package org.jboss.test.ws.jaxws.jbws1857.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for verlustscheinArt.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="verlustscheinArt">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="BETREIBUNG"/>
 *     &lt;enumeration value="KONKURS"/>
 *     &lt;enumeration value="UNBEKANNT"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "verlustscheinArt")
@XmlEnum
public enum VerlustscheinArt {

    BETREIBUNG,
    KONKURS,
    UNBEKANNT;

    public String value() {
        return name();
    }

    public static VerlustscheinArt fromValue(String v) {
        return valueOf(v);
    }

}
