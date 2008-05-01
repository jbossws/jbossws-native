
package org.jboss.test.ws.jaxws.jbws1857.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for verbindungsTyp.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="verbindungsTyp">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="VERLUSTSCHEIN"/>
 *     &lt;enumeration value="UNBEKANNT"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "verbindungsTyp")
@XmlEnum
public enum VerbindungsTyp {

    VERLUSTSCHEIN,
    UNBEKANNT;

    public String value() {
        return name();
    }

    public static VerbindungsTyp fromValue(String v) {
        return valueOf(v);
    }

}
