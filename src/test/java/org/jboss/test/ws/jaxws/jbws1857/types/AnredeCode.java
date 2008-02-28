
package org.jboss.test.ws.jaxws.jbws1857.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anredeCode.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="anredeCode">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="FRAEULEIN"/>
 *     &lt;enumeration value="FRAU"/>
 *     &lt;enumeration value="HERR"/>
 *     &lt;enumeration value="UNBEKANNT"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "anredeCode")
@XmlEnum
public enum AnredeCode {

    FRAEULEIN,
    FRAU,
    HERR,
    UNBEKANNT;

    public String value() {
        return name();
    }

    public static AnredeCode fromValue(String v) {
        return valueOf(v);
    }

}
