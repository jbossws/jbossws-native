
package org.jboss.test.ws.jaxws.jbws1857.types;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for amtArt.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="amtArt">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="KONKURSAMT"/>
 *     &lt;enumeration value="BETREIBUNGSAMT"/>
 *     &lt;enumeration value="UNBEKANNT"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "amtArt")
@XmlEnum
public enum AmtArt {

    KONKURSAMT,
    BETREIBUNGSAMT,
    UNBEKANNT;

    public String value() {
        return name();
    }

    public static AmtArt fromValue(String v) {
        return valueOf(v);
    }

}
