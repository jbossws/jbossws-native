
package org.jboss.test.ws.jaxws.jbws1857.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for nationalitaetStatus.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="nationalitaetStatus">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="INAKTIV"/>
 *     &lt;enumeration value="AKTIV"/>
 *     &lt;enumeration value="UNBEKANNT"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "nationalitaetStatus")
@XmlEnum
public enum NationalitaetStatus {

    INAKTIV,
    AKTIV,
    UNBEKANNT;

    public String value() {
        return name();
    }

    public static NationalitaetStatus fromValue(String v) {
        return valueOf(v);
    }

}
